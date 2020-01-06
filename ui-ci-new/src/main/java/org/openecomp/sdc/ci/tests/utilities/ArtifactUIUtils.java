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

package org.openecomp.sdc.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;

public final class ArtifactUIUtils {

    private static final String DATA_TESTS_ID = "//*[@data-tests-id='";

    private ArtifactUIUtils() {
    }

    public static void validateArtifactNameVersionType(String artifactLabel, String artifactVersion, String artifactType) {
        if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactLabel)) {
            SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact label not equal - this warning represent defect.");
        }
        if (artifactVersion != null) {
            if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.VERSION.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactVersion)) {
                SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact version not equal - this warning represent defect.");
            }
        }
        if (artifactType != null) {
            if (!GeneralUIUtils.getDriver().findElement(By.xpath(DATA_TESTS_ID + DataTestIdEnum.ArtifactPageEnum.TYPE.getValue() + artifactLabel + "']")).getAttribute("textContent").trim().equals(artifactType)) {
                SetupCDTest.getExtendTest().log(Status.WARNING, "Artifact type not equal - this warning represent defect.");
            }
        }
    }

}
