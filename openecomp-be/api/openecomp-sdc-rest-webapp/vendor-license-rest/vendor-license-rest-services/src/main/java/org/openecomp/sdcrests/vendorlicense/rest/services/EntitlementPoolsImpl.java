/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.vendorlicense.rest.services;

import java.util.Comparator;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.EntitlementPools;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapEntitlementPoolEntityToEntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapEntitlementPoolRequestDtoToEntitlementPoolEntity;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Named
@Service("entitlementPools")
@Scope(value = "prototype")
public class EntitlementPoolsImpl implements EntitlementPools {

    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();

    /**
     * List entitlement pools response.
     *
     * @param vlmId     the vlm id
     * @param versionId the version
     * @param user      the user
     * @return the response
     */
    public ResponseEntity listEntitlementPools(String vlmId, String versionId, String user) {
        MapEntitlementPoolEntityToEntitlementPoolEntityDto outputMapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        GenericCollectionWrapper<EntitlementPoolEntityDto> result = new GenericCollectionWrapper<>(
            vendorLicenseManager.listEntitlementPools(vlmId, new Version(versionId)).stream()
                .sorted(Comparator.comparing(EntitlementPoolEntity::getName))
                .map(item -> outputMapper.applyMapping(item, EntitlementPoolEntityDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    /**
     * Create entitlement pool response.
     *
     * @param request the request
     * @param vlmId   the vlm id
     * @param user    the user
     * @return the response
     */
    public ResponseEntity createEntitlementPool(EntitlementPoolRequestDto request, String vlmId, String versionId, String user) {
        EntitlementPoolEntity entitlementPoolEntity = new MapEntitlementPoolRequestDtoToEntitlementPoolEntity()
            .applyMapping(request, EntitlementPoolEntity.class);
        entitlementPoolEntity.setVendorLicenseModelId(vlmId);
        entitlementPoolEntity.setVersion(new Version(versionId));
        EntitlementPoolEntity createdEntitlementPool = vendorLicenseManager.createEntitlementPool(entitlementPoolEntity);
        StringWrapperResponse result = createdEntitlementPool != null ? new StringWrapperResponse(createdEntitlementPool.getId()) : null;
        return ResponseEntity.ok(result);
    }

    /**
     * Update entitlement pool response.
     *
     * @param request           the request
     * @param vlmId             the vlm id
     * @param entitlementPoolId the entitlement pool id
     * @param user              the user
     * @return the response
     */
    public ResponseEntity updateEntitlementPool(EntitlementPoolRequestDto request, String vlmId, String versionId, String entitlementPoolId, String user) {
        EntitlementPoolEntity entitlementPoolEntity = new MapEntitlementPoolRequestDtoToEntitlementPoolEntity()
            .applyMapping(request, EntitlementPoolEntity.class);
        entitlementPoolEntity.setVendorLicenseModelId(vlmId);
        entitlementPoolEntity.setVersion(new Version(versionId));
        entitlementPoolEntity.setId(entitlementPoolId);
        vendorLicenseManager.updateEntitlementPool(entitlementPoolEntity);
        return ResponseEntity.ok().build();
    }

    /**
     * Gets entitlement pool.
     *
     * @param vlmId             the vlm id
     * @param versionId         the version id
     * @param entitlementPoolId the entitlement pool id
     * @param user              the user
     * @return the entitlement pool
     */
    public ResponseEntity getEntitlementPool(String vlmId, String versionId, String entitlementPoolId, String user) {
        EntitlementPoolEntity epInput = new EntitlementPoolEntity();
        epInput.setVendorLicenseModelId(vlmId);
        epInput.setVersion(new Version(versionId));
        epInput.setId(entitlementPoolId);
        EntitlementPoolEntity entitlementPool = vendorLicenseManager.getEntitlementPool(epInput);
        EntitlementPoolEntityDto entitlementPoolEntityDto = entitlementPool == null ? null
            : new MapEntitlementPoolEntityToEntitlementPoolEntityDto().applyMapping(entitlementPool, EntitlementPoolEntityDto.class);
        return ResponseEntity.ok(entitlementPoolEntityDto);
    }

    /**
     * Delete entitlement pool response.
     *
     * @param vlmId             the vlm id
     * @param entitlementPoolId the entitlement pool id
     * @param user              the user
     * @return the response
     */
    public ResponseEntity deleteEntitlementPool(String vlmId, String versionId, String entitlementPoolId, String user) {
        EntitlementPoolEntity epInput = new EntitlementPoolEntity();
        epInput.setVendorLicenseModelId(vlmId);
        epInput.setId(entitlementPoolId);
        epInput.setVersion(new Version(versionId));
        vendorLicenseManager.deleteEntitlementPool(epInput);
        return ResponseEntity.ok().build();
    }
}
