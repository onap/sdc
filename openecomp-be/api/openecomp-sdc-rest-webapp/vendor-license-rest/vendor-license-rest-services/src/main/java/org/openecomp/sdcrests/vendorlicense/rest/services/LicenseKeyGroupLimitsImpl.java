package org.openecomp.sdcrests.vendorlicense.rest.services;

import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorlicense.rest.LicenseKeyGroupLimits;
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
@Service("licenseKeyGroupLimits")
@Scope(value = "prototype")
public class LicenseKeyGroupLimitsImpl implements LicenseKeyGroupLimits {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  public static final String parent = "LicenseKeyGroup";


  @Override
  public Response createLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String licenseKeyGroupId,
                              String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId);

    MdcUtil.initMdc(LoggerServiceName.Create_LIMIT.toString());
    Version version = new Version(versionId);
    vendorLicenseManager.getLicenseKeyGroup(
        new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(licenseKeyGroupId);
    limitEntity.setParent(parent);

    LimitEntity createdLimit = vendorLicenseManager.createLimit(limitEntity);
    MapLimitEntityToLimitCreationDto mapper = new MapLimitEntityToLimitCreationDto();
    LimitCreationDto createdLimitDto = mapper.applyMapping(createdLimit, LimitCreationDto.class);
    /*StringWrapperResponse result =
        createdLimit != null ? new StringWrapperResponse(createdLimit.getId())
            : null;*/

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId);

    //return Response.ok(result).build();
    return Response.ok(createdLimitDto != null ? createdLimitDto : null).build();
  }

  @Override
  public Response listLimits(String vlmId, String versionId, String licenseKeyGroupId,
                             String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId);

    MdcUtil.initMdc(LoggerServiceName.List_EP.toString());
    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    Collection<LimitEntity> limits =
        vendorLicenseManager.listLimits(vlmId, version, licenseKeyGroupId);

    GenericCollectionWrapper<LimitEntityDto> result = new GenericCollectionWrapper<>();
    MapLimitEntityToLimitDto outputMapper =
        new MapLimitEntityToLimitDto();
    for (LimitEntity limit : limits) {
      result.add(outputMapper.applyMapping(limit, LimitEntityDto.class));
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId);

    return Response.ok(result).build();
  }

  @Override
  public Response updateLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String licenseKeyGroupId,
                              String limitId,
                              String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId, "limit Id",
        limitId);

    MdcUtil.initMdc(LoggerServiceName.Update_LIMIT.toString());

    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(licenseKeyGroupId);
    limitEntity.setId(limitId);
    limitEntity.setParent(parent);

    vendorLicenseManager.updateLimit(limitEntity);

    mdcDataDebugMessage
        .debugExitMessage("VLM id", vlmId, "LKG id", licenseKeyGroupId, "limit Id", limitId);

    return Response.ok().build();
  }

  /**
   * Delete License Key Group.
   *
   * @param vlmId             the vlm id
   * @param licenseKeyGroupId the license Key Group id
   * @param limitId           the limitId
   * @param user              the user
   * @return the response
   */
  public Response deleteLimit(String vlmId, String versionId, String licenseKeyGroupId,
                              String limitId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, Verison Id, LKG id, Limit Id", vlmId, versionId,
        licenseKeyGroupId, limitId);
    MdcUtil.initMdc(LoggerServiceName.Delete_LIMIT.toString());

    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitInput = new LimitEntity();
    limitInput.setVendorLicenseModelId(vlmId);
    limitInput.setVersion(version);
    limitInput.setEpLkgId(licenseKeyGroupId);
    limitInput.setId(limitId);
    limitInput.setParent(parent);

    vendorLicenseManager.deleteLimit(limitInput);

    mdcDataDebugMessage.debugExitMessage("VLM id, Verison Id, LKG id, Limit Id", vlmId, versionId,
        licenseKeyGroupId, limitId);

    return Response.ok().build();
  }

  @Override
  public Response getLimit(String vlmId, String versionId, String licenseKeyGroupId,
                           String limitId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id, Limit Id", vlmId, licenseKeyGroupId,
        limitId);

    MdcUtil.initMdc(LoggerServiceName.Get_LIMIT.toString());
    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));
    LimitEntity limitInput = new LimitEntity();
    limitInput.setVendorLicenseModelId(vlmId);
    limitInput.setVersion(version);
    limitInput.setEpLkgId(licenseKeyGroupId);
    limitInput.setId(limitId);
    LimitEntity limit = vendorLicenseManager.getLimit(limitInput);

    LimitEntityDto entitlementPoolEntityDto = limit == null ? null
        : new MapLimitEntityToLimitDto().applyMapping(limit, LimitEntityDto.class);

    mdcDataDebugMessage
        .debugExitMessage("VLM id, LKG id, Limit Id", vlmId, licenseKeyGroupId, limitId);

    return Response.ok(entitlementPoolEntityDto).build();
  }
}
