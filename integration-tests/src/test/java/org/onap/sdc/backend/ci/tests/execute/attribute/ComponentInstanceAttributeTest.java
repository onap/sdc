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

package org.onap.sdc.backend.ci.tests.execute.attribute;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.swallowException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.function.Function;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.Resource;
import org.testng.annotations.Test;

public class ComponentInstanceAttributeTest extends ComponentBaseTest {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Rule
	public static TestName name = new TestName();

	@Test
	public void testUpdateAttributeOnResourceInstance() {
		// Prepare VF with vfc instance with Attributes
		String testResourcesPath = config.getResourceConfigDir() + File.separator + "importToscaResourceByCreateUrl";
		final Resource vfcWithAttributes = new AtomicOperationUtils()
			.importResource(testResourcesPath, "CPWithAttributes.yml").left().value();
		swallowException(() -> new AtomicOperationUtils().changeComponentState(vfcWithAttributes, UserRoleEnum.DESIGNER,
			LifeCycleStatesEnum.CHECKIN, false));
		Resource vf = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, false)
			.left().value();
		ComponentInstance vfcInstance = new AtomicOperationUtils()
			.addComponentInstanceToComponentContainer(vfcWithAttributes, vf).left().value();

		// util method to get the specific attribute from the vf
		Function<Resource, ComponentInstanceAttribute> attributeGetter = resourceVf -> resourceVf
			.getComponentInstancesAttributes().values().iterator().next().stream()
			.filter(att -> att.getName().equals("private_address")).findAny().get();
		// update attribute on vfc instance
		final Resource vfWithInsatncePreUpdate = swallowException(
			() -> (Resource) new AtomicOperationUtils().getComponentObject(vf, UserRoleEnum.DESIGNER));
		ComponentInstanceAttribute attributeOfRI = attributeGetter.apply(vfWithInsatncePreUpdate);
		final String newAttValue = "NewValue";
		attributeOfRI.set_default(newAttValue);
		String body = gson.toJson(attributeOfRI);
		String url = String.format(Urls.UPDATE_ATTRIBUTE_ON_RESOURCE_INSTANCE, config.getCatalogBeHost(),
			config.getCatalogBePort(), ComponentTypeEnum.findParamByType(ComponentTypeEnum.RESOURCE),
			vf.getUniqueId(), vfcInstance.getUniqueId());
		swallowException(() -> BaseRestUtils.sendPost(url, body, UserRoleEnum.DESIGNER.getUserId(),
			BaseRestUtils.acceptHeaderData));
		// Retrieve updated vf and verify attribute was updated
		final Resource vfWithInsatncePostUpdate = swallowException(
			() -> (Resource) new AtomicOperationUtils().getComponentObject(vf, UserRoleEnum.DESIGNER));
		ComponentInstanceAttribute updatedAttribute = attributeGetter.apply(vfWithInsatncePostUpdate);
		assertEquals(updatedAttribute.get_default(), newAttValue);

	}
}
