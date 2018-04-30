package org.openecomp.sdc.versioning.impl;

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

import java.util.HashSet;
import java.util.Set;

public class VersionCalculatorImpl implements VersionCalculator {

  private static final String INITIAL_VERSION = "1.0";
  private static final String VERSION_STRING_VIOLATION_MSG =
      "Version string must be in the format of: {integer}.{integer}";
  private static final String PARENT_LEVEL_VERSION_CANNOT_BE_CREATED_FROM_TOP_LEVEL =
      "Creation of parent level version on top level version is invalid.";
  private static final String SUB_LEVEL_VERSION_CANNOT_BE_CREATED_FROM_LOWEST_LEVEL =
      "Creation of parent level version on top level version is invalid.";

  private static final String VERSION_CALCULATION_ERROR_MSG =
      "Version calculation error.";

  private static final String INVALID_CREATION_METHOD_MSG = "Invalid creation method-";


  @Override
  public String calculate(String baseVersion, VersionCreationMethod creationMethod) {

    if (baseVersion == null) {
      return INITIAL_VERSION;
    }

    String[] versionLevels = baseVersion.split("\\.");
    if (versionLevels.length != 2) {
      throw new IllegalArgumentException(VERSION_STRING_VIOLATION_MSG);
    }

    int index;
    switch (creationMethod) {
      case major:
        index = Integer.parseInt(versionLevels[0]);
        index++;
        versionLevels[0] = Integer.toString(index);
        versionLevels[1] = "0";
        break;
      case minor:
        index = Integer.parseInt(versionLevels[1]);
        index++;
        versionLevels[1] = Integer.toString(index);
        break;
    }
    return CommonMethods.arrayToSeparatedString(versionLevels, '.');
  }

  @Override
  public void injectAdditionalInfo(Version version, Set<String> existingVersions) {
    String optionalVersion;
    Set<VersionCreationMethod> optionalCreationMethods = new HashSet<>();
    if(version.getStatus().equals(VersionStatus.Certified)) {
      for (VersionCreationMethod versionCreationMethod : VersionCreationMethod.values()) {
        try {
          optionalVersion = calculate(version.getName(), versionCreationMethod);
          if (!existingVersions.contains(optionalVersion)) {
            optionalCreationMethods.add(versionCreationMethod);
          }
        } catch (IllegalArgumentException iae) {
          //not a valid creation method.
        }
      }
    }
    version.getAdditionalInfo().put("OptionalCreationMethods", optionalCreationMethods);

  }

}
