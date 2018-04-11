package org.openecomp.sdcrests.vsp.rest.services;

import org.openecomp.sdcrests.uniquevalue.types.UniqueTypesProvider;

import java.util.HashMap;
import java.util.Map;

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME;

public class VspUniqueTypeProvider implements UniqueTypesProvider {

  private static final Map<String, String> uniqueTypes = new HashMap<>();

  static {
    uniqueTypes.put("VspName", VENDOR_SOFTWARE_PRODUCT_NAME);
  }

  @Override
  public Map<String, String> listUniqueTypes() {
    return uniqueTypes;
  }
}
