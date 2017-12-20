package org.openecomp.sdcrests.vendorlicense.rest.services;


import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.EntitlementPoolLimits;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.LimitCreationDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitEntityToLimitCreationDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitEntityToLimitDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapLimitRequestDtoToLimitEntity;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Named
@Service("entitlementPoolLimits")
@Scope(value = "prototype")
public class EntitlementPoolLimitsImpl implements EntitlementPoolLimits {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  public static final String parent = "EntitlementPool";

  @Override
  public Response createLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String entitlementPoolId,
                              String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "EP id", entitlementPoolId);

    MdcUtil.initMdc(LoggerServiceName.Create_LIMIT.toString());
    Version version = new Version(versionId);
    vendorLicenseManager
        .getEntitlementPool(new EntitlementPoolEntity(vlmId, version, entitlementPoolId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(entitlementPoolId);
    limitEntity.setParent(parent);

    LimitEntity createdLimit = vendorLicenseManager.createLimit(limitEntity);
    MapLimitEntityToLimitCreationDto mapper = new MapLimitEntityToLimitCreationDto();
    LimitCreationDto createdLimitDto = mapper.applyMapping(createdLimit, LimitCreationDto.class);

    /*StringWrapperResponse result =
        createdLimit != null ? new StringWrapperResponse(createdLimit.getId())
            : null;*/

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "EP id", entitlementPoolId);

    //return Response.ok(result).build();
    return Response.ok(createdLimitDto != null ? createdLimitDto : null).build();
  }

  @Override
  public Response listLimits(String vlmId, String versionId, String entitlementPoolId, String
      user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "EP id", entitlementPoolId);

    MdcUtil.initMdc(LoggerServiceName.List_EP.toString());
    Version version = new Version(versionId);
    vendorLicenseManager
        .getEntitlementPool(new EntitlementPoolEntity(vlmId, version, entitlementPoolId));

    Collection<LimitEntity> limits =
        vendorLicenseManager.listLimits(vlmId, version, entitlementPoolId);

    GenericCollectionWrapper<LimitEntityDto> result = new GenericCollectionWrapper<>();
    MapLimitEntityToLimitDto outputMapper =
        new MapLimitEntityToLimitDto();
    for (LimitEntity limit : limits) {
      result.add(outputMapper.applyMapping(limit, LimitEntityDto.class));
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "EP id", entitlementPoolId);

    return Response.ok(result).build();
  }

  @Override
  public Response getLimit(String vlmId, String versionId, String entitlementPoolId,
                           String limitId, String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id, EP id, Limit Id", vlmId, entitlementPoolId, limitId);

    MdcUtil.initMdc(LoggerServiceName.Get_LIMIT.toString());

    Version version = new Version(versionId);
    vendorLicenseManager
        .getEntitlementPool(new EntitlementPoolEntity(vlmId, version, entitlementPoolId));
    LimitEntity limitInput = new LimitEntity();
    limitInput.setVendorLicenseModelId(vlmId);
    limitInput.setVersion(version);
    limitInput.setEpLkgId(entitlementPoolId);
    limitInput.setId(limitId);
    LimitEntity limit = vendorLicenseManager.getLimit(limitInput);

    LimitEntityDto entitlementPoolEntityDto = limit == null ? null :
        new MapLimitEntityToLimitDto().applyMapping(limit, LimitEntityDto.class);

    mdcDataDebugMessage
        .debugExitMessage("VLM id, EP id, Limit Id", vlmId, entitlementPoolId, limitId);

    return Response.ok(entitlementPoolEntityDto).build();
  }

  @Override
  public Response updateLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String entitlementPoolId,
                              String limitId,
                              String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", vlmId, "EP id", entitlementPoolId, "limit Id", limitId);

    MdcUtil.initMdc(LoggerServiceName.Update_LIMIT.toString());

    Version version = new Version(versionId);
    vendorLicenseManager
        .getEntitlementPool(new EntitlementPoolEntity(vlmId, version, entitlementPoolId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(entitlementPoolId);
    limitEntity.setId(limitId);
    limitEntity.setParent(parent);

    vendorLicenseManager.updateLimit(limitEntity);

    mdcDataDebugMessage
        .debugExitMessage("VLM id", vlmId, "EP id", entitlementPoolId, "limit Id", limitId);

    return Response.ok().build();
  }

  /**
   * Delete entitlement pool.
   *
   * @param vlmId             the vlm id
   * @param entitlementPoolId the entitlement pool id
   * @param limitId           the limitId
   * @param user              the user
   * @return the response
   */
  public Response deleteLimit(String vlmId, String versionId, String entitlementPoolId,
                              String limitId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, Verison Id, EP id, Limit Id", vlmId, versionId,
        entitlementPoolId, limitId);
    MdcUtil.initMdc(LoggerServiceName.Delete_LIMIT.toString());

    Version version = new Version(versionId);
    vendorLicenseManager.getEntitlementPool(
        new EntitlementPoolEntity(vlmId, version, entitlementPoolId));

    LimitEntity limitInput = new LimitEntity();
    limitInput.setVendorLicenseModelId(vlmId);
    limitInput.setVersion(version);
    limitInput.setEpLkgId(entitlementPoolId);
    limitInput.setId(limitId);
    limitInput.setParent(parent);

    vendorLicenseManager.deleteLimit(limitInput);

    mdcDataDebugMessage.debugExitMessage("VLM id, Verison Id, EP id, Limit Id", vlmId, versionId,
        entitlementPoolId, limitId);

    return Response.ok().build();
  }
}
