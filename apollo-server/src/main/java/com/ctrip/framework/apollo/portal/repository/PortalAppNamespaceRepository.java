package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalAppNamespace;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PortalAppNamespaceRepository extends PagingAndSortingRepository<PortalAppNamespace, Long> {

  PortalAppNamespace findByAppIdAndName(String appId, String namespaceName);

  PortalAppNamespace findByName(String namespaceName);

  List<PortalAppNamespace> findByNameAndIsPublic(String namespaceName, boolean isPublic);

  List<PortalAppNamespace> findByIsPublicTrue();

  List<PortalAppNamespace> findByAppId(String appId);

  @Modifying
  @Query("UPDATE PortalAppNamespace SET IsDeleted=1,DataChange_LastModifiedBy=?2 WHERE AppId=?1")
  int batchDeleteByAppId(String appId, String operator);

  @Modifying
  @Query("UPDATE PortalAppNamespace SET IsDeleted=1,DataChange_LastModifiedBy = ?3 WHERE AppId=?1 and Name = ?2")
  int delete(String appId, String namespaceName, String operator);
}
