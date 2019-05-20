package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ActivationRequestInformation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chaya on 10/18/2017.
 */
@org.springframework.stereotype.Component("serviceDistributionValidation")
public class ServiceDistributionValidation {
    private static final Logger log = Logger.getLogger(ServiceDistributionValidation.class);
    @Resource
    private ComponentsUtils componentsUtils;
    @Resource
    private ToscaOperationFacade toscaOperationFacade;
    @Resource
    private UserValidations userValidations;
    @Resource
    private IDistributionEngine distributionEngine;

    public Either<ActivationRequestInformation, ResponseFormat> validateActivateServiceRequest(String serviceUUID, String opEnvId, User modifier, ServiceDistributionReqInfo data) {
        try {
            validateUserExists(modifier.getUserId());
            Service serviceToActivate = validateServiceExists(serviceUUID);
            validateDistributionServiceLifeCycleState(serviceToActivate);
            OperationalEnvironmentEntry operationalEnvironmentEntry = validateOperationalEnvExists(opEnvId);
            String workloadContext = validateWorkloadContext(data);
            ActivationRequestInformation activationRequestInformation = new ActivationRequestInformation(serviceToActivate, workloadContext, operationalEnvironmentEntry.getTenant());
            return Either.left(activationRequestInformation);
        } catch (ValidationException e) {
            log.error("failed while validating activate service UUID {} request. error {}", serviceUUID, e.getExceptionResponseFormat(), e);
            return Either.right(e.getExceptionResponseFormat());
        }
    }

    private Service validateServiceExists(String serviceUUID) {
        if (StringUtils.isEmpty(serviceUUID.trim())) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.BAD_REQUEST_MISSING_RESOURCE);
            throw new ValidationException(responseFormat);
        }
        Either<Component, StorageOperationStatus> latestComponentByUuid = toscaOperationFacade.getLatestServiceByUuid(serviceUUID);
        if (latestComponentByUuid.isRight()) {
            log.error("failed retrieving service {}", serviceUUID);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.API_RESOURCE_NOT_FOUND, ApiResourceEnum.SERVICE_ID.getValue());
            throw new ValidationException(responseFormat);
        }
        return (Service)latestComponentByUuid.left().value();
    }

    private String validateWorkloadContext(ServiceDistributionReqInfo data) {
        String workloadContext = data.getWorkloadContext();
        if (workloadContext == null || workloadContext.isEmpty()) {
            log.error("workload context does not exist on data to distribute");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_BODY);
            throw new ValidationException(responseFormat);
        }
        return workloadContext;
    }

    private OperationalEnvironmentEntry validateOperationalEnvExists(String opEnvId) {
        if (StringUtils.isEmpty(opEnvId.trim())) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.BAD_REQUEST_MISSING_RESOURCE);
            throw new ValidationException(responseFormat);
        }
        OperationalEnvironmentEntry operationalEnvironment = distributionEngine.getEnvironmentById(opEnvId);
        if (operationalEnvironment == null) {
            return failOnEnvNotExist(opEnvId);
        }
        if (!operationalEnvironment.getStatus().equals(EnvironmentStatusEnum.COMPLETED.getName())) {
            log.error("the operational environment is not ready to receive distributions. environment status: {}", operationalEnvironment.getStatus());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.API_RESOURCE_NOT_FOUND , ApiResourceEnum.ENVIRONMENT_ID.getValue());
            throw new ValidationException(responseFormat);
        }
        return operationalEnvironment;
    }

    private OperationalEnvironmentEntry failOnEnvNotExist(String opEnvId) {
        return ValidationUtils.throwValidationException(componentsUtils.getResponseFormat(ActionStatus.API_RESOURCE_NOT_FOUND, ApiResourceEnum.ENVIRONMENT_ID.getValue()), "failed to get operational environment {}", opEnvId);
    }

    private void validateServiceState(Service service, List<LifecycleStateEnum> allowedStates) {
        LifecycleStateEnum state = service.getLifecycleState();
        if (!allowedStates.contains(state)) {
            log.error("service {} life cycle state {} is not valid for distribution", service.getUniqueId(), service.getLifecycleState());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_SERVICE_STATE);
            throw new ValidationException(responseFormat);
        }
    }
    private void validateUserExists(String userId) {
        userValidations.validateUserExists(userId, "activate Distribution", false);
    }

    private void validateDistributionServiceLifeCycleState(Service serviceToActivate) {
        validateServiceState(serviceToActivate,
                Arrays.asList(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS, LifecycleStateEnum.CERTIFIED));
    }
}
