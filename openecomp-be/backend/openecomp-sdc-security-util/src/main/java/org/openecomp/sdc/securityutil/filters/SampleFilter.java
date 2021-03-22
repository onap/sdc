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
package org.openecomp.sdc.securityutil.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.Cookie;
import org.openecomp.sdc.securityutil.ISessionValidationFilterConfiguration;

public class SampleFilter extends SessionValidationFilter {

    @Override
    public ISessionValidationFilterConfiguration getFilterConfiguration() {
        return Configuration.getInstance();
    }

    @Override
    protected Cookie addRoleToCookie(Cookie updatedCookie) {
        return updatedCookie;
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
        private String redirectURL;
        private List<String> excludedUrls;
        private String cookieName;
        private String cookieDomain;
        private String cookiePath;
        private boolean isCookieHttpOnly;

        private Configuration() {
            //security key should be exactly 16 characters long clear text and then encoded to base64
            this.securityKey = "AGLDdG4D04BKm2IxIWEr8o==";
            this.maxSessionTimeOut = 24L * 60L * 60L * 1000L;
            this.sessionIdleTimeOut = 60L * 60L * 1000L;
            this.redirectURL = "https://www.e-access.att.com/ecomp_portal_ist/ecompportal/process_csp";
            this.excludedUrls = new ArrayList<>(Arrays.asList("/config", "/configmgr", "/rest", "/kibanaProxy", "/healthcheck", "/upload.*"));
            this.cookieName = "kuku";
            this.cookieDomain = "";
            this.cookiePath = "/";
            this.isCookieHttpOnly = true;
        }

        public static Configuration getInstance() {
            if (instance == null) {
                instance = new Configuration();
            }
            return instance;
        }

        @Override
        public String getSecurityKey() {
            return securityKey;
        }

        public void setSecurityKey(String securityKey) {
            this.securityKey = securityKey;
        }

        @Override
        public long getMaxSessionTimeOut() {
            return maxSessionTimeOut;
        }

        public void setMaxSessionTimeOut(long maxSessionTimeOut) {
            this.maxSessionTimeOut = maxSessionTimeOut;
        }

        @Override
        public long getSessionIdleTimeOut() {
            return sessionIdleTimeOut;
        }

        @Override
        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
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

        public void setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
        }

        @Override
        public List<String> getExcludedUrls() {
            return excludedUrls;
        }

        public void setExcludedUrls(List<String> excludedUrls) {
            this.excludedUrls = excludedUrls;
        }
    }
}
