package org.openecomp.sdcrests.vendorlicense.rest.services;

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
  private VendorLicenseManager vendorLicenseManager =
      VendorLicenseManagerFactory.getInstance().createInterface();

  private static final String PARENT = "LicenseKeyGroup";


  @Override
  public Response createLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String licenseKeyGroupId,
                              String user) {
    Version version = new Version(versionId);
    vendorLicenseManager.getLicenseKeyGroup(
        new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(licenseKeyGroupId);
    limitEntity.setParent(PARENT);

    LimitEntity createdLimit = vendorLicenseManager.createLimit(limitEntity);
    MapLimitEntityToLimitCreationDto mapper = new MapLimitEntityToLimitCreationDto();
    LimitCreationDto createdLimitDto = mapper.applyMapping(createdLimit, LimitCreationDto.class);
    return Response.ok(createdLimitDto != null ? createdLimitDto : null).build();
  }

  @Override
  public Response listLimits(String vlmId, String versionId, String licenseKeyGroupId,
                             String user) {
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
    return Response.ok(result).build();
  }

  @Override
  public Response updateLimit(LimitRequestDto request,
                              String vlmId,
                              String versionId,
                              String licenseKeyGroupId,
                              String limitId,
                              String user) {
    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitEntity =
        new MapLimitRequestDtoToLimitEntity().applyMapping(request, LimitEntity.class);
    limitEntity.setVendorLicenseModelId(vlmId);
    limitEntity.setVersion(version);
    limitEntity.setEpLkgId(licenseKeyGroupId);
    limitEntity.setId(limitId);
    limitEntity.setParent(PARENT);

    vendorLicenseManager.updateLimit(limitEntity);
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
    Version version = new Version(versionId);
    vendorLicenseManager
        .getLicenseKeyGroup(new LicenseKeyGroupEntity(vlmId, version, licenseKeyGroupId));

    LimitEntity limitInput = new LimitEntity();
    limitInput.setVendorLicenseModelId(vlmId);
    limitInput.setVersion(version);
    limitInput.setEpLkgId(licenseKeyGroupId);
    limitInput.setId(limitId);
    limitInput.setParent(PARENT);

    vendorLicenseManager.deleteLimit(limitInput);
    return Response.ok().build();
  }

  @Override
  public Response getLimit(String vlmId, String versionId, String licenseKeyGroupId,
                           String limitId, String user) {
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
    return Response.ok(entitlementPoolEntityDto).build();
  }
}
