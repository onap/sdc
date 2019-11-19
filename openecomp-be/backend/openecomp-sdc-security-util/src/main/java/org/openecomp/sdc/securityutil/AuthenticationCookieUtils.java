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

package org.openecomp.sdc.securityutil;

import org.openecomp.sdc.securityutil.filters.SessionValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;

public class AuthenticationCookieUtils {

    private static final Logger log = LoggerFactory.getLogger(SessionValidationFilter.class.getName());

    private AuthenticationCookieUtils() {
    }

    /**
     * Update given cookie session time value to current time
     *
     * @param cookie
     * @param filterConfiguration
     * @return
     * @throws CipherUtilException
     * @throws IOException
     */
    public static Cookie updateSessionTime(Cookie cookie, ISessionValidationFilterConfiguration filterConfiguration) throws CipherUtilException, IOException {
        AuthenticationCookie authenticationCookie = getAuthenticationCookie(cookie, filterConfiguration);
        long newTime = System.currentTimeMillis();
        log.debug("SessionValidationFilter: Going to set new session time in cookie, old value: {}, new value: {}", authenticationCookie.getCurrentSessionTime(), newTime);
        authenticationCookie.setCurrentSessionTime(newTime);
        String encryptedCookie = getEncryptedCookie(authenticationCookie, filterConfiguration);
        return createUpdatedCookie(cookie, encryptedCookie, filterConfiguration);
    }

    /**
     * Create new Cookie object with same attributes as original cookie
     * @param cookie
     * @param encryptedCookie
     * @param cookieConfiguration
     * @return
     */
    public static Cookie createUpdatedCookie(Cookie cookie, String encryptedCookie, ISessionValidationCookieConfiguration cookieConfiguration) {
        Cookie updatedCookie = new Cookie(cookie.getName(), encryptedCookie );
        updatedCookie.setSecure(true);
        updatedCookie.setPath(cookieConfiguration.getCookiePath());
        updatedCookie.setDomain(cookieConfiguration.getCookieDomain());
        updatedCookie.setHttpOnly(cookieConfiguration.isCookieHttpOnly());
        return updatedCookie;
    }

    /**
     * Convert AuthenticationCookie to JSON and encrypt with given key
     *
     * @param authenticationCookie
     * @param filterConfiguration
     * @return
     * @throws IOException
     * @throws CipherUtilException
     */
    public static String getEncryptedCookie(AuthenticationCookie authenticationCookie, ISessionValidationFilterConfiguration filterConfiguration) throws IOException, CipherUtilException {
        String changedCookieJson = RepresentationUtils.toRepresentation(authenticationCookie);
        return CipherUtil.encryptPKC(changedCookieJson, filterConfiguration.getSecurityKey());
    }

    /**
     * Decrypt given Cookie to JSON and convert to AuthenticationCookie object
     *
     * @param cookie
     * @param filterConfiguration
     * @return
     * @throws CipherUtilException
     */
    public static AuthenticationCookie getAuthenticationCookie(Cookie cookie, ISessionValidationFilterConfiguration filterConfiguration) throws CipherUtilException {
        String originalCookieJson = CipherUtil.decryptPKC(cookie.getValue(), filterConfiguration.getSecurityKey());
        return RepresentationUtils.fromRepresentation(originalCookieJson, AuthenticationCookie.class);
    }

    /**
     * session expired if session was idle or max time reached
     *
     * @param cookie
     * @param filterConfiguration
     * @return
     * @throws CipherUtilException
     */
    public static boolean isSessionExpired(Cookie cookie, ISessionValidationFilterConfiguration filterConfiguration) throws CipherUtilException {
        AuthenticationCookie authenticationCookie = getAuthenticationCookie(cookie, filterConfiguration);
        long sessionExpirationDate = authenticationCookie.getMaxSessionTime() + filterConfiguration.getMaxSessionTimeOut();
        long sessionTime = authenticationCookie.getCurrentSessionTime();
        long currentTime = System.currentTimeMillis();
        log.debug("SessionValidationFilter: Checking if session expired: session time: {}, expiration time: {}, current time: {}", sessionTime, sessionExpirationDate, currentTime);
        return currentTime > sessionExpirationDate || isSessionIdle(sessionTime, currentTime, filterConfiguration);
    }

    /**
     * Session is idle if wasn't updated ( wasn't in use ) for more then value from filter configuration
     *
     * @param sessionTimeValue
     * @param currentTime
     * @param filterConfiguration
     * @return
     */
    private static boolean isSessionIdle(long sessionTimeValue, long currentTime, ISessionValidationFilterConfiguration filterConfiguration) {
        long currentIdleTime = currentTime - sessionTimeValue;
        long maxIdleTime = filterConfiguration.getSessionIdleTimeOut();
        log.debug("SessionValidationFilter: Checking if session idle: session time: {}, current idle time: {}, max idle time: {}", currentTime, currentIdleTime, maxIdleTime);
        return currentIdleTime >= maxIdleTime;
    }

}
