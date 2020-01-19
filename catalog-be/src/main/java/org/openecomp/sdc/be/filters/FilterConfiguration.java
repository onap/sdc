/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.filters;

import org.onap.sdc.security.ISessionValidationFilterConfiguration;
import org.openecomp.sdc.be.config.Configuration;

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
