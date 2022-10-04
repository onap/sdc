/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;

/**
 * Provides mechanism to filter request and wrap to {@link RequestWrapper} as needed.
 */
public class RequestWrapperFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to override
    }

    @Override
    public void destroy() {
        // nothing to override
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (isExcluded(((HttpServletRequest) request).getRequestURI())
            || !isPostOrPut(((HttpServletRequest) request).getMethod())
            || canSkipCheck(request)) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new RequestWrapper((HttpServletRequest) request), response);
        }
    }

    private List<String> getDataValidatorFilterExcludedUrls() {
        final String dataValidatorFilterExcludedUrls = ConfigurationManager.getConfigurationManager().getConfiguration().getDataValidatorFilterExcludedUrls();
        if (StringUtils.isNotBlank(dataValidatorFilterExcludedUrls)) {
            return Arrays.asList(dataValidatorFilterExcludedUrls.split(","));
        }
        return new ArrayList<>();
    }

    private boolean isPostOrPut(final String method) {
        return method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT);
    }

    private boolean canSkipCheck(final ServletRequest requestWrapper) {
        final String contentType = requestWrapper.getContentType();
        return StringUtils.isNotEmpty(contentType) && contentType.contains(MULTIPART_FORM_DATA_VALUE);
    }

    private boolean isExcluded(final String path) {
        final List<String> dataValidatorFilterExcludedUrlsList = getDataValidatorFilterExcludedUrls();
        return CollectionUtils.isNotEmpty(dataValidatorFilterExcludedUrlsList) && dataValidatorFilterExcludedUrlsList.stream().anyMatch(path::contains);
    }

}
