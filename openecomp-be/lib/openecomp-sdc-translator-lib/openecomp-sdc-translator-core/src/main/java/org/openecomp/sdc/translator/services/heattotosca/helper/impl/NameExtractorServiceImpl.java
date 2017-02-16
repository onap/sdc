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

package org.openecomp.sdc.translator.services.heattotosca.helper.impl;

import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.helper.NameExtractorService;
import org.openecomp.sdc.translator.services.heattotosca.helper.PropertyRegexMatcher;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class NameExtractorServiceImpl implements NameExtractorService {

  @Override
  public Optional<String> extractNodeTypeNameByPropertiesPriority(
          Map<String, Object> properties,List<PropertyRegexMatcher> propertiesRegexMatchers) {
    for (PropertyRegexMatcher propertyRegexMatcher : propertiesRegexMatchers) {
      Optional<String> parameterNameValue =
          getPropertyParameterNameValue(properties, propertyRegexMatcher.getPropertyName());
      if (parameterNameValue.isPresent()) {
        if (isPropertyValueMatchNamingConvention(propertyRegexMatcher, parameterNameValue.get())) {
          return Optional.of(parameterNameValue.get().substring(0, parameterNameValue.get()
              .lastIndexOf(propertyRegexMatcher.getStringToSearchForPropertyValue())));
        }
      }
    }

    return Optional.empty();
  }

  private boolean isPropertyValueMatchNamingConvention(PropertyRegexMatcher propertyRegexMatcher,
                                                       String propertyValue) {
    for (Pattern pattern : propertyRegexMatcher.getRegexPatterns()) {
      if (pattern.matcher(propertyValue).matches()) {
        return true;
      }
    }
    return false;
  }

  private Optional<String> getPropertyParameterNameValue(Map<String, Object> properties,
                                                         String prop) {
    Object propObj = properties.get(prop);
    Optional<AttachedResourceId> property = HeatToToscaUtil.extractProperty(propObj);
    if (property.isPresent()) {
      AttachedResourceId extractedProperty = property.get();
      return getParameterName(extractedProperty);
    }
    return Optional.empty();
  }

  private Optional<String> getParameterName(AttachedResourceId extractedProperty) {
    if (!extractedProperty.isGetParam()) {
      return Optional.empty();
    }
    Object entityId = extractedProperty.getEntityId();
    if (entityId instanceof String) {
      return Optional.of((String) entityId);
    } else {
      return Optional.of((String) ((List) entityId).get(0));
    }
  }

  @Override
  public PropertyRegexMatcher getPropertyRegexMatcher(String propertyName,
                                                      List<String> regexMatchers,
                                                      String propertyValueSearchTerm) {
    PropertyRegexMatcher propertyRegexMatcher = new PropertyRegexMatcher();
    propertyRegexMatcher.setPropertyName(propertyName);
    propertyRegexMatcher.setRegex(regexMatchers);
    propertyRegexMatcher.setStringToSearchForPropertyValue(propertyValueSearchTerm);
    return propertyRegexMatcher;
  }
}
