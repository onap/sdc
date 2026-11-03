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

package org.openecomp.server.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.openecomp.core.externaltesting.impl.ExternalTestingManagerImpl;
import org.openecomp.sdc.action.errors.ActionExceptionMapper;
import org.openecomp.sdc.action.impl.ActionManagerImpl;
import org.openecomp.sdc.applicationconfig.impl.ApplicationConfigManagerImpl;
import org.openecomp.sdc.common.errors.DefaultExceptionMapper;
import org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter;
import org.openecomp.sdc.logging.servlet.jaxrs.LoggingResponseFilter;
import org.openecomp.sdc.validation.impl.UploadValidationManagerImpl;
import org.openecomp.sdcrests.errors.ZusammenExceptionMapper;
import org.openecomp.sdcrests.health.rest.services.HealthCheckImpl;
import org.openecomp.sdcrests.item.rest.services.ItemsImpl;
import org.openecomp.sdcrests.itempermissions.rest.services.ItemPermissionsImpl;
import org.openecomp.sdcrests.notifications.rest.services.impl.NotificationsImpl;
import org.openecomp.sdcrests.togglz.rest.services.TogglzFeaturesImpl;
import org.openecomp.sdcrests.uniquevalue.rest.services.UniqueTypesImpl;
import org.openecomp.server.interceptors.EmptyOutputOutInterceptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Root application context of the onboarding web application, loaded by the {@code ContextLoaderListener} in
 * {@code web.xml}.
 */
@Configuration
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
@ComponentScan({"org.openecomp.sdcrests", "org.openecomp.sdc.vendorsoftwareproduct.dao"})
public class OnboardingApplicationConfig {

    private static final List<String> SERVICE_BEANS = List.of(
        "versions",
        "conflicts",
        "vendorLicenseModels",
        "licenseAgreements",
        "featureGroups",
        "entitlementPools",
        "entitlementPoolLimits",
        "licenseKeyGroups",
        "licenseKeyGroupLimits",
        "vendorSoftwareProducts",
        "packageUploadManagerController",
        "networks",
        "components",
        "nics",
        "computes",
        "processes",
        "componentProcesses",
        "validation",
        "externaltesting",
        "actions",
        "applicationConfiguration",
        "componentMonitoringUploads",
        "deploymentFlavors",
        "images",
        "orchestrationTemplateCandidate",
        "vnfPackageRepository",
        "componentDependencies",
        "healthCheck",
        "itemPermissions",
        "notifications",
        "togglz",
        "items",
        "uniqueTypes",
        "prometheusMetricsController");

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    public ObjectMapper jacksonObjectMapper() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public JacksonJsonProvider jsonProvider() {
        JacksonJsonProvider provider = new JacksonJsonProvider();
        provider.setMapper(jacksonObjectMapper());
        return provider;
    }

    @Bean
    public EmptyOutputOutInterceptor outEmptyResponseInterceptor() {
        return new EmptyOutputOutInterceptor();
    }

    @Bean
    public UploadValidationManagerImpl uploadValidationManager() {
        return new UploadValidationManagerImpl();
    }

    @Bean
    public ActionManagerImpl actionManager() {
        return new ActionManagerImpl();
    }

    @Bean
    public ApplicationConfigManagerImpl applicationConfigManager() {
        return new ApplicationConfigManagerImpl();
    }

    @Bean
    public ExternalTestingManagerImpl externalTestingManager() {
        return new ExternalTestingManagerImpl();
    }

    /**
     * Replaces the prototype that {@code @Service("healthCheck")} scans with a singleton, as do the four after it.
     */
    @Bean
    public HealthCheckImpl healthCheck() {
        return new HealthCheckImpl();
    }

    @Bean
    public ItemPermissionsImpl itemPermissions() {
        return new ItemPermissionsImpl();
    }

    @Bean
    public NotificationsImpl notifications() {
        return new NotificationsImpl();
    }

    @Bean
    public ItemsImpl items() {
        return new ItemsImpl();
    }

    @Bean
    public UniqueTypesImpl uniqueTypes() {
        return new UniqueTypesImpl();
    }

    @Bean
    public TogglzFeaturesImpl togglz() {
        return new TogglzFeaturesImpl();
    }

    /**
     * Each service bean is resolved once, when the server is created, so a prototype-scoped one serves every request
     * from the same instance.
     */
    @Bean(destroyMethod = "destroy")
    public Server restContainer(Bus cxf, BeanFactory beanFactory) {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(cxf);
        factory.setAddress("/");
        factory.setServiceBeans(SERVICE_BEANS.stream().map(beanFactory::getBean).collect(Collectors.toList()));
        LoggingRequestFilter loggingRequestFilter = new LoggingRequestFilter();
        loggingRequestFilter.setRequestIdHeaders("X-ONAP-RequestID,X-RequestID,X-TransactionId,X-ECOMP-RequestID");
        loggingRequestFilter.setPartnerNameHeaders("USER_ID,X-ONAP-PartnerName,User-Agent");
        factory.setProviders(List.of(jsonProvider(), new ActionExceptionMapper(), new DefaultExceptionMapper(),
            new ZusammenExceptionMapper(), loggingRequestFilter, new LoggingResponseFilter()));
        factory.setOutInterceptors(List.of(outEmptyResponseInterceptor()));
        return factory.create();
    }
}
