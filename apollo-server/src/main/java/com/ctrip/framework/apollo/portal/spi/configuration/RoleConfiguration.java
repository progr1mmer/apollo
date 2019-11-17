package com.ctrip.framework.apollo.portal.spi.configuration;

import com.ctrip.framework.apollo.portal.service.PortalRoleInitializationService;
import com.ctrip.framework.apollo.portal.service.PortalRolePermissionService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultRoleInitializationService;
import com.ctrip.framework.apollo.portal.spi.defaultimpl.DefaultRolePermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Timothy Liu(timothy.liu@cvte.com)
 */
@Configuration
public class RoleConfiguration {
    @Bean
    public PortalRoleInitializationService roleInitializationService() {
        return new DefaultRoleInitializationService();
    }

    @Bean
    public PortalRolePermissionService rolePermissionService() {
        return new DefaultRolePermissionService();
    }
}
