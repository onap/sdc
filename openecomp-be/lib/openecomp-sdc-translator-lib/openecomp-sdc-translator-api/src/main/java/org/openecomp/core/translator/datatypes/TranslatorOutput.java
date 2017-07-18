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

package org.openecomp.core.translator.datatypes;

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.util.List;
import java.util.Map;


public class TranslatorOutput {
  private Map<String, List<ErrorMessage>> errorMessages;
  private ToscaServiceModel toscaServiceModel;
  private ToscaServiceModel nonUnifiedToscaServiceModel;

  public Map<String, List<ErrorMessage>> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(Map<String, List<ErrorMessage>> errorMessages) {
    this.errorMessages = errorMessages;
  }

  public ToscaServiceModel getToscaServiceModel() {
    return toscaServiceModel;
  }

  public void setToscaServiceModel(ToscaServiceModel toscaServiceModel) {
    this.toscaServiceModel = toscaServiceModel;
  }

  public ToscaServiceModel getNonUnifiedToscaServiceModel() {
    return nonUnifiedToscaServiceModel;
  }

  public void setNonUnifiedToscaServiceModel(
      ToscaServiceModel nonUnifiedToscaServiceModel) {
    this.nonUnifiedToscaServiceModel = nonUnifiedToscaServiceModel;
  }
}
