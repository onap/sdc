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

package org.openecomp.sdcrests.vsp.rest.mapping;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdcrests.common.mapping.MapErrorCodeToDto;
import org.openecomp.sdcrests.common.mapping.MapErrorMessageToDto;
import org.openecomp.sdcrests.common.types.ErrorCodeDto;
import org.openecomp.sdcrests.common.types.ErrorMessageDto;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireValidationResultDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapValidationResponseToDto
    extends MappingBase<ValidationResponse, ValidationResponseDto> {
  private static Map<String, List<ErrorMessageDto>> mapUploadDataErrors(
      Map<String, List<ErrorMessage>> uploadDataErrors) {
    if (MapUtils.isEmpty(uploadDataErrors)) {
      return null;
    }
    return uploadDataErrors.entrySet().stream().collect(
        Collectors.toMap(entry -> entry.getKey(), entry -> mapErrorMessages(entry.getValue())));
  }

  private static QuestionnaireValidationResultDto mapQuestionnaireValidationResult(
      QuestionnaireValidationResult questionnaireValidationResult) {
    if (questionnaireValidationResult == null) {
      return null;
    }
    QuestionnaireValidationResultDto questionnaireValidationResultDto =
        new QuestionnaireValidationResultDto();
    questionnaireValidationResultDto.setValid(questionnaireValidationResult.isValid());

    Set<CompositionEntityValidationDataDto> validationDataDto = new HashSet<>();
    for(CompositionEntityValidationData validationData : questionnaireValidationResult.getValidationData()){
      validationDataDto.add(new MapCompositionEntityValidationDataToDto().applyMapping
          (validationData, CompositionEntityValidationDataDto.class));
    }

    questionnaireValidationResultDto.setValidationData(validationDataDto);
    return questionnaireValidationResultDto;
  }

  private static List<ErrorMessageDto> mapErrorMessages(List<ErrorMessage> errorMessages) {
    return errorMessages == null ? null : errorMessages.stream().map(
        errorMessage -> new MapErrorMessageToDto()
            .applyMapping(errorMessage, ErrorMessageDto.class)).collect(Collectors.toList());
  }

  private static Collection<ErrorCodeDto> mapErrorCodes(Collection<ErrorCode> errorCodes) {
    return CollectionUtils.isEmpty(errorCodes) ? null : errorCodes.stream()
        .map(errorCode -> new MapErrorCodeToDto().applyMapping(errorCode, ErrorCodeDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public void doMapping(ValidationResponse source, ValidationResponseDto target) {
    target.setValid(source.isValid());
    target.setVspErrors(mapErrorCodes(source.getVspErrors()));
    target.setLicensingDataErrors(mapErrorCodes(source.getLicensingDataErrors()));
    target.setUploadDataErrors(mapUploadDataErrors(source.getUploadDataErrors()));
    target.setQuestionnaireValidationResult(
        mapQuestionnaireValidationResult(source.getQuestionnaireValidationResult()));
  }
}
