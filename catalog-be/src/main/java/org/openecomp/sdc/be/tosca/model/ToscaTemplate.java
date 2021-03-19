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
package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.model.Component;

@Getter
@Setter
public class ToscaTemplate {

    private String tosca_definitions_version;
    private Map<String, String> metadata;
    private List<Map<String, Map<String, String>>> imports;
    private Map<String, Object> interface_types;
    private Map<String, ToscaDataType> data_types;
    private Map<String, ToscaNodeType> node_types;
    private ToscaTopolgyTemplate topology_template;
    private List<Triple<String, String, Component>> dependencies;

    public ToscaTemplate(final String toscaDefinitionsVersion) {
        this.tosca_definitions_version = toscaDefinitionsVersion;
    }
}
