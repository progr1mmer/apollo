package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalFavorite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PortalFavoriteRepository extends PagingAndSortingRepository<PortalFavorite, Long> {

  List<PortalFavorite> findByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId, Pageable page);

  List<PortalFavorite> findByAppIdOrderByPositionAscDataChangeCreatedTimeAsc(String appId, Pageable page);

  PortalFavorite findFirstByUserIdOrderByPositionAscDataChangeCreatedTimeAsc(String userId);

  PortalFavorite findByUserIdAndAppId(String userId, String appId);

  @Modifying
  @Query("UPDATE PortalFavorite SET IsDeleted=1,DataChange_LastModifiedBy = ?2 WHERE AppId=?1")
  int batchDeleteByAppId(String appId, String operator);
}
