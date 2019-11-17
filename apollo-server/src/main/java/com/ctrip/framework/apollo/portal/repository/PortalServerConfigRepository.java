package com.ctrip.framework.apollo.portal.repository;


import com.ctrip.framework.apollo.portal.entity.po.PortalServerConfig;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PortalServerConfigRepository extends PagingAndSortingRepository<PortalServerConfig, Long> {
  PortalServerConfig findByKey(String key);
}
