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

package org.onap.sdc.backend.ci.tests.execute.product;


import org.junit.Before;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.*;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.utils.ArtifactUtils;
import org.onap.sdc.backend.ci.tests.utils.DbUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class ProductTestBase extends ProductBaseTest {

	public ProductTestBase(TestName testName, String className) {
		super();
	}

	protected ResourceReqDetails downloadResourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected User sdncUserDetails;
	protected ArtifactReqDetails heatArtifactDetails;
	protected ArtifactReqDetails defaultArtifactDetails;

	protected ArtifactUtils artifactUtils;
	protected Resource resource;
	protected Service service;
	protected Product product;

	// protected static ServiceUtils serviceUtils = new ServiceUtils();

	@Before
	public void before() throws Exception {

		initializeMembers();
		createComponents();

	}

	public void initializeMembers() throws IOException, Exception {

		downloadResourceDetails = ElementFactory.getDefaultResource();
		serviceDetails = ElementFactory.getDefaultService();
		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance();

	}

	protected void createComponents() throws Exception {

		RestResponse response = ResourceRestUtils.createResource(downloadResourceDetails, sdncUserDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", downloadResourceDetails.getUniqueId());

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncUserDetails,
				downloadResourceDetails.getUniqueId());
		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		// certified resource
		response = LifecycleRestUtils.certifyResource(downloadResourceDetails);
		assertTrue("certify resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = ResourceRestUtils.getResource(downloadResourceDetails.getUniqueId());
		assertTrue("response code is not 200, returned: " + response.getErrorCode(), response.getErrorCode() == 200);
		resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());

		response = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

		// add resource instance with HEAT deployment artifact to the service
		resourceInstanceReqDetails.setComponentUid(downloadResourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, sdncUserDetails,
				serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);

		// certified service
		response = LifecycleRestUtils.certifyService(serviceDetails);
		assertTrue("certify service request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = ServiceRestUtils.getService(serviceDetails, sdncUserDetails);
		assertTrue("response code is not 200, returned: " + response.getErrorCode(), response.getErrorCode() == 200);
		service = ResponseParser.convertServiceResponseToJavaObject(response.getResponse());

		DbUtils.cleanAllAudits();

		ProductReqDetails defaultProduct = ElementFactory.getDefaultProduct(defaultCategories);
		RestResponse createProduct = ProductRestUtils.createProduct(defaultProduct, productStrategistUser1);
		assertEquals("Check response code after create Product", BaseRestUtils.STATUS_CODE_CREATED,
				createProduct.getErrorCode().intValue());
		product = ResponseParser.parseToObjectUsingMapper(createProduct.getResponse(), Product.class);

		// add service instance to product
		//

	}
}
