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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaDefinition;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.ToscaParserUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.pages.ResourceGeneralPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class Annotation extends SetupCDTest {
    private String filePath;

    @BeforeMethod
    public void beforeTest() {
        filePath = FileHandling.getFilePath("SRIOV");
    }

    @Test
    public void importCsarWithAnnotationVerifyDownloadYmlContainsAnnotationSection() throws Exception {
        String fileName = "SIROV_annotations_VSP.csar";
        ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF,
                NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
        ResourceUIUtils.importVfFromCsar(vfMetaData, filePath, fileName, getUser());
        getExtendTest().log(Status.INFO, "Csar with annotations imported successfully.");
        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ToscaArtifactsScreenEnum.TOSCA_MODEL.getValue());
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
        ToscaDefinition toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(latestFilefromDir);
        assertTrueAnnotationTestSuite(toscaMainVfDefinition);
        getExtendTest().log(Status.INFO, "Success to validate the ToscaMainYaml contains annotation type source with properties.");
    }


    public void assertTrueAnnotationTestSuite(ToscaDefinition toscaMainVfDefinition) {
        assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").annotations).containsKey("source");
        assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").getAnnotations().get("source").getType()).isEqualTo("org.openecomp.annotations.Source");
        assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").getAnnotations().get("source").getProperties().get("source_type")).isEqualTo("HEAT");
    }

}
