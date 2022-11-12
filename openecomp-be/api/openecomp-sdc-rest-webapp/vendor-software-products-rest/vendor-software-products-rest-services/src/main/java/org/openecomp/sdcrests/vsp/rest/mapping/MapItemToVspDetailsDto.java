/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
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
        target.setOwner(source.getOwner());
        target.setStatus(source.getStatus().name());
        target.setTenant(source.getTenant());
    }
}
