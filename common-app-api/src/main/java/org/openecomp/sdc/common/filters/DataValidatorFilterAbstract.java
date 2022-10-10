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

package org.openecomp.sdc.common.filters;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.util.DataValidator;
import org.openecomp.sdc.common.util.SecureString;
import org.openecomp.sdc.exception.NotAllowedSpecialCharsException;

public abstract class DataValidatorFilterAbstract implements Filter {

    protected static final String DATA_VALIDATOR_FILTER_EXCLUDED_URLS = "dataValidatorFilterExcludedUrls";
    private DataValidator dataValidator;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        dataValidator = new DataValidator();
    }

    @Override
    public void destroy() {
        dataValidator = null;
    }

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException, NotAllowedSpecialCharsException {
        if (isExcluded(((HttpServletRequest) request).getRequestURI()) || !isPostOrPut(((HttpServletRequest) request).getMethod())) {
            chain.doFilter(request, response);
        } else {
            if (!skipCheckBody((HttpServletRequest) request)) {
                request = new RequestWrapper((HttpServletRequest) request);
            }
            if (isValid((HttpServletRequest) request)) {
                chain.doFilter(request, response);
            } else {
                throw new NotAllowedSpecialCharsException();
            }
        }
    }

    private boolean isPostOrPut(final String method) {
        return method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT);
    }

    private boolean isExcluded(final String path) {
        final List<String> dataValidatorFilterExcludedUrlsList = getDataValidatorFilterExcludedUrls();
        return CollectionUtils.isNotEmpty(dataValidatorFilterExcludedUrlsList)
            && dataValidatorFilterExcludedUrlsList.stream().anyMatch(s -> path.trim().contains(s.trim()));
    }

    protected abstract List<String> getDataValidatorFilterExcludedUrls();

    private boolean skipCheckBody(final HttpServletRequest requestWrapper) {
        final String contentType = requestWrapper.getContentType();
        return StringUtils.isNotEmpty(contentType) && contentType.contains("multipart/form-data");
    }

    private boolean isValid(final HttpServletRequest request) {
        final boolean skipCheckBody = skipCheckBody(request);
        return (skipCheckBody || checkBody((RequestWrapper) request))
            && checkHeaders(request)
            && checkCookies(request)
            && checkParameters(request)
            && checkQuery(request);
    }

    private boolean checkParameters(final HttpServletRequest httpRequest) {
        final Iterator<String> parameterNamesIterator = httpRequest.getParameterNames().asIterator();
        while (parameterNamesIterator.hasNext()) {
            final String parameterName = parameterNamesIterator.next();
            final String parameter = httpRequest.getParameter(parameterName);
            if (!dataValidator.isValid(new SecureString(parameter))) {
                return false;
            }
            final String[] parameterValues = httpRequest.getParameterValues(parameterName);
            if (parameterValues != null) {
                for (final String parameterValue : parameterValues) {
                    if (!dataValidator.isValid(new SecureString(parameterValue))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkHeaders(final HttpServletRequest httpRequest) {
        final Iterator<String> headerNamesIterator = httpRequest.getHeaderNames().asIterator();
        while (headerNamesIterator.hasNext()) {
            final String headerName = headerNamesIterator.next();
            final String header = httpRequest.getHeader(headerName);
            if (!dataValidator.isValid(new SecureString(header))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCookies(final HttpServletRequest httpRequest) {
        final Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                if (!dataValidator.isValid(new SecureString(cookie.getValue()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkQuery(final HttpServletRequest httpRequest) {
        final String queryString = httpRequest.getQueryString();
        return StringUtils.isEmpty(queryString) || dataValidator.isValid(new SecureString(queryString));
    }

    private boolean checkBody(final RequestWrapper httpRequest) {
        final String body = httpRequest.getBody();
        return StringUtils.isEmpty(body) || dataValidator.isValid(new SecureString(body));
    }

}

