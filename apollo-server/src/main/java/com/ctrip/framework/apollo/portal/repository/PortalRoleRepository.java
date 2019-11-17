package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalRole;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PortalRoleRepository extends PagingAndSortingRepository<PortalRole, Long> {

  /**
   * find role by role name
   */
  PortalRole findTopByRoleName(String roleName);

  @Query("SELECT r.id from PortalRole r where (r.roleName = CONCAT('Master+', ?1) "
      + "OR r.roleName like CONCAT('ModifyNamespace+', ?1, '+%') "
      + "OR r.roleName like CONCAT('ReleaseNamespace+', ?1, '+%')  "
      + "OR r.roleName = CONCAT('ManageAppMaster+', ?1))")
  List<Long> findRoleIdsByAppId(String appId);

  @Query("SELECT r.id from PortalRole r where (r.roleName = CONCAT('ModifyNamespace+', ?1, '+', ?2) "
      + "OR r.roleName = CONCAT('ReleaseNamespace+', ?1, '+', ?2))")
  List<Long> findRoleIdsByAppIdAndNamespace(String appId, String namespaceName);

  @Modifying
  @Query("UPDATE PortalRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE Id in ?1")
  Integer batchDelete(List<Long> roleIds, String operator);
}
