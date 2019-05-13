package org.openecomp.sdc.be.components.property;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

public class GetInputUtilsTest {
    private static final String INPUT_ID = "inputUid";
    private GetInputValueDataDefinition getInput;

    @Before
    public void init() {
        getInput = new GetInputValueDataDefinition();
        getInput.setInputId(INPUT_ID);
    }

    @Test
    public void isGetInputValueForInput_equalId() {
        boolean getInputValueForInput = GetInputUtils.isGetInputValueForInput(getInput, INPUT_ID);
        assertTrue(getInputValueForInput);
    }

    @Test
    public void isGetInputValueForInput_byInputData() {
        GetInputValueDataDefinition getInputIndex = new GetInputValueDataDefinition();
        getInputIndex.setInputId(INPUT_ID);
        getInput.setGetInputIndex(getInputIndex);
        getInput.setInputId("");

        boolean getInputValueForInput = GetInputUtils.isGetInputValueForInput(getInput, INPUT_ID);
        assertTrue(getInputValueForInput);
    }
}