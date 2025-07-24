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

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.FeatureGroups;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapEntitlementPoolEntityToEntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapFeatureGroupDescriptorDtoToFeatureGroupEntity;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapFeatureGroupEntityToFeatureGroupDescriptorDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.*;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

@Named
@Service("featureGroups")
@Scope(value = "prototype")
public class FeatureGroupsImpl implements FeatureGroups {

    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();

    @Override
    public ResponseEntity listFeatureGroups(String vlmId, String versionId, String user) {
        MapFeatureGroupEntityToFeatureGroupDescriptorDto outputMapper = new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
        GenericCollectionWrapper<FeatureGroupEntityDto> results = new GenericCollectionWrapper<>(
            vendorLicenseManager.listFeatureGroups(vlmId, new Version(versionId)).stream().sorted(Comparator.comparing(FeatureGroupEntity::getName))
                .map(fg -> getFeatureGroupEntityDto(outputMapper, fg)).collect(Collectors.toList()));
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity createFeatureGroup(FeatureGroupRequestDto request, String vlmId, String versionId, String user) {
        FeatureGroupEntity featureGroupEntity = new MapFeatureGroupDescriptorDtoToFeatureGroupEntity()
            .applyMapping(request, FeatureGroupEntity.class);
        featureGroupEntity.setVendorLicenseModelId(vlmId);
        featureGroupEntity.setVersion(new Version(versionId));
        featureGroupEntity.setLicenseKeyGroupIds(request.getAddedLicenseKeyGroupsIds());
        featureGroupEntity.setEntitlementPoolIds(request.getAddedEntitlementPoolsIds());
        FeatureGroupEntity createdFeatureGroup = vendorLicenseManager.createFeatureGroup(featureGroupEntity);
        StringWrapperResponse result = createdFeatureGroup != null ? new StringWrapperResponse(createdFeatureGroup.getId()) : null;
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity updateFeatureGroup(FeatureGroupUpdateRequestDto request, String vlmId, String versionId, String featureGroupId, String user) {
        FeatureGroupEntity featureGroupEntity = new MapFeatureGroupDescriptorDtoToFeatureGroupEntity()
            .applyMapping(request, FeatureGroupEntity.class);
        featureGroupEntity.setVendorLicenseModelId(vlmId);
        featureGroupEntity.setVersion(new Version(versionId));
        featureGroupEntity.setId(featureGroupId);
        vendorLicenseManager.updateFeatureGroup(featureGroupEntity, request.getAddedLicenseKeyGroupsIds(), request.getRemovedLicenseKeyGroupsIds(),
            request.getAddedEntitlementPoolsIds(), request.getRemovedEntitlementPoolsIds());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity getFeatureGroup(String vlmId, String versionId, String featureGroupId, String user) {
        FeatureGroupEntity fgInput = new FeatureGroupEntity();
        fgInput.setVendorLicenseModelId(vlmId);
        fgInput.setVersion(new Version(versionId));
        fgInput.setId(featureGroupId);
        FeatureGroupModel featureGroupModel = vendorLicenseManager.getFeatureGroupModel(fgInput);
        if (featureGroupModel == null) {
            return ResponseEntity.ok().build();
        }
        FeatureGroupModelDto fgmDto = new FeatureGroupModelDto();
        fgmDto.setId(featureGroupModel.getFeatureGroup().getId());
        fgmDto.setReferencingLicenseAgreements(featureGroupModel.getFeatureGroup().getReferencingLicenseAgreements());
        new MapFeatureGroupEntityToFeatureGroupDescriptorDto().doMapping(featureGroupModel.getFeatureGroup(), fgmDto);
        if (!CollectionUtils.isEmpty(featureGroupModel.getLicenseKeyGroups())) {
            fgmDto.setLicenseKeyGroups(new HashSet<>());
            MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto lkgMapper = new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
            for (LicenseKeyGroupEntity lkg : featureGroupModel.getLicenseKeyGroups()) {
                fgmDto.getLicenseKeyGroups().add(lkgMapper.applyMapping(lkg, LicenseKeyGroupEntityDto.class));
            }
        }
        if (!CollectionUtils.isEmpty(featureGroupModel.getEntitlementPools())) {
            fgmDto.setEntitlementPools(new HashSet<>());
            MapEntitlementPoolEntityToEntitlementPoolEntityDto epMapper = new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
            for (EntitlementPoolEntity ep : featureGroupModel.getEntitlementPools()) {
                fgmDto.getEntitlementPools().add(epMapper.applyMapping(ep, EntitlementPoolEntityDto.class));
            }
        }
        return ResponseEntity.ok(fgmDto);
    }

    @Override
    public ResponseEntity deleteFeatureGroup(String vlmId, String versionId, String featureGroupId, String user) {
        FeatureGroupEntity fgInput = new FeatureGroupEntity();
        fgInput.setVendorLicenseModelId(vlmId);
        fgInput.setVersion(new Version(versionId));
        fgInput.setId(featureGroupId);
        vendorLicenseManager.deleteFeatureGroup(fgInput);
        return ResponseEntity.ok().build();
    }

    private FeatureGroupEntityDto getFeatureGroupEntityDto(MapFeatureGroupEntityToFeatureGroupDescriptorDto mapper, FeatureGroupEntity fg) {
        FeatureGroupEntityDto fgDto = new FeatureGroupEntityDto();
        fgDto.setId(fg.getId());
        fgDto.setLicenseKeyGroupsIds(fg.getLicenseKeyGroupIds());
        fgDto.setEntitlementPoolsIds(fg.getEntitlementPoolIds());
        fgDto.setReferencingLicenseAgreements(fg.getReferencingLicenseAgreements());
        mapper.doMapping(fg, fgDto);
        return fgDto;
    }
}
