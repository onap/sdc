/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.tosca.datatypes;

import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;

public class ToscaGroupType {

    //TOSCA native types
    public static final String NATIVE_ROOT = "tosca.groups.Root";
    private static Configuration config = ConfigurationManager.lookup();
    private static final String GROUP_TYPE_PREFIX = config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_GROUP_TYPE);
    //Additional types
    public static final String HEAT_STACK = GROUP_TYPE_PREFIX + "heat.HeatStack";
}
