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
package org.openecomp.sdc.enrichment.impl.tosca;

import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.NFC_NAMING_CODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ComponentQuestionnaireData {

    ComponentDao componentDao = ComponentDaoFactory.getInstance().createInterface();
    ComponentDependencyModelDao componentDependencyModelDao = ComponentDependencyModelDaoFactory.getInstance().createInterface();
    private Map<String, String> sourceToTargetComponent;

    public Map<String, String> getSourceToTargetComponent() {
        return sourceToTargetComponent;
    }

    public void setSourceToTargetComponent(Map<String, String> sourceToTargetComponent) {
        this.sourceToTargetComponent = sourceToTargetComponent;
    }

    public Map<String, Map<String, Object>> getPropertiesfromCompQuestionnaire(String key, Version version) {
        Map<String, Map<String, Object>> componentProperties = new HashMap<>();
        final Collection<ComponentEntity> componentEntities = componentDao.listCompositionAndQuestionnaire(key, version);
        Map<String, String> sourceToTarget = new HashMap<>();
        for (ComponentEntity component : componentEntities) {
            Map<String, Object> questionnaireParams = new HashMap<>();
            final ComponentQuestionnaire componentQuestionnaire = JsonUtil
                .json2Object(component.getQuestionnaireData(), ComponentQuestionnaire.class);
            final ComponentData componentData = JsonUtil.json2Object(component.getCompositionData(), ComponentData.class);
            sourceToTarget.put(component.getId(), componentData.getDisplayName());
            String nfcNamingCode = componentQuestionnaire.getGeneral().getNfcNamingCode();
            questionnaireParams.put(NFC_NAMING_CODE, nfcNamingCode);
            String vfcDescription = componentQuestionnaire.getGeneral().getNfcFunction();
            questionnaireParams.put(EnrichmentConstants.NFC_FUNCTION, vfcDescription);
            if (componentQuestionnaire.getHighAvailabilityAndLoadBalancing() != null) {
                String mandatory = componentQuestionnaire.getHighAvailabilityAndLoadBalancing().getIsComponentMandatory();
                questionnaireParams.put(MANDATORY, mandatory);
                String mode = componentQuestionnaire.getHighAvailabilityAndLoadBalancing().getHighAvailabilityMode();
                questionnaireParams.put(HIGH_AVAIL_MODE, mode);
            }
            final Integer maxVms =
                componentQuestionnaire.getCompute() != null ? (componentQuestionnaire.getCompute().getNumOfVMs() != null ? componentQuestionnaire
                    .getCompute().getNumOfVMs().getMaximum() : null) : null;
            final Integer minVms =
                componentQuestionnaire.getCompute() != null ? (componentQuestionnaire.getCompute().getNumOfVMs() != null ? componentQuestionnaire
                    .getCompute().getNumOfVMs().getMinimum() : null) : null;
            questionnaireParams.put(MIN_INSTANCES, minVms != null && minVms == 0 ? null : minVms);
            questionnaireParams.put(MAX_INSTANCES, maxVms != null && maxVms == 0 ? null : maxVms);
            if (!questionnaireParams.isEmpty()) {
                componentProperties
                    .put(JsonUtil.json2Object(component.getCompositionData(), ComponentData.class).getDisplayName(), questionnaireParams);
            }
        }
        setSourceToTargetComponent(sourceToTarget);
        return componentProperties;
    }

    public Map<String, List<String>> populateDependencies(String vspId, Version version, Map<String, String> componentNameData) {
        Collection<ComponentDependencyModelEntity> componentDependencies = componentDependencyModelDao
            .list(new ComponentDependencyModelEntity(vspId, version, null));
        Map<String, List<String>> dependencies = new HashMap<>();
        List<String> targetComponents;
        for (ComponentDependencyModelEntity dependency : componentDependencies) {
            String sourceComponentName = componentNameData.get(dependency.getSourceComponentId());
            String targetComponentName = componentNameData.get(dependency.getTargetComponentId());
            if (!dependencies.containsKey(sourceComponentName)) {
                targetComponents = new ArrayList<>();
            } else {
                targetComponents = dependencies.get(sourceComponentName);
            }
            targetComponents.add(targetComponentName);
            dependencies.put(sourceComponentName, targetComponents);
        }
        return dependencies;
    }
}
