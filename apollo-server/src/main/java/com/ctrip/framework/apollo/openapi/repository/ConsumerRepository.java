package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.PortalConsumer;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerRepository extends PagingAndSortingRepository<PortalConsumer, Long> {

  PortalConsumer findByAppId(String appId);

}
