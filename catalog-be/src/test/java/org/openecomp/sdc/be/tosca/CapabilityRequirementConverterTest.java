/*
 * Copyright © 2016-2019 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.tosca;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class CapabilityRequirementConverterTest {

	@InjectMocks
	CapabilityRequirementConverter testSubject;

	@Mock
	ToscaOperationFacade toscaOperationFacade;
	@Mock
	private PropertyConvertor propertyConvertorMock;

    CapabilityRequirementConverter capabiltyRequirementConvertor =  Mockito.spy(new CapabilityRequirementConverter());
    ComponentInstance instanceProxy  =  Mockito.spy( new ComponentInstance() );
    ComponentInstance vfInstance  =  Mockito.spy( new ComponentInstance() );
    Component vfComponent =  Mockito.spy( new Resource() );
    ComponentInstance vfcInstance  =  Mockito.spy( new ComponentInstance() );
    Component vfcComponent  =  Mockito.spy( new Resource() );

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetInstance() {
		CapabilityRequirementConverter.getInstance();
	}

	@Test
	public void testGetReducedPathByOwner() throws Exception {
		List<String> pathList = new ArrayList<>();
		String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

		String exerpt = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
		String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
		pathList.add(exerpt);
		pathList.add(duplicate);
		pathList.add(duplicate);
		pathList.add(uniqueId);

		pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
		pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

		List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner( pathList , uniqueId );

		assertThat(reducedMap).isNotNull().doesNotContain(exerpt).containsOnlyOnce(duplicate).hasSize(4);

		List<String> path = new ArrayList<String>();

		capabiltyRequirementConvertor.getReducedPathByOwner(path, uniqueId);

		path.add("");
		capabiltyRequirementConvertor.getReducedPathByOwner(path, uniqueId);
		capabiltyRequirementConvertor.getReducedPathByOwner(path, "");
	}

	// generate stub capability
	private Map<String, List<CapabilityDefinition>> newCapabilities(String capabilityName) {
		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
		List<CapabilityDefinition> list = new ArrayList<>();
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.setName(capabilityName);
		capabilityDefinition.setType("att.Node");
		List<ComponentInstanceProperty> properties = new ArrayList<>();
		ComponentInstanceProperty prop = new ComponentInstanceProperty();
		prop.setValue("value");
		properties.add(prop);
		capabilityDefinition.setProperties(properties);
		List<String> pathList = new ArrayList<>();

		capabilityDefinition.setOwnerId("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693");
		pathList.add("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
		// pathList.add("a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi");
		pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
		pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

		capabilityDefinition.setPath(pathList);
		list.add(capabilityDefinition);
		capabilities.put(capabilityDefinition.getType(), list);

		return capabilities;
	}

	@Test
	public void testBuildName() {
		doReturn("1").when(instanceProxy).getActualComponentUid();
		doReturn("2").when(vfInstance).getActualComponentUid();
		doReturn("3").when(vfcInstance).getActualComponentUid();
		// region proxy
		Component proxyOrigin = new Resource();

		proxyOrigin.setName("vepdgtp4837svc_proxy0");
		proxyOrigin.setComponentType(ComponentTypeEnum.RESOURCE);
		proxyOrigin.setComponentInstances(asList(vfInstance));

		// endregion
		// region vf+vfc
		vfInstance.setName("vepdgtp4837vf0");
		vfInstance.setNormalizedName("vepdgtp4837vf0");
		vfInstance.setUniqueId(
				"5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
		vfComponent.setName("vepdgtp4837vf0"); // origin
		vfComponent.setComponentInstances(Arrays.asList(vfcInstance));
		vfcInstance.setUniqueId("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
		vfcInstance.setName("lb_1");
		vfcInstance.setNormalizedName("lb_1");
		vfcInstance.setName("41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1");
		vfcComponent.setName("lb_1");
		// endregion
		Map<String, List<CapabilityDefinition>> capabilities = newCapabilities("port");
		vfcComponent.setCapabilities(capabilities);
		Map<Component, ComponentInstance> map = Collections
				.unmodifiableMap(new HashMap<Component, ComponentInstance>() {
					{
						put(proxyOrigin, null);
						put(vfComponent, vfInstance);
						put(vfcComponent, vfcInstance);
					}
				});
		Map<String, Component> cache = Collections.unmodifiableMap(new HashMap<String, Component>() {
			{
				put("1", proxyOrigin);
				put("2", vfComponent);
				put("3", vfcComponent);
			}
		});
		instanceProxy.setCapabilities(capabilities);
		proxyOrigin.setCapabilities(capabilities);
		List<CapabilityDefinition> flatList = capabilities.values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
		flatList.stream().forEach((CapabilityDefinition capabilityDefinition) -> {
			String name = capabiltyRequirementConvertor.buildCapabilityNameForComponentInstance(cache, instanceProxy,
					capabilityDefinition);
			System.out.println("built name -> " + name);
			assertThat(name).isEqualTo("vepdgtp4837vf0.lb_1." + capabilityDefinition.getName());
		});
	}
	@Test
	public void getReducedPathByOwner() throws Exception {
		List<String> pathList = new ArrayList<>();
		String uniqueId = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_2";

		String exerpt = "41d3a665-1313-4b5e-9bf0-e901ecf4b806.a77df84e-83eb-4edc-9823-d1f9f6549693.lb_1";
		String duplicate = "a77df84e-83eb-4edc-9823-d1f9f6549693.c79e9a4a-b172-4323-a2e2-1c48d6603241.lb_swu_direct_4_rvmi";
		pathList.add(exerpt);
		pathList.add(duplicate);
		pathList.add(duplicate);
		pathList.add(uniqueId);

		pathList.add("5f172af9-1588-443e-8897-1432b19aad8c.2cb7514a-1e50-4280-8457-baacb97b50bf.vepdgtp4837vf0");
		pathList.add("86ae128e-3d0a-41f7-a957-db1df9fe598c.9cc8f8ac-6869-4dd6-a6e1-74ecb9570dc4.vepdgtp4837svc_proxy0");

		List<String> reducedMap = new CapabilityRequirementConverter().getReducedPathByOwner( pathList , uniqueId );

		assertThat( reducedMap ).isNotNull().doesNotContain(exerpt).containsOnlyOnce(duplicate).hasSize(4);
	}

}
