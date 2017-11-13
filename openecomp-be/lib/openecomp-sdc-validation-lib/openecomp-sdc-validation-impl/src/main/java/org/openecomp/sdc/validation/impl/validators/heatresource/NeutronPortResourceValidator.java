package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by TALIO on 2/22/2017.
 */
public class NeutronPortResourceValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {

    validateNovaServerPortBinding
        (fileName, resourceEntry, (HeatResourceValidationContext) validationContext, globalContext);
  }


  @SuppressWarnings("unchecked")
  private static void validateNovaServerPortBinding(String fileName,
                                                    Map.Entry<String, Resource> resourceEntry,
                                                    HeatResourceValidationContext heatResourceValidationContext,
                                                    GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Map<String, Map<String, List<String>>> portIdToPointingResources =
        heatResourceValidationContext.getFileLevelResourceDependencies()
            .get(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());

    if (MapUtils.isEmpty(portIdToPointingResources)
        || MapUtils.isEmpty(portIdToPointingResources.get(resourceEntry.getKey()))) {
      globalContext
          .addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(
                      Messages.PORT_NO_BIND_TO_ANY_NOVA_SERVER.getErrorMessage(),
                      resourceEntry.getKey()), LoggerTragetServiceName.CHECK_FOR_ORPHAN_PORTS,
              LoggerErrorDescription.NO_BIND_FROM_PORT_TO_NOVA);

      return;
    }

    Map<String, List<String>> pointingResourcesToCurrPort =
        portIdToPointingResources.get(resourceEntry.getKey());
    checkPortBindingFromMap(
        fileName, resourceEntry.getKey(), pointingResourcesToCurrPort, globalContext);

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void checkPortBindingFromMap(String fileName,
                                              String portResourceId,
                                              Map<String, List<String>> resourcesPointingToCurrPort,
                                              GlobalValidationContext globalContext) {
    List<String> pointingNovaServers = resourcesPointingToCurrPort
        .get(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource());

    if (CollectionUtils.isEmpty(pointingNovaServers)) {
      return;
    }

    handleErrorEventsForPortBinding(
        fileName, portResourceId, globalContext, pointingNovaServers);


  }

  private static void handleErrorEventsForPortBinding(String fileName,
                                                      String portResourceId,
                                                      GlobalValidationContext globalContext,
                                                      List<String> pointingNovaServers) {
    if (isThereMoreThanOneBindFromNovaToPort(pointingNovaServers)) {
      globalContext
          .addMessage(fileName, ErrorLevel.ERROR,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(
                      Messages.MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT.getErrorMessage(),
                      portResourceId),
              LoggerTragetServiceName.VALIDATE_NOVA_SERVER_PORT_BINDING,
              LoggerErrorDescription.PORT_BINDS_MORE_THAN_ONE_NOVA);
    }

    if (isNoNovaPointingToPort(pointingNovaServers)) {
      globalContext
          .addMessage(fileName, ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(
                      Messages.PORT_NO_BIND_TO_ANY_NOVA_SERVER.getErrorMessage(),
                      portResourceId), LoggerTragetServiceName.CHECK_FOR_ORPHAN_PORTS,
              LoggerErrorDescription.NO_BIND_FROM_PORT_TO_NOVA);
    }
  }

  private static boolean isNoNovaPointingToPort(List<String> pointingNovaServers) {
    return pointingNovaServers.size() == 0;
  }

  private static boolean isThereMoreThanOneBindFromNovaToPort(List<String> pointingNovaServers) {
    return pointingNovaServers.size() > 1;
  }

  @SuppressWarnings("unchecked")
  private static void validateAllSecurityGroupsAreUsed(String filename,
                                                       Map.Entry<String, Resource> resourceEntry,
                                                       List<String> securityGroupResourceNameList,
                                                       GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", filename);

    Map<String, Object> propertiesMap = resourceEntry.getValue().getProperties();

    if (MapUtils.isEmpty(propertiesMap)) {
      return;
    }

    Object securityGroupsValue = propertiesMap.get("security_groups");

    if (Objects.isNull(securityGroupsValue)) {
      return;
    }

    if (securityGroupsValue instanceof List) {
      List<Object> securityGroupsListFromCurrResource =
          (List<Object>) propertiesMap.get("security_groups");
      for (Object securityGroup : securityGroupsListFromCurrResource) {
        removeSecurityGroupNamesFromListByGivenFunction(filename,
            ResourceReferenceFunctions.GET_RESOURCE.getFunction(), securityGroup,
            securityGroupResourceNameList, globalContext);
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", filename);
  }

  private static void removeSecurityGroupNamesFromListByGivenFunction(String filename,
                                                                      String functionName,
                                                                      Object securityGroup,
                                                                      Collection<String>
                                                                          securityGroupResourceNameList,
                                                                      GlobalValidationContext globalContext) {
    Set<String> securityGroupsNamesFromFunction = HeatStructureUtil
        .getReferencedValuesByFunctionName(filename, functionName, securityGroup, globalContext);
    securityGroupsNamesFromFunction.forEach(securityGroupResourceNameList::remove);
  }
}
