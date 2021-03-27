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

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class RelationshipType {

    private String derived_from;
    private String version;
    private Map<String, String> metadata;
    private String description;
    private Map<String, PropertyDefinition> properties;
    private Map<String, AttributeDefinition> attributes;
    private Map<String, Object> interfaces;
    private List<String> valid_target_types;
    //An optional list of one or more names of Capability Types that are valid targets

    // for this relationship
}
