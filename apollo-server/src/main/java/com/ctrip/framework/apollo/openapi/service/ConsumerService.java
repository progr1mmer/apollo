package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.openapi.entity.PortalConsumer;
import com.ctrip.framework.apollo.openapi.entity.PortalConsumerAudit;
import com.ctrip.framework.apollo.openapi.entity.PortalConsumerRole;
import com.ctrip.framework.apollo.openapi.entity.PortalConsumerToken;
import com.ctrip.framework.apollo.openapi.repository.ConsumerAuditRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.openapi.repository.ConsumerTokenRepository;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.PortalRole;
import com.ctrip.framework.apollo.portal.service.PortalRolePermissionService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.portal.util.RoleUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerService {

  private static final FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");
  private static final Joiner KEY_JOINER = Joiner.on("|");

  private final UserInfoHolder userInfoHolder;
  private final ConsumerTokenRepository consumerTokenRepository;
  private final ConsumerRepository consumerRepository;
  private final ConsumerAuditRepository consumerAuditRepository;
  private final ConsumerRoleRepository consumerRoleRepository;
  private final PortalConfig portalConfig;
  private final PortalRolePermissionService rolePermissionService;
  private final UserService userService;

  public ConsumerService(
      final UserInfoHolder userInfoHolder,
      final ConsumerTokenRepository consumerTokenRepository,
      final ConsumerRepository consumerRepository,
      final ConsumerAuditRepository consumerAuditRepository,
      final ConsumerRoleRepository consumerRoleRepository,
      final PortalConfig portalConfig,
      final PortalRolePermissionService rolePermissionService,
      final UserService userService) {
    this.userInfoHolder = userInfoHolder;
    this.consumerTokenRepository = consumerTokenRepository;
    this.consumerRepository = consumerRepository;
    this.consumerAuditRepository = consumerAuditRepository;
    this.consumerRoleRepository = consumerRoleRepository;
    this.portalConfig = portalConfig;
    this.rolePermissionService = rolePermissionService;
    this.userService = userService;
  }


  public PortalConsumer createConsumer(PortalConsumer consumer) {
    String appId = consumer.getAppId();

    PortalConsumer managedConsumer = consumerRepository.findByAppId(appId);
    if (managedConsumer != null) {
      throw new BadRequestException("Consumer already exist");
    }

    String ownerName = consumer.getOwnerName();
    UserInfo owner = userService.findByUserId(ownerName);
    if (owner == null) {
      throw new BadRequestException(String.format("User does not exist. UserId = %s", ownerName));
    }
    consumer.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    consumer.setDataChangeCreatedBy(operator);
    consumer.setDataChangeLastModifiedBy(operator);

    return consumerRepository.save(consumer);
  }

  public PortalConsumerToken generateAndSaveConsumerToken(PortalConsumer consumer, Date expires) {
    Preconditions.checkArgument(consumer != null, "Consumer can not be null");

    PortalConsumerToken consumerToken = generateConsumerToken(consumer, expires);
    consumerToken.setId(0);

    return consumerTokenRepository.save(consumerToken);
  }

  public PortalConsumerToken getConsumerTokenByAppId(String appId) {
    PortalConsumer consumer = consumerRepository.findByAppId(appId);
    if (consumer == null) {
      return null;
    }

    return consumerTokenRepository.findByConsumerId(consumer.getId());
  }

  public Long getConsumerIdByToken(String token) {
    if (Strings.isNullOrEmpty(token)) {
      return null;
    }
    PortalConsumerToken consumerToken = consumerTokenRepository.findTopByTokenAndExpiresAfter(token,
                                                                                        new Date());
    return consumerToken == null ? null : consumerToken.getConsumerId();
  }

  public PortalConsumer getConsumerByConsumerId(long consumerId) {
    return consumerRepository.findById(consumerId).orElse(null);
  }

  public List<PortalConsumerRole> assignNamespaceRoleToConsumer(String token, String appId, String namespaceName) {
    return assignNamespaceRoleToConsumer(token, appId, namespaceName, null);
  }

  @Transactional
  public List<PortalConsumerRole> assignNamespaceRoleToConsumer(String token, String appId, String namespaceName, String env) {
    Long consumerId = getConsumerIdByToken(token);
    if (consumerId == null) {
      throw new BadRequestException("Token is Illegal");
    }

    PortalRole namespaceModifyRole =
        rolePermissionService.findRoleByRoleName(RoleUtils.buildModifyNamespaceRoleName(appId, namespaceName, env));
    PortalRole namespaceReleaseRole =
        rolePermissionService.findRoleByRoleName(RoleUtils.buildReleaseNamespaceRoleName(appId, namespaceName, env));

    if (namespaceModifyRole == null || namespaceReleaseRole == null) {
      throw new BadRequestException("Namespace's role does not exist. Please check whether namespace has created.");
    }

    long namespaceModifyRoleId = namespaceModifyRole.getId();
    long namespaceReleaseRoleId = namespaceReleaseRole.getId();

    PortalConsumerRole managedModifyRole = consumerRoleRepository.findByConsumerIdAndRoleId(consumerId, namespaceModifyRoleId);
    PortalConsumerRole managedReleaseRole = consumerRoleRepository.findByConsumerIdAndRoleId(consumerId, namespaceReleaseRoleId);
    if (managedModifyRole != null && managedReleaseRole != null) {
      return Arrays.asList(managedModifyRole, managedReleaseRole);
    }

    String operator = userInfoHolder.getUser().getUserId();

    PortalConsumerRole namespaceModifyConsumerRole = createConsumerRole(consumerId, namespaceModifyRoleId, operator);
    PortalConsumerRole namespaceReleaseConsumerRole = createConsumerRole(consumerId, namespaceReleaseRoleId, operator);

    PortalConsumerRole createdModifyConsumerRole = consumerRoleRepository.save(namespaceModifyConsumerRole);
    PortalConsumerRole createdReleaseConsumerRole = consumerRoleRepository.save(namespaceReleaseConsumerRole);

    return Arrays.asList(createdModifyConsumerRole, createdReleaseConsumerRole);
  }

  @Transactional
  public PortalConsumerRole assignAppRoleToConsumer(String token, String appId) {
    Long consumerId = getConsumerIdByToken(token);
    if (consumerId == null) {
      throw new BadRequestException("Token is Illegal");
    }

    PortalRole masterRole = rolePermissionService.findRoleByRoleName(RoleUtils.buildAppMasterRoleName(appId));
    if (masterRole == null) {
      throw new BadRequestException("App's role does not exist. Please check whether app has created.");
    }

    long roleId = masterRole.getId();
    PortalConsumerRole managedModifyRole = consumerRoleRepository.findByConsumerIdAndRoleId(consumerId, roleId);
    if (managedModifyRole != null) {
      return managedModifyRole;
    }

    String operator = userInfoHolder.getUser().getUserId();
    PortalConsumerRole consumerRole = createConsumerRole(consumerId, roleId, operator);
    return consumerRoleRepository.save(consumerRole);
  }

  @Transactional
  public void createConsumerAudits(Iterable<PortalConsumerAudit> consumerAudits) {
    consumerAuditRepository.saveAll(consumerAudits);
  }

  @Transactional
  public PortalConsumerToken createConsumerToken(PortalConsumerToken entity) {
    entity.setId(0); //for protection

    return consumerTokenRepository.save(entity);
  }

  private PortalConsumerToken generateConsumerToken(PortalConsumer consumer, Date expires) {
    long consumerId = consumer.getId();
    String createdBy = userInfoHolder.getUser().getUserId();
    Date createdTime = new Date();

    PortalConsumerToken consumerToken = new PortalConsumerToken();
    consumerToken.setConsumerId(consumerId);
    consumerToken.setExpires(expires);
    consumerToken.setDataChangeCreatedBy(createdBy);
    consumerToken.setDataChangeCreatedTime(createdTime);
    consumerToken.setDataChangeLastModifiedBy(createdBy);
    consumerToken.setDataChangeLastModifiedTime(createdTime);

    generateAndEnrichToken(consumer, consumerToken);

    return consumerToken;
  }

  void generateAndEnrichToken(PortalConsumer consumer, PortalConsumerToken consumerToken) {

    Preconditions.checkArgument(consumer != null);

    if (consumerToken.getDataChangeCreatedTime() == null) {
      consumerToken.setDataChangeCreatedTime(new Date());
    }
    consumerToken.setToken(generateToken(consumer.getAppId(), consumerToken
        .getDataChangeCreatedTime(), portalConfig.consumerTokenSalt()));
  }

  String generateToken(String consumerAppId, Date generationTime, String
      consumerTokenSalt) {
    return Hashing.sha1().hashString(KEY_JOINER.join(consumerAppId, TIMESTAMP_FORMAT.format
        (generationTime), consumerTokenSalt), Charsets.UTF_8).toString();
  }

    PortalConsumerRole createConsumerRole(Long consumerId, Long roleId, String operator) {
    PortalConsumerRole consumerRole = new PortalConsumerRole();

    consumerRole.setConsumerId(consumerId);
    consumerRole.setRoleId(roleId);
    consumerRole.setDataChangeCreatedBy(operator);
    consumerRole.setDataChangeLastModifiedBy(operator);

    return consumerRole;
  }

}
