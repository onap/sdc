package org.openecomp.server.configuration;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.openecomp.server.filters.*;
import org.openecomp.sdc.common.filters.*;
import org.openecomp.sdc.itempermissions.servlet.PermissionsFilter;
import org.openecomp.server.listeners.OnboardingAppStartupListener;
import org.springframework.boot.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class WebXmlMigrationConfig {

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> onboardingAppStartupListener() {
        return new ServletListenerRegistrationBean<>(new OnboardingAppStartupListener());
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> springMapper(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> bean = new ServletRegistrationBean<>(dispatcherServlet, "/ws/*");
        bean.setName("spring-mapper");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<MultitenancyKeycloakFilter> keycloakFilter() {
        FilterRegistrationBean<MultitenancyKeycloakFilter> bean = new FilterRegistrationBean<>(new MultitenancyKeycloakFilter());
        bean.setUrlPatterns(List.of(
                "/keycloak/*",
                "/v1.0/vendor-license-models/*",
                "/v1.0/vendor-software-products",
                "*/actions",
                "/v1.0/items/*"
        ));
        bean.setName("KeycloakFilter");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<DataValidatorFilter> dataValidatorFilter() {
        return buildFilter(new DataValidatorFilter(), "/v1.0/*", 2);
    }

    @Bean
    public FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> cspFilter() {
        return buildFilter(new ContentSecurityPolicyHeaderFilter(), "/*", 3);
    }

    @Bean
    public FilterRegistrationBean<PermissionsFilter> permissionsFilter() {
        FilterRegistrationBean<PermissionsFilter> bean = new FilterRegistrationBean<>(new PermissionsFilter());
        bean.setUrlPatterns(List.of(
                "/v1.0/vendor-license-models/*",
                "/v1.0/vendor-software-products/*"
        ));
        bean.setName("PermissionsFilter");
        bean.setOrder(4);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CrossOriginFilter> corsFilter() {
        FilterRegistrationBean<CrossOriginFilter> bean = new FilterRegistrationBean<>(new CrossOriginFilter());
        bean.setUrlPatterns(List.of("/*"));
        bean.setInitParameters(Map.of(
                "allowedOrigins", "*",
                "allowedMethods", "*",
                "allowedHeaders", "*"
        ));
        bean.setName("CrossOriginFilter");
        bean.setOrder(5);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RestrictionAccessFilter> restrictionAccessFilter() {
        return buildFilter(new RestrictionAccessFilter(), "/*", 6);
    }

    @Bean
    public FilterRegistrationBean<BasicAuthenticationFilter> basicAuthFilter() {
        return buildFilter(new BasicAuthenticationFilter(), "/1.0/*", 7);
    }

    @Bean
    public FilterRegistrationBean<ActionAuthenticationFilter> authNFilter() {
        return buildFilter(new ActionAuthenticationFilter(), "/workflow/v1.0/actions/*", 8);
    }

    @Bean
    public FilterRegistrationBean<ActionAuthorizationFilter> authZFilter() {
        return buildFilter(new ActionAuthorizationFilter(), "/workflow/v1.0/actions/*", 9);
    }

    @Bean
    public FilterRegistrationBean<OnboardingSessionContextFilter> sessionContextFilter() {
        return buildFilter(new OnboardingSessionContextFilter(), "/*", 10);
    }

    private <T extends Filter> FilterRegistrationBean<T> buildFilter(T filter, String pattern, int order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setUrlPatterns(List.of(pattern));
        bean.setOrder(order);
        return bean;
    }

     @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        Properties props = new Properties();
        mapping.setMappings(props);
        return mapping;
    }
}
