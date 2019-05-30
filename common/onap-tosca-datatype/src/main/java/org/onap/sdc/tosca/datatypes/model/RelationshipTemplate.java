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

package org.onap.sdc.tosca.datatypes.model;

import java.util.Map;

import lombok.Data;

@Data
public class RelationshipTemplate implements Template {

  private String type;
  private String description;
  private Map<String, String> metadata;
  private Map<String, Object> properties;
  private Map<String, Object> attributes;
  private Map<String, RequirementAssignment> requirements;
  private Map<String, CapabilityAssignment> capabilities;
  private Map<String, Object> interfaces;
  private String copy;

}
