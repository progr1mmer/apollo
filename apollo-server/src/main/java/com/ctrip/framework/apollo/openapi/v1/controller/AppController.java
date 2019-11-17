package com.ctrip.framework.apollo.openapi.v1.controller;

import com.ctrip.framework.apollo.common.dto.ClusterDTO;
import com.ctrip.framework.apollo.portal.entity.po.PortalApp;
import com.ctrip.framework.apollo.common.utils.BeanUtils;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.openapi.dto.OpenAppDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.util.OpenApiBeanUtils;
import com.ctrip.framework.apollo.portal.component.PortalSettings;
import com.ctrip.framework.apollo.portal.service.PortalAppService;
import com.ctrip.framework.apollo.portal.service.PortalClusterService;
import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController("openapiAppController")
@RequestMapping("/openapi/v1")
public class AppController {

  private final PortalSettings portalSettings;
  private final PortalClusterService clusterService;
  private final PortalAppService appService;

  public AppController(final PortalSettings portalSettings,
                       final PortalClusterService clusterService,
                       final PortalAppService appService) {
    this.portalSettings = portalSettings;
    this.clusterService = clusterService;
    this.appService = appService;
  }

  @GetMapping(value = "/apps/{appId}/envclusters")
  public List<OpenEnvClusterDTO> loadEnvClusterInfo(@PathVariable String appId){

    List<OpenEnvClusterDTO> envClusters = new LinkedList<>();

    List<Env> envs = portalSettings.getActiveEnvs();
    for (Env env : envs) {
      OpenEnvClusterDTO envCluster = new OpenEnvClusterDTO();

      envCluster.setEnv(env.name());
      List<ClusterDTO> clusterDTOs = clusterService.findClusters(env, appId);
      envCluster.setClusters(BeanUtils.toPropertySet("name", clusterDTOs));

      envClusters.add(envCluster);
    }

    return envClusters;

  }

  @GetMapping("/apps")
  public List<OpenAppDTO> findApps(@RequestParam(value = "appIds", required = false) String appIds) {
    final List<PortalApp> apps = new ArrayList<>();
    if (StringUtils.isEmpty(appIds)) {
      apps.addAll(appService.findAll());
    } else {
      apps.addAll(appService.findByAppIds(Sets.newHashSet(appIds.split(","))));
    }
    return OpenApiBeanUtils.transformFromApps(apps);
  }
}
