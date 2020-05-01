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

package org.openecomp.sdcrests.vsp.rest.services;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.OrchestrationTemplateActionResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;

public class OrchestrationTemplateCandidateImplTest {

    private Logger logger = LoggerFactory.getLogger(OrchestrationTemplateCandidateImplTest.class);

    @Mock
    private OrchestrationTemplateCandidateManager candidateManager;
    @Mock
    private VendorSoftwareProductManager vendorSoftwareProductManager;

    @Mock
    private ActivityLogManager activityLogManager;

    private OrchestrationTemplateCandidateImpl orchestrationTemplateCandidate;

    private final String candidateId = UUID.randomUUID().toString();
    private final String softwareProductId = UUID.randomUUID().toString();
    private final String versionId = UUID.randomUUID().toString();

    private final String user = "cs0008";

    @Before
    public void setUp(){
        try {
            initMocks(this);
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setOnboardingType(OnboardingTypesEnum.ZIP);
            uploadFileResponse.setNetworkPackageName("test");
            when(candidateManager.upload(any(), any())).thenReturn(uploadFileResponse);


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

            orchestrationTemplateCandidate =
                new OrchestrationTemplateCandidateImpl(candidateManager, vendorSoftwareProductManager, activityLogManager);


        }catch (Exception e){
           logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void uploadSignedTest() {
        Response response = orchestrationTemplateCandidate.upload("1", "1", mockAttachment("filename.zip"), "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void uploadNotSignedTest(){
        Response response = orchestrationTemplateCandidate.upload("1", "1", mockAttachment("filename.csar"), "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private Attachment mockAttachment(final String fileName) {
        final Attachment attachment = Mockito.mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        final DataHandler dataHandler = Mockito.mock(DataHandler.class);
        when(dataHandler.getName()).thenReturn(fileName);
        when(attachment.getDataHandler()).thenReturn(dataHandler);
        final byte[] bytes = "upload package Test".getBytes();
        when(attachment.getObject(ArgumentMatchers.any())).thenReturn(bytes);
        return attachment;
    }

    @Test
    public void uploadSignNotValidTest() {
        Response response = orchestrationTemplateCandidate.upload("1", "1", mockAttachment("filename.zip"), "1");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertFalse(((UploadFileResponseDto)response.getEntity()).getErrors().isEmpty());
    }

    @Test
    public void testCandidateGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(candidateId, versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Candidate"),-1);
        byte[] content = (byte[])rsp.getEntity();
        Assert.assertEquals("World", new String(content));
    }

    @Test
    public void testVendorSoftwareProductGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(softwareProductId, versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Processed"),-1);
        byte[] content = (byte[])rsp.getEntity();
        Assert.assertEquals("World", new String(content));
    }

    @Test
    public void testMissingGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(UUID.randomUUID().toString(), versionId, user);
        Assert.assertEquals("Response status equals", Response.Status.NOT_FOUND.getStatusCode(), rsp.getStatus());
    }

    @Test
    public void testAbort() {
        try {
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
            Response rsp = orchestrationTemplateCandidate.getFilesDataStructure(candidateId, versionId, user);
            Assert.assertEquals("Response status equals", Response.Status.OK.getStatusCode(), rsp.getStatus());
        }
        catch (Exception ex) {
            logger.error("unexpected exception", ex);
            Assert.fail("abort should not throw an exception");
        }
    }

}
