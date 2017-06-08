package org.openecomp.core.migration.util.marker;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

/**
 * @author katyr
 * @since May 03, 2017
 */

@Table(keyspace = "dox", name = "migration")
public class MigrationMarkerEntity {

  @ClusteringColumn
  private String id;

  @Column(name = "ismigrated")
  private Boolean migrated;

  public Boolean getMigrated() {
    return migrated;
  }

  public void setMigrated(Boolean migrated) {
    this.migrated = migrated;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MigrationMarkerEntity{");
    sb.append("id='").append(id).append('\'');
    sb.append(", migrated=").append(migrated);
    sb.append('}');
    return sb.toString();
  }
}
