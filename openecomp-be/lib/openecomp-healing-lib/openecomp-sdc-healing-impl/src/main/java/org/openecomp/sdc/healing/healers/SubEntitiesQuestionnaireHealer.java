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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class SubEntitiesQuestionnaireHealer implements Healer {
  private static Version version00 = new Version(0, 0);
  private MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static ComponentDao componentDao = ComponentDaoFactory.getInstance().createInterface();
  private static NicDao nicDao = NicDaoFactory.getInstance().createInterface();
  private static NetworkDao networkDao = NetworkDaoFactory.getInstance().createInterface();

  private static String emptyString = "";
  private static String emptyJson = "{}";

  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {


    mdcDataDebugMessage.debugEntryMessage(null);

    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = version00.equals(healingParams.get(SdcCommon.VERSION)) ? new Version
        (0, 1)
        : (Version) healingParams.get(SdcCommon.VERSION);

    Collection<ComponentEntity> componentEntities =
        componentDao.listCompositionAndQuestionnaire(vspId, version);

    networkDao.list(new NetworkEntity(vspId, version, null));

    Collection<NicEntity> nicEntities = vendorSoftwareProductDao.listNicsByVsp(vspId, version);

    healCompositionEntityQuestionnaire(componentEntities, version, CompositionEntityType.component);
    healCompositionEntityQuestionnaire(nicEntities, version, CompositionEntityType.nic);

    mdcDataDebugMessage.debugExitMessage(null);
    return new Object();
  }


  private void healCompositionEntityQuestionnaire(Collection
                                                      compositionEntities,
                                                  Version newVersion, CompositionEntityType type) {


    mdcDataDebugMessage.debugEntryMessage(null);

    for (Object entity : compositionEntities) {
      CompositionEntity compositionEntity = (CompositionEntity) entity;
      if (isQuestionnaireNeedsToGetHealed(compositionEntity)) {
        compositionEntity.setVersion(newVersion);
        updateNullQuestionnaire(compositionEntity, type);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private boolean isQuestionnaireNeedsToGetHealed(CompositionEntity compositionEntity) {
    return Objects.isNull(compositionEntity.getQuestionnaireData())
        || emptyString.equals(compositionEntity.getQuestionnaireData())
        || emptyJson.equals(compositionEntity.getQuestionnaireData());
  }

  private void updateNullQuestionnaire(CompositionEntity entity,
                                       CompositionEntityType type) {


    mdcDataDebugMessage.debugEntryMessage(null);

    entity.setQuestionnaireData(
        new JsonSchemaDataGenerator(SchemaGenerator
            .generate(SchemaTemplateContext.questionnaire, type,
                null)).generateData());

    switch (type) {
      case component:
        ComponentEntity component = (ComponentEntity) entity;
        componentDao.updateQuestionnaireData(component.getVspId(), component
            .getVersion(), component.getId(), component.getQuestionnaireData());
        break;

      case nic:
        NicEntity nic = (NicEntity) entity;
        nicDao.updateQuestionnaireData(nic.getVspId(), nic.getVersion(), nic.getComponentId(),
            nic.getId(), nic.getQuestionnaireData());
        break;
    }
    mdcDataDebugMessage.debugExitMessage(null);
  }

}
