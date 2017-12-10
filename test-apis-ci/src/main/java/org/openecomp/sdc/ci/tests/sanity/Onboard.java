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

package org.openecomp.sdc.ci.tests.sanity;


import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.ExtentTestActions;
import org.openecomp.sdc.ci.tests.dataProviders.OnbordingDataProviders;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtillViaApis;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;

import fj.data.Either;


public class Onboard extends ComponentBaseTest {
	
	
	@Rule
	public static TestName name = new TestName();
	
	public Onboard() {
		super(name, Onboard.class.getName());
	}

	protected String makeDistributionValue;
	protected String makeToscaValidationValue;


	@Parameters({ "makeDistribution" })
	@BeforeMethod
	public void beforeTestReadParams(@Optional("true") String makeDistributionReadValue) {
		makeDistributionValue = makeDistributionReadValue;                             
		logger.info("makeDistributionReadValue - > " + makeDistributionValue);
	}

	@Parameters({ "makeToscaValidation" })
	@BeforeClass
	public void makeToscaValidation(@Optional("false") String makeToscaValidationReadValue) {
		makeToscaValidationValue = makeToscaValidationReadValue;
		logger.info("makeToscaValidationReadValue - > " + makeToscaValidationValue);
	}
	

	@Test(dataProviderClass = OnbordingDataProviders.class, dataProvider = "VNF_List")
	public void onboardVNFShotFlow(String filePath, String vnfFile) throws Exception, Throwable {
		setLog(vnfFile);
		System.out.println("print - >" + makeDistributionValue);
		runOnboardToDistributionFlow(filePath, vnfFile);
	}
	

	

	
	public void runOnboardToDistributionFlow(String filePath, String vnfFile) throws Exception {

		ExtentTestActions.log(Status.INFO, String.format("Going to onboard the VNF %s", vnfFile));
		User user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER3);
     	ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
		Pair<String, VendorSoftwareProductObject> createVendorSoftwareProduct = OnboardingUtillViaApis.createVspViaApis(resourceReqDetails, filePath, vnfFile, user);
		VendorSoftwareProductObject vendorSoftwareProductObject = createVendorSoftwareProduct.right;
		vendorSoftwareProductObject.setName(createVendorSoftwareProduct.left);

		//		create VF base on VNF imported from previous step - have, resourceReqDetails object include part of resource metadata
//		ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();
		resourceReqDetails = OnboardingUtillViaApis.prepareOnboardedResourceDetailsBeforeCreate(resourceReqDetails, vendorSoftwareProductObject);
		ExtentTestActions.log(Status.INFO, String.format("Create VF %s From VSP", resourceReqDetails.getName()));
		Resource resource = OnboardingUtillViaApis.createResourceFromVSP(resourceReqDetails, UserRoleEnum.DESIGNER3);
		ExtentTestActions.log(Status.INFO, String.format("Certify VF"));
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER3, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		//--------------------------SERVICE--------------------------------	
		ServiceReqDetails serviceReqDetails = OnboardingUtillViaApis.prepareServiceDetailsBeforeCreate(user);
		ExtentTestActions.log(Status.INFO, String.format("Create Service %s", serviceReqDetails.getName()));
		Service service = AtomicOperationUtils.createCustomService(serviceReqDetails, UserRoleEnum.DESIGNER3, true).left().value();
		ExtentTestActions.log(Status.INFO, String.format("add VF to Service"));
		Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer = AtomicOperationUtils.addComponentInstanceToComponentContainer(resource, service, UserRoleEnum.DESIGNER3, true);
		addComponentInstanceToComponentContainer.left().value();
		ExtentTestActions.log(Status.INFO, String.format("Certify Service"));
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER3, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		if (makeDistributionValue.equals("true")) {
			ExtentTestActions.log(Status.INFO, String.format("Distribute Service"));
			AtomicOperationUtils.distributeService(service, true);
		}

		if (makeToscaValidationValue.equals("true")) {

			ExtentTestActions.log(Status.INFO, String.format("Start tosca validation"));
			AtomicOperationUtils.toscaValidation(service ,vnfFile);


		}



		
		ExtentTestActions.log(Status.INFO, String.format("The onboarding %s test is passed ! ", vnfFile));
	}



}
