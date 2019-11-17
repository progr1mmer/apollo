package com.ctrip.framework.apollo.portal;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Suxy
 * @date 2019/11/15
 * @description file description
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = PortalAppConfig.class)
public class PortalAppConfig {

}
