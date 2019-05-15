package org.openecomp.sdcrests.validation.rest.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdcrests.validation.rest.services.ValidationImpl;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ValidationImplTest {

    @Mock
    private UploadValidationManager uploadValidationManager;
    @InjectMocks
    private ValidationImpl validation;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void validateFileTest() throws IOException {
        when(uploadValidationManager.validateFile(any(), any())).thenReturn(new ValidationFileResponse());
        Response response = validation.validateFile("", new ByteArrayInputStream("".getBytes()));
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void validateFileExceptionTest() throws IOException {
        when(uploadValidationManager.validateFile(any(), any())).thenThrow(new IOException());
        Response response = validation.validateFile("", new ByteArrayInputStream("".getBytes()));
   }
}
