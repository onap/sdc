package org.openecomp.activityspec.mocks;

import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Certified;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Deprecated;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Deleted;

import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VersionManagerMock implements VersioningManager {

  private String id;
  private Version version;


  @Override
  public List<Version> list(String itemId) {
    List<Version> versions = new ArrayList<Version>();
    versions.add(version);
    return versions;
  }


  @Override
  public Version get(String itemId, Version version) {
    return this.version;
  }

  @Override
  public Version create(String itemId, Version version, VersionCreationMethod creationMethod) {
    this.id = UUID.randomUUID().toString();
    version.setId(this.id);
    version.setStatus(VersionStatus.Draft);
    this.version = version;

    return version;
  }

  @Override
  public void submit(String itemId, Version version, String submitDescription) {

  }


  @Override
  public void publish(String itemId, Version version, String message) {

  }

  @Override
  public void sync(String itemId, Version version) {

  }

  @Override
  public void forceSync(String itemId, Version version) {

  }

  @Override
  public void revert(String itemId, Version version, String revisionId) {

  }

  @Override
  public List<Revision> listRevisions(String itemId, Version version) {
    return null;
  }

  @Override
  public void updateVersion(String itemId, Version version) {
    if (version.getStatus() == Certified) {
      this.version.setStatus(Certified);
    }
    if (version.getStatus() == Deprecated) {
      this.version.setStatus(Deprecated);
    }
    if (version.getStatus() == Deleted) {
      this.version.setStatus(Deleted);
    }
  }
}
