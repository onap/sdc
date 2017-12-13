/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.core.converter.datatypes;

import java.io.File;

public class Constants {
  public static final String mainStName = "MainServiceTemplate.yaml";
  public static final String globalStName = "GlobalSubstitutionTypesServiceTemplate.yaml";
  public static final String manifestFileName = "MainServiceTemplate.mf";
  public static final String definitionsDir = "Definitions/";
  public static final String metadataFile = "TOSCA-Metadata/TOSCA.meta";


  public static final String definitionVersion = "tosca_definitions_version";
  private static final String DEFAULT_NAMESPACE = "tosca_default_namespace";
  private static final String TEMPLATE_NAME = "template_name";
  public static final String topologyTemplate = "topology_template";
  private static final String TEMPLATE_AUTHOR = "template_author";
  private static final String TEMPLATE_VERSION = "template_version";
  private static final String DESCRIPTION = "description";
  private static final String IMPORTS = "imports";
  private static final String DSL_DEFINITIONS = "dsl_definitions";
  public static final String nodeType = "node_type";
  public static final String nodeTypes = "node_types";
  private static final String RELATIONSHIP_TYPES = "relationship_types";
  private static final String RELATIONSHIP_TEMPLATES = "relationship_templates";
  private static final String CAPABILITY_TYPES = "capability_types";
  private static final String ARTIFACT_TYPES = "artifact_types";
  private static final String DATA_TYPES = "data_types";
  private static final String INTERFACE_TYPES = "interface_types";
  private static final String POLICY_TYPES = "policy_types";
  private static final String GROUP_TYPES = "group_types";
  private static final String REPOSITORIES = "repositories";
  public static final String metadata = "metadata";
  public static final String nodeTemplates = "node_templates";
  public static final String inputs = "inputs";
  public static final String outputs = "outputs";
  public static final String substitutionMappings = "substitution_mappings";
  public static final String capabilities = "capabilities";
  public static final String requirements = "requirements";

  public static final String openecompHeatIndex = "openecomp_heat_index";
  public static final String ONAP_INDEX = "onap_index";
  public static final String globalSubstitution = "GlobalSubstitutionTypes";

  public static final String externalFilesFolder = "External" + File.separator;
}
