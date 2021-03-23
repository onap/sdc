package org.openecomp.sdc.action.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_ARTIFACT_UPDATE_READ_ONLY;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_AUTHENTICATION_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE;
import static org.openecomp.sdc.action.errors.ActionErrorConstants.ACTION_NOT_LOCKED_CODE;

import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ActionExceptionMapperTest {

    private ActionExceptionMapper createTestSubject() {
        return new ActionExceptionMapper();
    }

    @Test
    void toResponse_test() {
        final ActionExceptionMapper testSubject = createTestSubject();
        ActionException actionException;
        Response response;

        actionException = new ActionException(ACTION_NOT_LOCKED_CODE, "ACT1021");
        response = testSubject.toResponse(actionException);
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        actionException = new ActionException(ACTION_AUTHENTICATION_ERR_CODE, "ACT1000");
        response = testSubject.toResponse(actionException);
        assertNotNull(response);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

        actionException = new ActionException(ACTION_ARTIFACT_UPDATE_READ_ONLY, "ACT1026");
        response = testSubject.toResponse(actionException);
        assertNotNull(response);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

        actionException = new ActionException(ACTION_ARTIFACT_ENTITY_NOT_EXIST_CODE, "ACT1046");
        response = testSubject.toResponse(actionException);
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        actionException = new ActionException(ACTION_INTERNAL_SERVER_ERR_CODE, "ACT1060");
        response = testSubject.toResponse(actionException);
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

    }

}
