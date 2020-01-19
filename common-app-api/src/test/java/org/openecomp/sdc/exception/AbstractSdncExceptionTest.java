package org.openecomp.sdc.exception;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.slf4j.MDC;

public class AbstractSdncExceptionTest {
    @Before
    public void clearMdcTable(){
        MDC.clear();
    }


    @Test
    public void testServiceExceptionEcompRequestIdNull() {
        String[] variables = {"1234","Test_VF"};
        ServiceException serviceException = new ServiceException("SVC4628", "Error: The VSP with UUID %1 was already imported for VF %2. Please select another or update the existing VF.", variables);
        String requestId=serviceException.getEcompRequestId();
        Assert.assertNull(requestId);
    }

    @Test
    public void testServiceExceptionEcompRequestIdNotNull() {
        String[] variables = {"1234","Test_VF"};
        String expectedRequestId="b819266d-3b92-4e07-aec4-cb7f0d4010a4";
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,expectedRequestId);
        ServiceException serviceException = new ServiceException("SVC4628", "Error: The VSP with UUID %1 was already imported for VF %2. Please select another or update the existing VF.", variables);
        String requestId=serviceException.getEcompRequestId();
        Assert.assertEquals(requestId,expectedRequestId);
    }


    @Test
    public void testPolicyExceptionEcompRequestIdfieldNull() {
        String[] variables = {"1234","Test_VF"};
        PolicyException policyexception = new PolicyException("SVC4628", "Error: The VSP with UUID %1 was already imported for VF %2. Please select another or update the existing VF.", variables);
        String requestId=policyexception.getEcompRequestId();
        Assert.assertNull(requestId);
    }

    @Test
    public void testPolicyExceptionEcompRequestIdNotNull() {
        String[] variables = {"1234","Test_VF"};
        String expectedRequestId="b819266d-3b92-4e07-aec4-cb7f0d4010a4";
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,expectedRequestId);
        PolicyException policyexception = new PolicyException("SVC4628", "Error: The VSP with UUID %1 was already imported for VF %2. Please select another or update the existing VF.", variables);
        String requestId=policyexception.getEcompRequestId();
        Assert.assertEquals(requestId,expectedRequestId);
    }
}
