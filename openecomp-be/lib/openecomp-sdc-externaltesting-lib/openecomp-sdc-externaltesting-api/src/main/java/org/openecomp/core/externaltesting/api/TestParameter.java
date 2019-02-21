/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Parameter for test that may require user input.
 */
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Test Parameter Definition")
public class TestParameter {

  @ApiModelProperty("Internal ID for this parameter.  This is the key to use when invoking the test.")
  private String id;

  @ApiModelProperty(value="Label to display to user for this field", example="Some Label")
  private String label;

  @ApiModelProperty(dataType = "string", allowableValues =  "text, numeric, boolean, date, datetime, select, multi-select, hidden")
  private String inputType;

  @ApiModelProperty(value="Optional max length for UI field", example="3")
  private Integer maxLength;

  @ApiModelProperty(value="Optional min length for UI field", example="1")
  private Integer minLength;

  @ApiModelProperty(value="Optional min value for numeric UI field", example="1")
  private Integer min;

  @ApiModelProperty(value="Optional max value for numeric UI field", example="100")
  private Integer max;

  @ApiModelProperty("Optional placeholder text for UI field - appears in grey when no value entered by user")
  private String placeholder;

  @ApiModelProperty(value="Default value to place in UI field when displayed in a UI", example="B")
  private String defaultValue;

  @ApiModelProperty("If true, present the field in the UI as disabled")
  private boolean disabled;

  @ApiModelProperty(value = "For select and multi-select properties, this is the list of options to display", example = "B,N,H")
  private List<String> choices;

  @ApiModelProperty("If true, user or API must provide a value for this parameter")
  private boolean required;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getInputType() {
    return inputType;
  }

  public void setInputType(String inputType) {
    this.inputType = inputType;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public void setMinLength(Integer minLength) {
    this.minLength = minLength;
  }

  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public List<String> getChoices() {
    return choices;
  }

  public void setChoices(List<String> choices) {
    this.choices = choices;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }
}
  
