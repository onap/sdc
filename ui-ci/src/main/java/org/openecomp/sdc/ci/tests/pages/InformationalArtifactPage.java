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

package org.openecomp.sdc.ci.tests.pages;

import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class InformationalArtifactPage extends DeploymentArtifactPage {

	public InformationalArtifactPage() {
		super();
	}

	public static void clickAddNewArtifact() {
		addNewArtifact(ArtifactGroupTypeEnum.INFORMATIONAL);
	}

	public static String getArtifactDescription(String artifactLabel) throws Exception {
		InformationalArtifactPage.clickOnArtifact(artifactLabel);
		String artifactDesc = GeneralUIUtils.getWebElementByTestID(
				artifactLabel + DataTestIdEnum.ArtifactPageEnum.GET_INFORMATIONAL_ARTIFACT_DESCRIPTION.getValue())
				.getText();
		InformationalArtifactPage.clickOnArtifact(artifactLabel); // close artifact
		return artifactDesc;
	}

	public static List<WebElement> getElemenetsFromTable() {
		return GeneralUIUtils.getWebElementsListByTestID("InformationalArtifactRow");
	}
	
	public static WebElement clickOnArtifact(String artifactLabel) throws Exception {
		try{
			WebElement artifact = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue() + artifactLabel);
			artifact.click();
			GeneralUIUtils.waitForLoader();
			return artifact;
		}
		catch(Exception e){
			throw new Exception("Artifact " + artifactLabel + "is not found");
		}
	}
	
	
	public static void clickDeleteArtifact(String artifactLabel) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Deleting %s Artefact ",artifactLabel));
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.DELETE_ARTIFACT.getValue() + artifactLabel).click();
	}

}
