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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.be.csar.storage.ArtifactStorageManager;
import org.openecomp.sdc.be.csar.storage.PackageSizeReducer;
import org.openecomp.sdc.be.csar.storage.PersistentStorageArtifactInfo;
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

class OrchestrationTemplateCandidateImplTest {

    private final Logger logger = LoggerFactory.getLogger(OrchestrationTemplateCandidateImplTest.class);
    private final String candidateId = UUID.randomUUID().toString();
    private final String softwareProductId = UUID.randomUUID().toString();
    private final String versionId = UUID.randomUUID().toString();
    private final String user = "cs0008";
    @Mock
    private OrchestrationTemplateCandidateManager candidateManager;
    @Mock
    private VendorSoftwareProductManager vendorSoftwareProductManager;
    @Mock
    private ActivityLogManager activityLogManager;
    @Mock
    private ArtifactStorageManager artifactStorageManager;
    @Mock
    private PackageSizeReducer packageSizeReducer;
    private OrchestrationTemplateCandidateImpl orchestrationTemplateCandidate;

    @BeforeEach
    public void setUp() {
        try {
            MockitoAnnotations.openMocks(this);
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setOnboardingType(OnboardingTypesEnum.ZIP);
            uploadFileResponse.setNetworkPackageName("test");
            when(candidateManager.upload(any(), any())).thenReturn(uploadFileResponse);

            // get using the candidate manager.
            Optional<Pair<String, byte[]>> zipFile = Optional.of(Pair.of("Hello", "World".getBytes()));

            when(candidateManager.get(
                ArgumentMatchers.eq(candidateId),
                ArgumentMatchers.any())).thenReturn(zipFile);

            when(vendorSoftwareProductManager.get(
                ArgumentMatchers.eq(softwareProductId),
                ArgumentMatchers.any())).thenReturn(zipFile);

            OrchestrationTemplateActionResponse processResponse = new OrchestrationTemplateActionResponse();
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
            fds.setArtifacts(Arrays.asList("a", "b"));
            fds.setNested(Arrays.asList("foo", "bar"));
            fds.setUnassigned(Arrays.asList("c", "d"));
            fds.setModules(Arrays.asList(new Module(), new Module()));

            when(candidateManager.getFilesDataStructure(
                ArgumentMatchers.eq(candidateId),
                ArgumentMatchers.any())).thenReturn(Optional.of(fds));

            orchestrationTemplateCandidate =
                new OrchestrationTemplateCandidateImpl(candidateManager, vendorSoftwareProductManager, activityLogManager,
                    artifactStorageManager, packageSizeReducer);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    void uploadSignedTest() {
        Response response = orchestrationTemplateCandidate
            .upload("1", "1", mockAttachment("filename.zip", this.getClass().getResource("/files/sample-signed.zip")),
                "1");
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(((UploadFileResponseDto) response.getEntity()).getErrors().isEmpty());
    }

    @Test
    void uploadNotSignedTest() throws IOException {
        Response response = orchestrationTemplateCandidate.upload("1", "1",
            mockAttachment("filename.csar", this.getClass().getResource("/files/sample-not-signed.csar")), "1");
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(((UploadFileResponseDto) response.getEntity()).getErrors().isEmpty());
    }

    @Test
    void uploadNotSignedArtifactStorageManagerIsEnabledTest() throws IOException {
        when(artifactStorageManager.isEnabled()).thenReturn(true);
        final Path path = Path.of("src/test/resources/files/sample-not-signed.csar");
        when(artifactStorageManager.upload(anyString(), anyString(), any())).thenReturn(new PersistentStorageArtifactInfo(path));
        final byte[] bytes = Files.readAllBytes(path);
        when(packageSizeReducer.reduce(any())).thenReturn(bytes);

        Response response = orchestrationTemplateCandidate.upload("1", "1",
            mockAttachment("filename.csar", this.getClass().getResource("/files/sample-not-signed.csar")), "1");
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(((UploadFileResponseDto) response.getEntity()).getErrors().isEmpty());
    }

    private Attachment mockAttachment(final String fileName, final URL fileToUpload) {
        final Attachment attachment = Mockito.mock(Attachment.class);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
        final DataHandler dataHandler = Mockito.mock(DataHandler.class);
        when(dataHandler.getName()).thenReturn(fileName);
        when(attachment.getDataHandler()).thenReturn(dataHandler);
        byte[] bytes = "upload package Test".getBytes();
        if (Objects.nonNull(fileToUpload)) {
            try {
                bytes = IOUtils.toByteArray(fileToUpload);
            } catch (final IOException e) {
                logger.error("unexpected exception", e);
                fail("Not able to convert file to byte array");
            }
        }
        when(attachment.getObject(ArgumentMatchers.any())).thenReturn(bytes);
        return attachment;
    }

    @Test
    void uploadSignNotValidTest() {
        Response response = orchestrationTemplateCandidate
            .upload("1", "1", mockAttachment("filename.zip", null), "1");
        assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        assertFalse(((UploadFileResponseDto) response.getEntity()).getErrors().isEmpty());
    }

    @Test
    void testCandidateGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(candidateId, versionId, user);
        assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
        assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Candidate"), -1);
        byte[] content = (byte[]) rsp.getEntity();
        assertEquals("World", new String(content));
    }

    @Test
    void testVendorSoftwareProductGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(softwareProductId, versionId, user);
        assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
        assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf("Processed"), -1);
        byte[] content = (byte[]) rsp.getEntity();
        assertEquals("World", new String(content));
    }

    @Test
    void testMissingGet() throws IOException {
        Response rsp = orchestrationTemplateCandidate.get(UUID.randomUUID().toString(), versionId, user);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rsp.getStatus(), "Response status equals");
    }

    @Test
    void testAbort() {
        try {
            Response rsp = orchestrationTemplateCandidate.abort(candidateId, versionId);
            assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
            assertNull(rsp.getEntity());
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("abort should not throw an exception");
        }
    }

    @Test
    void testProcess() {
        try {
            Response rsp = orchestrationTemplateCandidate.process(candidateId, versionId, user);
            assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
            assertNotNull(rsp.getEntity());
            OrchestrationTemplateActionResponseDto dto = (OrchestrationTemplateActionResponseDto) rsp.getEntity();
            assertEquals(UploadFileStatus.Success, dto.getStatus(), "status check");
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("abort should not throw an exception");
        }
    }

    @Test
    void testFilesDataStructureUpload() {
        try {
            FileDataStructureDto dto = new FileDataStructureDto();
            dto.setArtifacts(Arrays.asList("a", "b", "c"));
            Response rsp = orchestrationTemplateCandidate.updateFilesDataStructure(candidateId, versionId, dto, user);
            assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("abort should not throw an exception");
        }
    }

    @Test
    void testFilesDataStructureGet() {
        try {
            FileDataStructureDto dto = new FileDataStructureDto();
            dto.setArtifacts(Arrays.asList("a", "b", "c"));
            Response rsp = orchestrationTemplateCandidate.getFilesDataStructure(candidateId, versionId, user);
            assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus(), "Response status equals");
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("abort should not throw an exception");
        }
    }

}
