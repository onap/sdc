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