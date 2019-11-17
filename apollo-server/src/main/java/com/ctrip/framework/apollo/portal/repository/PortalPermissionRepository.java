package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalPermission;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PortalPermissionRepository extends PagingAndSortingRepository<PortalPermission, Long> {
  /**
   * find permission by permission type and targetId
   */
  PortalPermission findTopByPermissionTypeAndTargetId(String permissionType, String targetId);

  /**
   * find permissions by permission types and targetId
   */
  List<PortalPermission> findByPermissionTypeInAndTargetId(Collection<String> permissionTypes,
                                                           String targetId);

  @Query("SELECT p.id from PortalPermission p where p.targetId = ?1 or p.targetId like CONCAT(?1, '+%')")
  List<Long> findPermissionIdsByAppId(String appId);

  @Query("SELECT p.id from PortalPermission p where p.targetId = CONCAT(?1, '+', ?2)")
  List<Long> findPermissionIdsByAppIdAndNamespace(String appId, String namespaceName);

  @Modifying
  @Query("UPDATE PortalPermission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1")
  Integer batchDelete(List<Long> permissionIds, String operator);
}
