package org.openecomp.sdc.securityutil;

import java.util.List;

public interface ISessionValidationFilterConfiguration extends ISessionValidationCookieConfiguration {

    String getSecurityKey();
    long getMaxSessionTimeOut();
    long getSessionIdleTimeOut(); // max idle time for session
    String getRedirectURL();
    List<String> getExcludedUrls(); // comma separated URLs, like this "/config,/configmgr,/rest,/kibanaProxy"
}

