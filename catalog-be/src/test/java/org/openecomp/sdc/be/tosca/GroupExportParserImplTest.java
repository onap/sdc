/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.tosca;

import fj.data.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.tosca.model.ToscaGroupTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupExportParserImplTest {

	private static final String GROUP_DEFINITION_NAME = "groupDefinitionName";
	private static final String GROUP_DEFINITION_INVARIANT_UUID = "groupDefinitionInvariantUUID";
	private static final String GROUP_DEFINITION_GROUP_UUID = "groupDefinitionGroupUUID";
	private static final String GROUP_DEFINITION_VERSION = "groupDefinitionVersion";
	private static final String GROUP_DEFINITION_TYPE = "groupDefinitionType";

	private static final String CAPABILITIE_NAME = "capabilitieName";
	private static final String COMPONENT_INSTANCE_PROPERTY_NAME = "componentInstancePropertyName";
	private static final String COMPONENT_INSTANCE_PROPERTY_VALUE = "componentInstancePropertyValue";


	private GroupExportParser groupExportParser;

	@Mock
	private ApplicationDataTypeCache dataTypeCache;

	@Mock
	private Component component;

	@Before
	public void setUp() throws Exception {
		initGroupExportParser();
	}

	private void initGroupExportParser() {
		when(dataTypeCache.getAll()).thenReturn(Either.left(null));
		groupExportParser = new GroupExportParserImpl(dataTypeCache);
	}

	@Test
	public void failToGetAllDataTypes() {
		when(dataTypeCache.getAll()).thenReturn(Either.right(null));
		assertThatExceptionOfType(SdcResourceNotFoundException.class).isThrownBy(() -> groupExportParser = new GroupExportParserImpl(dataTypeCache));
	}

	@Test
	public void noGroupsInComponent() {
		when(component.getGroups()).thenReturn(null);
		Map<String, ToscaGroupTemplate> groups = groupExportParser.getGroups(component);
		assertThat(groups).isNull();
	}

	@Test
	public void oneGroupInComponent() {
		when(component.getGroups()).thenReturn(genOneGroup());
		Map<String, ToscaGroupTemplate> groups = groupExportParser.getGroups(component);
		assertThat(groups).isNotNull();
		assertThat(groups.size()).isOne();
		ToscaGroupTemplate toscaGroupTemplate = groups.get(GROUP_DEFINITION_NAME);
		Map<String, ToscaTemplateCapability> capabilities = toscaGroupTemplate.getCapabilities();
		ToscaTemplateCapability toscaTemplateCapability = capabilities.get(CAPABILITIE_NAME);
		Map<String, Object> properties = toscaTemplateCapability.getProperties();		
		assertThat(properties).containsEntry(COMPONENT_INSTANCE_PROPERTY_NAME, COMPONENT_INSTANCE_PROPERTY_VALUE);
		
	}

	@Test
	public void whenExportingGroupMembers_takeNamesFromTheMembersAndNotFromTheGroupMembersMap() {
		GroupDefinition group1 = getGroup("group1", "type1", Pair.of("instance1Name", "inst1"));
		GroupDefinition group2 = getGroup("group2", "type2", Pair.of("instance2Name", "inst2"));
		Resource resource = new ResourceBuilder()
				.addComponentInstance("inst1")
				.addComponentInstance("inst2")
				.addGroup(group1)
				.addGroup(group2)
				.build();
		Map<String, ToscaGroupTemplate> groups = groupExportParser.getGroups(resource);
		assertThat(groups.values())
				.extracting("members")
				.containsExactlyInAnyOrder(singletonList("inst1"), singletonList("inst2"));
	}

	@SafeVarargs
	private final GroupDefinition getGroup(String name, String type, Pair<String, String>... members) {
		GroupDefinitionBuilder groupBuilder = GroupDefinitionBuilder.create()
				.setName(name)
				.setType(type);
		Stream.of(members).forEach(member -> groupBuilder.addMember(member.getKey(), member.getValue()));
		return groupBuilder.build();
	}

	private List<GroupDefinition> genOneGroup() {
		List<GroupDefinition> group = new ArrayList<>();
		GroupDefinitionBuilder groupBuilder = GroupDefinitionBuilder.create()
				.setName(GROUP_DEFINITION_NAME)	
				.setInvariantUUID(GROUP_DEFINITION_INVARIANT_UUID)
				.setGroupUUID(GROUP_DEFINITION_GROUP_UUID)
				.setVersion(GROUP_DEFINITION_VERSION)
				.setType(GROUP_DEFINITION_TYPE);
		
		GroupDefinition groupDefinition = groupBuilder.build();

		Map<String, List<CapabilityDefinition>> capabilities = genCapabilities();
		groupDefinition.setCapabilities(capabilities);

		group.add(groupDefinition);
		return group;
	}

	private Map<String, List<CapabilityDefinition>> genCapabilities() {

		Map<String, List<CapabilityDefinition>> capabilities = new HashMap<>();
		List<CapabilityDefinition> capabilityDefinitions = new ArrayList<>();
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();

		List<ComponentInstanceProperty> capabilityProperties = new ArrayList<>();
		ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
		componentInstanceProperty.setName(COMPONENT_INSTANCE_PROPERTY_NAME);
		componentInstanceProperty.setValue(COMPONENT_INSTANCE_PROPERTY_VALUE);

		capabilityProperties.add(componentInstanceProperty);
		capabilityDefinition.setProperties(capabilityProperties);
		capabilityDefinition.setName(CAPABILITIE_NAME);

		capabilityDefinitions.add(capabilityDefinition);
		capabilities.put("JustSoneName", capabilityDefinitions);
		return capabilities;
	}

}
