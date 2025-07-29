package org.openecomp.server.configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.openecomp.server.filters.ActionAuthenticationFilter;
import org.openecomp.server.filters.ActionAuthorizationFilter;
import org.openecomp.server.filters.BasicAuthenticationFilter;
import org.openecomp.server.filters.MultitenancyKeycloakFilter;
import org.openecomp.server.filters.OnboardingSessionContextFilter;
import org.openecomp.server.filters.RestrictionAccessFilter;
import org.openecomp.sdc.common.filters.*;
import org.openecomp.sdc.common.session.impl.AsdcSessionContextProvider;
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

    private final AsdcSessionContextProvider provider = new AsdcSessionContextProvider();

    @PostConstruct
    public void init() {
        provider.create("onboarding", "dox");
    }

    @PreDestroy
    public void cleanup() {
        provider.close();
    }

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
    public FilterRegistrationBean<OnboardingSessionContextFilter> sessionContextFilter() {
        FilterRegistrationBean<OnboardingSessionContextFilter> bean =
                new FilterRegistrationBean<>(new OnboardingSessionContextFilter());
        bean.setUrlPatterns(List.of("/*"));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); 
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
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<DataValidatorFilter> dataValidatorFilter() {
        return buildFilter(new DataValidatorFilter(), "/v1.0/*", 3);
    }

    @Bean
    public FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> cspFilter() {
        return buildFilter(new ContentSecurityPolicyHeaderFilter(), "/*", 4);
    }

    @Bean
    public FilterRegistrationBean<PermissionsFilter> permissionsFilter() {
        FilterRegistrationBean<PermissionsFilter> bean = new FilterRegistrationBean<>(new PermissionsFilter());
        bean.setUrlPatterns(List.of(
                "/v1.0/vendor-license-models/*",
                "/v1.0/vendor-software-products/*"
        ));
        bean.setName("PermissionsFilter");
        bean.setOrder(5);
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
        bean.setOrder(6);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RestrictionAccessFilter> restrictionAccessFilter() {
        return buildFilter(new RestrictionAccessFilter(), "/*", 7);
    }

    @Bean
    public FilterRegistrationBean<BasicAuthenticationFilter> basicAuthFilter() {
        return buildFilter(new BasicAuthenticationFilter(), "/1.0/*", 8);
    }

    @Bean
    public FilterRegistrationBean<ActionAuthenticationFilter> authNFilter() {
        return buildFilter(new ActionAuthenticationFilter(), "/workflow/v1.0/actions/*", 9);
    }

    @Bean
    public FilterRegistrationBean<ActionAuthorizationFilter> authZFilter() {
        return buildFilter(new ActionAuthorizationFilter(), "/workflow/v1.0/actions/*", 10);
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
