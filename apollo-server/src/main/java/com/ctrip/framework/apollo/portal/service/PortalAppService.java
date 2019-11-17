package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.dto.AppDTO;
import com.ctrip.framework.apollo.common.dto.PageDTO;
import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.portal.entity.po.PortalApp;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;
import com.ctrip.framework.apollo.portal.constant.TracerEventType;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.vo.EnvClusterInfo;
import com.ctrip.framework.apollo.portal.repository.PortalAppRepository;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import com.ctrip.framework.apollo.portal.spi.UserService;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class PortalAppService {

  private final UserInfoHolder userInfoHolder;
  private final AdminServiceAPI.AppAPI appAPI;
  private final PortalAppRepository appRepository;
  private final PortalClusterService clusterService;
  private final PortalAppNamespaceService appNamespaceService;
  private final PortalRoleInitializationService roleInitializationService;
  private final PortalRolePermissionService rolePermissionService;
  private final PortalFavoriteService favoriteService;
  private final UserService userService;

  public PortalAppService(
      final UserInfoHolder userInfoHolder,
      final AdminServiceAPI.AppAPI appAPI,
      final PortalAppRepository appRepository,
      final PortalClusterService clusterService,
      final PortalAppNamespaceService appNamespaceService,
      final PortalRoleInitializationService roleInitializationService,
      final PortalRolePermissionService rolePermissionService,
      final PortalFavoriteService favoriteService,
      final UserService userService) {
    this.userInfoHolder = userInfoHolder;
    this.appAPI = appAPI;
    this.appRepository = appRepository;
    this.clusterService = clusterService;
    this.appNamespaceService = appNamespaceService;
    this.roleInitializationService = roleInitializationService;
    this.rolePermissionService = rolePermissionService;
    this.favoriteService = favoriteService;
    this.userService = userService;
  }


  public List<PortalApp> findAll() {
    Iterable<PortalApp> apps = appRepository.findAll();
    if (apps == null) {
      return Collections.emptyList();
    }
    return Lists.newArrayList((apps));
  }

  public PageDTO<PortalApp> findAll(Pageable pageable) {
    Page<PortalApp> apps = appRepository.findAll(pageable);

    return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
  }

  public PageDTO<PortalApp> searchByAppIdOrAppName(String query, Pageable pageable) {
    Page<PortalApp> apps = appRepository.findByAppIdContainingOrNameContaining(query, query, pageable);

    return new PageDTO<>(apps.getContent(), pageable, apps.getTotalElements());
  }

  public List<PortalApp> findByAppIds(Set<String> appIds) {
    return appRepository.findByAppIdIn(appIds);
  }

  public List<PortalApp> findByAppIds(Set<String> appIds, Pageable pageable) {
    return appRepository.findByAppIdIn(appIds, pageable);
  }

  public List<PortalApp> findByOwnerName(String ownerName, Pageable page) {
    return appRepository.findByOwnerName(ownerName, page);
  }

  public PortalApp load(String appId) {
    return appRepository.findByAppId(appId);
  }

  public AppDTO load(Env env, String appId) {
    return appAPI.loadApp(env, appId);
  }

  public void createAppInRemote(Env env, App app) {
    String username = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(username);
    app.setDataChangeLastModifiedBy(username);

    AppDTO appDTO = BeanUtils.transform(AppDTO.class, app);
    appAPI.createApp(env, appDTO);
  }

  @Transactional
  public PortalApp createAppInLocal(PortalApp app) {
    String appId = app.getAppId();
    PortalApp managedApp = appRepository.findByAppId(appId);

    if (managedApp != null) {
      throw new BadRequestException(String.format("App already exists. AppId = %s", appId));
    }

    UserInfo owner = userService.findByUserId(app.getOwnerName());
    if (owner == null) {
      throw new BadRequestException("Application's owner not exist.");
    }
    app.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    app.setDataChangeCreatedBy(operator);
    app.setDataChangeLastModifiedBy(operator);

    PortalApp createdApp = appRepository.save(app);

    appNamespaceService.createDefaultAppNamespace(appId);
    roleInitializationService.initAppRoles(createdApp);

    Tracer.logEvent(TracerEventType.CREATE_APP, appId);

    return createdApp;
  }

  @Transactional
  public PortalApp updateAppInLocal(PortalApp app) {
    String appId = app.getAppId();

    PortalApp managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }

    managedApp.setName(app.getName());
    managedApp.setOrgId(app.getOrgId());
    managedApp.setOrgName(app.getOrgName());

    String ownerName = app.getOwnerName();
    UserInfo owner = userService.findByUserId(ownerName);
    if (owner == null) {
      throw new BadRequestException(String.format("App's owner not exists. owner = %s", ownerName));
    }
    managedApp.setOwnerName(owner.getUserId());
    managedApp.setOwnerEmail(owner.getEmail());

    String operator = userInfoHolder.getUser().getUserId();
    managedApp.setDataChangeLastModifiedBy(operator);

    return appRepository.save(managedApp);
  }

  public EnvClusterInfo createEnvNavNode(Env env, String appId) {
    EnvClusterInfo node = new EnvClusterInfo(env);
    node.setClusters(clusterService.findClusters(env, appId));
    return node;
  }

  @Transactional
  public PortalApp deleteAppInLocal(String appId) {
    PortalApp managedApp = appRepository.findByAppId(appId);
    if (managedApp == null) {
      throw new BadRequestException(String.format("App not exists. AppId = %s", appId));
    }
    String operator = userInfoHolder.getUser().getUserId();

    //this operator is passed to com.ctrip.framework.apollo.portal.listener.DeletionListener.onAppDeletionEvent
    managedApp.setDataChangeLastModifiedBy(operator);

    //删除portal数据库中的app
    appRepository.deleteApp(appId, operator);

    //删除portal数据库中的appNamespace
    appNamespaceService.batchDeleteByAppId(appId, operator);

    //删除portal数据库中的收藏表
    favoriteService.batchDeleteByAppId(appId, operator);

    //删除portal数据库中Permission、Role相关数据
    rolePermissionService.deleteRolePermissionsByAppId(appId, operator);

    return managedApp;
  }
}
