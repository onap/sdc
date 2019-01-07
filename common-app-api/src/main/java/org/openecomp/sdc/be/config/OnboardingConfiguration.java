package org.openecomp.sdc.be.config;

import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.List;

public class OnboardingConfiguration extends BasicConfiguration {
    private CookieConfig authCookie;

    public CookieConfig getAuthCookie() {
        return authCookie;
    }

    public void setAuthCookie(CookieConfig authCookie) {
        this.authCookie = authCookie;
    }
}
