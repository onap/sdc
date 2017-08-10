package org.openecomp.sdcrests.vendorlicense.types;

import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.NotBlank;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ApiModel(value = "LimitRequest")
public class LimitRequestDto {

  @NotBlank(message = "is mandatory and should not be empty")
  @Size(max = 120, message = "length should not exceed 120 characters.")
  private String name;
  @NotBlank(message = "is mandatory and should not be empty")
  private String type;
  @Size(max = 1000, message = "length should not exceed 1000 characters.")
  private String description;
  @NotBlank(message = "is mandatory and should not be empty")
  private String metric;
  @NotBlank(message = "is mandatory and should not be empty")
  private String value;
  private String unit;
  private String aggregationFunction;
  private String time;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getAggregationFunction() {
    return aggregationFunction;
  }

  public void setAggregationFunction(
      String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
