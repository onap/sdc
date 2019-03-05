package org.openecomp.server.configuration;

import java.util.List;

public class CookieConfig {

    String securityKey = "";
    long maxSessionTimeOut = 600*1000L;
    long sessionIdleTimeOut = 30*1000L;
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
