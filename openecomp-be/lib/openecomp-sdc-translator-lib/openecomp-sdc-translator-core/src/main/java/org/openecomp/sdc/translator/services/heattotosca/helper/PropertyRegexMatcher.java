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

package org.openecomp.sdc.translator.services.heattotosca.helper;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PropertyRegexMatcher {
  private String propertyName;
  private List<Pattern> regexPatterns;
  private String stringToSearchForPropertyValue;

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * Sets regex.
   *
   * @param regexPatterns the regex patterns
   */
  public void setRegex(List<String> regexPatterns) {
    if (CollectionUtils.isEmpty(this.regexPatterns)) {
      this.regexPatterns = new ArrayList<>();
    }

    for (String regexPattern : regexPatterns) {
      this.regexPatterns.add(Pattern.compile(regexPattern));
    }
  }

  public List<Pattern> getRegexPatterns() {
    return regexPatterns;
  }

  public String getStringToSearchForPropertyValue() {
    return stringToSearchForPropertyValue;
  }

  public void setStringToSearchForPropertyValue(String stringToSearchForPropertyValue) {
    this.stringToSearchForPropertyValue = stringToSearchForPropertyValue;
  }

}
