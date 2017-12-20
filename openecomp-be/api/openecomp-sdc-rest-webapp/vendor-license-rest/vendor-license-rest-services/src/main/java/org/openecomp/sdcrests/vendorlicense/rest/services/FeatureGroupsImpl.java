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

import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerServiceName;
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
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupModelDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupUpdateRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;

@Named
@Service("featureGroups")
@Scope(value = "prototype")
public class FeatureGroupsImpl implements FeatureGroups {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  @Override
  public Response listFeatureGroups(String vlmId, String versionId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.List_FG.toString());
    Collection<FeatureGroupEntity> featureGroupEntities =
        vendorLicenseManager.listFeatureGroups(vlmId, new Version(versionId));

    MapFeatureGroupEntityToFeatureGroupDescriptorDto outputMapper =
        new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
    GenericCollectionWrapper<FeatureGroupEntityDto> results = new GenericCollectionWrapper<>();

    for (FeatureGroupEntity fg : featureGroupEntities) {
      FeatureGroupEntityDto fgDto = new FeatureGroupEntityDto();
      fgDto.setId(fg.getId());
      fgDto.setLicenseKeyGroupsIds(fg.getLicenseKeyGroupIds());
      fgDto.setEntitlementPoolsIds(fg.getEntitlementPoolIds());
      fgDto.setReferencingLicenseAgreements(fg.getReferencingLicenseAgreements());
      fgDto.setManufacturerReferenceNumber(fg.getManufacturerReferenceNumber());
      outputMapper.doMapping(fg, fgDto);
      results.add(fgDto);
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

    return Response.ok(results).build();
  }

  @Override
  public Response createFeatureGroup(FeatureGroupRequestDto request, String vlmId, String versionId,
                                     String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.Create_FG.toString());
    FeatureGroupEntity featureGroupEntity = new MapFeatureGroupDescriptorDtoToFeatureGroupEntity()
        .applyMapping(request, FeatureGroupEntity.class);
    featureGroupEntity.setVendorLicenseModelId(vlmId);
    featureGroupEntity.setVersion(new Version(versionId));
    featureGroupEntity.setLicenseKeyGroupIds(request.getAddedLicenseKeyGroupsIds());
    featureGroupEntity.setEntitlementPoolIds(request.getAddedEntitlementPoolsIds());

    FeatureGroupEntity createdFeatureGroup =
        vendorLicenseManager.createFeatureGroup(featureGroupEntity);

    StringWrapperResponse result =
        createdFeatureGroup != null ? new StringWrapperResponse(createdFeatureGroup.getId()) : null;

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

    return Response.ok(result).build();
  }

  @Override
  public Response updateFeatureGroup(FeatureGroupUpdateRequestDto request, String vlmId,
                                     String versionId, String featureGroupId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id", vlmId, featureGroupId);

    MdcUtil.initMdc(LoggerServiceName.Update_FG.toString());
    FeatureGroupEntity featureGroupEntity = new MapFeatureGroupDescriptorDtoToFeatureGroupEntity()
        .applyMapping(request, FeatureGroupEntity.class);
    featureGroupEntity.setVendorLicenseModelId(vlmId);
    featureGroupEntity.setVersion(new Version(versionId));
    featureGroupEntity.setId(featureGroupId);

    vendorLicenseManager
        .updateFeatureGroup(featureGroupEntity, request.getAddedLicenseKeyGroupsIds(),
            request.getRemovedLicenseKeyGroupsIds(), request.getAddedEntitlementPoolsIds(),
            request.getRemovedEntitlementPoolsIds());

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id", vlmId, featureGroupId);

    return Response.ok().build();
  }

  @Override
  public Response getFeatureGroup(String vlmId, String versionId, String featureGroupId,
                                  String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id", vlmId, featureGroupId);

    MdcUtil.initMdc(LoggerServiceName.Get_FG.toString());
    FeatureGroupEntity fgInput = new FeatureGroupEntity();
    fgInput.setVendorLicenseModelId(vlmId);
    fgInput.setVersion(new Version(versionId));
    fgInput.setId(featureGroupId);
    FeatureGroupModel featureGroupModel = vendorLicenseManager.getFeatureGroupModel(fgInput);

    if (featureGroupModel == null) {
      return Response.ok().build();
    }

    FeatureGroupModelDto fgmDto = new FeatureGroupModelDto();
    fgmDto.setId(featureGroupModel.getFeatureGroup().getId());
    fgmDto.setReferencingLicenseAgreements(
        featureGroupModel.getFeatureGroup().getReferencingLicenseAgreements());
    new MapFeatureGroupEntityToFeatureGroupDescriptorDto()
        .doMapping(featureGroupModel.getFeatureGroup(), fgmDto);

    if (!CommonMethods.isEmpty(featureGroupModel.getLicenseKeyGroups())) {
      fgmDto.setLicenseKeyGroups(new HashSet<>());

      MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto lkgMapper =
          new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
      for (LicenseKeyGroupEntity lkg : featureGroupModel.getLicenseKeyGroups()) {
        fgmDto.getLicenseKeyGroups()
            .add(lkgMapper.applyMapping(lkg, LicenseKeyGroupEntityDto.class));
      }
    }

    if (!CommonMethods.isEmpty(featureGroupModel.getEntitlementPools())) {
      fgmDto.setEntitlementPools(new HashSet<>());

      MapEntitlementPoolEntityToEntitlementPoolEntityDto epMapper =
          new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
      for (EntitlementPoolEntity ep : featureGroupModel.getEntitlementPools()) {
        fgmDto.getEntitlementPools().add(epMapper.applyMapping(ep, EntitlementPoolEntityDto.class));

      }
    }

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id", vlmId, featureGroupId);

    return Response.ok(fgmDto).build();
  }

  @Override
  public Response deleteFeatureGroup(String vlmId, String versionId, String featureGroupId,
                                     String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id", vlmId, featureGroupId);

    MdcUtil.initMdc(LoggerServiceName.Delete_FG.toString());
    FeatureGroupEntity fgInput = new FeatureGroupEntity();
    fgInput.setVendorLicenseModelId(vlmId);
    fgInput.setVersion(new Version(versionId));
    fgInput.setId(featureGroupId);
    vendorLicenseManager.deleteFeatureGroup(fgInput);

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id", vlmId, featureGroupId);

    return Response.ok().build();
  }

}
