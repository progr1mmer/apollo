package com.ctrip.framework.apollo.openapi.service;

import com.ctrip.framework.apollo.openapi.entity.PortalConsumerRole;
import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.entity.po.PortalPermission;
import com.ctrip.framework.apollo.portal.entity.po.PortalRolePermission;
import com.ctrip.framework.apollo.portal.repository.PortalPermissionRepository;
import com.ctrip.framework.apollo.portal.repository.PortalRolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ConsumerRolePermissionService {
  private final PortalPermissionRepository permissionRepository;
  private final ConsumerRoleRepository consumerRoleRepository;
  private final PortalRolePermissionRepository rolePermissionRepository;

  public ConsumerRolePermissionService(
      final PortalPermissionRepository permissionRepository,
      final ConsumerRoleRepository consumerRoleRepository,
      final PortalRolePermissionRepository rolePermissionRepository) {
    this.permissionRepository = permissionRepository;
    this.consumerRoleRepository = consumerRoleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
  }

  /**
   * Check whether user has the permission
   */
  public boolean consumerHasPermission(long consumerId, String permissionType, String targetId) {
    PortalPermission permission =
        permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
    if (permission == null) {
      return false;
    }

    List<PortalConsumerRole> consumerRoles = consumerRoleRepository.findByConsumerId(consumerId);
    if (CollectionUtils.isEmpty(consumerRoles)) {
      return false;
    }

    Set<Long> roleIds =
        consumerRoles.stream().map(PortalConsumerRole::getRoleId).collect(Collectors.toSet());
    List<PortalRolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
    if (CollectionUtils.isEmpty(rolePermissions)) {
      return false;
    }

    for (PortalRolePermission rolePermission : rolePermissions) {
      if (rolePermission.getPermissionId() == permission.getId()) {
        return true;
      }
    }

    return false;
  }
}
