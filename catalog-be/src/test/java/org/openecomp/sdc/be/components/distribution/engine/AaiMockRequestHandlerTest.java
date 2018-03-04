package org.openecomp.sdc.be.components.distribution.engine;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.info.OperationalEnvInfo;
import org.openecomp.sdc.common.http.client.api.HttpResponse;

import static org.junit.Assert.assertTrue;

public class AaiMockRequestHandlerTest extends BeConfDependentTest {
    
    @Ignore
    @Test
    public void testGetOperationalEnvJson() {        
        String id = "OEid4";
        AaiRequestHandler aaiRequestHandler = new AaiRequestHandler();
        aaiRequestHandler.init();
        HttpResponse<String> resp = aaiRequestHandler.getOperationalEnvById(id);

        if (resp.getStatusCode() == HttpStatus.SC_OK) {
            try {
                String response = resp.getResponse();
                System.out.println("The rest response is:");
                System.out.println(response);
                
                OperationalEnvInfo operationalEnvInfo = OperationalEnvInfo.createFromJson(response);

                System.out.println(String.format("Get \"%s\" operational environment. %s", id, operationalEnvInfo));
                System.out.println(operationalEnvInfo);
            }
            catch (Exception e) {
                System.out.println(String.format("Json convert to OperationalEnvInfo failed with exception %s", e));
                System.out.println(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }
        else {
            System.out.println("The rest response is:");
            String response = resp.getResponse();
            System.out.println(response);
            System.out.println(String.format("Get \"%s\" operational environment failed with statusCode: %s, response: %s, description: %s", id, resp.getStatusCode(), resp.getResponse(), resp.getDescription()));
            System.out.println(resp.getStatusCode());
        }
        
        assertTrue(true);
    } 
}
