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

package org.openecomp.sdc.ci.tests.pages;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;

public class HeatParamNameBuilder {
    private static final String CURRENT_VAL = DataTestIdEnum.EnvParameterView.ENV_CURRENT_VALUE.getValue();
    private static final String DEFAULT_VAL = DataTestIdEnum.EnvParameterView.ENV_DEFAULT_VALUE.getValue();


    public static String buildCurrentHeatParamValue(String paramName){
        return new StringBuilder().append(CURRENT_VAL).append(paramName).toString();
    }

    public static String buildDefaultHeatParamValue(String paramName){
        return new StringBuilder().append(DEFAULT_VAL).append(paramName).toString();
    }


}
