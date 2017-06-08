/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca;


import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;

import java.util.Map;
import java.util.Optional;


public class FunctionTranslationFactory {
  private static Map<String, ImplementationConfiguration> functionTranslationImplMap;

  static {
    Configuration config = ConfigurationManager.lookup();
    functionTranslationImplMap = config.populateMap(ConfigConstants.TRANSLATOR_NAMESPACE,
        ConfigConstants.FUNCTION_TRANSLATION_IMPL_KEY, ImplementationConfiguration.class);
    functionTranslationImplMap.putAll(config.populateMap(ConfigConstants.MANDATORY_TRANSLATOR_NAMESPACE,
        ConfigConstants.FUNCTION_TRANSLATION_IMPL_KEY, ImplementationConfiguration.class));

  }

  /**
   * Gets function translation instance.
   *
   * @param heatFunctionKey heat function key
   * @return the instance
   */
  public static Optional<FunctionTranslation> getInstance(String heatFunctionKey) {
    if (isSupportedFunction(heatFunctionKey)) {
      String functionTranslationImplClassName =
          functionTranslationImplMap.get(heatFunctionKey).getImplementationClass();
      return Optional.of(CommonMethods
          .newInstance(functionTranslationImplClassName, FunctionTranslation.class));
    }

    return Optional.empty();
  }

  private static boolean isSupportedFunction(String heatFunctionKey) {
    if (functionTranslationImplMap.containsKey(heatFunctionKey)) {
      return true;
    }
    return false;
  }

}

