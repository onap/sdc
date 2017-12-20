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

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;

@Named
@Service("licenseAgreements")
@Scope(value = "prototype")
public class LicenseAgreementsImpl implements LicenseAgreements {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  /**
   * List license agreements response.
   *
   * @param vlmId     the vlm id
   * @param versionId the version
   * @param user      the user
   * @return the response
   */
  public Response listLicenseAgreements(String vlmId, String versionId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.List_LA.toString());
    Collection<LicenseAgreementEntity> licenseAgreements =
        vendorLicenseManager.listLicenseAgreements(vlmId, new Version(versionId));

    GenericCollectionWrapper<LicenseAgreementEntityDto> results = new GenericCollectionWrapper<>();
    MapLicenseAgreementEntityToLicenseAgreementDescriptorDto outputMapper =
        new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
    for (LicenseAgreementEntity lae : licenseAgreements) {
      LicenseAgreementEntityDto laeDto = new LicenseAgreementEntityDto();
      laeDto.setId(lae.getId());
      laeDto.setFeatureGroupsIds(lae.getFeatureGroupIds());
      outputMapper.doMapping(lae, laeDto);
      results.add(laeDto);
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

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
  public Response createLicenseAgreement(LicenseAgreementRequestDto request, String vlmId,
                                         String versionId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.Create_LA.toString());
    LicenseAgreementEntity licenseAgreementEntity =
        new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity()
            .applyMapping(request, LicenseAgreementEntity.class);
    licenseAgreementEntity.setVendorLicenseModelId(vlmId);
    licenseAgreementEntity.setVersion(new Version(versionId));
    licenseAgreementEntity.setFeatureGroupIds(request.getAddedFeatureGroupsIds());

    LicenseAgreementEntity createdLicenseAgreement =
        vendorLicenseManager.createLicenseAgreement(licenseAgreementEntity);
    StringWrapperResponse result =
        createdLicenseAgreement != null ? new StringWrapperResponse(createdLicenseAgreement.getId())
            : null;

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

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
  public Response updateLicenseAgreement(LicenseAgreementUpdateRequestDto request, String vlmId,
                                         String versionId, String licenseAgreementId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);

    MdcUtil.initMdc(LoggerServiceName.Update_LA.toString());
    LicenseAgreementEntity licenseAgreementEntity =
        new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity()
            .applyMapping(request, LicenseAgreementEntity.class);
    licenseAgreementEntity.setVendorLicenseModelId(vlmId);
    licenseAgreementEntity.setVersion(new Version(versionId));
    licenseAgreementEntity.setId(licenseAgreementId);

    vendorLicenseManager
        .updateLicenseAgreement(licenseAgreementEntity, request.getAddedFeatureGroupsIds(),
            request.getRemovedFeatureGroupsIds());

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);

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
  public Response getLicenseAgreement(String vlmId, String versionId, String licenseAgreementId,
                                      String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);

    MdcUtil.initMdc(LoggerServiceName.Get_LA.toString());
    LicenseAgreementModel licenseAgreementModel = vendorLicenseManager
        .getLicenseAgreementModel(vlmId, new Version(versionId), licenseAgreementId);

    if (licenseAgreementModel == null) {
      return Response.ok().build();
    }

    LicenseAgreementModelDto lamDto = new LicenseAgreementModelDto();
    lamDto.setId(licenseAgreementModel.getLicenseAgreement().getId());
    new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto()
        .doMapping(licenseAgreementModel.getLicenseAgreement(), lamDto);

    if (!CommonMethods.isEmpty(licenseAgreementModel.getFeatureGroups())) {
      lamDto.setFeatureGroups(new HashSet<>());

      MapFeatureGroupEntityToFeatureGroupDescriptorDto fgMapper =
          new MapFeatureGroupEntityToFeatureGroupDescriptorDto();
      for (FeatureGroupEntity fg : licenseAgreementModel.getFeatureGroups()) {
        FeatureGroupEntityDto fgeDto = new FeatureGroupEntityDto();
        fgeDto.setId(fg.getId());
        fgeDto.setEntitlementPoolsIds(fg.getEntitlementPoolIds());
        fgeDto.setLicenseKeyGroupsIds(fg.getLicenseKeyGroupIds());
        fgMapper.doMapping(fg, fgeDto);

        lamDto.getFeatureGroups().add(fgeDto);
      }
    }

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);

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
  public Response deleteLicenseAgreement(String vlmId, String versionId, String licenseAgreementId,
                                         String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);

    MdcUtil.initMdc(LoggerServiceName.Delete_LA.toString());
    vendorLicenseManager.deleteLicenseAgreement(vlmId, new Version(versionId), licenseAgreementId);

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);

    return Response.ok().build();
  }
}
