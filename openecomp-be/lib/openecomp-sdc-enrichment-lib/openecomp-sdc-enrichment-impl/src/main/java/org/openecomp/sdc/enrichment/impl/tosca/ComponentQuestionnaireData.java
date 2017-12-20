package org.openecomp.sdc.enrichment.impl.tosca;


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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_NAMING_CODE;


public class ComponentQuestionnaireData {

  ComponentDao componentDao = ComponentDaoFactory.getInstance().createInterface();
  ComponentDependencyModelDao componentDependencyModelDao =
      ComponentDependencyModelDaoFactory.getInstance().createInterface();

  private Map<String, String> sourceToTargetComponent;

  public Map<String, String> getSourceToTargetComponent() {
    return sourceToTargetComponent;
  }

  public void setSourceToTargetComponent(Map<String, String> sourceToTargetComponent) {
    this.sourceToTargetComponent = sourceToTargetComponent;
  }

  public Map<String, Map<String, Object>> getPropertiesfromCompQuestionnaire(String key,
                                                                             Version version) {
    Map<String, Map<String, Object>> componentProperties =
        new HashMap<String, Map<String, Object>>();

    ComponentEntity entity = new ComponentEntity(key, version, null);
    final Collection<ComponentEntity> componentEntities =
        componentDao.listCompositionAndQuestionnaire(key, version);

    Map<String, String> sourceToTarget = new HashMap<>();

    for (ComponentEntity component : componentEntities) {
      Map<String, Object> questionnaireParams = new HashMap<>();

      final ComponentQuestionnaire componentQuestionnaire =
          JsonUtil.json2Object(component.getQuestionnaireData(), ComponentQuestionnaire.class);

      final ComponentData componentData =
          JsonUtil.json2Object(component.getCompositionData(), ComponentData.class);

      sourceToTarget.put(component.getId(), componentData.getDisplayName());

      String vfc_code = componentData != null ? componentData.getVfcCode() : null;
      questionnaireParams.put(VFC_NAMING_CODE, vfc_code);

      String nfcCode = componentData.getNfcCode() != null ? componentData.getNfcCode() : null;
      questionnaireParams.put(EnrichmentConstants.VFC_CODE, nfcCode);

      String vfcDescription =
          componentData.getNfcFunction() != null ? componentData.getNfcFunction() :
              null;
      questionnaireParams.put(EnrichmentConstants.VFC_FUNCTION, vfcDescription);


      if (componentQuestionnaire.getHighAvailabilityAndLoadBalancing() != null) {
        String mandatory = componentQuestionnaire.getHighAvailabilityAndLoadBalancing()
            .getIsComponentMandatory();
        questionnaireParams.put(MANDATORY, mandatory);

        String mode = componentQuestionnaire.getHighAvailabilityAndLoadBalancing()
            .getHighAvailabilityMode();

        questionnaireParams.put(HIGH_AVAIL_MODE, mode);
      }

      final Integer maxVms =
          componentQuestionnaire.getCompute() != null ? (componentQuestionnaire.getCompute()
              .getNumOfVMs() != null ? componentQuestionnaire.getCompute().getNumOfVMs()
              .getMaximum() : null) : null;

      final Integer minVms =
          componentQuestionnaire.getCompute() != null ? (componentQuestionnaire.getCompute()
              .getNumOfVMs() != null ? componentQuestionnaire.getCompute().getNumOfVMs()
              .getMinimum() : null) : null;

      questionnaireParams.put(MIN_INSTANCES, minVms != null && minVms == 0 ? null : minVms);
      questionnaireParams.put(MAX_INSTANCES, maxVms != null && maxVms == 0 ? null : maxVms);

      if (!questionnaireParams.isEmpty()) {
        componentProperties.put(JsonUtil.json2Object(component.getCompositionData(),
            ComponentData.class).getDisplayName(), questionnaireParams);
      }
    }

    setSourceToTargetComponent(sourceToTarget);

    return componentProperties;
  }

  public Map<String, List<String>> populateDependencies(String vspId, Version version, Map<String,
      String> componentNameData) {
    Collection<ComponentDependencyModelEntity> componentDependencies =
        componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId, version, null));

    Map<String, List<String>> sourceToTargetComponent = new HashMap<String, List<String>>();
    List<String> targetComponents = null;
    for (ComponentDependencyModelEntity dependency : componentDependencies) {
      String sourceComponentName = componentNameData.get(dependency.getSourceComponentId());
      String targetComponentName = componentNameData.get(dependency.getTargetComponentId());
      if (!sourceToTargetComponent.containsKey(sourceComponentName)) {
        targetComponents = new ArrayList<String>();
      } else {
        targetComponents = sourceToTargetComponent.get(sourceComponentName);
      }
      targetComponents.add(targetComponentName);
      sourceToTargetComponent.put(sourceComponentName, targetComponents);
    }

    return sourceToTargetComponent;
  }

}
