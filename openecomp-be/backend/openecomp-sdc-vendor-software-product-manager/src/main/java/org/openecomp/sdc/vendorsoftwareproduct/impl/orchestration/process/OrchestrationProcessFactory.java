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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.vendorsoftwareproduct.types.ConfigConstants;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil.ORCHESTRATION_CONFIG_NAMESPACE;
public class OrchestrationProcessFactory {

  private static final Map<String, ImplementationConfiguration> PROCESS_IMPL_MAP;
  private OrchestrationProcessFactory() {

  }

  static {
    Configuration config = ConfigurationManager.lookup();
    PROCESS_IMPL_MAP = new ConcurrentHashMap<>(config.populateMap(ORCHESTRATION_CONFIG_NAMESPACE,
        ConfigConstants.PROCESS_IMPL_KEY, ImplementationConfiguration.class));

  }

  public static Optional<OrchestrationTemplateProcessHandler> getInstance(String fileSuffix) {

    if (fileSuffix == null) {
      return Optional.empty();
    }
    String updatedFileSuffix = fileSuffix;
    updatedFileSuffix = updatedFileSuffix.toLowerCase().trim();
    OnboardingTypesEnum onboardingTypesEnum = OnboardingTypesEnum.getOnboardingTypesEnum(updatedFileSuffix);
    if (onboardingTypesEnum == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(createInstance(PROCESS_IMPL_MAP.get(onboardingTypesEnum.toString())));
    }catch (Exception e){
      return Optional.empty();
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
