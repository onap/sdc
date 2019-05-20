package org.openecomp.sdc.be.components.path;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("forwardingPathValidator")
public class ForwardingPathValidator {

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    private static final Logger logger = Logger.getLogger(ForwardingPathValidator.class);
    private static final int  PATH_NAME_LENGTH = 200;
    private static final int  PROTOCOL_LENGTH = 200;
    private static final int  DESTINATION_PORT_LENGTH = 200;

    public Either<Boolean, ResponseFormat> validateForwardingPaths(Collection<ForwardingPathDataDefinition> paths,
                                                                   String serviceId, boolean isUpdate) {
        for (ForwardingPathDataDefinition path : paths) {
            Either<Boolean, ResponseFormat> forwardingPathResponseEither = validateForwardingPath(path,
                    serviceId, isUpdate);
            if (forwardingPathResponseEither.isRight()) {
                return forwardingPathResponseEither;
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateForwardingPath(ForwardingPathDataDefinition path,
                                                                   String serviceId, boolean isUpdate) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();

        Either<Boolean, ResponseFormat> errorResponseName = validateName(path,
                responseFormatManager, serviceId, isUpdate);
        if (errorResponseName != null)
            return errorResponseName;

        Either<Boolean, ResponseFormat> protocolErrorResponse = validateProtocol(path, responseFormatManager);
        if (protocolErrorResponse != null)
            return protocolErrorResponse;

        Either<Boolean, ResponseFormat> portNumberResponse = validateDestinationPortNumber(path, responseFormatManager);
        if (portNumberResponse != null)
            return portNumberResponse;

        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateDestinationPortNumber(ForwardingPathDataDefinition dataDefinition,
                                                                          ResponseFormatManager responseFormatManager) {
        if (dataDefinition.getDestinationPortNumber() != null &&
            dataDefinition.getDestinationPortNumber().length() > DESTINATION_PORT_LENGTH ) {
            logger.debug("Forwarding path destination port {} too long, , maximum allowed 200 characters ",
                    dataDefinition.getDestinationPortNumber());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .FORWARDING_PATH_DESTINATION_PORT_MAXIMUM_LENGTH, dataDefinition.getDestinationPortNumber());
            return Either.right(errorResponse);
        }
        return null;
    }

    private Either<Boolean, ResponseFormat> validateProtocol(ForwardingPathDataDefinition dataDefinition,
                                                             ResponseFormatManager responseFormatManager) {
        if (dataDefinition.getProtocol() != null && dataDefinition.getProtocol().length() > PROTOCOL_LENGTH) {
            logger.debug("Forwarding path protocol {} too long, , maximum allowed 200 characters ", dataDefinition.getProtocol());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .FORWARDING_PATH_PROTOCOL_MAXIMUM_LENGTH, dataDefinition.getProtocol());
            return Either.right(errorResponse);
        }
        return null;
    }

    private Either<Boolean, ResponseFormat> validateName(ForwardingPathDataDefinition dataDefinition,
                                                         ResponseFormatManager responseFormatManager,
                                                         String serviceId, boolean isUpdate) {
        String pathName = dataDefinition.getName();
        Either<Boolean, ResponseFormat> pathEmptyResponse = validatePathNameIfEmpty(responseFormatManager, pathName);
        if (pathEmptyResponse != null)
            return pathEmptyResponse;

        Either<Boolean, ResponseFormat> pathLengthResponse = validatePathNameLength(responseFormatManager, pathName);
        if (pathLengthResponse != null)
            return pathLengthResponse;

        Either<Boolean, ResponseFormat> isPathNameUniqueResponse = validatePathIfUnique(dataDefinition, serviceId, isUpdate, responseFormatManager );
        if(isPathNameUniqueResponse.isRight()) {
            return Either.right(isPathNameUniqueResponse.right().value());
        }
        if (!isPathNameUniqueResponse.left().value()) {
            logger.debug("Forwarding path name {} already in use ", dataDefinition.getName());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .FORWARDING_PATH_NAME_ALREADY_IN_USE, dataDefinition.getName());
            return Either.right(errorResponse);
        }
        return null;
    }

    private Either<Boolean, ResponseFormat> validatePathNameLength(ResponseFormatManager responseFormatManager, String pathName) {
        if (pathName.length() > PATH_NAME_LENGTH) {
            logger.debug("Forwarding path name  {} too long, , maximum allowed 200 characters ", pathName);
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .FORWARDING_PATH_NAME_MAXIMUM_LENGTH, pathName);
            return Either.right(errorResponse);
        }
        return null;
    }

    private Either<Boolean, ResponseFormat> validatePathNameIfEmpty(ResponseFormatManager responseFormatManager, String pathName) {
        if (StringUtils.isEmpty(pathName)) {
            logger.debug("Forwarding Path Name can't be empty");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus.FORWARDING_PATH_NAME_EMPTY);
            return Either.right(errorResponse);
        }
        return null;
    }


    private Either<Boolean, ResponseFormat> validatePathIfUnique(ForwardingPathDataDefinition dataDefinition, String serviceId,
                                                                 boolean isUpdate, ResponseFormatManager responseFormatManager) {
        boolean isPathNameUnique = false;
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreForwardingPath(false);
        Either<Service, StorageOperationStatus> forwardingPathOrigin = toscaOperationFacade
                .getToscaElement(serviceId, filter);
        if (forwardingPathOrigin.isRight()){
            return Either.right(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        Collection<ForwardingPathDataDefinition> allPaths = forwardingPathOrigin.left().value().getForwardingPaths().values();
        Map<String, String> pathNames = new HashMap<>();
        allPaths.forEach( path -> pathNames.put(path.getUniqueId(), path.getName()) );

        if (isUpdate){
            for(Map.Entry<String, String> entry : pathNames.entrySet()){
                if (entry.getKey().equals(dataDefinition.getUniqueId()) && entry.getValue().
                        equals(dataDefinition.getName())) {
                    isPathNameUnique = true;
                }

                if(entry.getKey().equals(dataDefinition.getUniqueId()) && !pathNames.values().contains(dataDefinition.getName())){
                    isPathNameUnique = true;
                }
            }
        }
        else
        if (!pathNames.values().contains(dataDefinition.getName())){
            isPathNameUnique = true;
        }

        return Either.left(isPathNameUnique);
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }


}
