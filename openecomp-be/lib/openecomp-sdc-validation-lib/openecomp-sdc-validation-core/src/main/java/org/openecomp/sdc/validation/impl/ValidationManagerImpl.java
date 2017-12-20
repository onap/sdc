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

package org.openecomp.sdc.validation.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.services.ValidationFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValidationManagerImpl implements ValidationManager {

  private static Logger logger = (Logger) LoggerFactory.getLogger(ValidationManagerImpl.class);

  private GlobalValidationContext globalContext;
  private List<Validator> validators;

  public ValidationManagerImpl() {
    globalContext = new GlobalValidationContext();
    validators = ValidationFactory.getValidators();
  }

  @Override
  public Map<String, List<ErrorMessage>> validate() {
    for (Validator validator : validators) {
      if(Objects.nonNull(validator)) {
        validator.validate(globalContext);
      }
    }
    return convertMessageContainsToErrorMessage(globalContext.getContextMessageContainers());
  }

  @Override
  public void addFile(String fileName, byte[] fileContent) {
    globalContext.addFileContext(fileName, fileContent);
  }

  @Override
  public void updateGlobalContext(GlobalValidationContext globalContext) {
    this.globalContext = globalContext;
  }

  private Map<String, List<ErrorMessage>> convertMessageContainsToErrorMessage(
      Map<String, MessageContainer> contextMessageContainers) {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    contextMessageContainers.entrySet().stream()
        .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue().getErrorMessageList()))
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue().getErrorMessageList()));
    return errors;
  }

}
