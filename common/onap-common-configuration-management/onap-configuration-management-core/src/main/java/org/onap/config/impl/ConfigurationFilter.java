/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.onap.config.impl;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import org.onap.config.Constants;
import org.onap.config.api.Configuration;

@WebFilter("/")
public class ConfigurationFilter implements Filter {

    @Override
    public void init(FilterConfig paramFilterConfig) {
        //Use the default behavior
    }

    @Override
    public void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse, FilterChain paramFilterChain)
        throws IOException, ServletException {
        Configuration.TENANT.set(Constants.DEFAULT_TENANT);
        try {
            paramFilterChain.doFilter(paramServletRequest, paramServletResponse);
        } finally {
            Configuration.TENANT.remove();
        }
    }

    @Override
    public void destroy() {
        //Use the default behavior
    }
}
