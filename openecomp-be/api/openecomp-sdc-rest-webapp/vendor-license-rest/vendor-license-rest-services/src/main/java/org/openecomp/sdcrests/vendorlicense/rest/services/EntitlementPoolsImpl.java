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

import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.EntitlementPools;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapEntitlementPoolEntityToEntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapEntitlementPoolRequestDtoToEntitlementPoolEntity;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("entitlementPools")
@Scope(value = "prototype")
public class EntitlementPoolsImpl implements EntitlementPools {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  @Autowired
  private VendorLicenseManager vendorLicenseManager;

  /**
   * List entitlement pools response.
   *
   * @param vlmId     the vlm id
   * @param versionId the version
   * @param user      the user
   * @return the response
   */
  public Response listEntitlementPools(String vlmId, String versionId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.List_EP.toString());
    Collection<EntitlementPoolEntity> entitlementPools =
        vendorLicenseManager.listEntitlementPools(vlmId, Version.valueOf(versionId), user);

    GenericCollectionWrapper<EntitlementPoolEntityDto> result = new GenericCollectionWrapper<>();
    MapEntitlementPoolEntityToEntitlementPoolEntityDto outputMapper =
        new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
    for (EntitlementPoolEntity ep : entitlementPools) {
      result.add(outputMapper.applyMapping(ep, EntitlementPoolEntityDto.class));
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

    return Response.ok(result).build();
  }

  /**
   * Create entitlement pool response.
   *
   * @param request the request
   * @param vlmId   the vlm id
   * @param user    the user
   * @return the response
   */
  public Response createEntitlementPool(EntitlementPoolRequestDto request, String vlmId,
                                        String versionId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    MdcUtil.initMdc(LoggerServiceName.Create_EP.toString());
    EntitlementPoolEntity entitlementPoolEntity =
        new MapEntitlementPoolRequestDtoToEntitlementPoolEntity()
            .applyMapping(request, EntitlementPoolEntity.class);
    entitlementPoolEntity.setVendorLicenseModelId(vlmId);

    EntitlementPoolEntity createdEntitlementPool =
        vendorLicenseManager.createEntitlementPool(entitlementPoolEntity, user);
    StringWrapperResponse result =
        createdEntitlementPool != null ? new StringWrapperResponse(createdEntitlementPool.getId())
            : null;

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);

    return Response.ok(result).build();
  }

  /**
   * Update entitlement pool response.
   *
   * @param request           the request
   * @param vlmId             the vlm id
   * @param entitlementPoolId the entitlement pool id
   * @param user              the user
   * @return the response
   */
  public Response updateEntitlementPool(EntitlementPoolRequestDto request, String vlmId,
                                        String versionId, String entitlementPoolId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", vlmId, entitlementPoolId);

    MdcUtil.initMdc(LoggerServiceName.Update_EP.toString());
    EntitlementPoolEntity entitlementPoolEntity =
        new MapEntitlementPoolRequestDtoToEntitlementPoolEntity()
            .applyMapping(request, EntitlementPoolEntity.class);
    entitlementPoolEntity.setVendorLicenseModelId(vlmId);
    entitlementPoolEntity.setId(entitlementPoolId);

    vendorLicenseManager.updateEntitlementPool(entitlementPoolEntity, user);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", vlmId, entitlementPoolId);

    return Response.ok().build();
  }

  /**
   * Gets entitlement pool.
   *
   * @param vlmId             the vlm id
   * @param version           the version
   * @param entitlementPoolId the entitlement pool id
   * @param user              the user
   * @return the entitlement pool
   */
  public Response getEntitlementPool(String vlmId, String version, String entitlementPoolId,
                                     String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", vlmId, entitlementPoolId);

    MdcUtil.initMdc(LoggerServiceName.Get_EP.toString());
    EntitlementPoolEntity epInput = new EntitlementPoolEntity();
    epInput.setVendorLicenseModelId(vlmId);
    epInput.setVersion(Version.valueOf(version));
    epInput.setId(entitlementPoolId);
    EntitlementPoolEntity entitlementPool = vendorLicenseManager.getEntitlementPool(epInput, user);

    EntitlementPoolEntityDto entitlementPoolEntityDto = entitlementPool == null ? null :
        new MapEntitlementPoolEntityToEntitlementPoolEntityDto()
            .applyMapping(entitlementPool, EntitlementPoolEntityDto.class);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", vlmId, entitlementPoolId);

    return Response.ok(entitlementPoolEntityDto).build();
  }

  /**
   * Delete entitlement pool response.
   *
   * @param vlmId             the vlm id
   * @param entitlementPoolId the entitlement pool id
   * @param user              the user
   * @return the response
   */
  public Response deleteEntitlementPool(String vlmId, String versionId, String entitlementPoolId,
                                        String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", vlmId, entitlementPoolId);

    MdcUtil.initMdc(LoggerServiceName.Delete_EP.toString());
    EntitlementPoolEntity epInput = new EntitlementPoolEntity();
    epInput.setVendorLicenseModelId(vlmId);
    epInput.setId(entitlementPoolId);
    vendorLicenseManager.deleteEntitlementPool(epInput, user);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", vlmId, entitlementPoolId);

    return Response.ok().build();
  }
}
