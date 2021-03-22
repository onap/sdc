/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorsoftwareproduct.quiestionnaire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactData;
import org.openecomp.sdc.vendorsoftwareproduct.questionnaire.QuestionnaireDataService;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.NicQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.VspQuestionnaire;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * Created by TALIO on 11/22/2016
 */
public class QuestionnaireDataServiceImpl implements QuestionnaireDataService {

    private static final ComponentDao componentDao = ComponentDaoFactory.getInstance().createInterface();
    private static final NicDao nicDao = NicDaoFactory.getInstance().createInterface();
    private static final VendorSoftwareProductInfoDao vspInfoDao = VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();

    @Override
    public InformationArtifactData generateQuestionnaireDataForInformationArtifact(String vspId, Version version) {
        VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
        Collection<ComponentEntity> componentEntities = componentDao.listQuestionnaires(vspId, version);
        Collection<NicEntity> nicEntities = nicDao.listByVsp(vspId, version);
        VspQuestionnaire vspQuestionnaire = getVspQuestionnaireFromJson(vspId, version);
        List<ComponentQuestionnaire> componentQuestionnaireList = getListOfComponentQuestionnaireFromJson(componentEntities);
        List<NicQuestionnaire> nicQuestionnaireList = getListOfNicQuestionnaireFromJson(nicEntities);
        return new InformationArtifactData(vspDetails, vspQuestionnaire, componentQuestionnaireList, nicQuestionnaireList);
    }

    private VspQuestionnaire getVspQuestionnaireFromJson(String vspId, Version version) {
        VspQuestionnaireEntity vspQuestionnaireEntity = vspInfoDao.getQuestionnaire(vspId, version);
        if (vspQuestionnaireEntity == null) {
            return null;
        }
        return JsonUtil.json2Object(vspQuestionnaireEntity.getQuestionnaireData(), VspQuestionnaire.class);
    }

    private List<ComponentQuestionnaire> getListOfComponentQuestionnaireFromJson(Collection<ComponentEntity> entities) {
        List<ComponentQuestionnaire> componentQuestionnaireList = new ArrayList<>();
        for (CompositionEntity componentEntity : entities) {
            componentQuestionnaireList.add(JsonUtil.json2Object(componentEntity.getQuestionnaireData(), ComponentQuestionnaire.class));
        }
        return componentQuestionnaireList;
    }

    private List<NicQuestionnaire> getListOfNicQuestionnaireFromJson(Collection<NicEntity> entities) {
        List<NicQuestionnaire> nicQuestionnaireList = new ArrayList<>();
        for (NicEntity nicEntity : entities) {
            nicQuestionnaireList.add(JsonUtil.json2Object(nicEntity.getQuestionnaireData(), NicQuestionnaire.class));
        }
        return nicQuestionnaireList;
    }
}
