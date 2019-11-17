package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.PortalConsumerRole;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerRoleRepository extends PagingAndSortingRepository<PortalConsumerRole, Long> {
  /**
   * find consumer roles by userId
   *
   * @param consumerId consumer id
   */
  List<PortalConsumerRole> findByConsumerId(long consumerId);

  /**
   * find consumer roles by roleId
   */
  List<PortalConsumerRole> findByRoleId(long roleId);

  PortalConsumerRole findByConsumerIdAndRoleId(long consumerId, long roleId);

  @Modifying
  @Query("UPDATE PortalConsumerRole SET IsDeleted=1, DataChange_LastModifiedBy = ?2 WHERE RoleId in ?1")
  Integer batchDeleteByRoleIds(List<Long> roleIds, String operator);
}
