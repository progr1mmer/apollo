package com.ctrip.framework.apollo.server;

import com.ctrip.framework.apollo.adminservice.AdminServiceConfig;
import com.ctrip.framework.apollo.biz.ApolloBizConfig;
import com.ctrip.framework.apollo.common.ApolloCommonConfig;
import com.ctrip.framework.apollo.configservice.ApolloConfigServiceConfig;
import com.ctrip.framework.apollo.metaservice.ApolloMetaServiceConfig;
import com.ctrip.framework.apollo.openapi.PortalOpenApiConfig;
import com.ctrip.framework.apollo.portal.PortalAppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring boot application entry point
 *
 * @author Jason Song(song_s@ctrip.com)
 */

@EnableAspectJAutoProxy
@EnableAutoConfiguration // (exclude = EurekaClientConfigBean.class)
@Configuration
@EnableTransactionManagement
@PropertySource(value = {"classpath:configservice.properties"})
@ComponentScan(basePackageClasses = {
        ConfigServerApplication.class,
        ApolloCommonConfig.class,
        ApolloBizConfig.class,
        ApolloConfigServiceConfig.class,
        ApolloMetaServiceConfig.class,
        AdminServiceConfig.class,
        PortalAppConfig.class,
        PortalOpenApiConfig.class
})
public class ConfigServerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
