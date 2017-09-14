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

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;

public class ToscaPolicyType {

  private static Configuration config = ConfigurationManager.lookup();

  public static final String POLICY_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_POLICY_TYPE);

  //TOSCA native types
  public static final String NATIVE_PLACEMENT = "tosca.policy.placement";

  //Additional types
  public static final String PLACEMENT_ANTILOCATE = POLICY_TYPE_PREFIX + "placement.Antilocate";
  public static final String PLACEMENT_COLOCATE = POLICY_TYPE_PREFIX + "placement.Colocate";
}
