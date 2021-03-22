/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.server.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.config.Configuration.BasicAuthConfig;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;

public class BasicAuthenticationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthenticationFilter.class);
    private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    private static final String CONFIG_SECTION = "basicAuth";

    private static Object getAuthenticationConfiguration(String file) throws IOException {
        InputStream fileInput = new FileInputStream(file);
        YamlUtil yamlUtil = new YamlUtil();
        Map<?, ?> configuration = Objects.requireNonNull(yamlUtil.yamlToMap(fileInput), "Configuration cannot be empty");
        Object authenticationConfig = configuration.get(CONFIG_SECTION);
        if (authenticationConfig == null) {
            throw new EntryNotConfiguredException(CONFIG_SECTION + " section");
        }
        return authenticationConfig;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
        String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
            "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);
        Object config = getAuthenticationConfiguration(file);
        ObjectMapper mapper = new ObjectMapper();
        BasicAuthConfig basicAuthConfig = mapper.convertValue(config, BasicAuthConfig.class);
        HttpServletRequest httpRequest = (HttpServletRequest) arg0;
        HttpServletRequestWrapper servletRequest = new HttpServletRequestWrapper(httpRequest);
        // BasicAuth is disabled
        if (!basicAuthConfig.isEnabled()) {
            arg2.doFilter(servletRequest, arg1);
            return;
        }
        List<String> excludedUrls = Arrays.asList(basicAuthConfig.getExcludedUrls().split(","));
        if (excludedUrls.contains(httpRequest.getServletPath() + httpRequest.getPathInfo())) {
            // this url is included in the excludeUrls list, no need for authentication
            arg2.doFilter(servletRequest, arg1);
            return;
        }
        // Get the basicAuth info from the header
        String authorizationHeader = httpRequest.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            ((HttpServletResponse) arg1).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String base64Credentials = httpRequest.getHeader("Authorization").replace("Basic", "").trim();
        if (verifyCredentials(basicAuthConfig, base64Credentials)) {
            arg2.doFilter(servletRequest, arg1);
        } else {
            ((HttpServletResponse) arg1).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    private boolean verifyCredentials(BasicAuthConfig basicAuthConfig, String credential) {
        String decodedCredentials = new String(Base64.getDecoder().decode(credential));
        int p = decodedCredentials.indexOf(':');
        if (p != -1) {
            String userName = decodedCredentials.substring(0, p).trim();
            String password = decodedCredentials.substring(p + 1).trim();
            if (!userName.equals(basicAuthConfig.getUserName()) || !password.equals(basicAuthConfig.getUserPass())) {
                log.error("Authentication failed. Invalid user name or password");
                return false;
            }
            return true;
        } else {
            log.error("Failed to decode credentials");
            return false;
        }
    }
}
