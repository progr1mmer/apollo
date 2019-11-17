package com.ctrip.framework.apollo.portal.controller;


import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.portal.entity.po.PortalServerConfig;
import com.ctrip.framework.apollo.portal.repository.PortalServerConfigRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * 配置中心本身需要一些配置,这些配置放在数据库里面
 */
@RestController
public class ServerConfigController {

  private final PortalServerConfigRepository serverConfigRepository;
  private final UserInfoHolder userInfoHolder;

  public ServerConfigController(final PortalServerConfigRepository serverConfigRepository, final UserInfoHolder userInfoHolder) {
    this.serverConfigRepository = serverConfigRepository;
    this.userInfoHolder = userInfoHolder;
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @PostMapping("/server/config")
  public PortalServerConfig createOrUpdate(@Valid @RequestBody PortalServerConfig serverConfig) {
    String modifiedBy = userInfoHolder.getUser().getUserId();

    PortalServerConfig storedConfig = serverConfigRepository.findByKey(serverConfig.getKey());

    if (Objects.isNull(storedConfig)) {//create
      serverConfig.setDataChangeCreatedBy(modifiedBy);
      serverConfig.setDataChangeLastModifiedBy(modifiedBy);
      serverConfig.setId(0L);//为空，设置ID 为0，jpa执行新增操作
      return serverConfigRepository.save(serverConfig);
    } else {//update
      BeanUtils.copyEntityProperties(serverConfig, storedConfig);
      storedConfig.setDataChangeLastModifiedBy(modifiedBy);
      return serverConfigRepository.save(storedConfig);
    }
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @GetMapping("/server/config/{key:.+}")
  public PortalServerConfig loadServerConfig(@PathVariable String key) {
    return serverConfigRepository.findByKey(key);
  }

}
