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
 */

package org.openecomp.sdc.healing.healers;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Objects;

public class ComponentDataHealer implements Healer {

    private static final String VFC_CODE = "vfcCode"; //earlier present in composition data
    private static final String NFC_FUNCTION = "nfcFunction";
    private  static final String NFC_NAMING_CODE = "nfcNamingCode";
    private static final String GENERAL = "general";
    private final ComponentDao componentDao;

    public ComponentDataHealer() {
        this.componentDao = ComponentDaoFactory.getInstance().createInterface();
    }

    @VisibleForTesting
    ComponentDataHealer(ComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    @Override
    public boolean isHealingNeeded(String itemId, Version version) {
        final Collection<ComponentEntity> componentEntities =
                componentDao.listCompositionAndQuestionnaire(itemId, version);
        return Objects.nonNull(componentEntities) && !componentEntities.isEmpty() &&
                       componentEntities.stream().anyMatch(this::checkNfcParams);
    }

    private boolean checkNfcParams(ComponentEntity componentEntity) {
        final String compositionData = componentEntity.getCompositionData();
        if (!StringUtils.isEmpty(compositionData)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = (JsonObject) jsonParser.parse(compositionData);
            return Objects.nonNull(json.get(VFC_CODE)) || Objects.nonNull(json.get(NFC_FUNCTION));
        }
        return false;
    }

    @Override
    public void heal(String itemId, Version version) throws Exception {
        final Collection<ComponentEntity> componentEntities =
                componentDao.listCompositionAndQuestionnaire(itemId, version);
        if (Objects.nonNull(componentEntities) && !componentEntities.isEmpty()) {
            componentEntities.forEach(componentEntity -> {
                final String compositionData = componentEntity.getCompositionData();
                updateComponentData(itemId, version, componentEntity, componentEntity.getQuestionnaireData(), compositionData);
            });
        }
    }

    private void updateComponentData(String itemId, Version version, ComponentEntity componentEntity,
                                            String questionnaireData, String compositionData) {
        if (!StringUtils.isEmpty(compositionData)) {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = (JsonObject) jsonParser.parse(compositionData);
            JsonObject questionnaireJson = (JsonObject) jsonParser.parse(questionnaireData);
            moveAttribute(json, questionnaireJson, questionnaireJson.getAsJsonObject(GENERAL), VFC_CODE,
                    NFC_NAMING_CODE);
            moveAttribute(json, questionnaireJson, questionnaireJson.getAsJsonObject(GENERAL), NFC_FUNCTION,
                    NFC_FUNCTION);
            componentEntity.setCompositionData(json.toString());
            componentDao.update(componentEntity);
            componentEntity.setQuestionnaireData(questionnaireJson.toString());
            componentDao.updateQuestionnaireData(itemId,version,componentEntity.getId(), questionnaireJson.toString());
        }
    }

    private static void moveAttribute(JsonObject compositionJsonObj, JsonObject questJsonObject,
                                      JsonObject general, String compositionAttrName, String questAttrName ) {
        if (Objects.nonNull(compositionJsonObj.get(compositionAttrName))) {
            if (general == null) {
                general = new JsonObject();
            }
            general.addProperty(questAttrName, compositionJsonObj.get(compositionAttrName).getAsString());
            questJsonObject.add(GENERAL, general);
            compositionJsonObj.remove(compositionAttrName);
        }
    }
}
