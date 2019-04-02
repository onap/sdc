package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.security.SecurityManagerException;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.data.PackageArchive;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import javax.ws.rs.core.Response;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VspManagerFactory.class, ActivityLogManagerFactory.class,
        OrchestrationTemplateCandidateManagerFactory.class, OrchestrationTemplateCandidateImpl.class})
public class OrchestrationTemplateCandidateImplTest {

    Logger logger = LoggerFactory.getLogger(OrchestrationTemplateCandidateImplTest.class);
    @Mock
    private OrchestrationTemplateCandidateManager candidateManager;
    @Mock
    private VendorSoftwareProductManager vendorSoftwareProductManager;
    @Mock
    private PackageArchive packageArchive;
    @Mock
    private VspManagerFactory vspManagerFactory;
    @Mock
    private ActivityLogManager activityLogManager;
    @Mock
    private ActivityLogManagerFactory activityLogManagerFactory;
    @Mock
    OrchestrationTemplateCandidateManagerFactory orchestrationTemplateCandidateManagerFactory;

    private OrchestrationTemplateCandidateImpl orchestrationTemplateCandidate;

    @Before
    public void setUp(){
        try {
            initMocks(this);
            packageArchive = mock(PackageArchive.class);
            mockStatic(VspManagerFactory.class);
            when(VspManagerFactory.getInstance()).thenReturn(vspManagerFactory);
            when(vspManagerFactory.createInterface()).thenReturn(vendorSoftwareProductManager);
            mockStatic(ActivityLogManagerFactory.class);
            when(ActivityLogManagerFactory.getInstance()).thenReturn(activityLogManagerFactory);
            when(activityLogManagerFactory.createInterface()).thenReturn(activityLogManager);
            whenNew(PackageArchive.class).withAnyArguments().thenReturn(packageArchive);
            mockStatic(OrchestrationTemplateCandidateManagerFactory.class);
            when(OrchestrationTemplateCandidateManagerFactory.getInstance()).thenReturn(orchestrationTemplateCandidateManagerFactory);
            when(orchestrationTemplateCandidateManagerFactory.createInterface()).thenReturn(candidateManager);
            when(packageArchive.getArchiveFileName()).thenReturn(Optional.of("test"));
            when(packageArchive.getPackageFileContents()).thenReturn(new byte[0]);
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setOnboardingType(OnboardingTypesEnum.ZIP);
            uploadFileResponse.setNetworkPackageName("test");
            when(candidateManager.upload(any(), any(), any(), any(), any())).thenReturn(uploadFileResponse);
        }catch (Exception e){
           logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void uploadSignedTest() throws SecurityManagerException {
        when(packageArchive.isSigned()).thenReturn(true);
        when(packageArchive.isSignatureValid()).thenReturn(true);
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Attachment attachment = mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        Response response = orchestrationTemplateCandidate.upload("1", "1", attachment, "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void uploadNotSignedTest(){
        when(packageArchive.isSigned()).thenReturn(false);
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Attachment attachment = mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        Response response = orchestrationTemplateCandidate.upload("1", "1", attachment, "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void uploadSignNotValidTest() throws SecurityManagerException {
        when(packageArchive.isSigned()).thenReturn(true);
        when(packageArchive.isSignatureValid()).thenReturn(false);
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Attachment attachment = mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        Response response = orchestrationTemplateCandidate.upload("1", "1", attachment, "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertFalse(((UploadFileResponseDto)response.getEntity()).getErrors().isEmpty());

    }
}
