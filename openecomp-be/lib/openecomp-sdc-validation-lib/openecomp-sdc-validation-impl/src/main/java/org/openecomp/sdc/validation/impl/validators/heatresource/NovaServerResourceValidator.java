package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.PropertiesMapKeyTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

import java.util.Map;

/**
 * Created by TALIO on 2/22/2017.
 */
public class NovaServerResourceValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final ErrorMessageCode ERROR_CODE_HNS1 = new ErrorMessageCode("HNS1");
  private static final ErrorMessageCode ERROR_CODE_HNS2 = new ErrorMessageCode("HNS2");

  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {

    HeatResourceValidationContext heatResourceValidationContext = (HeatResourceValidationContext)
            validationContext;
    validateNovaServerResourceType
            (fileName, resourceEntry, heatResourceValidationContext, globalContext);
  }

  private static void validateNovaServerResourceType(String fileName,
                                                     Map.Entry<String, Resource> resourceEntry,
                                                     HeatResourceValidationContext heatResourceValidationContext,
                                                     GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    validateAssignedValueForImageOrFlavorFromNova(fileName, resourceEntry, globalContext);
    validateAllServerGroupsPointedByServerExistAndDefined
            (fileName, resourceEntry,
                    heatResourceValidationContext.getHeatOrchestrationTemplate(), globalContext);

    mdcDataDebugMessage.debugExitMessage("file", fileName);

  }

  private static void validateAssignedValueForImageOrFlavorFromNova(String fileName,
                                                                    Map.Entry<String, Resource>
                                                                            resourceEntry,
                                                                    GlobalValidationContext
                                                                            globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Resource resource = resourceEntry.getValue();
    Map<String, Object> propertiesMap = resource.getProperties();
    if (propertiesMap.get(PropertiesMapKeyTypes.IMAGE.getKeyMap()) == null
            && propertiesMap.get(PropertiesMapKeyTypes.FLAVOR.getKeyMap()) == null) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(ERROR_CODE_HNS1, Messages.MISSING_IMAGE_AND_FLAVOR.getErrorMessage(),
                              resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_ASSIGNED_VALUES_FOR_NOVA_IMAGE_FLAVOR,
              LoggerErrorDescription.MISSING_NOVA_PROPERTIES);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  @SuppressWarnings("unchecked")
  private static void validateAllServerGroupsPointedByServerExistAndDefined(String fileName,
                                                                            Map.Entry<String, Resource> resourceEntry,
                                                                            HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                                            GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
    Map<String, Object> resourceProperties = resourceEntry.getValue().getProperties();
    Map<String, Object> schedulerHintsMap =
            resourceProperties == null ? null : (Map<String, Object>) resourceProperties.get(
                    ResourceReferenceFunctions.SCHEDULER_HINTS.getFunction());

    if (MapUtils.isEmpty(schedulerHintsMap)) {
      return;
    }

    if (schedulerHintsMap != null) {
      for (Object serverGroupValue : schedulerHintsMap.values()) {
        if (!(serverGroupValue instanceof Map)) {
          continue;
        }
        Map<String, Object> currentServerMap = (Map<String, Object>) serverGroupValue;
        String serverResourceName = (String) currentServerMap
                .get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
        Resource serverResource =
                serverResourceName == null || resourcesMap == null ? null
                        : resourcesMap.get(serverResourceName);

        if (serverResource != null && !serverResource.getType()
                .equals(HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource())) {
          globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                          .getErrorWithParameters(ERROR_CODE_HNS2, Messages.SERVER_NOT_DEFINED_FROM_NOVA.getErrorMessage(),
                                  serverResourceName, resourceEntry.getKey()),
                  LoggerTragetServiceName.VALIDATE_SERVER_GROUP_EXISTENCE,
                  LoggerErrorDescription.SERVER_NOT_DEFINED_NOVA);
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

}
