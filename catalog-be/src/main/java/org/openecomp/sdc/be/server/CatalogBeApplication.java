/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.server;

import org.openecomp.sdc.config.CatalogBeApplicationConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Runs catalog-be: the context of {@link CatalogBeApplicationConfig} in embedded Jetty, shaped by {@link CatalogBeServer}
 * and {@link CatalogBeWebApp}.
 */
@SpringBootConfiguration
// Only the web server is auto-configured: @EnableAutoConfiguration would also find Jackson, Gson, bean validation and
// more on this classpath and add beans of its own to catalog-be's context.
@ImportAutoConfiguration(ServletWebServerFactoryAutoConfiguration.class)
@Import(CatalogBeApplicationConfig.class)
public class CatalogBeApplication {

    public static void main(String[] args) {
        // -Dlogback.configurationFile configures logback; Boot's logging system would replace that with its defaults.
        if (System.getProperty(LoggingSystem.SYSTEM_PROPERTY) == null) {
            System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
        }
        SpringApplication application = new SpringApplication(CatalogBeApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        // Plain Spring allows both and Boot does not. JanusGraphSpringConfig#janusGraphGenericDao replaces the scanned
        // bean of the same name, and beans such as DataTypeOperation and PropertyOperation depend on each other.
        application.setAllowBeanDefinitionOverriding(true);
        application.setAllowCircularReferences(true);
        application.run(args);
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> catalogBeServer() {
        return CatalogBeServer::customize;
    }

    /**
     * Boot maps every Filter bean to {@code /*} and creates it while the web server starts, before the configuration is
     * loaded. These two filters belong behind the DelegatingFilterProxy that {@link CatalogBeWebApp} maps instead; a
     * disabled registration keeps Boot from adding them a second time.
     */
    @Bean
    public DelegatingFilterProxyRegistrationBean gatewayFilterRegistration() {
        return disabled("gatewayFilter");
    }

    @Bean
    public DelegatingFilterProxyRegistrationBean reqValidationFilterRegistration() {
        return disabled("reqValidationFilter");
    }

    private static DelegatingFilterProxyRegistrationBean disabled(String filterBeanName) {
        DelegatingFilterProxyRegistrationBean registration = new DelegatingFilterProxyRegistrationBean(filterBeanName);
        registration.setEnabled(false);
        return registration;
    }
}
