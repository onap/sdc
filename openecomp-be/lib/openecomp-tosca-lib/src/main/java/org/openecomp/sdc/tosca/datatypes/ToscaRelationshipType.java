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

public class ToscaRelationshipType {

  private static Configuration config = ConfigurationManager.lookup();

  public static final String RELATIONSHIP_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_RELATIONSHIP_TYPE);

  public static final String NATIVE_ROOT = "tosca.relationships.Root";
  public static final String NATIVE_ATTACHES_TO = "tosca.relationships.AttachesTo";
  public static final String NATIVE_DEPENDS_ON = "tosca.relationships.DependsOn";
  public static final String NATIVE_NETWORK_LINK_TO = "tosca.relationships.network.LinksTo";
  public static final String NATIVE_NETWORK_BINDS_TO = "tosca.relationships.network.BindsTo";
  public static final String CINDER_VOLUME_ATTACHES_TO =
      RELATIONSHIP_TYPE_PREFIX + "VolumeAttachesTo";
  public static final String ATTACHES_TO = RELATIONSHIP_TYPE_PREFIX + "AttachesTo";

}
