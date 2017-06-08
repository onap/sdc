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

package org.openecomp.sdc.translator.services.heattotosca;

public class ConfigConstants {
  //namespaces
  public static final String MANDATORY_TRANSLATOR_NAMESPACE = "mandatoryHeatToToscaTranslator";
  public static final String MANDATORY_UNIFIED_MODEL_NAMESPACE = "mandatoryUnifiedModel";
  public static final String TRANSLATOR_NAMESPACE = "heatToToscaTranslator";
  public static final String MAPPING_NAMESPACE = "heatToToscaMapping";

  //keys
  public static final String RESOURCE_TRANSLATION_IMPL_KEY = "resourceTranslationImpl";
  public static final String NESTED_RESOURCE_TRANSLATION_IMPL_KEY = "NestedResource";
  public static final String DEFAULT_RESOURCE_TRANSLATION_IMPL_KEY = "DefaultResource";
  public static final String FUNCTION_TRANSLATION_IMPL_KEY = "functionTranslationImpl";
  public static final String NAMING_CONVENTION_EXTRACTOR_IMPL_KEY = "namingConventionExtractImpl";
  public static final String UNIFIED_COMPOSITION_IMPL_KEY = "unifiedCompositionImpl";
  public static final String CONTRAIL_COMPUTE_NODE_TYPE_IMPL_KEY = "ContrailComputeNodeTypeName";
  public static final String RESOURCE_MAPPING_KEY = "resourceMapping";
  public static final String SUPPORTED_CONSOLIDATION_COMPUTE_RESOURCES_KEY =
      "supportedConsolidationComputeResources";
  public static final String SUPPORTED_CONSOLIDATION_PORT_RESOURCES_KEY =
      "supportedConsolidationPortResources";

  //others
  public static final String TRANS_MAPPING_DELIMITER_CHAR = "#";

  private ConfigConstants() {
  }
}
