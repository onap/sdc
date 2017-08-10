package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.vendorlicense.errors.LimitErrorBuilder;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;


public class MapLimitRequestDtoToLimitEntity extends MappingBase<LimitRequestDto, LimitEntity> {

    private static final Logger logger =
            LoggerFactory.getLogger(MapLimitRequestDtoToLimitEntity.class);

    @Override
    public void doMapping(LimitRequestDto source, LimitEntity target) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        try {
            LimitType type = LimitType.valueOf(source.getType());
            target.setType(type);
        } catch (IllegalArgumentException exception) {
            logger.error(exception.getMessage(), exception);
            throwInvalidValueError("type", VendorLicenseErrorCodes.LIMIT_INVALID_TYPE);
        }

        try {
            AggregationFunction function = source.getAggregationFunction() != null ?
                    AggregationFunction.valueOf(source.getAggregationFunction()) : null;
            target.setAggregationFunction(function);
        } catch (IllegalArgumentException exception) {
            logger.error(exception.getMessage(), exception);
            throwInvalidValueError("aggregationFunction",
                    VendorLicenseErrorCodes.LIMIT_INVALID_AGGREGATIONFUNCTION);
        }

        target.setTime(source.getTime());
        target.setMetric(source.getMetric());
        target.setValue(source.getValue());
        target.setUnit(source.getUnit());

    }

    private void throwInvalidValueError(String attribute, String vendorLicenseErrorCode) {
        ErrorCode errorCode = LimitErrorBuilder.getInvalidValueErrorBuilder(attribute,
                vendorLicenseErrorCode);
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerServiceName.Create_LIMIT.toString(), ErrorLevel.ERROR.name(),
                errorCode.id(), errorCode.message());
        throw new CoreException(errorCode);
    }
}
