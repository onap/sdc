/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Modifications copyright (c) 2019 Nokia
 */

package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;

public class ComponentQuestionnaireSchemaInput implements SchemaTemplateInput {
  private String componentDisplayName;
  private boolean manual;
  private List<String> nicNames;
  private Map componentQuestionnaireData;

  @VisibleForTesting
  ComponentQuestionnaireSchemaInput() {}

  public ComponentQuestionnaireSchemaInput(List<String> nicNames, Map componentQuestionnaireData,
                               String componentDisplayName, boolean manual) {
    this.nicNames = nicNames;
    this.componentQuestionnaireData = componentQuestionnaireData;
    this.componentDisplayName = componentDisplayName;
    this.manual = manual;
  }

  public List<String> getNicNames() {
    return nicNames;
  }

  public Map getComponentQuestionnaireData() {
    return componentQuestionnaireData;
  }

  public String getComponentDisplayName() {
    return componentDisplayName;
  }

  public boolean isManual() {
    return manual;
  }
}
