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
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.VendorLicenseModels;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
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
@Service("vendorLicenseModels")
@Scope(value = "prototype")
@Validated
public class VendorLicenseModelsImpl implements VendorLicenseModels {

  @Autowired
  private VendorLicenseManager vendorLicenseManager;

  @Override
  public Response listLicenseModels(String versionFilter, String user) {
    Collection<VersionedVendorLicenseModel> versionedVendorLicenseModels =
        vendorLicenseManager.listVendorLicenseModels(versionFilter, user);

    GenericCollectionWrapper<VendorLicenseModelEntityDto> results =
        new GenericCollectionWrapper<>();
    MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto outputMapper =
        new MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto();
    for (VersionedVendorLicenseModel versionedVlm : versionedVendorLicenseModels) {
      results.add(outputMapper.applyMapping(versionedVlm, VendorLicenseModelEntityDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response createLicenseModel(VendorLicenseModelRequestDto request, String user) {
    VendorLicenseModelEntity vendorLicenseModelEntity =
        new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
            .applyMapping(request, VendorLicenseModelEntity.class);
    VendorLicenseModelEntity createdVendorLicenseModel =
        vendorLicenseManager.createVendorLicenseModel(vendorLicenseModelEntity, user);
    StringWrapperResponse result = createdVendorLicenseModel != null ? new StringWrapperResponse(
        createdVendorLicenseModel.getId()) : null;

    return Response.ok(result).build();
  }

  @Override
  public Response updateLicenseModel(VendorLicenseModelRequestDto request, String vlmId,
                                     String user) {
    VendorLicenseModelEntity vendorLicenseModelEntity =
        new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
            .applyMapping(request, VendorLicenseModelEntity.class);
    vendorLicenseModelEntity.setId(vlmId);

    vendorLicenseManager.updateVendorLicenseModel(vendorLicenseModelEntity, user);
    return Response.ok().build();
  }

  @Override
  public Response getLicenseModel(String vlmId, String version, String user) {
    VersionedVendorLicenseModel versionedVlm =
        vendorLicenseManager.getVendorLicenseModel(vlmId, Version.valueOf(version), user);

    VendorLicenseModelEntityDto vlmDto = versionedVlm == null ? null :
        new MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto()
            .applyMapping(versionedVlm, VendorLicenseModelEntityDto.class);
    return Response.ok(vlmDto).build();
  }

  @Override
  public Response deleteLicenseModel(String vlmId, String user) {
    vendorLicenseManager.deleteVendorLicenseModel(vlmId, user);
    return Response.ok().build();
  }

  @Override
  public Response actOnLicenseModel(VendorLicenseModelActionRequestDto request, String vlmId,
                                    String user) {

    switch (request.getAction()) {
      case Checkout:
        vendorLicenseManager.checkout(vlmId, user);
        break;
      case Undo_Checkout:
        vendorLicenseManager.undoCheckout(vlmId, user);
        break;
      case Checkin:
        vendorLicenseManager.checkin(vlmId, user);
        break;
      case Submit:
        vendorLicenseManager.submit(vlmId, user);
        break;
      default:
    }

    return Response.ok().build();
  }
}
