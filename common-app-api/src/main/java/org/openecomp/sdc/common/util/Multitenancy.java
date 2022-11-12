/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Tech-Mahindra Intellectual Property. All rights reserved.
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


package org.openecomp.sdc.common.util;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.openecomp.sdc.common.log.wrappers.Logger;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


/**
 * To check the Multitenancy
 */

public class Multitenancy {
    private static final Logger log = Logger.getLogger(Multitenancy.class);
    boolean keycloak;
    public AccessToken getAccessToken(HttpServletRequest request){
     KeycloakPrincipal principal = (KeycloakPrincipal) request.getUserPrincipal();
        return  principal.getKeycloakSecurityContext().getToken();
    }

    public boolean multitenancycheck() {
        log.info("Checking the Multitenancy ");
           Multitenancy mt = new Multitenancy();
            InputStream ioStream = mt.getClass()
                    .getResourceAsStream("/multitenancy.json");
            try (InputStreamReader isr = new InputStreamReader(ioStream,
                    StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                JSONParser jsonparser = new JSONParser();
                Object obj = jsonparser.parse(br);
                JSONObject mob=(JSONObject)obj;
                keycloak= (boolean) mob.get("multitenancy");
                ioStream.close();
            } catch (Exception e) {
                log.debug("Multitenancy Exception",e);
            }
        log.info("Multitenancy =", keycloak);
        return keycloak;
    }
}
