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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.LicenseKeyGroups;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Named
@Service("licenseKeyGroups")
@Scope(value = "prototype")
@Validated
public class LicenseKeyGroupsImpl implements LicenseKeyGroups {

    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();

    /**
     * List license key groups response.
     *
     * @param vlmId     the vlm id
     * @param versionId the version
     * @param user      the user
     * @return the response
     */
    public ResponseEntity listLicenseKeyGroups(String vlmId, String versionId, String user) {
        MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto outputMapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
        GenericCollectionWrapper<LicenseKeyGroupEntityDto> result = new GenericCollectionWrapper<>(
            vendorLicenseManager.listLicenseKeyGroups(vlmId, new Version(versionId)).stream()
                .sorted(Comparator.comparing(LicenseKeyGroupEntity::getName))
                .map(item -> outputMapper.applyMapping(item, LicenseKeyGroupEntityDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    /**
     * Create license key group response.
     *
     * @param request the request
     * @param vlmId   the vlm id
     * @param user    the user
     * @return the response
     */
    public ResponseEntity createLicenseKeyGroup(LicenseKeyGroupRequestDto request, String vlmId, String versionId, String user) {
        LicenseKeyGroupEntity licenseKeyGroupEntity = new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity()
            .applyMapping(request, LicenseKeyGroupEntity.class);
        licenseKeyGroupEntity.setVendorLicenseModelId(vlmId);
        licenseKeyGroupEntity.setVersion(new Version(versionId));
        LicenseKeyGroupEntity createdLicenseKeyGroup = vendorLicenseManager.createLicenseKeyGroup(licenseKeyGroupEntity);
        StringWrapperResponse result = createdLicenseKeyGroup != null ? new StringWrapperResponse(createdLicenseKeyGroup.getId()) : null;
        return ResponseEntity.ok(result);
    }

    /**
     * Update license key group response.
     *
     * @param request           the request
     * @param vlmId             the vlm id
     * @param licenseKeyGroupId the license key group id
     * @param user              the user
     * @return the response
     */
    public ResponseEntity updateLicenseKeyGroup(LicenseKeyGroupRequestDto request, String vlmId, String versionId, String licenseKeyGroupId, String user) {
        LicenseKeyGroupEntity licenseKeyGroupEntity = new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity()
            .applyMapping(request, LicenseKeyGroupEntity.class);
        licenseKeyGroupEntity.setVendorLicenseModelId(vlmId);
        licenseKeyGroupEntity.setVersion(new Version(versionId));
        licenseKeyGroupEntity.setId(licenseKeyGroupId);
        vendorLicenseManager.updateLicenseKeyGroup(licenseKeyGroupEntity);
        return ResponseEntity.ok().build();
    }

    /**
     * Gets license key group.
     *
     * @param vlmId             the vlm id
     * @param versionId         the version
     * @param licenseKeyGroupId the license key group id
     * @param user              the user
     * @return the license key group
     */
    public ResponseEntity getLicenseKeyGroup(String vlmId, String versionId, String licenseKeyGroupId, String user) {
        LicenseKeyGroupEntity lkgInput = new LicenseKeyGroupEntity();
        lkgInput.setVendorLicenseModelId(vlmId);
        lkgInput.setVersion(new Version(versionId));
        lkgInput.setId(licenseKeyGroupId);
        LicenseKeyGroupEntity licenseKeyGroup = vendorLicenseManager.getLicenseKeyGroup(lkgInput);
        LicenseKeyGroupEntityDto licenseKeyGroupEntityDto = licenseKeyGroup == null ? null
            : new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto().applyMapping(licenseKeyGroup, LicenseKeyGroupEntityDto.class);
        return ResponseEntity.ok(licenseKeyGroupEntityDto);
    }

    /**
     * Delete license key group response.
     *
     * @param vlmId             the vlm id
     * @param licenseKeyGroupId the license key group id
     * @param user              the user
     * @return the response
     */
    public ResponseEntity deleteLicenseKeyGroup(String vlmId, String versionId, String licenseKeyGroupId, String user) {
        LicenseKeyGroupEntity lkgInput = new LicenseKeyGroupEntity();
        lkgInput.setVendorLicenseModelId(vlmId);
        lkgInput.setVersion(new Version(versionId));
        lkgInput.setId(licenseKeyGroupId);
        vendorLicenseManager.deleteLicenseKeyGroup(lkgInput);
        return ResponseEntity.ok().build();
    }
}
