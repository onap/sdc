package org.openecomp.sdc.enrichment.impl.tosca;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_CODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_FUNCTION;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_NAMING_CODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VM_TYPE_TAG;
import static org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType.NATIVE_NODE;
import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.VFC_ABSTRACT_SUBSTITUTE;
import static org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType.NATIVE_DEPENDS_ON;
import static org.openecomp.sdc.tosca.services.ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.ABSTRACT_NODE_TEMPLATE_ID_PREFIX;

public class AbstractSubstituteToscaEnricher {
  private ToscaAnalyzerService toscaAnalyzerService ;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ComponentQuestionnaireData componentQuestionnaireData;


  public Map<String,List<ErrorMessage>> enrich(ToscaServiceModel toscaModel, String vspId, Version
      version) {

    mdcDataDebugMessage.debugEntryMessage(vspId, version.toString());

    componentQuestionnaireData = getComponentQuestionnaireData();
    toscaAnalyzerService = new ToscaAnalyzerServiceImpl();

    Map<String, Map<String, Object>> componentProperties =
        componentQuestionnaireData.getPropertiesfromCompQuestionnaire(vspId, version);

    final Map<String, List<String>> sourceToTargetDependencies =
        componentQuestionnaireData.populateDependencies(vspId, version,
            componentQuestionnaireData.getSourceToTargetComponent());

    Map<String, List<ErrorMessage>> errors = new HashMap<>();

    final ServiceTemplate serviceTemplate = toscaModel.getServiceTemplates()
        .get(toscaModel.getEntryDefinitionServiceTemplate());

    if (serviceTemplate == null) return errors;

    final Map<String, NodeTemplate> node_templates =
        serviceTemplate.getTopology_template().getNode_templates();
    if(node_templates == null) return errors;

    final Map<String, List<String>> componentDisplayNameToNodeTempalteIds =
        populateAllNodeTemplateIdForComponent(node_templates, serviceTemplate, toscaModel);

    node_templates.keySet()
        .stream()
        .forEach(nodeTemplateId -> {
          final Optional<NodeTemplate> nodeTemplateById =
              toscaAnalyzerService.getNodeTemplateById(serviceTemplate, nodeTemplateId);
          final NodeTemplate nodeTemplate =
              nodeTemplateById.isPresent() ? nodeTemplateById.get() : null;

          if (toscaAnalyzerService.isTypeOf(nodeTemplate, VFC_ABSTRACT_SUBSTITUTE, serviceTemplate,
              toscaModel)) {

            String componentDisplayName = getComponentDisplayName(nodeTemplateId, nodeTemplate);

            setProperty(nodeTemplate, VM_TYPE_TAG, componentDisplayName);

            if (componentProperties != null && componentProperties.containsKey
                (componentDisplayName)) {
              final String mandatory =
                  getValueFromQuestionnaireDetails(componentProperties, componentDisplayName,
                      MANDATORY);

              boolean isServiceTemplateFilterNotExists = false;
              if (!StringUtils.isEmpty(mandatory)) {
                Map innerProps = (Map<String, Object>) nodeTemplate.getProperties()
                    .get(SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);

                if (innerProps == null) {
                  innerProps = new HashMap<String, Object>();
                  isServiceTemplateFilterNotExists = true;
                }

                innerProps.put(MANDATORY, getValue(mandatory));

                if(isServiceTemplateFilterNotExists)
                  nodeTemplate.getProperties().put(SERVICE_TEMPLATE_FILTER_PROPERTY_NAME,
                    innerProps);
              }

              setProperty(nodeTemplate, HIGH_AVAIL_MODE, getValueFromQuestionnaireDetails
                  (componentProperties, componentDisplayName, HIGH_AVAIL_MODE));

              setProperty(nodeTemplate, VFC_NAMING_CODE, getValueFromQuestionnaireDetails
                  (componentProperties, componentDisplayName, VFC_NAMING_CODE));

              setProperty(nodeTemplate, VFC_CODE, getValueFromQuestionnaireDetails
                  (componentProperties, componentDisplayName, VFC_CODE));

              setProperty(nodeTemplate, VFC_FUNCTION, getValueFromQuestionnaireDetails
                  (componentProperties, componentDisplayName, VFC_FUNCTION));

              if(componentProperties.get(componentDisplayName).get(MIN_INSTANCES) != null) {
                nodeTemplate.getProperties().put(MIN_INSTANCES,
                    componentProperties.get(componentDisplayName).get(MIN_INSTANCES));
              }

              if(componentProperties.get(componentDisplayName).get(MAX_INSTANCES) != null) {
                nodeTemplate.getProperties().put(MAX_INSTANCES,
                    componentProperties.get(componentDisplayName).get(MAX_INSTANCES));
              }
            }

            enrichRequirements(sourceToTargetDependencies, componentDisplayName, nodeTemplate,
                componentDisplayNameToNodeTempalteIds);
          }
        });

    mdcDataDebugMessage.debugExitMessage(vspId, version.toString());
    return errors;
  }

