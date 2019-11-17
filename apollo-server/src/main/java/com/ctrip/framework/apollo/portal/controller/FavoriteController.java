package com.ctrip.framework.apollo.portal.controller;

import com.ctrip.framework.apollo.portal.entity.po.PortalFavorite;
import com.ctrip.framework.apollo.portal.service.PortalFavoriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FavoriteController {

  private final PortalFavoriteService favoriteService;

  public FavoriteController(final PortalFavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }


  @PostMapping("/favorites")
  public PortalFavorite addFavorite(@RequestBody PortalFavorite favorite) {
    return favoriteService.addFavorite(favorite);
  }


  @GetMapping("/favorites")
  public List<PortalFavorite> findFavorites(@RequestParam(value = "userId", required = false) String userId,
                                            @RequestParam(value = "appId", required = false) String appId,
                                            Pageable page) {
    return favoriteService.search(userId, appId, page);
  }


  @DeleteMapping("/favorites/{favoriteId}")
  public void deleteFavorite(@PathVariable long favoriteId) {
    favoriteService.deleteFavorite(favoriteId);
  }


  @PutMapping("/favorites/{favoriteId}")
  public void toTop(@PathVariable long favoriteId) {
    favoriteService.adjustFavoriteToFirst(favoriteId);
  }

}
