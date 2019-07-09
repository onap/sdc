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
