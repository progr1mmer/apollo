package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalApp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;


public interface PortalAppRepository extends PagingAndSortingRepository<PortalApp, Long> {

  PortalApp findByAppId(String appId);

  List<PortalApp> findByOwnerName(String ownerName, Pageable page);

  List<PortalApp> findByAppIdIn(Set<String> appIds);

  List<PortalApp> findByAppIdIn(Set<String> appIds, Pageable pageable);

  Page<PortalApp> findByAppIdContainingOrNameContaining(String appId, String name, Pageable pageable);

  @Modifying
  @Query("UPDATE PortalApp SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int deleteApp(String appId, String operator);
}
