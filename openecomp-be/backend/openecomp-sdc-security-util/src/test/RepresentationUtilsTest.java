package org.onap.sdc.security;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class RepresentationUtilsTest {

    private static AuthenticationCookie originalCookie = new AuthenticationCookie("kuku");

    @Test
    public void representationE2EwithRoleNull() throws IOException {
        originalCookie.setRoles(null);
        String jsonStr = RepresentationUtils.toRepresentation(originalCookie);
        AuthenticationCookie cookieFromJson = RepresentationUtils.fromRepresentation(jsonStr, AuthenticationCookie.class);
        assertTrue(originalCookie.equals(cookieFromJson));
    }

    @Test
    public void representationE2EwithRoleNotNull() throws IOException {
        Set<String> roles = new HashSet<String>();
        roles.add("Designer");
        roles.add("Admin");
        roles.add("Tester");
        originalCookie.setRoles(roles);
        String jsonStr = RepresentationUtils.toRepresentation(originalCookie);
        AuthenticationCookie cookieFromJson = RepresentationUtils.fromRepresentation(jsonStr, AuthenticationCookie.class);
        assertTrue(originalCookie.equals(cookieFromJson));
    }
}
