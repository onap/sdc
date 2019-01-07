package org.openecomp.sdc.be.config;

import java.util.List;

public class CookieConfig {

        String securityKey = "/";
        long maxSessionTimeOut = 600*1000;
        long sessionIdleTimeOut = 30*1000;
        String cookieName = "AuthenticationCookie";
        String redirectURL = "https://www.e-access.att.com/ecomp_portal_ist/ecompportal/process_csp";
        List<String> onboardingExcludedUrls;

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

        public List<String> getOnboardingExcludedUrls() {
            return onboardingExcludedUrls;
        }

        public void setOnboardingExcludedUrls(List<String> onboardingExcludedUrls) {
            this.onboardingExcludedUrls = onboardingExcludedUrls;
        }
    }
