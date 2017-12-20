package org.openecomp.sdc.vendorlicense.dao.types;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.Objects;

public class LimitEntity implements VersionableEntity {
  private static final String ENTITY_TYPE = "Limit";

  private String id;
  private String vendorLicenseModelId;
  private String epLkgId;
  private String name;
  private LimitType type;
  private String description;
  private String metric;
  private Version version;
  private String value;
  private String unit;
  private AggregationFunction aggregationFunction;
  private String time;
  //Defined and used only to find parent(EP/LKG) of Limit. Not to be persisted in DB and License
  // Xmls
  private String parent;

  public LimitEntity() {
  }

  public LimitEntity(String vlmId, Version version, String epLkgId, String id) {
    this.vendorLicenseModelId = vlmId;
    this.version = version;
    this.epLkgId = epLkgId;
    this.id = id;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public AggregationFunction getAggregationFunction() {
    return aggregationFunction;
  }

  public void setAggregationFunction(
      AggregationFunction aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }


  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVendorLicenseModelId();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  public String getEpLkgId() {
    return epLkgId;
  }

  public void setEpLkgId(String epLkgId) {
    this.epLkgId = epLkgId;
  }

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  public String getVendorLicenseModelId() {
    return vendorLicenseModelId;
  }

  public void setVendorLicenseModelId(String vendorLicenseModelId) {
    this.vendorLicenseModelId = vendorLicenseModelId;
  }

  public LimitType getType() {
    return type;
  }

  public void setType(LimitType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  //Defined and used only to find parent(EP/LKG) of Limit. Not to be persisted in DB and License
  // Xmls
  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  @Override
  public int hashCode() {
    return Objects.hash(vendorLicenseModelId, version, epLkgId, id, name, description, type,
        metric, unit, time, aggregationFunction, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    LimitEntity that = (LimitEntity) obj;
    return Objects.equals(that.unit, unit)
        && Objects.equals(that.value, value)
        && Objects.equals(vendorLicenseModelId, that.vendorLicenseModelId)
        && Objects.equals(epLkgId, that.epLkgId)
        && Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(type, that.type)
        && Objects.equals(metric, that.metric)
        && Objects.equals(aggregationFunction, that.aggregationFunction);

  }

  @Override
  public String toString() {
    return "LimitEntity{"
        + "vendorLicenseModelId='" + vendorLicenseModelId + '\''
        + ", version=" + version
        + ", epLkgId=" + epLkgId
        + ", id='" + id + '\''
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", type=" + type
        + ", metric=" + metric
        + ", value='" + value + '\''
        + ", unit='" + unit + '\''
        + ", aggregationFunction=" + aggregationFunction
        + ", time=" + time

        + '}';
  }

}
