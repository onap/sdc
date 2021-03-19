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
package org.openecomp.sdc.be.components.property;

import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;

public class GetInputUtils {

    private GetInputUtils() {
    }

    public static boolean isGetInputValueForInput(GetInputValueDataDefinition inputData, String inputId) {
        return inputData.getInputId().equals(inputId) || (inputData.getGetInputIndex() != null && inputData.getGetInputIndex().getInputId()
            .equals(inputId));
    }
}
