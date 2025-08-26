package org.openecomp.sdc.fe.servlets;

import org.glassfish.jersey.servlet.ServletContainer;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.fe.filters.ContentSecurityPolicyHeaderFilter;
import org.openecomp.sdc.fe.filters.GzipFilter;
import org.openecomp.sdc.fe.listen.FEAppContextListener;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    /** Jersey REST servlet (/rest/*) */
    @Bean
    public ServletRegistrationBean<ServletContainer> jerseyServlet() {
        ServletRegistrationBean<ServletContainer> reg =
            new ServletRegistrationBean<>(new ServletContainer(), "/rest/*");
        reg.setName("jersey");
        reg.addInitParameter("jersey.config.server.provider.packages",
                "org.openecomp.sdc.fe.servlets");
        reg.addInitParameter("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.media.multipart.MultiPartFeature");
        reg.addInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        reg.setLoadOnStartup(1);
        return reg;
    }

    /** FeProxy servlet (/feProxy/*) */
    @Bean
    public ServletRegistrationBean<FeProxyServlet> feProxyServlet() {
        ServletRegistrationBean<FeProxyServlet> reg =
            new ServletRegistrationBean<>(new FeProxyServlet(), "/feProxy/*");
        reg.setName("FeProxy");
        reg.setLoadOnStartup(1);
        return reg;
    }

    /** Portal servlet (/portal) */
    @Bean
    public ServletRegistrationBean<PortalServlet> portalServlet() {
        ServletRegistrationBean<PortalServlet> reg =
            new ServletRegistrationBean<>(new PortalServlet(), "/portal");
        reg.setName("portalServlet");
        return reg;
    }

    /** ViewStatusMessages servlet (/status) */
    // @Bean
    // public ServletRegistrationBean<ViewStatusServlet> statusServlet() {
    //     ServletRegistrationBean<ViewStatusServlet> reg =
    //         new ServletRegistrationBean<>(new ViewStatusServlet(), "/status");
    //     reg.setName("viewStatusMessagesServlet");
    //     return reg;
    // }

    /** Content-Security-Policy filter */
    @Bean
    public FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> cspFilter() {
        FilterRegistrationBean<ContentSecurityPolicyHeaderFilter> reg =
            new FilterRegistrationBean<>(new ContentSecurityPolicyHeaderFilter());
        reg.addUrlPatterns("/*");
        reg.setName("contentSecurityPolicyHeaderFilter");
        return reg;
    }

    /** Audit Log filter */
    // @Bean
    // public FilterRegistrationBean<AuditLogServletFilter> auditLogFilter() {
    //     FilterRegistrationBean<AuditLogServletFilter> reg =
    //         new FilterRegistrationBean<>(new AuditLogServletFilter());
    //     reg.addUrlPatterns("/*");
    //     reg.setName("auditLogServletFilter");
    //     return reg;
    // }

    /** Gzip filter (*.jsgz) */
    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilter() {
        FilterRegistrationBean<GzipFilter> reg =
            new FilterRegistrationBean<>(new GzipFilter());
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
}
