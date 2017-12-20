package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.utils.ResourceWalker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Avrahamg
 * @since April 03, 2017
 */
public class GlobalTypesServiceTemplates {
  private static Map<String, ServiceTemplate> globalTypesServiceTemplates;


  public static Map<String, ServiceTemplate> getGlobalTypesServiceTemplates() {
    if (globalTypesServiceTemplates == null) {
      synchronized (GlobalTypesServiceTemplates.class) {
        if (globalTypesServiceTemplates == null) {
          init();
        }
      }
    }
    return globalTypesServiceTemplates;
  }

  private static void init() {
    globalTypesServiceTemplates = new HashMap<>();
    Map<String, String> globalTypes = null;
    try {
      globalTypes = ResourceWalker.readResourcesFromDirectory("globalTypes");
    } catch (CoreException coreException) {
      throw coreException;
    } catch (Exception exception) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(LoggerErrorDescription.FAILED_TO_GENERATE_GLOBAL_TYPES)
          .withId("GlobalTypes Read Error").withCategory(ErrorCategory.APPLICATION).build(),
          exception);
    }
    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
    for (Map.Entry<String, String> globalTypeContent : globalTypes.entrySet()) {
      if (globalTypeContent.getKey().contains("openecomp-inventory")) { // this global types folder
        // should not be
        // processed to the CSAR
        continue;
      }
      ToscaUtil.addServiceTemplateToMapWithKeyFileName(globalTypesServiceTemplates,
          toscaExtensionYamlUtil.yamlToObject(globalTypeContent.getValue(), ServiceTemplate.class));
    }
  }

  private GlobalTypesServiceTemplates() {
  }
}
