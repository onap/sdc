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
                e.printStackTrace();
            }
        log.info("Multitenancy =" + keycloak);
        return keycloak;
    }
}
