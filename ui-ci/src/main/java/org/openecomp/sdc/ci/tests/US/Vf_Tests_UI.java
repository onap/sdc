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

package org.openecomp.sdc.ci.tests.US;

import java.awt.AWTException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ArtifactPageEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.sanity.Onboard;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Vf_Tests_UI extends SetupCDTest{

	public Vf_Tests_UI() {
		// TODO Auto-generated constructor stub
	}
	public void uploadHeatEnvVFLevel() throws Exception {
		
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ResourceUIUtils.createResource(vfMetaData, getUser());
		
	}
	
//	@Test
//	// Download ENV file from VF level.
//	public void downloadEnvVFLevel() throws AWTException, Exception {
//		String firstEnvArtifact = "base_stsi_dnt_frwl.env";
//		String secondEnvArtifact = "mod_vmsi_dnt_fw_parent.env";
//		String filePath=Config.instance().getWindowsDownloadDirectory();
//		String vnfFile = "FDNT.zip";
//	    OnboardingUtils.onboardAndValidate(Onboard.getFilePath(), vnfFile, getUser());
//		Map<String,File> mD5OfFilesToValidate = new HashMap<String,File>();
//		mD5OfFilesToValidate.put(firstEnvArtifact,new File(FileHandling.getResourcesEnvFilesPath() + firstEnvArtifact));
//		mD5OfFilesToValidate.put(secondEnvArtifact,new File(FileHandling.getResourcesEnvFilesPath() + secondEnvArtifact));
//		List<File>filesToBeDeleted=new ArrayList<>(mD5OfFilesToValidate.values());
//		FileHandling.deleteLastDowloadedFiles(filesToBeDeleted);
//		ResourceGeneralPage.getLeftMenu().moveToDeploymentArtifactScreen();
//		List<WebElement> allDisplayedArtifcats = DeploymentArtifactPage.getDeploymentArtifactsNamesWorkSpace();
//		for (int i=0;i<allDisplayedArtifcats.size();i++) {
//			if (DeploymentArtifactPage.geteArtifactType(allDisplayedArtifcats.get(i).getText()).equalsIgnoreCase("heat")) {
//				DeploymentArtifactPage.clickDownloadEnvArtifact(allDisplayedArtifcats.get(i));
//				GeneralUIUtils.waitForLoader();
//				File latestFilefromDir = FileHandling.getRequiredFromDir(filePath,allDisplayedArtifcats.get(i).getText()+".env");
//				VfVerificator.verifyFilesChecksum(latestFilefromDir,mD5OfFilesToValidate.get(latestFilefromDir.getName()));
//
//			}
//		}
//	}
	@Override
	protected UserRoleEnum getRole() {
		// TODO Auto-generated method stub
		return UserRoleEnum.DESIGNER;
	}
}
