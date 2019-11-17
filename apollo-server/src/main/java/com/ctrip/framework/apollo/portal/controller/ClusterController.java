package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.service.PortalClusterService;
import com.ctrip.framework.apollo.portal.spi.UserInfoHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class ClusterController {

  private final PortalClusterService clusterService;
  private final UserInfoHolder userInfoHolder;

  public ClusterController(final PortalClusterService clusterService, final UserInfoHolder userInfoHolder) {
    this.clusterService = clusterService;
    this.userInfoHolder = userInfoHolder;
  }

  @PreAuthorize(value = "@permissionValidator.hasCreateClusterPermission(#appId)")
  @PostMapping(value = "apps/{appId}/envs/{env}/clusters")
  public ClusterDTO createCluster(@PathVariable String appId, @PathVariable String env,
                                  @Valid @RequestBody ClusterDTO cluster) {
    String operator = userInfoHolder.getUser().getUserId();
    cluster.setDataChangeLastModifiedBy(operator);
    cluster.setDataChangeCreatedBy(operator);

    return clusterService.createCluster(Env.valueOf(env), cluster);
  }

  @PreAuthorize(value = "@permissionValidator.isSuperAdmin()")
  @DeleteMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ResponseEntity<Void> deleteCluster(@PathVariable String appId, @PathVariable String env,
                                            @PathVariable String clusterName){
    clusterService.deleteCluster(Env.fromString(env), appId, clusterName);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "apps/{appId}/envs/{env}/clusters/{clusterName:.+}")
  public ClusterDTO loadCluster(@PathVariable("appId") String appId, @PathVariable String env, @PathVariable("clusterName") String clusterName) {

    return clusterService.loadCluster(appId, Env.fromString(env), clusterName);
  }

}
