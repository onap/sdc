package org.openecomp.sdc.securityutil;

/**
 *  Configuration for Cookie object , have to be same over all components of application
 */
public interface ISessionValidationCookieConfiguration {

    String getCookieName();
    String getCookieDomain();
    String getCookiePath();
    boolean isCookieHttpOnly();
}
