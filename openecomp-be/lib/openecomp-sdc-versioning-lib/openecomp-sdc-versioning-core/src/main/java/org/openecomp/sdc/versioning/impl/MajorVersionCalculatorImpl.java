package org.openecomp.sdc.versioning.impl;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.util.HashSet;
import java.util.Set;

public class MajorVersionCalculatorImpl implements VersionCalculator {
  private static final String INITIAL_VERSION = "1.0";
  private static final String VERSION_STRING_VIOLATION_MSG =
      "Version string must be in the format of: {integer}.{integer}";

  @Override
  public String calculate(String baseVersion, VersionCreationMethod creationMethod) {

    if (baseVersion == null) {
      return INITIAL_VERSION;
    }

    String[] versionLevels = baseVersion.split("\\.");
    if (versionLevels.length != 2) {
      throw new IllegalArgumentException(VERSION_STRING_VIOLATION_MSG);
    }

    int index = Integer.parseInt(versionLevels[0]);
    index++;
    versionLevels[0] = Integer.toString(index);
    versionLevels[1] = "0";

    return CommonMethods.arrayToSeparatedString(versionLevels, '.');
  }

  @Override
  public void injectAdditionalInfo(Version version, Set<String> existingVersions) {
    String optionalVersion;
    Set<VersionCreationMethod> optionalCreationMethods = new HashSet<>();
    if(version.getStatus().equals(VersionStatus.Certified)) {
      try {
        optionalVersion = calculate(version.getName(), VersionCreationMethod.major);
        if (!existingVersions.contains(optionalVersion)) {
          optionalCreationMethods.add(VersionCreationMethod.major);
        }
      } catch (IllegalArgumentException iae) {
        //not a valid creation method.
      }
    }
    version.getAdditionalInfo().put("OptionalCreationMethods", optionalCreationMethods);
  }
}
