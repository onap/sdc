package org.openecomp.sdc.be.datamodel.utils;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationParamDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.Operation;

import static org.openecomp.sdc.test.utils.InterfaceOperationTestUtils.createMockOperationInputDefinition;
import static org.openecomp.sdc.test.utils.InterfaceOperationTestUtils.createMockOperationOutputDefinition;

public class InterfaceUIDataConverterTest {

	@Test
	public void testConvertInterfaceDataToOperationData() {
		InterfaceOperationDataDefinition interfaceOperation = createIODD("test",
				"test description",
				createParamDataList("inpName", "property", true, "String"),
				createParamDataList("OutName", null, true, "String"),
				"workflowId",
				"workflowVersionId");
		Operation result;
		result = InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperation);
		Assert.assertNotNull(result);
	}

	@Test
	public void testConvertInterfaceDataToOperationDataWithoutMandatory() {
		ListDataDefinition<InterfaceOperationParamDataDefinition> iopd = new ListDataDefinition<>();
		iopd.add(createParamData("test", "property", "String"));
		InterfaceOperationDataDefinition interfaceOperation = createIODD("test",
				"test description", iopd, iopd,
				"workflowId",
				"workflowVersionId");
		Operation result;
		result = InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperation);
		Assert.assertNotNull(result);
	}

	@Test
	public void testConvertInterfaceDataToOperationDataWithoutOptionalFields() {
		InterfaceOperationDataDefinition interfaceOperation = new InterfaceOperationDataDefinition();
		interfaceOperation.setOperationType("operationType");
		Operation result;
		result = InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperation);
		Assert.assertNotNull(result);
	}

	@Test
	public void testConvertOperationDataToInterfaceData() {
		Operation operationData = new Operation();
		InterfaceOperationDataDefinition result;
		ListDataDefinition<OperationInputDefinition> inputs = new ListDataDefinition<>();
		inputs.add(createMockOperationInputDefinition("Inp1"));
		ListDataDefinition<OperationOutputDefinition> outputs = new ListDataDefinition<>();
		outputs.add(createMockOperationOutputDefinition("out1"));
		operationData.setInputs(inputs);
		operationData.setOutputs(outputs);
		operationData.setImplementation(new ArtifactDataDefinition());
		result = InterfaceUIDataConverter.convertOperationDataToInterfaceData(operationData);
		Assert.assertNotNull(result);
	}

	private InterfaceOperationDataDefinition createIODD(String operationType, String description,
								ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams,
								ListDataDefinition<InterfaceOperationParamDataDefinition> outputParams,
								String workflowId,
								String workflowVersionId) {
		InterfaceOperationDataDefinition interfaceOperation = new InterfaceOperationDataDefinition();
		interfaceOperation.setOperationType(operationType);
		interfaceOperation.setDescription(description);
		interfaceOperation.setInputParams(inputParams);
		interfaceOperation.setOutputParams(outputParams);
		interfaceOperation.setWorkflowId(workflowId);
		interfaceOperation.setWorkflowVersionId(workflowVersionId);
		return interfaceOperation;
	}

	private InterfaceOperationParamDataDefinition createParamData(String name, String property, boolean mandatory, String type) {
		InterfaceOperationParamDataDefinition definition = createParamData(name, property, type);
		definition.setMandatory(mandatory);
		return  definition;
	}

	private InterfaceOperationParamDataDefinition createParamData(String name, String property, String type) {
		InterfaceOperationParamDataDefinition definition = new InterfaceOperationParamDataDefinition();
		definition.setName(name);
		definition.setProperty(property);
		definition.setType(type);
		return  definition;
	}

	private ListDataDefinition<InterfaceOperationParamDataDefinition> createParamDataList(String name,
											String property, boolean mandatory, String type) {
		ListDataDefinition<InterfaceOperationParamDataDefinition> list = new ListDataDefinition<>();
		list.add(createParamData(name, property, mandatory, type));
		return list;
	}

}