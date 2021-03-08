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

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CookieConfig {

    String securityKey = "";
    long maxSessionTimeOut = 600L*1000L;
    long sessionIdleTimeOut = 30L*1000L;
    String cookieName = "AuthenticationCookie";
    String redirectURL = "portal_url";
    List<String> excludedUrls;
    List<String> onboardingExcludedUrls;
    String domain = "";
    String path = "";
    boolean isHttpOnly = true;

    public boolean isHttpOnly() {
        return isHttpOnly;
    }
}
