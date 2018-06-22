package org.openecomp.sdc.be.datamodel.utils;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IO_INPUT_PARAMETERS;

import java.util.LinkedList;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.Operation;

public class InterfaceUIDataConverterTest {

	@Test
	public void testConvertInterfaceDataToOperationData() throws Exception {
		InterfaceOperationDataDefinition interfaceOperation = new InterfaceOperationDataDefinition();
		Operation result;

		// default test
		result = InterfaceUIDataConverter.convertInterfaceDataToOperationData(interfaceOperation);
	}

	@Test
	public void testConvertOperationDataToInterfaceData() throws Exception {
		Operation operationData = new Operation();
		InterfaceOperationDataDefinition result;
		ListDataDefinition<OperationInputDefinition> inputs = new ListDataDefinition<>();
		ListDataDefinition<OperationOutputDefinition> outputs = new ListDataDefinition<>();
		operationData.setInputs(inputs);
		operationData.setOutputs(outputs);
		operationData.setImplementation(new ArtifactDataDefinition());
		// default test
		result = InterfaceUIDataConverter.convertOperationDataToInterfaceData(operationData);
	}
}