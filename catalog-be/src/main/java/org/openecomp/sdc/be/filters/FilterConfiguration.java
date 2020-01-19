package org.openecomp.sdc.be.filters;

import org.onap.sdc.security.ISessionValidationFilterConfiguration;
import org.openecomp.sdc.be.config.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

public class FilterConfiguration implements ISessionValidationFilterConfiguration {

    protected Configuration config;

    public FilterConfiguration(org.openecomp.sdc.be.config.Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public String getSecurityKey() {
        return config.getAuthCookie().getSecurityKey();
    }

    @Override
    public long getMaxSessionTimeOut() {
        return config.getAuthCookie().getMaxSessionTimeOut();
    }

    @Override
    public long getSessionIdleTimeOut() {
        return config.getAuthCookie().getSessionIdleTimeOut();
    }

    @Override
    public String getRedirectURL() {
        return config.getAuthCookie().getRedirectURL();
    }

    @Override
    public List<String> getExcludedUrls() {
        return config.getAuthCookie().getExcludedUrls();
    }

    @Override
    public String getCookieName() {
        return config.getAuthCookie().getCookieName();
    }

    @Override
    public String getCookieDomain() {
        return config.getAuthCookie().getDomain();
    }

    @Override
    public String getCookiePath() {
        return config.getAuthCookie().getPath();
    }

    @Override
    public boolean isCookieHttpOnly() {
        return config.getAuthCookie().isHttpOnly();
    }
}
