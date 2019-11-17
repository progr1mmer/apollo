package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.PortalPermission;
import com.ctrip.framework.apollo.portal.entity.po.PortalRole;

import java.util.List;
import java.util.Set;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface PortalRolePermissionService {

  /**
   * Create role with permissions, note that role name should be unique
   */
  public PortalRole createRoleWithPermissions(PortalRole role, Set<Long> permissionIds);

  /**
   * Assign role to users
   *
   * @return the users assigned roles
   */
  public Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
                                       String operatorUserId);

  /**
   * Remove role from users
   */
  public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId);

  /**
   * Query users with role
   */
  public Set<UserInfo> queryUsersWithRole(String roleName);

  /**
   * Find role by role name, note that roleName should be unique
   */
  public PortalRole findRoleByRoleName(String roleName);

  /**
   * Check whether user has the permission
   */
  public boolean userHasPermission(String userId, String permissionType, String targetId);

  /**
   * Find the user's roles
   */
  public List<PortalRole> findUserRoles(String userId);

  public boolean isSuperAdmin(String userId);

  /**
   * Create permission, note that permissionType + targetId should be unique
   */
  public PortalPermission createPermission(PortalPermission permission);

  /**
   * Create permissions, note that permissionType + targetId should be unique
   */
  public Set<PortalPermission> createPermissions(Set<PortalPermission> permissions);

  /**
   * delete permissions when delete app.
   */
  public void deleteRolePermissionsByAppId(String appId, String operator);

  /**
   * delete permissions when delete app namespace.
   */
  public void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName, String operator);
}
