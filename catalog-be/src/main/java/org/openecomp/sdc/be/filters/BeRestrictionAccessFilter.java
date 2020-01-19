package org.openecomp.sdc.be.filters;

import org.onap.sdc.security.PortalClient;
import org.onap.sdc.security.filters.RestrictionAccessFilter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component("beRestrictionAccessFilter")
public class BeRestrictionAccessFilter extends RestrictionAccessFilter {

    private static final Logger log = Logger.getLogger(RestrictionAccessFilter.class.getName());

    public BeRestrictionAccessFilter(FilterConfiguration configuration, ThreadLocalUtils threadLocalUtils,
                       PortalClient portalClient) {
        super(configuration, threadLocalUtils, portalClient);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (ThreadLocalsHolder.isInternalRequest()) {
            super.doFilter(servletRequest, servletResponse, filterChain);
        } else {
            log.debug("Access Restriction cookie validation is not needed");
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
