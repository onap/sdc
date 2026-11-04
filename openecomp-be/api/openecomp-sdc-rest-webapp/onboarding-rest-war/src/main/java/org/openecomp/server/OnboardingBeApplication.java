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

package org.openecomp.server;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.server.configuration.OnboardingApplicationConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Runs onboarding-be: the context of {@link OnboardingApplicationConfig} in embedded Jetty, shaped by
 * {@link OnboardingBeServer} and {@link OnboardingBeWebApp}.
 */
@SpringBootConfiguration
// Only the web server is auto-configured: @EnableAutoConfiguration would also find Jackson, bean validation and more
// on this classpath and add beans of its own to onboarding-be's context.
@ImportAutoConfiguration(ServletWebServerFactoryAutoConfiguration.class)
@Import(OnboardingApplicationConfig.class)
public class OnboardingBeApplication {

    public static void main(String[] args) {
        // -Dlogback.configurationFile configures logback; Boot's logging system would replace that with its defaults.
        if (System.getProperty(LoggingSystem.SYSTEM_PROPERTY) == null) {
            System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
        }
        // The session context is per thread, and the beans that read it while they are created are created on this one.
        SessionContextProviderFactory.getInstance().createInterface().create("onboarding", "dox");
        SpringApplication application = new SpringApplication(OnboardingBeApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        // Plain Spring allows both and Boot does not. OnboardingApplicationConfig replaces scanned prototypes such as
        // healthCheck with singletons of the same name.
        application.setAllowBeanDefinitionOverriding(true);
        application.setAllowCircularReferences(true);
        application.run(args);
    }

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> onboardingBeServer() {
        return OnboardingBeServer::customize;
    }
}
