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

package org.openecomp.sdc.be.components.property;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;

import static org.junit.Assert.assertTrue;

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
