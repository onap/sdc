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

package org.onap.sdc.security;

import org.junit.Test;
import org.onap.sdc.security.filters.SampleFilter;

import javax.servlet.http.Cookie;

import java.io.IOException;

import static org.junit.Assert.*;

public class AuthenticationCookieUtilsTest {

    private SampleFilter sessionValidationFilter = new SampleFilter();
    private ISessionValidationFilterConfiguration filterCfg = sessionValidationFilter.getFilterConfiguration();

    @Test
    public void vaildateThatCookieCurrentSessionTimeIncreased() throws IOException, CipherUtilException {
        // original cookie, pojo and servlet cookie
        AuthenticationCookie authenticationCookieOriginal = new AuthenticationCookie("kuku");
        Cookie cookieWithOriginalTime = new Cookie(filterCfg.getCookieName(), AuthenticationCookieUtils.getEncryptedCookie(authenticationCookieOriginal,filterCfg ));
        // cookie with increased time, pojo and servlet cookie
        Cookie cookieWithIncreasedTime = AuthenticationCookieUtils.updateSessionTime(cookieWithOriginalTime, filterCfg);
        AuthenticationCookie authenticationCookieIncreasedTime = AuthenticationCookieUtils.getAuthenticationCookie(cookieWithIncreasedTime, filterCfg);
        // validation
        long currentSessionTimeOriginal = authenticationCookieOriginal.getCurrentSessionTime();
        long currentSessionTimeIncreased = authenticationCookieIncreasedTime.getCurrentSessionTime();
        assertTrue(currentSessionTimeOriginal < currentSessionTimeIncreased);
    }

    @Test
    public void validateSerializationEncriptionDeserializationDecryption() throws IOException, CipherUtilException {
        // original cookie, pojo and servlet cookie
        AuthenticationCookie authenticationCookieOriginal = new AuthenticationCookie("kuku");
        Cookie cookieWithOriginalTime = new Cookie(filterCfg.getCookieName(), AuthenticationCookieUtils.getEncryptedCookie(authenticationCookieOriginal,filterCfg ));
        // cookie with increased time, pojo and servlet cookie
        AuthenticationCookie decriptedAndDeserializedAuthenticationCookie = AuthenticationCookieUtils.getAuthenticationCookie(cookieWithOriginalTime,filterCfg);
        assertTrue(authenticationCookieOriginal.equals(decriptedAndDeserializedAuthenticationCookie));
    }



//    @Test
//    public void getEncryptedCookie() {
//    }
//
//    @Test
//    public void getAuthenticationCookie() {
//    }
//
//    @Test
//    public void isSessionExpired() {
//    }
}
