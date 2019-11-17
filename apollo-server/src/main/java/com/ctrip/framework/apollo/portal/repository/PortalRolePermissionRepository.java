package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalRolePermission;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PortalRolePermissionRepository extends PagingAndSortingRepository<PortalRolePermission, Long> {

  /**
   * find role permissions by role ids
   */
  List<PortalRolePermission> findByRoleIdIn(Collection<Long> roleId);

  @Modifying
  @Query("UPDATE PortalRolePermission SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE PermissionId in ?1")
  Integer batchDeleteByPermissionIds(List<Long> permissionIds, String operator);
}
