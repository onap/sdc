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
import java.util.HashSet;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.LicenseAgreements;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapFeatureGroupEntityToFeatureGroupDescriptorDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseAgreementEntityToLicenseAgreementDescriptorDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementModelDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementUpdateRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Named
@Service("licenseAgreements")
@Scope(value = "prototype")
public class LicenseAgreementsImpl implements LicenseAgreements {

    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();

    /**
     * List license agreements response.
     *
     * @param vlmId     the vlm id
     * @param versionId the version
     * @param user      the user
     * @return the response
     */
    public Response listLicenseAgreements(String vlmId, String versionId, String user) {
        MapLicenseAgreementEntityToLicenseAgreementDescriptorDto outputMapper = new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
        GenericCollectionWrapper<LicenseAgreementEntityDto> results = new GenericCollectionWrapper<>(
            vendorLicenseManager.listLicenseAgreements(vlmId, new Version(versionId)).stream()
                .sorted(Comparator.comparing(LicenseAgreementEntity::getName)).map(lae -> getLicenseAgreementEntityDto(outputMapper, lae))
                .collect(Collectors.toList()));
        return Response.ok(results).build();
    }

    /**
     * Create license agreement response.
     *
     * @param request the request
     * @param vlmId   the vlm id
     * @param user    the user
     * @return the response
     */
    public Response createLicenseAgreement(LicenseAgreementRequestDto request, String vlmId, String versionId, String user) {
        LicenseAgreementEntity licenseAgreementEntity = new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity()
            .applyMapping(request, LicenseAgreementEntity.class);
        licenseAgreementEntity.setVendorLicenseModelId(vlmId);
        licenseAgreementEntity.setVersion(new Version(versionId));
        licenseAgreementEntity.setFeatureGroupIds(request.getAddedFeatureGroupsIds());
        LicenseAgreementEntity createdLicenseAgreement = vendorLicenseManager.createLicenseAgreement(licenseAgreementEntity);
        StringWrapperResponse result = createdLicenseAgreement != null ? new StringWrapperResponse(createdLicenseAgreement.getId()) : null;
        return Response.ok(result).build();
    }

    /**
     * Update license agreement response.
     *
     * @param request            the request
     * @param vlmId              the vlm id
     * @param licenseAgreementId the license agreement id
     * @param user               the user
     * @return the response
     */
    public Response updateLicenseAgreement(LicenseAgreementUpdateRequestDto request, String vlmId, String versionId, String licenseAgreementId,
                                           String user) {
        LicenseAgreementEntity licenseAgreementEntity = new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity()
            .applyMapping(request, LicenseAgreementEntity.class);
        licenseAgreementEntity.setVendorLicenseModelId(vlmId);
        licenseAgreementEntity.setVersion(new Version(versionId));
        licenseAgreementEntity.setId(licenseAgreementId);
        vendorLicenseManager.updateLicenseAgreement(licenseAgreementEntity, request.getAddedFeatureGroupsIds(), request.getRemovedFeatureGroupsIds());
        return Response.ok().build();
    }

    /**
     * Gets license agreement.
     *
     * @param vlmId              the vlm id
     * @param versionId          the version
     * @param licenseAgreementId the license agreement id
     * @param user               the user
     * @return the license agreement
     */
    public Response getLicenseAgreement(String vlmId, String versionId, String licenseAgreementId, String user) {
        LicenseAgreementModel licenseAgreementModel = vendorLicenseManager
            .getLicenseAgreementModel(vlmId, new Version(versionId), licenseAgreementId);
        if (licenseAgreementModel == null) {
            return Response.ok().build();
        }
        LicenseAgreementModelDto lamDto = new LicenseAgreementModelDto();
        lamDto.setId(licenseAgreementModel.getLicenseAgreement().getId());
        new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto().doMapping(licenseAgreementModel.getLicenseAgreement(), lamDto);
        if (!CollectionUtils.isEmpty(licenseAgreementModel.getFeatureGroups())) {
            lamDto.setFeatureGroups(new HashSet<>());
            MapFeatureGroupEntityToFeatureGroupDescriptorDto fgMapper = new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
            for (FeatureGroupEntity fg : licenseAgreementModel.getFeatureGroups()) {
                FeatureGroupEntityDto fgeDto = new FeatureGroupEntityDto();
                fgeDto.setId(fg.getId());
                fgeDto.setEntitlementPoolsIds(fg.getEntitlementPoolIds());
                fgeDto.setLicenseKeyGroupsIds(fg.getLicenseKeyGroupIds());
                fgMapper.doMapping(fg, fgeDto);
                lamDto.getFeatureGroups().add(fgeDto);
            }
        }
        return Response.ok(lamDto).build();
    }

    /**
     * Delete license agreement response.
     *
     * @param vlmId              the vlm id
     * @param versionId          the version id
     * @param licenseAgreementId the license agreement id
     * @param user               the user
     * @return the response
     */
    public Response deleteLicenseAgreement(String vlmId, String versionId, String licenseAgreementId, String user) {
        vendorLicenseManager.deleteLicenseAgreement(vlmId, new Version(versionId), licenseAgreementId);
        return Response.ok().build();
    }

    private LicenseAgreementEntityDto getLicenseAgreementEntityDto(MapLicenseAgreementEntityToLicenseAgreementDescriptorDto mapper,
                                                                   LicenseAgreementEntity lae) {
        LicenseAgreementEntityDto laeDto = new LicenseAgreementEntityDto();
        laeDto.setId(lae.getId());
        laeDto.setFeatureGroupsIds(lae.getFeatureGroupIds());
        mapper.doMapping(lae, laeDto);
        return laeDto;
    }
}
