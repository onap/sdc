package org.openecomp.sdcrests.vendorlicense.rest.services;

import org.openecomp.sdcrests.uniquevalue.types.UniqueTypesProvider;

import java.util.HashMap;
import java.util.Map;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.UniqueValues.VENDOR_NAME;

public class VlmUniqueTypeProvider implements UniqueTypesProvider {
  private static final Map<String, String> uniqueTypes = new HashMap<>();

  static {
    uniqueTypes.put("VlmName", VENDOR_NAME);
  }

  @Override
  public Map<String, String> listUniqueTypes() {
    return uniqueTypes;
  }
}
