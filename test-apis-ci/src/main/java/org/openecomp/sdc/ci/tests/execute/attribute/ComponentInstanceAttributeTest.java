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

package org.openecomp.sdc.ci.tests.execute.attribute;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.swallowException;

import java.io.File;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ComponentInstanceAttributeTest extends ComponentBaseTest {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Rule
	public static TestName name = new TestName();

	public ComponentInstanceAttributeTest() {
		super(name, ComponentInstanceAttributeTest.class.getName());
	}

	@Test
	public void testUpdateAttributeOnResourceInstance() {
		// Prepare VF with vfc instance with Attributes
		String testResourcesPath = config.getResourceConfigDir() + File.separator + "importToscaResourceByCreateUrl";
		final Resource vfcWithAttributes = AtomicOperationUtils
				.importResource(testResourcesPath, "CPWithAttributes.yml").left().value();
		swallowException(() -> AtomicOperationUtils.changeComponentState(vfcWithAttributes, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CHECKIN, false));
		Resource vf = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, false)
				.left().value();
		ComponentInstance vfcInstance = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(vfcWithAttributes, vf).left().value();

		// util method to get the specific attribute from the vf
		Function<Resource, ComponentInstanceProperty> attributeGetter = resourceVf -> resourceVf
				.getComponentInstancesAttributes().values().iterator().next().stream()
				.filter(att -> att.getName().equals("private_address")).findAny().get();
		// update attribute on vfc instance
		final Resource vfWithInsatncePreUpdate = swallowException(
				() -> (Resource) AtomicOperationUtils.getComponentObject(vf, UserRoleEnum.DESIGNER));
		ComponentInstanceProperty attributeOfRI = attributeGetter.apply(vfWithInsatncePreUpdate);
		final String newAttValue = "NewValue";
		attributeOfRI.setValue(newAttValue);
		String body = gson.toJson(attributeOfRI);
		String url = String.format(Urls.UPDATE_ATTRIBUTE_ON_RESOURCE_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(ComponentTypeEnum.RESOURCE),
				vf.getUniqueId(), vfcInstance.getUniqueId());
		swallowException(() -> BaseRestUtils.sendPost(url, body, UserRoleEnum.DESIGNER.getUserId(),
				BaseRestUtils.acceptHeaderData));
		// Retrieve updated vf and verify attribute was updated
		final Resource vfWithInsatncePostUpdate = swallowException(
				() -> (Resource) AtomicOperationUtils.getComponentObject(vf, UserRoleEnum.DESIGNER));
		ComponentInstanceProperty updatedAttribute = attributeGetter.apply(vfWithInsatncePostUpdate);
		assertEquals(updatedAttribute.getValue(), newAttValue);

	}
}
