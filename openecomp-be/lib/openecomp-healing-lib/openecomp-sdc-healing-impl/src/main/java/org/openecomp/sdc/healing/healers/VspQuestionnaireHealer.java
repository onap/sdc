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

package org.openecomp.sdc.healing.healers;

import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class VspQuestionnaireHealer implements Healer {
  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public VspQuestionnaireHealer() {
  }

  @Override
  public Object heal(Map<String, Object> healingParams) throws IOException {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<String> questionnaireData = null;
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);

    VspQuestionnaireEntity vspQuestionnaireEntity =
        vspInfoDao.getQuestionnaire(vspId, version);

    if(Objects.isNull(vspQuestionnaireEntity.getQuestionnaireData())) {
      questionnaireData = healQuestionnaire(vspId, version);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return questionnaireData;
  }

  private Optional<String> healQuestionnaire(String vspId, Version version) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String questionnaireData;
    String generatedSchema = SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, null);
    try {
      questionnaireData = new JsonSchemaDataGenerator(generatedSchema).generateData();
      vspInfoDao.updateQuestionnaireData(vspId, version, questionnaireData);
    }catch(Exception e){
      return Optional.empty();
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(questionnaireData);
  }
}
