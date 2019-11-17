package com.ctrip.framework.apollo.openapi.repository;

import com.ctrip.framework.apollo.openapi.entity.PortalConsumerAudit;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface ConsumerAuditRepository extends PagingAndSortingRepository<PortalConsumerAudit, Long> {
}
