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

package org.openecomp.sdc.heat.datatypes.model;

import java.util.ArrayList;
import java.util.List;

public class Constraint {
  private Object[] length;
  private Integer[] range;
  private List<Object> valid_values;
  private String pattern;

  public Constraint() {
  }

  public Integer[] getRange() {
    return range;
  }

  public void setRange(Integer[] inRange) {
    this.range = new Integer[]{inRange[0], inRange[1]};
  }

  public List<Object> getValid_values() {
    return valid_values;
  }

  public void setValid_values(List<Object> validValues) {
    this.valid_values = validValues;
  }

  /**
   * Add valid value.
   *
   * @param validValue the valid value
   */
  public void addValidValue(Object validValue) {
    if (this.valid_values == null) {
      this.valid_values = new ArrayList<>();
    }
    valid_values.add(validValue);
  }

  public Object[] getLength() {
    return length;
  }

  public void setLength(Object[] length) {
    this.length = length;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
