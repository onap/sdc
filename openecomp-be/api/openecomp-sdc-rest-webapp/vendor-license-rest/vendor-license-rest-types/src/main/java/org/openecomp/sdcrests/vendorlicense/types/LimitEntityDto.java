package org.openecomp.sdcrests.vendorlicense.types;

public class LimitEntityDto {

  private String id;
  private String name;
  private String type;
  private String description;
  private String metric;
  private Integer value;
  private Integer unit;
  private String aggregationFunction;
  private String time;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public Integer getUnit() {
    return unit;
  }

  public void setUnit(Integer unit) {
    this.unit = unit;
  }

  public String getAggregationFunction() {
    return aggregationFunction;
  }

  public void setAggregationFunction(String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }
}
