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

import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedPropertyVal;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.PropertyRegexMatcher;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class NameExtractorUtil {

  /**
   * Extract Node Type Name By Properties Priority.
   * @param properties properties list
   * @param propertiesRegexMatchers Regex expression list
   * @return node type name
   */
  public static Optional<String> extractNodeTypeNameByPropertiesPriority(
      Map<String, Object> properties,
      List<PropertyRegexMatcher> propertiesRegexMatchers) {

    for (PropertyRegexMatcher propertyRegexMatcher : propertiesRegexMatchers) {
      Optional<String> parameterNameValue =
          HeatToToscaUtil.getPropertyParameterNameValue(
              properties.get(propertyRegexMatcher.getPropertyName()));
      if (parameterNameValue.isPresent()) {
        if (isPropertyValueMatchNamingConvention(propertyRegexMatcher, parameterNameValue.get())) {
          return Optional.of(parameterNameValue.get().substring(0, parameterNameValue.get()
              .lastIndexOf(propertyRegexMatcher.getStringToSearchForPropertyValue())));
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Check if property value match the naming convention using Regex expression.
   * @param propertyRegexMatcher naming convention using Regex expression
   * @param propertyValue property value
   * @return true is there is a match, false otherwise
   */
  public static boolean isPropertyValueMatchNamingConvention(PropertyRegexMatcher
                                                                 propertyRegexMatcher,
                                                             String propertyValue) {
    for (Pattern pattern : propertyRegexMatcher.getRegexPatterns()) {
      if (pattern.matcher(propertyValue).matches()) {
        return true;
      }
    }
    return false;
  }


}
