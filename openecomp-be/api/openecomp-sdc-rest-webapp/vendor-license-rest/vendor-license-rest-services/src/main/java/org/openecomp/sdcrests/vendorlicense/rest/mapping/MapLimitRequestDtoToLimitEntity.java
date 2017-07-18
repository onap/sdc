package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.errors.LimitErrorBuilder;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ImageErrorBuilder;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;


public class MapLimitRequestDtoToLimitEntity extends MappingBase<LimitRequestDto, LimitEntity> {
  @Override
  public void doMapping(LimitRequestDto source, LimitEntity target) {
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    try {
      LimitType type = LimitType.valueOf(source.getType());
      target.setType(type);
    }
    catch (IllegalArgumentException exception) {
      throwInvalidValueError("type", VendorLicenseErrorCodes.LIMIT_INVALID_TYPE);
    }

    try {
      EntitlementMetric metric = EntitlementMetric.valueOf(source.getMetric());
      target.setMetric(metric);
    }
    catch (IllegalArgumentException exception) {
      throwInvalidValueError("metric", VendorLicenseErrorCodes.LIMIT_INVALID_METRIC);
    }

    try {
      AggregationFunction function = source.getAggregationFunction() != null ?
              AggregationFunction.valueOf(source.getAggregationFunction()) : null;
      target.setAggregationFunction(function);
    }
    catch (IllegalArgumentException exception) {
      throwInvalidValueError("aggregationFunction",
          VendorLicenseErrorCodes.LIMIT_INVALID_AGGREGATIONFUNCTION);
    }

    try {
      EntitlementTime time = source.getTime() != null ?
              EntitlementTime.valueOf(source.getTime()) : null;
      target.setTime(time);
    }
    catch (IllegalArgumentException exception) {
      throwInvalidValueError("time", VendorLicenseErrorCodes.LIMIT_INVALID_TIME);
    }

    target.setValue(source.getValue());
    target.setUnit(source.getUnit());

  }

  private void throwInvalidValueError(String attribute, String vendorLicenseErrorCode) {
    ErrorCode errorCode = LimitErrorBuilder.getInvalidValueErrorBuilder(attribute,
        vendorLicenseErrorCode);
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerServiceName.Create_LIMIT.toString(), ErrorLevel.ERROR.name(),
        errorCode.id(), errorCode.message() );
    throw new CoreException(errorCode);
  }
}
