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

package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import java.util.List;
import java.util.Map;

public class ComponentQuestionnaireSchemaInput implements SchemaTemplateInput {
  private List<String> nicNames;
  private Map componentQuestionnaireData;

  public ComponentQuestionnaireSchemaInput(List<String> nicNames, Map componentQuestionnaireData) {
    this.nicNames = nicNames;
    this.componentQuestionnaireData = componentQuestionnaireData;
  }

  public List<String> getNicNames() {
    return nicNames;
  }

  public Map getComponentQuestionnaireData() {
    return componentQuestionnaireData;
  }
}
