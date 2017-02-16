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

import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import org.openecomp.sdcrests.vendorlicense.rest.LicenseKeyGroups;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("licenseKeyGroups")
@Scope(value = "prototype")
@Validated
public class LicenseKeyGroupsImpl implements LicenseKeyGroups {

  @Autowired
  private VendorLicenseManager vendorLicenseManager;

  /**
   * List license key groups response.
   *
   * @param vlmId   the vlm id
   * @param version the version
   * @param user    the user
   * @return the response
   */
  public Response listLicenseKeyGroups(String vlmId, String version, String user) {
    Collection<LicenseKeyGroupEntity> licenseKeyGroups =
        vendorLicenseManager.listLicenseKeyGroups(vlmId, Version.valueOf(version), user);

    GenericCollectionWrapper<LicenseKeyGroupEntityDto> result = new GenericCollectionWrapper<>();
    MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto outputMapper =
        new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto();
    for (LicenseKeyGroupEntity ep : licenseKeyGroups) {
      result.add(outputMapper.applyMapping(ep, LicenseKeyGroupEntityDto.class));
    }

    return Response.ok(result).build();
  }

  /**
   * Create license key group response.
   *
   * @param request the request
   * @param vlmId   the vlm id
   * @param user    the user
   * @return the response
   */
  public Response createLicenseKeyGroup(LicenseKeyGroupRequestDto request, String vlmId,
                                        String user) {
    LicenseKeyGroupEntity licenseKeyGroupEntity =
        new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity()
            .applyMapping(request, LicenseKeyGroupEntity.class);
    licenseKeyGroupEntity.setVendorLicenseModelId(vlmId);

    LicenseKeyGroupEntity createdLicenseKeyGroup =
        vendorLicenseManager.createLicenseKeyGroup(licenseKeyGroupEntity, user);
    StringWrapperResponse result =
        createdLicenseKeyGroup != null ? new StringWrapperResponse(createdLicenseKeyGroup.getId())
            : null;

    return Response.ok(result).build();
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
  public Response updateLicenseKeyGroup(LicenseKeyGroupRequestDto request, String vlmId,
                                        String licenseKeyGroupId, String user) {
    LicenseKeyGroupEntity licenseKeyGroupEntity =
        new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity()
            .applyMapping(request, LicenseKeyGroupEntity.class);

    licenseKeyGroupEntity.setVendorLicenseModelId(vlmId);
    licenseKeyGroupEntity.setId(licenseKeyGroupId);

    vendorLicenseManager.updateLicenseKeyGroup(licenseKeyGroupEntity, user);
    return Response.ok().build();
  }

  /**
   * Gets license key group.
   *
   * @param vlmId             the vlm id
   * @param version           the version
   * @param licenseKeyGroupId the license key group id
   * @param user              the user
   * @return the license key group
   */
  public Response getLicenseKeyGroup(String vlmId, String version, String licenseKeyGroupId,
                                     String user) {
    LicenseKeyGroupEntity lkgInput = new LicenseKeyGroupEntity();
    lkgInput.setVendorLicenseModelId(vlmId);
    lkgInput.setVersion(Version.valueOf(version));
    lkgInput.setId(licenseKeyGroupId);
    LicenseKeyGroupEntity licenseKeyGroup = vendorLicenseManager.getLicenseKeyGroup(lkgInput, user);

    LicenseKeyGroupEntityDto licenseKeyGroupEntityDto = licenseKeyGroup == null ? null :
        new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto()
            .applyMapping(licenseKeyGroup, LicenseKeyGroupEntityDto.class);
    return Response.ok(licenseKeyGroupEntityDto).build();
  }

  /**
   * Delete license key group response.
   *
   * @param vlmId             the vlm id
   * @param licenseKeyGroupId the license key group id
   * @param user              the user
   * @return the response
   */
  public Response deleteLicenseKeyGroup(String vlmId, String licenseKeyGroupId, String user) {
    LicenseKeyGroupEntity lkgInput = new LicenseKeyGroupEntity();
    lkgInput.setVendorLicenseModelId(vlmId);
    lkgInput.setId(licenseKeyGroupId);
    vendorLicenseManager.deleteLicenseKeyGroup(lkgInput, user);
    return Response.ok().build();
  }
}
