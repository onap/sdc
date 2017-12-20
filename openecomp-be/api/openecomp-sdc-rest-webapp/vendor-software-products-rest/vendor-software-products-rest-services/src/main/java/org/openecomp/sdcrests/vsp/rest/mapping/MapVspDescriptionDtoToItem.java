package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vsp.rest.services.VspItemProperty;

public class MapVspDescriptionDtoToItem extends MappingBase<VspDescriptionDto, Item> {
  @Override
  public void doMapping(VspDescriptionDto source, Item target) {
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.addProperty(VspItemProperty.VENDOR_ID, source.getVendorId());
    target.addProperty(VspItemProperty.VENDOR_NAME, source.getVendorName());
  }
}
