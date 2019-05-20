package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataTypesService {

    private final ComponentsUtils componentsUtils;

    public DataTypesService(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    public Either<Map<String, DataTypeDefinition>, ResponseFormat> getAllDataTypes(ApplicationDataTypeCache applicationDataTypeCache) {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus operationStatus = allDataTypes.right().value();
            if (operationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance().logInternalDataError("FetchDataTypes", "Data types are not loaded", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
            } else {
                BeEcompErrorManager.getInstance().logInternalFlowError("FetchDataTypes", "Failed to fetch data types", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return Either.left(allDataTypes.left().value());
    }

}
