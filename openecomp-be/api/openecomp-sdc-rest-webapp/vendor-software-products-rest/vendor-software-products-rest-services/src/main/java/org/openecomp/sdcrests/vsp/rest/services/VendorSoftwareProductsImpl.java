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

package org.openecomp.sdcrests.vsp.rest.services;

import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VersionSoftwareProductActionRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;
import org.openecomp.sdcrests.vsp.rest.VendorSoftwareProducts;
import org.openecomp.sdcrests.vsp.rest.mapping.MapPackageInfoToPackageInfoDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapValidationResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapVersionedVendorSoftwareProductInfoToVspDetailsDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapVspDescriptionDtoToVspDetails;
import org.openecomp.sdcrests.vsp.rest.mapping.MspVspDetailsToVspCreationDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("vendorSoftwareProducts")
@Scope(value = "prototype")
public class VendorSoftwareProductsImpl implements VendorSoftwareProducts {

  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response createNewVsp(VspDescriptionDto vspDescriptionDto, String user) {
    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);

    vspDetails = vendorSoftwareProductManager.createNewVsp(vspDetails, user);

    MspVspDetailsToVspCreationDto mapping = new MspVspDetailsToVspCreationDto();
    VspCreationDto vspCreationDto = mapping.applyMapping(vspDetails, VspCreationDto.class);

    return Response.ok(vspCreationDto).build();
  }

  @Override
  public Response getVspList(String versionFilter, String user) {
    List<VersionedVendorSoftwareProductInfo> vspList =
        vendorSoftwareProductManager.getVspList(versionFilter, user);

    GenericCollectionWrapper<VspDetailsDto> results = new GenericCollectionWrapper<>();
    if (!vspList.isEmpty()) {
      MapVersionedVendorSoftwareProductInfoToVspDetailsDto mapper =
          new MapVersionedVendorSoftwareProductInfoToVspDetailsDto();
      for (VersionedVendorSoftwareProductInfo versionedVsp : vspList) {
        results.add(mapper.applyMapping(versionedVsp, VspDetailsDto.class));
      }
    }

    return Response.ok(results).build();
  }

  @Override
  public Response getVspDetails(String vspId, String version, String user) {
    VersionedVendorSoftwareProductInfo vspDetails =
        vendorSoftwareProductManager.getVspDetails(vspId, Version.valueOf(version), user);

    VspDetailsDto vspDetailsDto = vspDetails == null ? null
        : new MapVersionedVendorSoftwareProductInfoToVspDetailsDto()
            .applyMapping(vspDetails, VspDetailsDto.class);

    return Response.ok(vspDetailsDto).build();
  }

  @Override
  public Response updateVsp(String vspId, VspDescriptionDto vspDescriptionDto, String user) {
    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);
    vspDetails.setId(vspId);

    vendorSoftwareProductManager.updateVsp(vspDetails, user);

    return Response.ok().build();
  }

  @Override
  public Response deleteVsp(String vspId, String user) {
    vendorSoftwareProductManager.deleteVsp(vspId, user);

    return Response.ok().build();
  }

  @Override
  public Response actOnVendorSoftwareProduct(String vspId,
                                             VersionSoftwareProductActionRequestDto request,
                                             String user) throws IOException {
    switch (request.getAction()) {
      case Checkout:
        vendorSoftwareProductManager.checkout(vspId, user);
        break;
      case Undo_Checkout:
        vendorSoftwareProductManager.undoCheckout(vspId, user);
        break;
      case Checkin:
        vendorSoftwareProductManager.checkin(vspId, user);
        break;
      case Submit:
        ValidationResponse validationResponse = vendorSoftwareProductManager.submit(vspId, user);
        if (!validationResponse.isValid()) {
          return Response.status(Response.Status.EXPECTATION_FAILED).entity(
              new MapValidationResponseToDto()
                  .applyMapping(validationResponse, ValidationResponseDto.class)).build();
        }
        break;
      case Create_Package:
        PackageInfo packageInfo = vendorSoftwareProductManager.createPackage(vspId, user);
        return Response.ok(packageInfo == null ? null
            : new MapPackageInfoToPackageInfoDto().applyMapping(packageInfo, PackageInfoDto.class))
            .build();
      default:
    }

    return Response.ok().build();
  }

  @Override
  public Response uploadFile(String uploadVspId, InputStream heatFileToUpload, String user) {
    UploadFileResponse uploadFileResponse =
        vendorSoftwareProductManager.uploadFile(uploadVspId, heatFileToUpload, user);

    UploadFileResponseDto uploadFileResponseDto = new MapUploadFileResponseToUploadFileResponseDto()
        .applyMapping(uploadFileResponse, UploadFileResponseDto.class);

    return Response.ok(uploadFileResponseDto).build();
  }

  @Override
  public Response getLatestHeatPackage(String vspId, String user) {
    File zipFile = vendorSoftwareProductManager.getLatestHeatPackage(vspId, user);

    if (zipFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Response.ResponseBuilder response = Response.ok(zipFile);
    response.header("Content-Disposition", "attachment; filename=" + zipFile.getName());
    return response.build();
  }

  @Override
  public Response listPackages(String category, String subCategory, String user) {
    List<PackageInfo> packageInfoList =
        vendorSoftwareProductManager.listPackages(category, subCategory);

    GenericCollectionWrapper<PackageInfoDto> results = new GenericCollectionWrapper<>();
    MapPackageInfoToPackageInfoDto mapper = new MapPackageInfoToPackageInfoDto();

    if (packageInfoList != null) {
      for (PackageInfo packageInfo : packageInfoList) {
        results.add(mapper.applyMapping(packageInfo, PackageInfoDto.class));
      }
    }
    return Response.ok(results).build();
  }

  @Override
  public Response getTranslatedFile(String vspId, String version, String user) {
    File zipFile =
        vendorSoftwareProductManager.getTranslatedFile(vspId, Version.valueOf(version), user);

    Response.ResponseBuilder response = Response.ok(zipFile);
    if (zipFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header("Content-Disposition", "attachment; filename=" + zipFile.getName());
    return response.build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String version, String user) {
    QuestionnaireResponse questionnaireResponse =
        vendorSoftwareProductManager.getVspQuestionnaire(vspId, Version.valueOf(version), user);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String user) {
    vendorSoftwareProductManager.updateVspQuestionnaire(vspId, questionnaireData, user);
    return Response.ok().build();
  }
}
