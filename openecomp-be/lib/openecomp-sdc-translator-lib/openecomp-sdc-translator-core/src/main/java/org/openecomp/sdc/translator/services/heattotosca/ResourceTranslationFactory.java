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
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.Map;


public class ResourceTranslationFactory {
  private static Map<String, ImplementationConfiguration> resourceTranslationImplMap;

  static {
    Configuration config = ConfigurationManager.lookup();
    resourceTranslationImplMap = config.populateMap(ConfigConstants.TRANSLATOR_NAMESPACE,
        ConfigConstants.RESOURCE_TRANSLATION_IMPL_KEY, ImplementationConfiguration.class);
    resourceTranslationImplMap.putAll(config.populateMap(ConfigConstants.MANDATORY_TRANSLATOR_NAMESPACE,
        ConfigConstants.RESOURCE_TRANSLATION_IMPL_KEY, ImplementationConfiguration.class));
  }

  /**
   * Gets resource translation instance.
   *
   * @param resource the resource
   * @return the instance
   */
  public static ResourceTranslationBase getInstance(Resource resource) {
    if (isSupportedResource(resource.getType())) {
      return getResourceTranslationImpl(resource.getType());
    } else if (HeatToToscaUtil.isYmlFileType(resource.getType())) {
      return getResourceTranslationImpl(ConfigConstants.NESTED_RESOURCE_TRANSLATION_IMPL_KEY);
    }
    return getResourceTranslationImpl(ConfigConstants.DEFAULT_RESOURCE_TRANSLATION_IMPL_KEY);
  }

  private static ResourceTranslationBase getResourceTranslationImpl(String resourceImplKey) {
    String resourceTranslationImplClassName = resourceTranslationImplMap
        .get(resourceImplKey).getImplementationClass();
    return CommonMethods
        .newInstance(resourceTranslationImplClassName, ResourceTranslationBase.class);
  }

  private static boolean isSupportedResource(String resourceType) {
    if (resourceTranslationImplMap.containsKey(resourceType)
        && resourceTranslationImplMap.get(resourceType).isEnable()) {
      return true;
    }
    return false;
  }

}

