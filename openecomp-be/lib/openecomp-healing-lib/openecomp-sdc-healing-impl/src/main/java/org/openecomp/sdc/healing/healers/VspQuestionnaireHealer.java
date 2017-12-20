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
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class VspQuestionnaireHealer implements Healer {
  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public VspQuestionnaireHealer() {
  }

  @Override
  public Object heal(String vspId, Version version) throws IOException {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<String> questionnaireData = null;
    VspQuestionnaireEntity vspQuestionnaireEntity =
        vspInfoDao.getQuestionnaire(vspId, version);

    if(Objects.isNull(vspQuestionnaireEntity.getQuestionnaireData())|| "".equals(vspQuestionnaireEntity.getQuestionnaireData())) {
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
      log.debug("", e);
      return Optional.empty();
    }
    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(questionnaireData);
  }
}
