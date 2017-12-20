package org.openecomp.sdc.versioning.dao;

import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Optional;

public interface VersionDao {
  List<Version> list(String itemId);

  void create(String itemId, Version version);

  void update(String itemId, Version version);

  Optional<Version> get(String itemId, Version version);

  void delete(String itemId, Version version);

  void publish(String itemId, Version version, String message);

  void sync(String itemId, Version version);

  void forceSync(String itemId, Version version);

  void revert(String itemId, Version version, String revisionId);

  List<Revision> listRevisions(String itemId, Version version);
}
