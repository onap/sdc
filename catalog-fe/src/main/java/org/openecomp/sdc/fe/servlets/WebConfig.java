package org.openecomp.sdc.fe.servlets;

import org.onap.logging.filter.base.AuditLogServletFilter;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.fe.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.fe.filters.GzipFilter;
import org.openecomp.sdc.fe.listen.FEAppContextListener;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;


@Configuration
public class WebConfig{


    @Bean
    public FilterRegistrationBean<AuditLogServletFilter> auditLogFilter() {
        FilterRegistrationBean<AuditLogServletFilter> reg = new FilterRegistrationBean<>(new AuditLogServletFilter());
        reg.addUrlPatterns("/*");
        reg.setName("AuditLogServletFilter");
        return reg;
    }



    /** Content-Security-Policy filter */
    @Bean
    public FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> cspFilter() {
        FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> reg = new FilterRegistrationBean<>(
                new ContentSecurityPolicyHeaderFilter());
        reg.addUrlPatterns("/*");
        reg.setName("contentSecurityPolicyHeaderFilter");
        return reg;
    }

    /** Gzip filter (*.jsgz) */
    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilter() {
        FilterRegistrationBean<GzipFilter> reg = new FilterRegistrationBean<>(new GzipFilter());
        reg.addUrlPatterns("*.jsgz");
        reg.setName("gzipFilter");
        return reg;
    }

    // /** FEAppContextListener */
    @Bean
    public ServletListenerRegistrationBean<FEAppContextListener> feAppContextListener() {
        return new ServletListenerRegistrationBean<>(new FEAppContextListener());
    }

    @Bean
    public ServletListenerRegistrationBean<AppContextListener> appContextListener() {
        return new ServletListenerRegistrationBean<>(new AppContextListener());
    }

  @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration(DispatcherServlet dispatcherServlet) {
    ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet);
    registration.getUrlMappings().clear();
    registration.addUrlMappings("/rest/*");
    return registration;
}

}
