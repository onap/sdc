package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.OrchestrationTemplateActionResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.data.PackageArchive;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VspManagerFactory.class, ActivityLogManagerFactory.class,
        OrchestrationTemplateCandidateManagerFactory.class, OrchestrationTemplateCandidateImpl.class})
public class OrchestrationTemplateCandidateImplTest {

    private Logger logger = LoggerFactory.getLogger(OrchestrationTemplateCandidateImplTest.class);

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

    private final String candidateId = UUID.randomUUID().toString();
    private final String softwareProductId = UUID.randomUUID().toString();
    private final String versionId = UUID.randomUUID().toString();

    private final String user = "cs0008";

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


            // get using the candidate manager.
            Optional<Pair<String,byte[]>> zipFile =
                    Optional.of(Pair.of("Hello", "World".getBytes()));

            when(candidateManager.get(
                    ArgumentMatchers.eq(candidateId),
                    ArgumentMatchers.any())).thenReturn(zipFile);

            when(vendorSoftwareProductManager.get(
                    ArgumentMatchers.eq(softwareProductId),
                    ArgumentMatchers.any())).thenReturn(zipFile);


            OrchestrationTemplateActionResponse processResponse =
                    new OrchestrationTemplateActionResponse();
            processResponse.setStatus(UploadFileStatus.Success);
            when(candidateManager.process(
                    ArgumentMatchers.eq(candidateId),
                    ArgumentMatchers.any())).thenReturn(processResponse);


            ValidationResponse vr = new ValidationResponse();
            when(candidateManager.updateFilesDataStructure(
                    ArgumentMatchers.eq(candidateId),
                    ArgumentMatchers.any(),
                    ArgumentMatchers.any())).thenReturn(vr);

            FilesDataStructure fds = new FilesDataStructure();
            fds.setArtifacts(Arrays.asList("a","b"));
            fds.setNested(Arrays.asList("foo", "bar"));
            fds.setUnassigned(Arrays.asList("c", "d"));
            fds.setModules(Arrays.asList(new Module(), new Module()));

            when(candidateManager.getFilesDataStructure(
                    ArgumentMatchers.eq(candidateId),
                    ArgumentMatchers.any())).thenReturn(Optional.of(fds));


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

    @Test
    public void testCandidateGet() throws IOException {
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Response rsp = orchestrationTemplateCandidate.get(candidateId, versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Candidate"),-1);
        byte[] content = (byte[])rsp.getEntity();
        Assert.assertEquals("World", new String(content));

    }

    @Test
    public void testVendorSoftwareProductGet() throws IOException {
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Response rsp = orchestrationTemplateCandidate.get(softwareProductId, versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Processed"),-1);
        byte[] content = (byte[])rsp.getEntity();
        Assert.assertEquals("World", new String(content));
    }

    @Test
    public void testMissingGet() throws IOException {
        orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
        Response rsp = orchestrationTemplateCandidate.get(UUID.randomUUID().toString(), versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.NOT_FOUND.getStatusCode(), rsp.getStatus());
    }

    @Test
    public void testAbort() {
        try {
            orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
            Response rsp = orchestrationTemplateCandidate.abort(candidateId, versionId);
            Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
            Assert.assertNull(rsp.getEntity());
        }
        catch (Exception ex) {
            logger.error("unexpected exception", ex);
            Assert.fail("abort should not throw an exception");
        }
    }

    @Test
    public void testProcess() {
        try {
            orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
            Response rsp = orchestrationTemplateCandidate.process(candidateId, versionId, user);
            Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
            Assert.assertNotNull(rsp.getEntity());
            OrchestrationTemplateActionResponseDto dto = (OrchestrationTemplateActionResponseDto)rsp.getEntity();
            Assert.assertEquals("status check", UploadFileStatus.Success, dto.getStatus());
        }
        catch (Exception ex) {
            logger.error("unexpected exception", ex);
            Assert.fail("abort should not throw an exception");
        }
    }

    @Test
    public void testFilesDataStructureUpload() {
        try {
            FileDataStructureDto dto = new FileDataStructureDto();
            dto.setArtifacts(Arrays.asList("a", "b", "c"));
            orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
            Response rsp = orchestrationTemplateCandidate.updateFilesDataStructure(candidateId, versionId, dto, user);
            Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        }
        catch (Exception ex) {
            logger.error("unexpected exception", ex);
            Assert.fail("abort should not throw an exception");
        }
    }

    @Test
    public void testFilesDataStructureGet() {
        try {
            FileDataStructureDto dto = new FileDataStructureDto();
            dto.setArtifacts(Arrays.asList("a", "b", "c"));
            orchestrationTemplateCandidate = new OrchestrationTemplateCandidateImpl();
            Response rsp = orchestrationTemplateCandidate.getFilesDataStructure(candidateId, versionId, user);
            Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        }
        catch (Exception ex) {
            logger.error("unexpected exception", ex);
            Assert.fail("abort should not throw an exception");
        }
    }

}
