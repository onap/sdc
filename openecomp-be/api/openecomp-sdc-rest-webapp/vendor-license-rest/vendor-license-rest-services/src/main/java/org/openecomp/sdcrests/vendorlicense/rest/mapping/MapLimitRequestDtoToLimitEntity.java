/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.vendorlicense.errors.LimitErrorBuilder;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseErrorCodes;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;

public class MapLimitRequestDtoToLimitEntity extends MappingBase<LimitRequestDto, LimitEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MapLimitRequestDtoToLimitEntity.class);

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
            AggregationFunction function =
                source.getAggregationFunction() != null ? AggregationFunction.valueOf(source.getAggregationFunction()) : null;
            target.setAggregationFunction(function);
        } catch (IllegalArgumentException exception) {
            logger.error(exception.getMessage(), exception);
            throwInvalidValueError("aggregationFunction", VendorLicenseErrorCodes.LIMIT_INVALID_AGGREGATIONFUNCTION);
        }
        target.setTime(source.getTime());
        target.setMetric(source.getMetric());
        target.setValue(source.getValue());
        target.setUnit(source.getUnit());
    }

    private void throwInvalidValueError(String attribute, String vendorLicenseErrorCode) {
        ErrorCode errorCode = LimitErrorBuilder.getInvalidValueErrorBuilder(attribute, vendorLicenseErrorCode);
        throw new CoreException(errorCode);
    }
}
