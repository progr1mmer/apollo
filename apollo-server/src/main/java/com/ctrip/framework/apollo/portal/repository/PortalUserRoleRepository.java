package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalUserRole;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PortalUserRoleRepository extends PagingAndSortingRepository<PortalUserRole, Long> {
  /**
   * find user roles by userId
   */
  List<PortalUserRole> findByUserId(String userId);

  /**
   * find user roles by roleId
   */
  List<PortalUserRole> findByRoleId(long roleId);

  /**
   * find user roles by userIds and roleId
   */
  List<PortalUserRole> findByUserIdInAndRoleId(Collection<String> userId, long roleId);

  @Modifying
  @Query("UPDATE PortalUserRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1")
  Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);

}
