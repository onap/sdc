package org.openecomp.sdc.validation.util;


import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class ValidationUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class.getName());

  private ValidationUtil(){}

  public static void removeExposedResourcesCalledByGetResource(String fileName,
                                                               Set<String> actualExposedResources,
                                                               HeatOrchestrationTemplate
                                                                   heatOrchestrationTemplate,
                                                               GlobalValidationContext globalContext) {
    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();

    for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
      Set<String> referencedResources =
          HeatStructureUtil.getReferencedValuesByFunctionName(fileName, ResourceReferenceFunctions
              .GET_RESOURCE
              .getFunction(), resourceEntry.getValue().getProperties(), globalContext);

      removeExposedResourcesCalledByGetResource(referencedResources, actualExposedResources,
          resourcesMap);
    }
  }

  private static void removeExposedResourcesCalledByGetResource(Set<String> referencedResources,
                                                                Set<String>
                                                                    actualExposedResources,
                                                                Map<String, Resource> resourcesMap) {
    for (String referencedResourceName : referencedResources) {
      Resource currResource = resourcesMap.get(referencedResourceName);
      if (Objects.nonNull(currResource) && isExpectedToBeExposed(currResource.getType())) {
          actualExposedResources.add(referencedResourceName);
      }
    }
  }

  public static boolean isExpectedToBeExposed(String type) {
    return HeatResourcesTypes.isResourceExpectedToBeExposed(type);
  }

  public static String getWantedNameFromPropertyValueGetParam(Object value) {
    Set<String> paramName = HeatStructureUtil
        .getReferencedValuesByFunctionName(null, ResourceReferenceFunctions.GET_PARAM.getFunction(),
            value, null);
    if (paramName != null && CollectionUtils.isNotEmpty(paramName)) {
      return (String) paramName.toArray()[0];
    }
    return null;
  }

  public static boolean evalPattern(Object paramVal, String[] regexList) {
    String value = "";
    if (paramVal instanceof String) {
      value = (String) paramVal;
    }
    if (paramVal instanceof Integer) {
      value = paramVal.toString();
    }
    return evalPattern(value, regexList);
  }

  private static boolean evalPattern(String paramVal, String[] regexList) {

    for (String regex : regexList) {
      if (Pattern.matches(regex, paramVal)) {
        return true;
      }
    }

    return false;
  }

  public static String getMessagePartAccordingToResourceType(Map.Entry<String, Resource>
                                                             resourceEntry) {
    HeatResourcesTypes resourcesType =
        HeatResourcesTypes.findByHeatResource(resourceEntry.getValue().getType());
    if (resourcesType == HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE) {
      return "Server";
    } else if (resourcesType == HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE) {
      return "Service Template";
    } else if (resourcesType == HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE) {
      return "Service Instance";
    } else {
      return "";
    }
  }

  public static Environment validateEnvContent(String envFileName,
                                         GlobalValidationContext globalContext) {
    Environment envContent;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(envFileName);
      if (fileContent.isPresent()) {
        envContent = new YamlUtil().yamlToObject(fileContent.get(), Environment.class);
      } else {
        throw new Exception("The file '" + envFileName + "' has no content");
      }
    } catch (Exception exception) {
      LOG.debug("",exception);
      return null;
    }
    return envContent;
  }

  public static boolean validateMapPropertyValue(String fileName,
                                           Map.Entry<String, Resource> resourceEntry,
                                           GlobalValidationContext globalContext,
                                           String propertyName, Object nameValue,
                                           String[] regexList) {
    String propertyValue = getWantedNameFromPropertyValueGetParam(nameValue);
    if (nonNull(propertyValue) && !evalPattern(propertyValue, regexList)) {
        globalContext.addMessage(
            fileName,
            ErrorLevel.WARNING,
            ErrorMessagesFormatBuilder.getErrorWithParameters(globalContext.getMessageCode(),
                Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                getMessagePartAccordingToResourceType(resourceEntry), propertyName, propertyValue,
                resourceEntry.getKey()),
            LoggerTragetServiceName.VALIDATE_IMAGE_AND_FLAVOR_NAME,
            LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
        return true;
      }
    return false;
  }

  public static ManifestContent validateManifest(GlobalValidationContext globalContext) {
    Optional<InputStream> manifest = globalContext.getFileContent(SdcCommon.MANIFEST_NAME);
    if (!manifest.isPresent()) {
      throw new RuntimeException("Can't load manifest file for Heat Validator");
    }
    ManifestContent manifestContent;
    try {
      manifestContent = JsonUtil.json2Object(manifest.get(), ManifestContent.class);
    } catch (Exception exception) {
      LOG.debug("",exception);
      throw new SdcRuntimeException("Can't load manifest file for Heat Validator");
    }

    return manifestContent;
  }

  public static String getParserExceptionReason(Exception exception) {
    String reason;

    if (exception.getCause() != null && exception.getCause().getCause() != null) {
      reason = exception.getCause().getCause().getMessage();
    } else if (exception.getCause() != null) {
      reason = exception.getCause().getMessage();
    } else {
      reason = Messages.GENERAL_HEAT_PARSER_ERROR.getErrorMessage();
    }
    return reason;
  }

  public static HeatOrchestrationTemplate checkHeatOrchestrationPreCondition(String fileName,
                                                                         GlobalValidationContext globalContext) {
    HeatOrchestrationTemplate heatOrchestrationTemplate;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(fileName);
      if (fileContent.isPresent()) {
        heatOrchestrationTemplate =
            new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
      } else {
        heatOrchestrationTemplate = null;
      }
    } catch (Exception exception) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(globalContext.getMessageCode(),
                      Messages.INVALID_HEAT_FORMAT_REASON.getErrorMessage()
                      , getParserExceptionReason(exception)),
          LoggerTragetServiceName.VALIDATE_HEAT_FORMAT,
          LoggerErrorDescription.INVALID_HEAT_FORMAT);
      return null;
    }
    return heatOrchestrationTemplate;
  }


}
