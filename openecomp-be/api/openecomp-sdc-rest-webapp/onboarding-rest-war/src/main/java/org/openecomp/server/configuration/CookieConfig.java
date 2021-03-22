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
package org.openecomp.server.configuration;

import java.util.List;

public class CookieConfig {

    String securityKey = "";
    long maxSessionTimeOut = 600L * 1000L;
    long sessionIdleTimeOut = 30L * 1000L;
    String cookieName = "AuthenticationCookie";
    String redirectURL = "portal_url";
    List<String> excludedUrls;
    List<String> onboardingExcludedUrls;
    String domain = "";
    String path = "";
    boolean isHttpOnly = true;

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public long getMaxSessionTimeOut() {
        return maxSessionTimeOut;
    }

    public void setMaxSessionTimeOut(long maxSessionTimeOut) {
        this.maxSessionTimeOut = maxSessionTimeOut;
    }

    public long getSessionIdleTimeOut() {
        return sessionIdleTimeOut;
    }

    public void setSessionIdleTimeOut(long sessionIdleTimeOut) {
        this.sessionIdleTimeOut = sessionIdleTimeOut;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public List<String> getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(List<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isHttpOnly() {
        return isHttpOnly;
    }

    public void setIsHttpOnly(boolean isHttpOnly) {
        this.isHttpOnly = isHttpOnly;
    }

    public List<String> getOnboardingExcludedUrls() {
        return onboardingExcludedUrls;
    }

    public void setOnboardingExcludedUrls(List<String> onboardingExcludedUrls) {
        this.onboardingExcludedUrls = onboardingExcludedUrls;
    }
}
