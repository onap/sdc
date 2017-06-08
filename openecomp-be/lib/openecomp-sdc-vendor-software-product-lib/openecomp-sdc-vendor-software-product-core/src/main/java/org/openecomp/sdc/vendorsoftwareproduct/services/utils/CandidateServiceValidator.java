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

package org.openecomp.sdc.vendorsoftwareproduct.services.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by Talio on 12/6/2016.
 */
public class CandidateServiceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public Optional<List<ErrorMessage>> validateFileDataStructure(
      FilesDataStructure filesDataStructure) {
    if (Objects.isNull(filesDataStructure)) {
      return Optional.empty();
    }
    if (validateAtLeaseOneModuleExist(filesDataStructure)) {
      return Optional.of(Arrays.asList(new ErrorMessage(ErrorLevel.ERROR, Messages
          .NO_MODULES_IN_MANIFEST.getErrorMessage())));
    }

    List<ErrorMessage> errors = new ArrayList<>();
    for (Module module : filesDataStructure.getModules()) {
      validateModuleHaveYaml(errors, module);
      validateNoVolEnvWithoutVol(errors, module);
    }
    return Optional.of(errors);
  }


  private boolean validateAtLeaseOneModuleExist(FilesDataStructure filesDataStructure) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return CollectionUtils.isEmpty(filesDataStructure.getModules());
  }

  private void validateNoVolEnvWithoutVol(List<ErrorMessage> errors, Module module) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (StringUtils.isEmpty(module.getVol()) && StringUtils.isNotEmpty(module.getVolEnv())) {
      errors.add(new ErrorMessage(ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MODULE_IN_MANIFEST_VOL_ENV_NO_VOL.getErrorMessage(),
              module.getName())));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void validateModuleHaveYaml(List<ErrorMessage> errors, Module module) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (StringUtils.isEmpty(module.getYaml())) {
      errors.add(new ErrorMessage(ErrorLevel.ERROR, ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MODULE_IN_MANIFEST_NO_YAML.getErrorMessage(),
              module.getName())));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}
