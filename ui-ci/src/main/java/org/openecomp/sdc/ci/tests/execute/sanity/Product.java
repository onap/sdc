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

package org.openecomp.sdc.ci.tests.execute.sanity;

import java.io.File;

import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.CompositionPage;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ProductGeneralPage;
import org.openecomp.sdc.ci.tests.pages.TesterOperationPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ProductUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author al714h
 *
 */

public class Product extends SetupCDTest {
	
	private String filePath;
	@BeforeMethod
	public void beforeTest(){
		filePath = System.getProperty("filepath");
	
		if (filePath == null && System.getProperty("os.name").contains("Windows")) {
			filePath = FileHandling.getResourcesFilesPath();
		}
		
		else if(filePath.isEmpty() && !System.getProperty("os.name").contains("Windows")){
			filePath = FileHandling.getBasePath() + File.separator + "Files" + File.separator;
		}
	}

	
	@Test
	public void createProductAndAddCertifiedServiceInstance() throws Exception {		
		ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
		ProductReqDetails productReqDetails = ElementFactory.getDefaultProduct();
		
		ServiceUIUtils.createService(serviceMetadata, getUser());
		GeneralPageElements.clickSubmitForTestingButton(serviceMetadata.getName());
		reloginWithNewRole(UserRoleEnum.TESTER);
		GeneralUIUtils.findComponentAndClick(serviceMetadata.getName());
		TesterOperationPage.certifyComponent(serviceMetadata.getName());
		reloginWithNewRole(UserRoleEnum.PRODUCT_MANAGER1);
		ProductUIUtils.createProduct(productReqDetails, getUser());
		ProductGeneralPage.getProductLeftMenu().moveToCompositionScreen();
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement canvasElement = CompositionPage.addElementToCanvasScreen(serviceMetadata.getName(), canvasManager);
		canvasManager.clickOnCanvaElement(canvasElement);
	}
	
	@Test
	public void loginAsProductStrateger() throws Exception {		
		reloginWithNewRole(UserRoleEnum.PRODUCT_STRATEGIST1);
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
