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

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.AssocType;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.testng.annotations.Test;

public class MultipleResourceUpdate extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public MultipleResourceUpdate() {
		super(name, MultipleResourceUpdate.class.getName());
	}

	@Test
	public void simpleScenario() throws Exception {

		// Creating VF and Resource instances
		Resource vf = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		Resource cp1 = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.PORT,
						ResourceCategoryEnum.NETWORK_CONNECTIVITY_CON_POINT, UserRoleEnum.DESIGNER, true)
				.left().value();
		Resource cp2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.CP, UserRoleEnum.DESIGNER, true)
				.left().value();
		Resource vl = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VL, NormativeTypesEnum.NETWORK,
						ResourceCategoryEnum.NETWORK_CONNECTIVITY_VIRTUAL_LINK, UserRoleEnum.DESIGNER, true)
				.left().value();

		vf.getCreatorUserId();

		// Check In Resources
		AtomicOperationUtils.changeComponentState(cp1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		AtomicOperationUtils.changeComponentState(cp2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		AtomicOperationUtils.changeComponentState(vl, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);

		// CheckIn all other except VF
		ComponentInstance instanceCP1 = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(cp1, vf, UserRoleEnum.DESIGNER, true).left().value();
		ComponentInstance instanceVL = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(vl, vf, UserRoleEnum.DESIGNER, true).left().value();
		ComponentInstance instanceCP2 = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(cp2, vf, UserRoleEnum.DESIGNER, true).left().value();

		vf = (Resource) AtomicOperationUtils.getCompoenntObject(vf, UserRoleEnum.DESIGNER);

		// Create Vertex(Link/Associate 2 Resource Instances on Canvas)
		AtomicOperationUtils.associate2ResourceInstances(vf, instanceCP1, instanceVL, AssocType.LINKABLE.getAssocType(),
				UserRoleEnum.DESIGNER, true);

		List<ComponentInstanceReqDetails> componentInstanceReqDetailsList = new ArrayList<>();
		componentInstanceReqDetailsList.add(new ComponentInstanceReqDetails(instanceCP1));
		componentInstanceReqDetailsList.add(new ComponentInstanceReqDetails(instanceCP2));
		componentInstanceReqDetailsList.add(new ComponentInstanceReqDetails(instanceVL));

		ComponentInstanceReqDetails compInstDet = componentInstanceReqDetailsList.get(0);
		compInstDet.setPosX("150");
		compInstDet.setPosY("150");
		compInstDet = componentInstanceReqDetailsList.get(1);
		compInstDet.setPosX("400");
		compInstDet.setPosY("150");
		compInstDet = componentInstanceReqDetailsList.get(2);
		compInstDet.setPosX("150");
		compInstDet.setPosY("300");

		RestResponse response = ComponentInstanceRestUtils.updateMultipleComponentInstance(
				componentInstanceReqDetailsList, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), vf.getUniqueId(),
				vf.getComponentType());
		assertTrue("response code is not 200, returned: " + response.getErrorCode(),
				response.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);

		compInstDet = componentInstanceReqDetailsList.get(0);
		compInstDet.setPosX("350");
		compInstDet.setPosY("350");
		compInstDet = componentInstanceReqDetailsList.get(1);
		compInstDet.setPosX("600");
		compInstDet.setPosY("350");
		compInstDet = componentInstanceReqDetailsList.get(2);
		compInstDet.setPosX("350");
		compInstDet.setPosY("500");

		response = ComponentInstanceRestUtils.updateMultipleComponentInstance(componentInstanceReqDetailsList,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), vf.getUniqueId(), vf.getComponentType());
		assertTrue("response code is not 200, returned: " + response.getErrorCode(),
				response.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);
	}
}
