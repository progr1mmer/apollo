package com.ctrip.framework.apollo.portal.repository;

import com.ctrip.framework.apollo.portal.entity.po.PortalUserPO;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author lepdou 2017-04-08
 */
public interface PortalUserRepository extends PagingAndSortingRepository<PortalUserPO, Long> {

  List<PortalUserPO> findFirst20ByEnabled(int enabled);

  List<PortalUserPO> findByUsernameLikeAndEnabled(String username, int enabled);

  PortalUserPO findByUsername(String username);

  List<PortalUserPO> findByUsernameIn(List<String> userNames);
}
