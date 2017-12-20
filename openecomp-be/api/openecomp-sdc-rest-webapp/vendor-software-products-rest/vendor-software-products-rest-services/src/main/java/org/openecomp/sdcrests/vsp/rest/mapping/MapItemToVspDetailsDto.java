package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;
import org.openecomp.sdcrests.vsp.rest.services.VspItemProperty;

public class MapItemToVspDetailsDto extends MappingBase<Item, VspDetailsDto> {
  @Override
  public void doMapping(Item source, VspDetailsDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setVendorId((String) source.getProperties().get(VspItemProperty.VENDOR_ID));
    target.setVendorName((String) source.getProperties().get(VspItemProperty.VENDOR_NAME));
    target.setOnboardingMethod((String) source.getProperties().get(VspItemProperty.ONBOARDING_METHOD));
  }
}
