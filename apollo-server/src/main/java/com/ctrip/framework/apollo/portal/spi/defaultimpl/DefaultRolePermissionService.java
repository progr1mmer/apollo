package com.ctrip.framework.apollo.portal.spi.defaultimpl;

import com.ctrip.framework.apollo.openapi.repository.ConsumerRoleRepository;
import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.ctrip.framework.apollo.portal.entity.bo.UserInfo;
import com.ctrip.framework.apollo.portal.entity.po.PortalPermission;
import com.ctrip.framework.apollo.portal.entity.po.PortalRole;
import com.ctrip.framework.apollo.portal.entity.po.PortalRolePermission;
import com.ctrip.framework.apollo.portal.entity.po.PortalUserRole;
import com.ctrip.framework.apollo.portal.repository.PortalPermissionRepository;
import com.ctrip.framework.apollo.portal.repository.PortalRolePermissionRepository;
import com.ctrip.framework.apollo.portal.repository.PortalRoleRepository;
import com.ctrip.framework.apollo.portal.repository.PortalUserRoleRepository;
import com.ctrip.framework.apollo.portal.service.PortalRolePermissionService;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by timothy on 2017/4/26.
 */
public class DefaultRolePermissionService implements PortalRolePermissionService {
    @Autowired
    private PortalRoleRepository roleRepository;
    @Autowired
    private PortalRolePermissionRepository rolePermissionRepository;
    @Autowired
    private PortalUserRoleRepository userRoleRepository;
    @Autowired
    private PortalPermissionRepository permissionRepository;
    @Autowired
    private PortalConfig portalConfig;
    @Autowired
    private ConsumerRoleRepository consumerRoleRepository;

    /**
     * Create role with permissions, note that role name should be unique
     */
    @Transactional
    public PortalRole createRoleWithPermissions(PortalRole role, Set<Long> permissionIds) {
        PortalRole current = findRoleByRoleName(role.getRoleName());
        Preconditions.checkState(current == null, "Role %s already exists!", role.getRoleName());

        PortalRole createdRole = roleRepository.save(role);

        if (!CollectionUtils.isEmpty(permissionIds)) {
            Iterable<PortalRolePermission> rolePermissions = permissionIds.stream().map(permissionId -> {
                PortalRolePermission rolePermission = new PortalRolePermission();
                rolePermission.setRoleId(createdRole.getId());
                rolePermission.setPermissionId(permissionId);
                rolePermission.setDataChangeCreatedBy(createdRole.getDataChangeCreatedBy());
                rolePermission.setDataChangeLastModifiedBy(createdRole.getDataChangeLastModifiedBy());
                return rolePermission;
            }).collect(Collectors.toList());
            rolePermissionRepository.saveAll(rolePermissions);
        }

        return createdRole;
    }

    /**
     * Assign role to users
     *
     * @return the users assigned roles
     */
    @Transactional
    public Set<String> assignRoleToUsers(String roleName, Set<String> userIds,
                                         String operatorUserId) {
        PortalRole role = findRoleByRoleName(roleName);
        Preconditions.checkState(role != null, "Role %s doesn't exist!", roleName);

        List<PortalUserRole> existedUserRoles =
                userRoleRepository.findByUserIdInAndRoleId(userIds, role.getId());
        Set<String> existedUserIds =
            existedUserRoles.stream().map(PortalUserRole::getUserId).collect(Collectors.toSet());

        Set<String> toAssignUserIds = Sets.difference(userIds, existedUserIds);

        Iterable<PortalUserRole> toCreate = toAssignUserIds.stream().map(userId -> {
            PortalUserRole userRole = new PortalUserRole();
            userRole.setRoleId(role.getId());
            userRole.setUserId(userId);
            userRole.setDataChangeCreatedBy(operatorUserId);
            userRole.setDataChangeLastModifiedBy(operatorUserId);
            return userRole;
        }).collect(Collectors.toList());

        userRoleRepository.saveAll(toCreate);
        return toAssignUserIds;
    }

    /**
     * Remove role from users
     */
    @Transactional
    public void removeRoleFromUsers(String roleName, Set<String> userIds, String operatorUserId) {
        PortalRole role = findRoleByRoleName(roleName);
        Preconditions.checkState(role != null, "Role %s doesn't exist!", roleName);

        List<PortalUserRole> existedUserRoles =
                userRoleRepository.findByUserIdInAndRoleId(userIds, role.getId());

        for (PortalUserRole userRole : existedUserRoles) {
            userRole.setDeleted(true);
            userRole.setDataChangeLastModifiedTime(new Date());
            userRole.setDataChangeLastModifiedBy(operatorUserId);
        }

        userRoleRepository.saveAll(existedUserRoles);
    }

