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

package org.openecomp.sdc.tosca.services;

public class ConfigConstants {
  //namespaces
  public static final String NAMESPACE = "ToscaModel";

  //keys
  public static final String PREFIX_CAPABILITY_TYPE =
      "tosca.entity.namespace.prefix.capabilityType";
  public static final String PREFIX_DATA_TYPE = "tosca.entity.namespace.prefix.dataType";
  public static final String PREFIX_GROUP_TYPE = "tosca.entity.namespace.prefix.groupType";
  public static final String PREFIX_POLICY_TYPE = "tosca.entity.namespace.prefix.policyType";
  public static final String PREFIX_ARTIFACT_TYPE = "tosca.entity.namespace.prefix.artifactType";
  public static final String PREFIX_RELATIONSHIP_TYPE =
      "tosca.entity.namespace.prefix.relationshipType";

  public static final String PREFIX_NODE_TYPE_VFC = "tosca.entity.namespace.prefix.nodeType.vfc";
  public static final String PREFIX_NODE_TYPE_NETWORK =
      "tosca.entity.namespace.prefix.nodeType.network";
  public static final String PREFIX_NODE_TYPE_CP =
      "tosca.entity.namespace.prefix.nodeType.connectionPoint";
  public static final String PREFIX_NODE_TYPE_ABSTARCT =
      "tosca.entity.namespace.prefix.nodeType.abstract";
  public static final String PREFIX_NODE_TYPE_RULE = "tosca.entity.namespace.prefix.nodeType.rule";


  private ConfigConstants() {
  }
}
