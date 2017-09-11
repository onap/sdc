/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.OrchestrationTemplateCandidateManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.ManifestCreatorNamingConventionImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class UploadCSARFileTest {
    private static final String USER1 = "vspTestUser1";

    public static final Version VERSION01 = new Version(0, 1);

    @Mock
    private VendorSoftwareProductDao vendorSoftwareProductDaoMock;
    @Mock
    private OrchestrationTemplateDao orchestrationTemplateDataDaoMock;

    @Spy
    private CandidateServiceImpl candidateService;
    @Mock
    private HealingManager healingManagerMock;
    @Mock
    private CompositionDataExtractor compositionDataExtractorMock;
    @Mock
    private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDaoMock;
    @Mock
    private CompositionEntityDataManager compositionEntityDataManagerMock;
    @Mock
    private VendorSoftwareProductInfoDao vspInfoDaoMock;
    @Mock
    private OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDao;
    @Mock
    private ManifestCreatorNamingConventionImpl manifestCreator;

    private OrchestrationTemplateCandidateManagerImpl candidateManager;


    public static String id001 = null;

    public static Version activeVersion002 = null;


    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        candidateService = new CandidateServiceImpl(manifestCreator,orchestrationTemplateCandidateDao);
        candidateManager = new OrchestrationTemplateCandidateManagerImpl( vendorSoftwareProductDaoMock,
                vspInfoDaoMock,
                orchestrationTemplateDataDaoMock,
                candidateService,  healingManagerMock,
                compositionDataExtractorMock,
                serviceModelDaoMock,
                compositionEntityDataManagerMock,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    public void testSuccessfulUploadFile() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = getClass().getResourceAsStream("/vspmanager.csar/SDCmock.csar")) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1, "csar", "SDCmock");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(0, uploadFileResponse.getErrors().size());
            assertTrue(uploadFileResponse.getErrors().isEmpty());
        }
    }

    @Test
    public void testFail1UploadFile() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = getClass().getResourceAsStream("/vspmanager.csar/SDCmockFail1.csar")) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1,
                    "csar", "SDCmockFail1");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(1, uploadFileResponse.getErrors().size());
            assertTrue( uploadFileResponse.getErrors().values().stream()
                    .filter(getListPredicate(Messages.CSAR_FILES_NOT_ALLOWED
                            .getErrorMessage().substring(0, 7))).findAny().isPresent());
        }
    }

    private Predicate<List<ErrorMessage>> getListPredicate(String substring) {
        return error -> isEquals(substring, error);
    }

    private boolean isEquals(String substring, List<ErrorMessage> error) {
        return error.iterator().next().getMessage().contains(substring);
    }

    @Test
    public void testFail2UploadFile() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = getClass().getResourceAsStream("/vspmanager.csar/SDCmockFail2.csar")) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1,
                    "csar", "SDCmockFail2");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(1, uploadFileResponse.getErrors().size());
            assertTrue( uploadFileResponse.getErrors().values().stream()
                    .filter(getListPredicate(Messages.CSAR_FILE_NOT_FOUND
                            .getErrorMessage().substring(0,7))).findAny().isPresent());
        }
    }
    @Test
    public void testFail3UploadFile() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = getClass().getResourceAsStream("/vspmanager.csar/SDCmockFail3.csar")) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1,
                    "csar", "SDCmockFail3");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(1, uploadFileResponse.getErrors().size());
        }
    }

    @Test
    public void testUploadFileIsNotZip() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = new ByteArrayInputStream( "Thia is not a zip file".getBytes() );) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1,
                    "csar", "file");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertFalse(uploadFileResponse.getErrors().isEmpty());
            assertTrue( uploadFileResponse.getErrors().values().stream()
                    .filter(getListPredicate(Messages.CSAR_FILE_NOT_FOUND
                            .getErrorMessage().substring(0,7))).findAny().isPresent());
        }
    }
    @Test
    public void testUploadFileIsEmpty() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = new ByteArrayInputStream( new byte[]{} )) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001,
                activeVersion002, is, USER1, "csar", "file");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(1, uploadFileResponse.getErrors().size());
        }
    }

    @Test
    public void testMFError() throws Exception {
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        doReturn(vspDetails).when(vspInfoDaoMock).get(any(VspDetails.class));

        try (InputStream is = getClass().getResourceAsStream("/vspmanager.csar/SDCmockBrokenMF.csar")) {
            UploadFileResponse uploadFileResponse = candidateManager.upload(id001, activeVersion002, is, USER1, "csar", "SDCmockBrokenMF");
            assertEquals(uploadFileResponse.getOnboardingType(), OnboardingTypesEnum.CSAR);
            assertEquals(1, uploadFileResponse.getErrors().size());
            assertTrue( uploadFileResponse.getErrors()
                    .values().stream()
                    .filter(getListPredicate(Messages.MANIFEST_NO_METADATA.getErrorMessage())).findAny().isPresent());

        }
    }


}
