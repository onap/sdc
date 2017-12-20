package org.openecomp.sdc.versioning;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.util.Set;

public interface VersionCalculator {
  String calculate(String baseVersion, VersionCreationMethod creationMethod);

  void injectAdditionalInfo(Version version, Set<String> existingVersions);
}
