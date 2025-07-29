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
package org.openecomp.sdcrests.vendorlicense.rest.services;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.LicenseKeyGroupLimits;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.LimitCreationDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitEntityToLimitCreationDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitEntityToLimitDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitRequestDtoToLimitEntity;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.ScopedProxyMode;
@Named
@Service("licenseKeyGroupLimits")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LicenseKeyGroupLimitsImpl implements LicenseKeyGroupLimits {

    private static final String PARENT = "LicenseKeyGroup";
    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();

    @Override
    public ResponseEntity createLimit(LimitRequestDto request, String vlmId, String versionId, String licenseKeyGroupId, String user) {
        Version version = new Version(versionId);
        vendorLicenseManager.getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
        LimitEntity limitEntity = new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
        limitEntity.setVendorLicenseModelId(vlmId);
        limitEntity.setVersion(version);
        limitEntity.setEpLkgId(licenseKeyGroupId);
        limitEntity.setParent(PARENT);
        LimitEntity createdLimit = vendorLicenseManager.createLimit(limitEntity);
        MapLimitEntityToLimitCreationDto mapper = new MapLimitEntityToLimitCreationDto();
        LimitCreationDto createdLimitDto = mapper.applyMapping(createdLimit, LimitCreationDto.class);
        return ResponseEntity.ok(createdLimitDto != null ? createdLimitDto : null);
    }

    @Override
    public ResponseEntity listLimits(String vlmId, String versionId, String licenseKeyGroupId, String user) {
        Version version = new Version(versionId);
        vendorLicenseManager.getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
        Collection<LimitEntity> limits = vendorLicenseManager.listLimits(vlmId, version, licenseKeyGroupId);
        GenericCollectionWrapper<LimitEntityDto> result = new GenericCollectionWrapper<>();
        MapLimitEntityToLimitDto outputMapper = new MapLimitEntityToLimitDto();
        for (LimitEntity limit : limits) {
            result.add(outputMapper.applyMapping(limit, LimitEntityDto.class));
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity updateLimit(LimitRequestDto request, String vlmId, String versionId, String licenseKeyGroupId, String limitId, String user) {
        Version version = new Version(versionId);
        vendorLicenseManager.getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
        LimitEntity limitEntity = new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
        limitEntity.setVendorLicenseModelId(vlmId);
        limitEntity.setVersion(version);
        limitEntity.setEpLkgId(licenseKeyGroupId);
        limitEntity.setId(limitId);
        limitEntity.setParent(PARENT);
        vendorLicenseManager.updateLimit(limitEntity);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete License Key Group.
     *
     * @param vlmId             the vlm id
     * @param licenseKeyGroupId the license Key Group id
     * @param limitId           the limitId
     * @param user              the user
     * @return the response
     */
    public ResponseEntity deleteLimit(String vlmId, String versionId, String licenseKeyGroupId, String limitId, String user) {
        Version version = new Version(versionId);
        vendorLicenseManager.getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
        LimitEntity limitInput = new LimitEntity();
        limitInput.setVendorLicenseModelId(vlmId);
        limitInput.setVersion(version);
        limitInput.setEpLkgId(licenseKeyGroupId);
        limitInput.setId(limitId);
        limitInput.setParent(PARENT);
        vendorLicenseManager.deleteLimit(limitInput);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity getLimit(String vlmId, String versionId, String licenseKeyGroupId, String limitId, String user) {
        Version version = new Version(versionId);
        vendorLicenseManager.getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
        LimitEntity limitInput = new LimitEntity();
        limitInput.setVendorLicenseModelId(vlmId);
        limitInput.setVersion(version);
        limitInput.setEpLkgId(licenseKeyGroupId);
        limitInput.setId(limitId);
        LimitEntity limit = vendorLicenseManager.getLimit(limitInput);
        LimitEntityDto entitlementPoolEntityDto = limit == null ? null : new MapLimitEntityToLimitDto().applyMapping(limit, LimitEntityDto.class);
        return ResponseEntity.ok(entitlementPoolEntityDto);
    }
}
