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

package org.openecomp.sdc.tosca.datatypes.model;

import java.util.ArrayList;
import java.util.List;

public class Constraint {
  private Object equal;
  private Object greater_or_equal;
  private Object greater_than;
  private Object less_than;
  private Object less_or_equal;
  private Object[] in_range;
  private List<Object> valid_values;
  private Integer length;
  private Integer min_length;
  private Integer max_length;
  private Object pattern;

  public Constraint() {
  }

  public Object getGreater_or_equal() {
    return greater_or_equal;
  }

  public void setGreater_or_equal(Object greater_or_equal) {
    this.greater_or_equal = greater_or_equal;
  }

  public Object getEqual() {
    return equal;
  }

  public void setEqual(Object equal) {
    this.equal = equal;
  }

  public Object getGreater_than() {
    return greater_than;
  }

  public void setGreater_than(Object greater_than) {
    this.greater_than = greater_than;
  }

  public Object getLess_than() {
    return less_than;
  }

  public void setLess_than(Object less_than) {
    this.less_than = less_than;
  }

  public Object getLess_or_equal() {
    return less_or_equal;
  }

  public void setLess_or_equal(Object less_or_equal) {
    this.less_or_equal = less_or_equal;
  }

  public Object[] getIn_range() {
    return in_range;
  }

  /**
   * Sets in_range attribute.
   * @param in_range.
   */
  public void setIn_range(Object[] in_range) {
    this.in_range = new Object[2];
    this.in_range[0] = in_range[0];
    this.in_range[1] = in_range[1];
  }

  public List<Object> getValid_values() {
    return valid_values;
  }

  public void setValid_values(List<Object> valid_values) {
    this.valid_values = valid_values;
  }

  /**
   * Add Valid value
   * @param validValue object.
   */
  public void addValidValue(Object validValue) {
    if (this.valid_values == null) {
      this.valid_values = new ArrayList<>();
    }
    valid_values.add(validValue);
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public Integer getMin_length() {
    return min_length;
  }

  public void setMin_length(Integer min_length) {
    this.min_length = min_length;
  }

  public Integer getMax_length() {
    return max_length;
  }

  public void setMax_length(Integer max_length) {
    this.max_length = max_length;
  }

  public Object getPattern() {
    return pattern;
  }

  public void setPattern(Object pattern) {
    this.pattern = pattern;
  }

  @Override
  public Constraint clone() {
    Constraint constraint = new Constraint();
    constraint.setEqual(this.getEqual());
    constraint.setGreater_or_equal(this.getGreater_or_equal());
    constraint.setGreater_than(this.getGreater_than());
    cloneInRange(constraint);
    constraint.setLength(this.getLength());
    constraint.setLess_or_equal(this.getLess_or_equal());
    constraint.setLess_than(this.getLess_than());
    constraint.setMax_length(this.getMax_length());
    constraint.setMin_length(this.getMin_length());
    constraint.setPattern(this.getPattern());
    cloneValidValues(constraint);

    return constraint;
  }

  private void cloneInRange(Constraint constraint) {
    if (this.getIn_range() != null) {
      constraint.setIn_range(new Object[]{this.getIn_range()[0], this.getIn_range()[1]});
    }
  }

  private void cloneValidValues(Constraint constraint) {
    if (this.getValid_values() != null) {
      constraint.setValid_values(new ArrayList<>());
      for (Object entry : this.getValid_values()) {
        constraint.getValid_values().add(entry);
      }
    }
  }
}