    /**
     * Query users with role
     */
    public Set<UserInfo> queryUsersWithRole(String roleName) {
        PortalRole role = findRoleByRoleName(roleName);

        if (role == null) {
            return Collections.emptySet();
        }

        List<PortalUserRole> userRoles = userRoleRepository.findByRoleId(role.getId());

        Set<UserInfo> users = userRoles.stream().map(userRole -> {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userRole.getUserId());
            return userInfo;
        }).collect(Collectors.toSet());

        return users;
    }

    /**
     * Find role by role name, note that roleName should be unique
     */
    public PortalRole findRoleByRoleName(String roleName) {
        return roleRepository.findTopByRoleName(roleName);
    }

    /**
     * Check whether user has the permission
     */
    public boolean userHasPermission(String userId, String permissionType, String targetId) {
        PortalPermission permission =
                permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
        if (permission == null) {
            return false;
        }

        if (isSuperAdmin(userId)) {
            return true;
        }

        List<PortalUserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return false;
        }

        Set<Long> roleIds =
            userRoles.stream().map(PortalUserRole::getRoleId).collect(Collectors.toSet());
        List<PortalRolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return false;
        }

        for (PortalRolePermission rolePermission : rolePermissions) {
            if (rolePermission.getPermissionId() == permission.getId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<PortalRole> findUserRoles(String userId) {
        List<PortalUserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        Set<Long> roleIds = userRoles.stream().map(PortalUserRole::getRoleId).collect(Collectors.toSet());

        return Lists.newLinkedList(roleRepository.findAllById(roleIds));
    }

    public boolean isSuperAdmin(String userId) {
        return portalConfig.superAdmins().contains(userId);
    }

    /**
     * Create permission, note that permissionType + targetId should be unique
     */
    @Transactional
    public PortalPermission createPermission(PortalPermission permission) {
        String permissionType = permission.getPermissionType();
        String targetId = permission.getTargetId();
        PortalPermission current =
                permissionRepository.findTopByPermissionTypeAndTargetId(permissionType, targetId);
        Preconditions.checkState(current == null,
                "Permission with permissionType %s targetId %s already exists!", permissionType, targetId);

        return permissionRepository.save(permission);
    }

    /**
     * Create permissions, note that permissionType + targetId should be unique
     */
    @Transactional
    public Set<PortalPermission> createPermissions(Set<PortalPermission> permissions) {
        Multimap<String, String> targetIdPermissionTypes = HashMultimap.create();
        for (PortalPermission permission : permissions) {
            targetIdPermissionTypes.put(permission.getTargetId(), permission.getPermissionType());
        }

        for (String targetId : targetIdPermissionTypes.keySet()) {
            Collection<String> permissionTypes = targetIdPermissionTypes.get(targetId);
            List<PortalPermission> current =
                    permissionRepository.findByPermissionTypeInAndTargetId(permissionTypes, targetId);
            Preconditions.checkState(CollectionUtils.isEmpty(current),
                    "Permission with permissionType %s targetId %s already exists!", permissionTypes,
                    targetId);
        }

        Iterable<PortalPermission> results = permissionRepository.saveAll(permissions);
        return StreamSupport.stream(results.spliterator(), false).collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public void deleteRolePermissionsByAppId(String appId, String operator) {
        List<Long> permissionIds = permissionRepository.findPermissionIdsByAppId(appId);

        if (!permissionIds.isEmpty()) {
            // 1. delete Permission
            permissionRepository.batchDelete(permissionIds, operator);

            // 2. delete Role Permission
            rolePermissionRepository.batchDeleteByPermissionIds(permissionIds, operator);
        }

        List<Long> roleIds = roleRepository.findRoleIdsByAppId(appId);

        if (!roleIds.isEmpty()) {
            // 3. delete Role
            roleRepository.batchDelete(roleIds, operator);

            // 4. delete User Role
            userRoleRepository.batchDeleteByRoleIds(roleIds, operator);

            // 5. delete Consumer Role
            consumerRoleRepository.batchDeleteByRoleIds(roleIds, operator);
        }
    }

    @Transactional
    @Override
    public void deleteRolePermissionsByAppIdAndNamespace(String appId, String namespaceName, String operator) {
        List<Long> permissionIds = permissionRepository.findPermissionIdsByAppIdAndNamespace(appId, namespaceName);

        if (!permissionIds.isEmpty()) {
            // 1. delete Permission
            permissionRepository.batchDelete(permissionIds, operator);

            // 2. delete Role Permission
            rolePermissionRepository.batchDeleteByPermissionIds(permissionIds, operator);
        }

        List<Long> roleIds = roleRepository.findRoleIdsByAppIdAndNamespace(appId, namespaceName);

        if (!roleIds.isEmpty()) {
            // 3. delete Role
            roleRepository.batchDelete(roleIds, operator);

            // 4. delete User Role
            userRoleRepository.batchDeleteByRoleIds(roleIds, operator);

            // 5. delete Consumer Role
            consumerRoleRepository.batchDeleteByRoleIds(roleIds, operator);
        }
    }
}
