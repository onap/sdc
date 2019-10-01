/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.heat.datatypes.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitFrequency;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitSize;
import org.openecomp.sdc.heat.datatypes.ToscaScalarUnitTime;

public class ToscaScalarUnitTypesTest {

    private String inputValue;

    @Test
    public void shouldReturnTrueForScalarUnitSize() {
        inputValue = "100 " + ToscaScalarUnitSize.GB.name();
        Assert.assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(inputValue,
            DefinedHeatParameterTypes.NUMBER.getType()));

        Assert.assertTrue(DefinedHeatParameterTypes.isValueScalarUnit(inputValue, ToscaScalarUnitSize.class));
    }

    @Test
    public void shouldReturnTrueForScalarUnitTime() {
        inputValue = "5000 " + ToscaScalarUnitTime.S.name();
        Assert.assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(
            inputValue, DefinedHeatParameterTypes.NUMBER.getType()));

        Assert.assertTrue(DefinedHeatParameterTypes.isValueScalarUnit(inputValue, ToscaScalarUnitTime.class));
    }

    @Test
    public void shouldReturnTrueForScalarUnitFrequency() {
        inputValue = "60 " + ToscaScalarUnitFrequency.GHZ.name();
        Assert.assertTrue(DefinedHeatParameterTypes.isValueIsFromGivenType(
            inputValue, DefinedHeatParameterTypes.NUMBER.getType()));

        Assert.assertTrue(DefinedHeatParameterTypes.isValueScalarUnit(inputValue, ToscaScalarUnitFrequency.class));
    }

    @Test
    public void shouldReturnFalse() {
        inputValue = "one hundred " + ToscaScalarUnitSize.GB.name();
        Assert.assertFalse(DefinedHeatParameterTypes.isValueIsFromGivenType(
            inputValue, DefinedHeatParameterTypes.NUMBER.getType()));

        Assert.assertFalse(DefinedHeatParameterTypes.isValueScalarUnit(inputValue, ToscaScalarUnitSize.class));
    }

}
