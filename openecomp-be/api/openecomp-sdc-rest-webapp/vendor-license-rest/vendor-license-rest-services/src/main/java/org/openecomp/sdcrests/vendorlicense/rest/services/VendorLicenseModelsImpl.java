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

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.VendorLicenseModels;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("vendorLicenseModels")
@Scope(value = "prototype")
@Validated
public class VendorLicenseModelsImpl implements VendorLicenseModels {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  private static final Logger logger =
          LoggerFactory.getLogger(VendorLicenseModelsImpl.class);

    @Override
    public Response listLicenseModels(String versionFilter, String user) {

        mdcDataDebugMessage.debugEntryMessage(null, null);
        MdcUtil.initMdc(LoggerServiceName.List_VLM.toString());
        Collection<VersionedVendorLicenseModel> versionedVendorLicenseModels =
                vendorLicenseManager.listVendorLicenseModels(versionFilter, user);

        GenericCollectionWrapper<VendorLicenseModelEntityDto> results =
                new GenericCollectionWrapper<>();
        MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto outputMapper =
                new MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto();
        for (VersionedVendorLicenseModel versionedVlm : versionedVendorLicenseModels) {
            results.add(outputMapper.applyMapping(versionedVlm, VendorLicenseModelEntityDto.class));
        }

        mdcDataDebugMessage.debugExitMessage(null, null);

        return Response.ok(results).build();
    }

    @Override
    public Response createLicenseModel(VendorLicenseModelRequestDto request, String user) {

        mdcDataDebugMessage.debugEntryMessage(null, null);

    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CREATE_VLM
            + request.getVendorName());

    MdcUtil.initMdc(LoggerServiceName.Create_VLM.toString());
    VendorLicenseModelEntity vendorLicenseModelEntity =
        new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
            .applyMapping(request, VendorLicenseModelEntity.class);
    VendorLicenseModelEntity createdVendorLicenseModel =
        vendorLicenseManager.createVendorLicenseModel(vendorLicenseModelEntity, user);
    StringWrapperResponse result = createdVendorLicenseModel != null ? new StringWrapperResponse(
        createdVendorLicenseModel.getId()) : null;

        mdcDataDebugMessage.debugExitMessage(null, null);

        return Response.ok(result).build();
    }

    @Override
    public Response updateLicenseModel(VendorLicenseModelRequestDto request, String vlmId,
                                       String versionId, String user) {

        mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

        MdcUtil.initMdc(LoggerServiceName.Update_VLM.toString());
        VendorLicenseModelEntity vendorLicenseModelEntity =
                new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
                        .applyMapping(request, VendorLicenseModelEntity.class);
        vendorLicenseModelEntity.setId(vlmId);

        vendorLicenseManager.updateVendorLicenseModel(vendorLicenseModelEntity, user);

        mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

        return Response.ok().build();
    }

    @Override
    public Response getLicenseModel(String vlmId, String versionId, String user) {

        mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

        MdcUtil.initMdc(LoggerServiceName.Get_VLM.toString());
        VersionedVendorLicenseModel versionedVlm =
                vendorLicenseManager.getVendorLicenseModel(vlmId, Version.valueOf(versionId), user);

        VendorLicenseModelEntityDto vlmDto = versionedVlm == null ? null :
                new MapVersionedVendorLicenseModelToVendorLicenseModelEntityDto()
                        .applyMapping(versionedVlm, VendorLicenseModelEntityDto.class);

        mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

        return Response.ok(vlmDto).build();
    }

    @Override
    public Response deleteLicenseModel(String vlmId, String versionId, String user) {

        mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

        MdcUtil.initMdc(LoggerServiceName.Delete_VLM.toString());
        vendorLicenseManager.deleteVendorLicenseModel(vlmId, user);

        mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

        return Response.ok().build();
    }

    @Override
    public Response actOnLicenseModel(VendorLicenseModelActionRequestDto request, String vlmId,
                                      String versionId, String user) {

    switch (request.getAction()) {
      case Checkout:
        MDC.put(LoggerConstants.SERVICE_NAME,
            LoggerServiceName.Checkout_VLM.toString());
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CHECK_OUT_VLM
                + vlmId);
        vendorLicenseManager.checkout(vlmId, user);
        break;
      case Undo_Checkout:
        MDC.put(LoggerConstants.SERVICE_NAME,
            LoggerServiceName.Undo_Checkout_VLM.toString());
        vendorLicenseManager.undoCheckout(vlmId, user);
        break;
      case Checkin:
        MDC.put(LoggerConstants.SERVICE_NAME,
            LoggerServiceName.Checkin_VLM.toString());
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CHECK_IN_VLM
                + vlmId);
        vendorLicenseManager.checkin(vlmId, user);
        break;
      case Submit:
        MDC.put(LoggerConstants.SERVICE_NAME,
            LoggerServiceName.Submit_VLM.toString());
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VLM
                + vlmId);
        vendorLicenseManager.submit(vlmId, user);
        break;
      default:
    }

        return Response.ok().build();
    }
}
