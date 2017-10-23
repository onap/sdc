package org.openecomp.sdc.be.components.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition.PropertyNames;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.base.Function;

import fj.data.Either;


public class GroupBusinessLogicTest {

	private GroupBusinessLogic createTestSubject() {
		return new GroupBusinessLogic();
	}

	
	@Test
	public void testGetComponentTypeForResponse() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		String result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testVerifyArtifactsBelongsToComponent() throws Exception {
	GroupBusinessLogic testSubject;Component component = null;
	List<String> artifacts = null;
	String context = "";
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();artifacts = null;
	
	
	}

	
	@Test
	public void testVerifyComponentInstancesAreValidMembers() throws Exception {
	GroupBusinessLogic testSubject;Component component = null;
	ComponentTypeEnum componentType = null;
	String groupName = "";
	String groupType = "";
	Map<String,String> groupMembers = null;
	List<String> memberToscaTypes = null;
	Either<Boolean,ResponseFormat> result;
	
	// test 1
	testSubject=createTestSubject();groupMembers = null;
	
	
	
	// test 2
	testSubject=createTestSubject();memberToscaTypes = null;
	
	
	}

	
	@Test
	public void testValidateAndUpdateGroupMetadata() throws Exception {
		GroupBusinessLogic testSubject;
		String componentId = "";
		User user = null;
		ComponentTypeEnum componentType = null;
		GroupDefinition updatedGroup = null;
		boolean inTransaction = false;
		boolean shouldLock = false;
		Either<GroupDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateGroupMetadata() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		GroupDefinition currentGroup = null;
		GroupDefinition updatedGroup = null;
		Either<GroupDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateGroup() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		GroupDefinition updatedGroup = null;
		String currentGroupName = "";
		Either<GroupDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateGroupProperties() throws Exception {
		GroupBusinessLogic testSubject;
		String componentId = "";
		String groupUniqueId = "";
		User user = null;
		ComponentTypeEnum componentType = null;
		List<GroupProperty> groupPropertiesToUpdate = null;
		boolean inTransaction = false;
		Either<List<GroupProperty>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testResetEmptyValueWithDefaults() throws Exception {
	GroupBusinessLogic testSubject;List<GroupProperty> groupPropertiesToUpdate = null;
	GroupDefinition originalGroup = null;
	
	
	// default test
	}

	
	@Test
	public void testValidateGroupPropertyAndResetEmptyValue() throws Exception {
	GroupBusinessLogic testSubject;GroupDefinition originalGroup = null;
	List<GroupProperty> groupPropertiesToUpdate = null;
	Either<List<GroupProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidatePropertyBusinessLogic() throws Exception {
	GroupBusinessLogic testSubject;List<GroupProperty> groupPropertiesToUpdate = null;
	GroupDefinition originalGroup = null;
	Either<List<GroupProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testPrepareMapWithOriginalProperties() throws Exception {
		GroupBusinessLogic testSubject;
		GroupDefinition originalGroup = null;
		Map<PropertyNames, String> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateOnlyValueChanged() throws Exception {
	GroupBusinessLogic testSubject;List<GroupProperty> groupPropertiesToUpdate = null;
	GroupDefinition originalGroup = null;
	Either<List<GroupProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testIsOnlyGroupPropertyValueChanged() throws Exception {
		GroupBusinessLogic testSubject;
		GroupProperty groupProperty = null;
		GroupProperty groupProperty2 = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateGroupMetadata_1() throws Exception {
		GroupBusinessLogic testSubject;
		GroupDefinition currentGroup = null;
		GroupDefinition groupUpdate = null;
		Either<GroupDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateGroupName() throws Exception {
		GroupBusinessLogic testSubject;
		GroupDefinition currentGroup = null;
		GroupDefinition groupUpdate = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateGroupName() throws Exception {
		GroupBusinessLogic testSubject;
		String currentGroupName = "";
		String groupUpdateName = "";
		boolean isforceNameModification = false;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetGroupWithArtifactsById() throws Exception {
		GroupBusinessLogic testSubject;
		ComponentTypeEnum componentType = null;
		String componentId = "";
		String groupId = "";
		String userId = "";
		boolean inTransaction = false;
		Either<GroupDefinitionInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testFindGroupOnComponent() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		String groupId = "";
		Either<GroupDefinition, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateGroupsBeforeUpdate() throws Exception {
	GroupBusinessLogic testSubject;String componentId = "";
	String userId = "";
	ComponentTypeEnum componentType = null;
	List<GroupDefinition> groups = null;
	boolean inTransaction = false;
	Either<org.openecomp.sdc.be.model.Component,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidateGroupsInComponentByFunc() throws Exception {
	GroupBusinessLogic testSubject;List<GroupDefinition> groups = null;
	Component component = null;
	Function<GroupDefinition,String> getByParam = null;
	ResponseFormat result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testGetAsString() throws Exception {
		GroupBusinessLogic testSubject;
		List<String> list = null;
		String result;

		// test 1
		testSubject = createTestSubject();
		list = null;
		
		
	}

	
	@Test
	public void testUpdateGroupPropertiesValue() throws Exception {
	GroupBusinessLogic testSubject;String componentId = "";
	GroupDefinition currentGroup = null;
	List<GroupProperty> groupPropertyToUpdate = null;
	boolean inTransaction = false;
	Either<List<GroupProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidateGenerateVfModuleGroupNames() throws Exception {
		GroupBusinessLogic testSubject;
		List<ArtifactTemplateInfo> allGroups = null;
		String resourceSystemName = "";
		int startGroupCounter = 0;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateGenerateVfModuleGroupName() throws Exception {
		GroupBusinessLogic testSubject;
		String resourceSystemName = "";
		String description = "";
		int groupCounter = 0;
		Either<String, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		resourceSystemName = null;
		description = null;
		
		

		// test 2
		testSubject = createTestSubject();
		resourceSystemName = "";
		description = null;
		
		

		// test 3
		testSubject = createTestSubject();
		description = null;
		resourceSystemName = null;
		
		

		// test 4
		testSubject = createTestSubject();
		description = "";
		resourceSystemName = null;
		
		
	}

	
	@Test
	public void testValidateUpdateVfGroupNames() throws Exception {
		GroupBusinessLogic testSubject;
		Map<String, GroupDefinition> groups = null;
		String resourceSystemName = "";
		Either<Map<String, GroupDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetNextVfModuleNameCounter() throws Exception {
		GroupBusinessLogic testSubject;
		Map<String, GroupDefinition> groups = null;
		int result;

		// test 1
		testSubject = createTestSubject();
		groups = null;
		
		
	}

	
	@Test
	public void testGetNextVfModuleNameCounter_1() throws Exception {
		GroupBusinessLogic testSubject;
		Collection<GroupDefinition> groups = null;
		int result;

		// test 1
		testSubject = createTestSubject();
		groups = null;
		
		
	}

	
	@Test
	public void testValidateUpdateVfGroupNamesOnGraph() throws Exception {
		GroupBusinessLogic testSubject;
		List<GroupDefinition> groups = null;
		Component component = null;
		boolean inTransaction = false;
		Either<List<GroupDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetGroupInstWithArtifactsById() throws Exception {
		GroupBusinessLogic testSubject;
		ComponentTypeEnum componentType = null;
		String componentId = "";
		String componentInstanceId = "";
		String groupInstId = "";
		String userId = "";
		boolean inTransaction = false;
		Either<GroupDefinitionInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testFindComponentInstanceAndGroupInstanceOnComponent() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		String componentInstanceId = "";
		String groupInstId = "";
		Either<ImmutablePair<ComponentInstance, GroupInstance>, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetLatestIntProperty() throws Exception {
	GroupBusinessLogic testSubject;Map<PropertyNames,String> newValues = null;
	Map<PropertyNames,String> parentValues = null;
	PropertyNames propertyKey = null;
	int result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testIsPropertyChanged() throws Exception {
	GroupBusinessLogic testSubject;Map<PropertyNames,String> newValues = null;
	Map<PropertyNames,String> parentValues = null;
	PropertyNames minInstances = null;
	boolean result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidateMinMaxAndInitialCountPropertyLogicVF() throws Exception {
	GroupBusinessLogic testSubject;Map<PropertyNames,String> newValues = null;
	Map<PropertyNames,String> parentValues = null;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidateMinMaxAndInitialCountPropertyLogic() throws Exception {
	GroupBusinessLogic testSubject;Map<PropertyNames,String> newValues = null;
	Map<PropertyNames,String> currValues = null;
	Map<PropertyNames,String> parentValues = null;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testValidateValueInRange() throws Exception {
	GroupBusinessLogic testSubject;ImmutablePair<PropertyNames,String> newValue = null;
	ImmutablePair<PropertyNames,String> min = null;
	ImmutablePair<PropertyNames,String> max = null;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testParseIntValue() throws Exception {
		GroupBusinessLogic testSubject;
		String value = "";
		PropertyNames propertyName = null;
		int result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdateGroupInstancePropertyValues() throws Exception {
		GroupBusinessLogic testSubject;
		String componentId = "";
		String instanceId = "";
		GroupInstance oldGroupInstance = null;
		List<GroupInstanceProperty> newProperties = null;
		boolean inTransaction = false;
		Either<GroupInstance, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateReduceGroupInstancePropertiesBeforeUpdate() throws Exception {
	GroupBusinessLogic testSubject;GroupInstance oldGroupInstance = null;
	List<GroupInstanceProperty> newProperties = null;
	Either<List<GroupInstanceProperty>,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testFillValuesAndParentValuesFromExistingProperties() throws Exception {
	GroupBusinessLogic testSubject;Map<String,GroupInstanceProperty> existingProperties = null;
	Map<PropertyNames,String> propertyValues = null;
	Map<PropertyNames,String> parentPropertyValues = null;
	
	
	// default test
	}

	
	@Test
	public void testHandleAndAddProperty() throws Exception {
	GroupBusinessLogic testSubject;List<GroupInstanceProperty> reducedProperties = null;
	Map<PropertyNames,String> newPropertyValues = null;
	GroupInstanceProperty currNewProperty = null;
	GroupInstanceProperty currExistingProperty = null;
	Either<Boolean,ResponseFormat> result;
	
	// default test
	testSubject=createTestSubject();
	}

	
	@Test
	public void testIsUpdatable() throws Exception {
		GroupBusinessLogic testSubject;
		PropertyNames updatablePropertyName = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testAddPropertyUpdatedValues() throws Exception {
	GroupBusinessLogic testSubject;List<GroupInstanceProperty> reducedProperties = null;
	PropertyNames propertyName = null;
	Map<PropertyNames,String> newPropertyValues = null;
	GroupInstanceProperty newProperty = null;
	GroupInstanceProperty existingProperty = null;
	
	
	// default test
	}

	
	@Test
	public void testIsEmptyMinInitialCountValue() throws Exception {
		GroupBusinessLogic testSubject;
		PropertyNames propertyName = null;
		String newValue = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testConvertIfUnboundMax() throws Exception {
		GroupBusinessLogic testSubject;
		String value = "";
		int result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateAndUpdatePropertyValue() throws Exception {
		GroupBusinessLogic testSubject;
		GroupInstanceProperty newProperty = null;
		GroupInstanceProperty existingProperty = null;
		Either<Boolean, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testValidateImmutableProperty() throws Exception {
		GroupBusinessLogic testSubject;
		GroupProperty oldProperty = null;
		GroupProperty newProperty = null;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testCreateGroups() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		User user = null;
		ComponentTypeEnum componentType = null;
		List<GroupDefinition> groupDefinitions = null;
		Either<List<GroupDefinition>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		groupDefinitions = null;
		
		
	}

	
	@Test
	public void testAddGroups() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		User user = null;
		ComponentTypeEnum componentType = null;
		List<GroupDefinition> groupDefinitions = null;
		Either<List<GroupDefinition>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		groupDefinitions = null;
		
		
	}

	
	@Test
	public void testDeleteGroups() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		User user = null;
		ComponentTypeEnum componentType = null;
		List<GroupDefinition> groupDefinitions = null;
		Either<List<GroupDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateGroups() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		ComponentTypeEnum componentType = null;
		List<GroupDefinition> groupDefinitions = null;
		Either<List<GroupDefinition>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testHandleGroup() throws Exception {
		GroupBusinessLogic testSubject;
		Component component = null;
		User user = null;
		ComponentTypeEnum componentType = null;
		GroupDefinition groupDefinition = null;
		Map<String, DataTypeDefinition> allDAtaTypes = null;
		Either<GroupDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testHandleProperty() throws Exception {
		GroupBusinessLogic testSubject;
		GroupProperty groupProperty = null;
		PropertyDefinition prop = null;
		Integer index = 0;
		Map<String, DataTypeDefinition> allDataTypes = null;
		Either<GroupProperty, TitanOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		prop = null;
		
		
	}
}