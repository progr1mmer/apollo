package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.PortalConsumerToken;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerTokenRepository extends PagingAndSortingRepository<PortalConsumerToken, Long> {
  /**
   * find consumer token by token
   *
   * @param token     the token
   * @param validDate the date when the token is valid
   */
  PortalConsumerToken findTopByTokenAndExpiresAfter(String token, Date validDate);

  PortalConsumerToken findByConsumerId(Long consumerId);
}
