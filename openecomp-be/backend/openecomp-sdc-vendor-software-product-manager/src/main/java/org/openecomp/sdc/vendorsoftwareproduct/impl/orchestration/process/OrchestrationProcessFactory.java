package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.vendorsoftwareproduct.types.ConfigConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_CONFIG_NAMESPACE;
public class OrchestrationProcessFactory {

  private static final String confFile = "config-orchestration.json";
  private static Map<String, ImplementationConfiguration> processImplMap;

  static {
    Configuration config = ConfigurationManager.lookup();
    processImplMap = new ConcurrentHashMap<>(config.populateMap(ORCHESTRATION_CONFIG_NAMESPACE,
        ConfigConstants.PROCESS_IMPL_KEY, ImplementationConfiguration.class));

  }

  public static Optional<OrchestrationTemplateProcessHandler> getInstance(String filePrefix) {
    if (filePrefix == null) {
      return Optional.empty();
    }

    filePrefix = filePrefix.toLowerCase().trim();
    OnboardingTypesEnum onboardingTypesEnum = OnboardingTypesEnum.getOnboardingTypesEnum(filePrefix);
    if (onboardingTypesEnum == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(createInstance(processImplMap.get(onboardingTypesEnum.toString())));
    }catch (Exception e){
      return Optional.empty();
    }
  }

  private static Map<String, String> getOrchestrationImplMap(){
    try {
      return FileUtils.readViaInputStream(confFile,
              stream -> JsonUtil.json2Object(stream, Map.class));
    }catch (Exception e){
      return new HashMap<>();
    }
  }

  private static OrchestrationTemplateProcessHandler createInstance(ImplementationConfiguration implClass)
      throws Exception {
    OrchestrationTemplateProcessHandler handler;
    handler =
        CommonMethods.newInstance(implClass.getImplementationClass(), OrchestrationTemplateProcessHandler.class);
    return handler;
  }
}