  private Map<String,List<String>> populateAllNodeTemplateIdForComponent(Map<String,
      NodeTemplate> node_templates, ServiceTemplate serviceTemplate, ToscaServiceModel
      toscaModel) {


    Map<String,List<String>> componentDisplayNameToNodeTempalteIds = new HashMap<String,
        List<String>>();

    //set dependency target
    node_templates.keySet()
        .stream()
        .forEach(nodeTemplateId -> {

          final Optional<NodeTemplate> nodeTemplateById =
              toscaAnalyzerService.getNodeTemplateById(serviceTemplate, nodeTemplateId);
          final NodeTemplate nodeTemplate =
              nodeTemplateById.isPresent() ? nodeTemplateById.get() : null;

          if (toscaAnalyzerService.isTypeOf(nodeTemplate, VFC_ABSTRACT_SUBSTITUTE, serviceTemplate,
              toscaModel)) {

            String componentDisplayName = getComponentDisplayName(nodeTemplateId, nodeTemplate);

            if (componentDisplayNameToNodeTempalteIds.containsKey(componentDisplayName)) {
              componentDisplayNameToNodeTempalteIds.get(componentDisplayName).add(nodeTemplateId);
            } else {
              List<String> nodeTemplateIds = new ArrayList<String>();
              nodeTemplateIds.add(nodeTemplateId);
              componentDisplayNameToNodeTempalteIds.put(componentDisplayName, nodeTemplateIds);
            }

          }
        });

    return componentDisplayNameToNodeTempalteIds;
  }

  private void enrichRequirements(Map<String, List<String>> sourceToTargetDependencies,
                                  String componentDisplayName, NodeTemplate nodeTemplate,
                                  Map<String, List<String>> componentDisplayNameToNodeTempalteIds) {
    List<Map<String, RequirementAssignment>> requirements =
        nodeTemplate.getRequirements();

    if(requirements == null) {
      requirements = new ArrayList<Map<String, RequirementAssignment>>();
    }

    final List<String> targets = sourceToTargetDependencies.get(componentDisplayName);
    if(targets != null) {
      for (String target : targets) {
        List<String> targetNodeTemplateIds = componentDisplayNameToNodeTempalteIds.get(target);
        if(targetNodeTemplateIds != null) {
          for (String targetNodeTemplateId : targetNodeTemplateIds) {
            Map<String, RequirementAssignment> requirement = new HashMap<String,
                RequirementAssignment>();
            RequirementAssignment requirementAssignment = new RequirementAssignment();
            requirementAssignment.setCapability(NATIVE_NODE);
            requirementAssignment.setRelationship(NATIVE_DEPENDS_ON);
            requirementAssignment.setNode(targetNodeTemplateId);
            requirement.put("dependency", requirementAssignment);
            requirements.add(requirement);
          }
        }
      }
    }

    if (!requirements.isEmpty())
      nodeTemplate.setRequirements(requirements);
  }

  private String getComponentDisplayName(String nodeTemplateId, NodeTemplate nodeTemplate) {
    String componentDisplayName = null;
    if (nodeTemplateId.contains(ABSTRACT_NODE_TEMPLATE_ID_PREFIX)) {
      String removedPrefix = nodeTemplateId.split(ABSTRACT_NODE_TEMPLATE_ID_PREFIX)[1];
      final String[] removedSuffix = removedPrefix.split("_\\d");
      componentDisplayName = removedSuffix[0];
    } else {
      final String type = nodeTemplate.getType();
      final String[] splitted = type.split("\\.");
      componentDisplayName = splitted[splitted.length - 1];

    }
    return componentDisplayName;
  }

  private String getValueFromQuestionnaireDetails(
      Map<String, Map<String, Object>> componentTypetoParams, String componentDisplayName, String
      propertyName) {
    return (String) componentTypetoParams.get(componentDisplayName).get(propertyName);
  }

  private void setProperty(NodeTemplate nodeTemplate, String key, String value) {
    if (!StringUtils.isEmpty(value)) {
      //YamlUtil throws IllegalStateException("duplicate key: " + key) if key is already present.
      // So first removing and then populating same key with new updated value
      nodeTemplate.getProperties().remove(key);
      nodeTemplate.getProperties().put(key, value);
    }
  }

  private Boolean getValue(String value) {
    String returnValue = null;
    switch (value) {
      case "YES" :
        return true;
      case "NO" :
          return false;
      default: return null;
    }
  }

  private ComponentQuestionnaireData getComponentQuestionnaireData() {
    if (componentQuestionnaireData == null) {
      componentQuestionnaireData = new ComponentQuestionnaireData();
    }
    return componentQuestionnaireData;
  }
}
