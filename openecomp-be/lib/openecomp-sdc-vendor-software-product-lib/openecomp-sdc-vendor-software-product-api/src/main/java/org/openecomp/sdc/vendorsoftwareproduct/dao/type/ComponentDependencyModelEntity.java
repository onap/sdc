package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

@Table(keyspace = "dox", name = "vsp_component_dependency_model")
public class ComponentDependencyModelEntity implements VersionableEntity {

  public static final String ENTITY_TYPE = "Vendor Software Product Component Dependency Model";
  @PartitionKey
  @Column(name = "vsp_id")
  private String vspId;
  @PartitionKey(value = 1)
  @Frozen
  private Version version;
  @ClusteringColumn
  @Column(name = "dependency_id")
  private String id;
  @Column(name = "sourcecomponent_id")
  private String sourceComponentId;
  @Column(name = "targetcomponent_id")
  private String targetComponentId;
  @Column(name = "relation")
  private String relation;

  public ComponentDependencyModelEntity() {

  }

  /**
   * Instantiates a new ComponentDependencyModelEntity entity.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @param dependencyId      the dependencyId
   */
  public ComponentDependencyModelEntity(String vspId, Version version, String dependencyId) {
    this.vspId = vspId;
    this.version = version;
    this.id = dependencyId;
  }

  @Override
  public String getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  public String getFirstClassCitizenId() {
    return getVspId();
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

  @Override
  public void setVersion(Version version) {
    this.version = version;
  }

  public String getVspId() {
    return vspId;
  }

  public void setVspId(String vspId) {
    this.vspId = vspId;
  }

  public String getTargetComponentId() {
    return targetComponentId;
  }

  public void setTargetComponentId(String targetComponentId) {
    this.targetComponentId = targetComponentId;
  }

  public String getSourceComponentId() {
    return sourceComponentId;
  }

  public void setSourceComponentId(String sourceComponentId) {
        this.sourceComponentId = sourceComponentId;
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ComponentDependencyModelEntity that = (ComponentDependencyModelEntity) o;

    if (vspId != null ? !vspId.equals(that.vspId) : that.vspId != null) {
      return false;
    }
    if (version != null ? !version.equals(that.version) : that.version != null) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (sourceComponentId != null ? !sourceComponentId.equals(that.sourceComponentId)
        : that.sourceComponentId != null) {
      return false;
    }
    if (targetComponentId != null ? !targetComponentId.equals(that.targetComponentId)
        : that.targetComponentId != null) {
      return false;
    }
    if (relation != null ? !relation.equals(that.relation) : that.relation != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = vspId != null ? vspId.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (sourceComponentId != null ? sourceComponentId.hashCode() : 0);
    result = 31 * result + (targetComponentId != null ? targetComponentId.hashCode() : 0);
    result = 31 * result + (relation != null ? relation.hashCode() : 0);
    return result;
  }
}
