/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.onap.sdc.security.PortalClient;
import org.onap.sdc.security.filters.RestrictionAccessFilter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.stereotype.Component;

@Component("beRestrictionAccessFilter")
public class BeRestrictionAccessFilter extends RestrictionAccessFilter {

    private static final Logger log = Logger.getLogger(BeRestrictionAccessFilter.class.getName());

    public BeRestrictionAccessFilter(FilterConfiguration configuration, ThreadLocalUtils threadLocalUtils, PortalClient portalClient) {
        super(configuration, threadLocalUtils, portalClient);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        if (ThreadLocalsHolder.isInternalRequest()) {
            super.doFilter(servletRequest, servletResponse, filterChain);
        } else {
            log.debug("Access Restriction cookie validation is not needed");
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
