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

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ImportAssetUIUtils {
	
	public static final String FILE_PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\Files\\";
	public static String fileName = "JDM_vfc.yml";
	public static final String toscaErrorMessage = "Invalid TOSCA template.";
	public static final String yamlError = "Invalid YAML file.";
	public static final String allReadyExistErro = "Imported resource already exists in ASDC Catalog.";

	public static void importAsssetAndFillGeneralInfo(String FILE_PATH, String fileName,
			ResourceReqDetails resourceDetails, User user, CreateAndImportButtonsEnum type) throws Exception {
		ResourceUIUtils.importFileWithSendKey(FILE_PATH, fileName, type);
		ResourceUIUtils.fillResourceGeneralInformationPage(resourceDetails, user,true);
	}

	public static void importAsssetFillGeneralInfoAndSelectIcon(String FILE_PATH, String fileName,
			ResourceReqDetails resourceDetails, User user, CreateAndImportButtonsEnum type) throws Exception {
		importAsssetAndFillGeneralInfo(FILE_PATH, fileName, resourceDetails, user, type);
		GeneralPageElements.clickCreateButton();
		ResourceUIUtils.selectRandomResourceIcon();
	}

	// checking or unchecking the checkbox on right palette at designer
	// workspace
	public static void checkbox(String checkBoxname, WebDriver driver) {
		driver.findElement(By.xpath("//label[@for='" + checkBoxname + "']")).click();
	}

}
