/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom Intellectual Property. All rights reserved.
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

import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.openecomp.sdc.common.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.common.filters.DataValidatorFilter;
import org.openecomp.sdc.itempermissions.servlet.PermissionsFilter;
import org.openecomp.server.filters.ActionAuthenticationFilter;
import org.openecomp.server.filters.ActionAuthorizationFilter;
import org.openecomp.server.filters.BasicAuthenticationFilter;
import org.openecomp.server.filters.MultitenancyKeycloakFilter;
import org.openecomp.server.filters.OnboardingSessionContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

  @Bean
  ServletRegistrationBean<CXFServlet> cxfServlet() {
    ServletRegistrationBean<CXFServlet> registrationBean = new ServletRegistrationBean<>(new CXFServlet());
    registrationBean.setName("CXFServlet");
    registrationBean.addInitParameter("redirects-list", "/docs/(\\S)+\\.json");
    registrationBean.addInitParameter("redirects-attributes", "javax.servlet.include.request_uri");
    registrationBean.addInitParameter("redirect-servlet-name", "default");
    registrationBean.addUrlMappings("/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<MultitenancyKeycloakFilter> multitenancyKeycloakFilter() {
    FilterRegistrationBean<MultitenancyKeycloakFilter> registrationBean = new FilterRegistrationBean<>(
        new MultitenancyKeycloakFilter());
    registrationBean.setName("Keycloak Filter");
    registrationBean.addUrlPatterns("/keycloak/*", "/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products",
        "*/actions", "/v1.0/items/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<DataValidatorFilter> dataValidatorFilter() {
    FilterRegistrationBean<DataValidatorFilter> registrationBean = new FilterRegistrationBean<>(
        new DataValidatorFilter());
    registrationBean.setName("dataValidatorFilter");
    registrationBean.addUrlPatterns("/v1.0/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> contentSecurityPolicyHeaderFilter() {
    FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> registrationBean = new FilterRegistrationBean<>(
        new ContentSecurityPolicyHeaderFilter());
    registrationBean.setName("contentSecurityPolicyHeaderFilter");
    registrationBean.addUrlPatterns("/*");
    registrationBean.setAsyncSupported(true);
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<PermissionsFilter> permissionsFilter() {
    FilterRegistrationBean<PermissionsFilter> registrationBean = new FilterRegistrationBean<>(
        new PermissionsFilter());
    registrationBean.setName("PermissionsFilter");
    registrationBean.addUrlPatterns("/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<CrossOriginFilter> crossOriginFilter() {
    FilterRegistrationBean<CrossOriginFilter> registrationBean = new FilterRegistrationBean<>(
        new CrossOriginFilter());
    registrationBean.setName("cross-origin");
    registrationBean.addUrlPatterns("/*");
    registrationBean.addInitParameter("allowedOrigins", "*");
    registrationBean.addInitParameter("allowedMethods", "*");
    registrationBean.addInitParameter("allowedHeaders", "*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<org.openecomp.server.filters.RestrictionAccessFilter> restrictionAccessFilter() {
    FilterRegistrationBean<org.openecomp.server.filters.RestrictionAccessFilter> registrationBean = new FilterRegistrationBean<>(
        new org.openecomp.server.filters.RestrictionAccessFilter());
    registrationBean.setName("RestrictionAccessFilter");
    registrationBean.addUrlPatterns("/*");
    registrationBean.setAsyncSupported(true);
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<BasicAuthenticationFilter> basicAuthenticationFilter() {
    FilterRegistrationBean<BasicAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(
        new BasicAuthenticationFilter());
    registrationBean.setName("BasicAuth");
    registrationBean.addUrlPatterns("/v1.0/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<ActionAuthenticationFilter> actionAuthenticationFilter() {
    FilterRegistrationBean<ActionAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(
        new ActionAuthenticationFilter());
    registrationBean.setName("AuthN");
    registrationBean.addUrlPatterns("/workflow/v1.0/actions/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<ActionAuthorizationFilter> actionAuthorizationFilter() {
    FilterRegistrationBean<ActionAuthorizationFilter> registrationBean = new FilterRegistrationBean<>(
        new ActionAuthorizationFilter());
    registrationBean.setName("AuthZ");
    registrationBean.addUrlPatterns("/workflow/v1.0/actions/*");
    return registrationBean;
  }

  @Bean
  FilterRegistrationBean<OnboardingSessionContextFilter> onboardingSessionContextFilter() {
    FilterRegistrationBean<OnboardingSessionContextFilter> registrationBean = new FilterRegistrationBean<>(
        new OnboardingSessionContextFilter());
    registrationBean.setName("SessionContextFilter");
    registrationBean.addUrlPatterns("/*");
    return registrationBean;
  }
}
