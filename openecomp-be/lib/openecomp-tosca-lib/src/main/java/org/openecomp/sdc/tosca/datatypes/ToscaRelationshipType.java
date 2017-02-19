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

public enum ToscaRelationshipType {

  ROOT("tosca.relationships.Root"),
  NATIVE_ATTACHES_TO("tosca.relationships.AttachesTo"),
  DEPENDS_ON("tosca.relationships.DependsOn"),
  NETWORK_LINK_TO("tosca.relationships.network.LinksTo"),
  NETWORK_BINDS_TO("tosca.relationships.network.BindsTo"),
  CINDER_VOLUME_ATTACHES_TO("org.openecomp.relationships.heat.cinder.VolumeAttachesTo"),
  ATTACHES_TO("org.openecomp.relationships.AttachesTo"),;

  private String displayName;

  ToscaRelationshipType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }


}
