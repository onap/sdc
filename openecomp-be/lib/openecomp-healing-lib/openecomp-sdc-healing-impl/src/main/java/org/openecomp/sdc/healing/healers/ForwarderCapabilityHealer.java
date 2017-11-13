package org.openecomp.sdc.healing.healers;

import org.apache.commons.collections.MapUtils;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesServiceTemplates;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ForwarderCapabilityHealer implements Healer {

  private MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
  private static final String forwarderCapabilityId = "Forwarder";
  private static final String underscore = "_";

  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);

    if(!ToggleableFeature.FORWARDER_CAPABILITY.isActive()) {
      return Optional.empty();
    }

    ToscaServiceModel serviceModel =
        serviceModelDao.getServiceModel(vspId, version);

    if (Objects.isNull(serviceModel)
        || MapUtils.isEmpty(serviceModel.getServiceTemplates())) {
      return Optional.empty();
    }

    addForwarderCapabilityToServiceModel(serviceModel);
    serviceModelDao.deleteAll(vspId, version);
    serviceModelDao.storeServiceModel(vspId, version, serviceModel);

    return Optional.of(serviceModel);
  }

  private void addForwarderCapabilityToServiceModel(ToscaServiceModel serviceModel) {
    serviceModel.getServiceTemplates().entrySet().stream().filter(serviceTemplateEntry -> Objects
        .nonNull(serviceTemplateEntry.getValue()))
        .forEach(serviceTemplateEntry -> handleServiceTemplate(serviceTemplateEntry.getValue(),
            serviceModel));

    handleGlobalTypes(serviceModel);
  }

  private void handleGlobalTypes(ToscaServiceModel serviceModel) {
    Map<String, ServiceTemplate> globalTypesServiceTemplates =
        GlobalTypesServiceTemplates.getGlobalTypesServiceTemplates();

    if (MapUtils.isEmpty(globalTypesServiceTemplates)) {
      return;
    }

    globalTypesServiceTemplates.entrySet()
        .stream()
        .filter(globalTypesServiceTemplateEntry -> Objects.nonNull
            (globalTypesServiceTemplateEntry.getValue()))
        .forEach(globalTypesServiceTemplateEntry -> serviceModel.addServiceTemplate
            (globalTypesServiceTemplateEntry.getKey(), globalTypesServiceTemplateEntry.getValue()));
  }

  private void handleServiceTemplate(ServiceTemplate serviceTemplate,
                                     ToscaServiceModel toscaServiceModel) {
    if (Objects.isNull(serviceTemplate.getTopology_template())
        || MapUtils.isEmpty(serviceTemplate.getTopology_template().getNode_templates())) {
      return;
    }

    Map<String, NodeTemplate> nodeTemplates =
        serviceTemplate.getTopology_template().getNode_templates();

    nodeTemplates.entrySet().stream()
        .filter(nodeTemplateEntry ->
            isTypeOfPort(serviceTemplate, toscaServiceModel, nodeTemplateEntry))
        .forEach(nodeTemplateEntry ->
            handleSubstitutionMappings(nodeTemplateEntry.getKey(), serviceTemplate)
        );
  }

  private boolean isTypeOfPort(ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel,
                               Map.Entry<String, NodeTemplate> nodeTemplateEntry) {
    return toscaAnalyzerService.isTypeOf(nodeTemplateEntry.getValue(),
        ToscaNodeType.NATIVE_NETWORK_PORT, serviceTemplate, toscaServiceModel);
  }

  private void handleSubstitutionMappings(String portNodeTemplateId,
                                          ServiceTemplate serviceTemplate) {
    if (Objects.isNull(serviceTemplate.getTopology_template())
        || Objects.isNull(serviceTemplate.getTopology_template().getSubstitution_mappings())) {
      return;
    }

    List<String> substitutionMappingCapabilityList =
        Arrays.asList(portNodeTemplateId, forwarderCapabilityId);

    DataModelUtil.addSubstitutionMappingCapability(
        serviceTemplate,
        forwarderCapabilityId + underscore + portNodeTemplateId,
        substitutionMappingCapabilityList);

  }
}
