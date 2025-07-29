/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.Cookie;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.securityutil.ISessionValidationFilterConfiguration;
import org.openecomp.sdc.securityutil.filters.SessionValidationFilter;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;
import org.openecomp.server.configuration.CookieConfig;

public class RestrictionAccessFilter extends SessionValidationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionAccessFilter.class);
    private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    private static final String CONFIG_SECTION = "authCookie";

    @Override
    public ISessionValidationFilterConfiguration getFilterConfiguration() {
        return Configuration.getInstance();
    }

    @Override
    protected Cookie addRoleToCookie(Cookie cookie) {
        return cookie;
    }

    @Override
    protected boolean isRoleValid(Cookie cookie) {
        return true;
    }

    private static class Configuration implements ISessionValidationFilterConfiguration {

        private static Configuration instance;
        private String securityKey;
        private long maxSessionTimeOut;
        private long sessionIdleTimeOut;
        private String cookieName;
        private String redirectURL;
        private List<String> excludedUrls;
        private String cookieDomain;
        private String cookiePath;
        private boolean isCookieHttpOnly;

        private Configuration() {
            try {
                LOGGER.debug("CONFIG_FILE_PROPERTY: {}", CONFIG_FILE_PROPERTY);
                String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
                      "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);              
                Object config = getAuthenticationConfiguration(file);
                ObjectMapper mapper = new ObjectMapper();
                CookieConfig cookieConfig = mapper.convertValue(config, CookieConfig.class);
                this.securityKey = cookieConfig.getSecurityKey();
                this.maxSessionTimeOut = cookieConfig.getMaxSessionTimeOut();
                this.sessionIdleTimeOut = cookieConfig.getSessionIdleTimeOut();
                this.cookieName = cookieConfig.getCookieName();
                this.redirectURL = cookieConfig.getRedirectURL();
                this.excludedUrls = cookieConfig.getOnboardingExcludedUrls();
                this.cookieDomain = cookieConfig.getDomain();
                this.cookiePath = cookieConfig.getPath();
                this.isCookieHttpOnly = cookieConfig.isHttpOnly();
            } catch (Exception e) {
                LOGGER.warn("Failed to load configuration. ", e);
            }
        }

        public static Configuration getInstance() {
            if (instance == null) {
                instance = new Configuration();
            }
            return instance;
        }

        private static Object getAuthenticationConfiguration(String file) throws IOException {
            Map<?, ?> configuration = Objects.requireNonNull(readConfigurationFile(file), "Configuration cannot be empty");
            Object authenticationConfig = configuration.get(CONFIG_SECTION);
            if (authenticationConfig == null) {
                throw new EntryNotConfiguredException(CONFIG_SECTION + " section");
            }
            return authenticationConfig;
        }

        private static Map<?, ?> readConfigurationFile(String file) throws IOException {
            try (InputStream fileInput = new FileInputStream(file)) {
                YamlUtil yamlUtil = new YamlUtil();
                return yamlUtil.yamlToMap(fileInput);
            }
        }

        @Override
        public String getSecurityKey() {
            return securityKey;
        }

        @Override
        public long getMaxSessionTimeOut() {
            return maxSessionTimeOut;
        }

        @Override
        public long getSessionIdleTimeOut() {
            return sessionIdleTimeOut;
        }

        @Override
        public String getCookieName() {
            return cookieName;
        }

        @Override
        public String getCookieDomain() {
            return cookieDomain;
        }

        @Override
        public String getCookiePath() {
            return cookiePath;
        }

        @Override
        public boolean isCookieHttpOnly() {
            return isCookieHttpOnly;
        }

        @Override
        public String getRedirectURL() {
            return redirectURL;
        }

        @Override
        public List<String> getExcludedUrls() {
            return excludedUrls;
        }
    }
}
