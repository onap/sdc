package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides specific functionality for policy
 */
public class PolicyUtils {

    private static final Logger log = LoggerFactory.getLogger(PolicyUtils.class);

    private PolicyUtils() {
        // No instances allowed
    }

    /**
     * Calculates the next integer counter according to the found max counter existing in the provided policies map
     *
     * @param policies the map of the policies
     * @return the integer counter
     */
    public static int getNextPolicyCounter(Map<String, PolicyDefinition> policies) {
        int nextCounter = 0;
        if (MapUtils.isNotEmpty(policies)) {
            int nextCounterFromIds = policies.values()
                                             .stream()
                                             .map(p -> extractNextPolicyCounterFromUniqueId(p.getUniqueId()))
                                             .max(Integer::compareTo)
                                             .orElse(0);
            int nextCounterFromNames = policies.values()
                                               .stream()
                                               .map(p -> extractNextPolicyCounterFromName(p.getName()))
                                               .max(Integer::compareTo)
                                               .orElse(0);
            nextCounter = nextCounterFromIds > nextCounterFromNames ? nextCounterFromIds : nextCounterFromNames;
        }
        return nextCounter;
    }

    /**
     * Validates policy fields on policy update. Updates mutable fields
     *
     * @param recievedPolicy the policy parsed from the HTTP request
     * @param validPolicy    the copy of the existing policy found on the component
     * @param policies       all the polices related to the component
     * @return validated and updated policy or an error as action status
     */
    public static Either<PolicyDefinition, ActionStatus> validatePolicyFields(PolicyDefinition recievedPolicy, PolicyDefinition validPolicy, Map<String, PolicyDefinition> policies) {
        validateImmutablePolicyFields(recievedPolicy, validPolicy);
        return validateUpdateMutablePolicyFields(recievedPolicy, validPolicy, policies);
    }

    /**
     * Retrieves the set of the excluded policy types for the specified component
     *
     * @param  the component
     * @return the set of the policies
     */
    public static Set<String> getExcludedPolicyTypesByComponent(Component component) {
        if (MapUtils.isEmpty(ConfigurationManager.getConfigurationManager()
                                                 .getConfiguration()
                                                 .getExcludedPolicyTypesMapping())) {
            return Collections.emptySet();
        }
        if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
            return ConfigurationManager.getConfigurationManager()
                                       .getConfiguration()
                                       .getExcludedPolicyTypesMapping()
                                       .get(component.getComponentType().name());
        }
        return ConfigurationManager.getConfigurationManager()
                                   .getConfiguration()
                                   .getExcludedPolicyTypesMapping()
                                   .get(((Resource) component).getResourceType().getValue());
    }

    private static int extractNextPolicyCounterFromUniqueId(String uniqueId) {
        int counter = 0;
        if (StringUtils.isNotEmpty(uniqueId)) {
            counter = extractNextPolicyCounter(uniqueId, uniqueId.lastIndexOf(Constants.POLICY_UID_POSTFIX));
        }
        return counter;
    }

    private static int extractNextPolicyCounterFromName(String policyName) {
        int counter = 0;
        if (StringUtils.isNotEmpty(policyName)) {
            counter = extractNextPolicyCounter(policyName, policyName.length());
        }
        return counter;
    }

    private static int extractNextPolicyCounter(String policyName, int endIndex) {
        int counter = 0;
        try {
            counter = Integer.valueOf(policyName.substring(policyName.lastIndexOf(Constants.GROUP_POLICY_NAME_DELIMETER) + Constants.GROUP_POLICY_NAME_DELIMETER
                    .length(), endIndex)) + 1;
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error("The exception {} occurred upon extraction counter from the srting value {}. ", e, policyName);
        }
        return counter;
    }

    private static Either<PolicyDefinition, ActionStatus> validateUpdateMutablePolicyFields(PolicyDefinition recievedPolicy, PolicyDefinition validPolicy, Map<String, PolicyDefinition> policies) {
        return validateUpdatePolicyName(recievedPolicy, validPolicy, policies);
    }

    private static void validateImmutablePolicyFields(PolicyDefinition receivedPolicy, PolicyDefinition validPolicy) {
        boolean isUpdatedField = isUpdatedField(receivedPolicy.getUniqueId(), validPolicy.getUniqueId());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getUniqueId(), validPolicy.getUniqueId(), JsonPresentationFields.UNIQUE_ID);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getComponentName(), validPolicy.getComponentName());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getComponentName(), validPolicy.getComponentName(), JsonPresentationFields.CI_COMPONENT_NAME);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getDerivedFrom(), validPolicy.getDerivedFrom());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getDerivedFrom(), validPolicy.getDerivedFrom(), JsonPresentationFields.DERIVED_FROM);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getDescription(), validPolicy.getDescription());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getDescription(), validPolicy.getDescription(), JsonPresentationFields.DESCRIPTION);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getInvariantName(), validPolicy.getInvariantName());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getInvariantName(), validPolicy.getInvariantName(), JsonPresentationFields.CI_INVARIANT_NAME);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getInvariantUUID(), validPolicy.getInvariantUUID());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getInvariantUUID(), validPolicy.getInvariantUUID(), JsonPresentationFields.INVARIANT_UUID);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getPolicyTypeName(), validPolicy.getPolicyTypeName());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getPolicyTypeName(), validPolicy.getPolicyTypeName(), JsonPresentationFields.TYPE);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getPolicyTypeUid(), validPolicy.getPolicyTypeUid());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getPolicyTypeUid(), validPolicy.getPolicyTypeUid(), JsonPresentationFields.TYPE_UNIQUE_ID);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getPolicyUUID(), validPolicy.getPolicyUUID());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getPolicyUUID(), validPolicy.getPolicyUUID(), JsonPresentationFields.UUID);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getVersion(), validPolicy.getVersion());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getVersion(), validPolicy.getVersion(), JsonPresentationFields.VERSION);
        }
        isUpdatedField = isUpdatedField(receivedPolicy.getIsFromCsar().toString(), validPolicy.getIsFromCsar()
                                                                                              .toString());
        if (isUpdatedField) {
            logImmutableFieldUpdateWarning(receivedPolicy.getIsFromCsar().toString(), validPolicy.getIsFromCsar()
                                                                                                 .toString(), JsonPresentationFields.IS_FROM_CSAR);
        }
    }

    private static boolean isUpdatedField(String oldField, String newField) {
        boolean isUpdatedField = false;
        if (StringUtils.isEmpty(oldField) && StringUtils.isNotEmpty(newField)) {
            isUpdatedField = true;
        }
        if (StringUtils.isNotEmpty(oldField) && StringUtils.isNotEmpty(newField) && !oldField.equals(newField)) {
            isUpdatedField = true;
        }
        return isUpdatedField;
    }

    private static void logImmutableFieldUpdateWarning(String oldValue, String newValue, JsonPresentationFields field) {
        log.warn("Update of the field {} of a policy not allowed. The change will be ignored. The old value is {} , the new value is {}. ", field, oldValue, newValue);
    }

    private static Either<PolicyDefinition, ActionStatus> validateUpdatePolicyName(PolicyDefinition receivedPolicy, PolicyDefinition validPolicy, Map<String, PolicyDefinition> policies) {
        Either<PolicyDefinition, ActionStatus> result = null;
        Optional<PolicyDefinition> sameNamePolicy = Optional.empty();
        if (StringUtils.isEmpty(receivedPolicy.getName()) || !ValidationUtils.POLICY_NAME_PATTERN.matcher(receivedPolicy
                .getName()).matches()) {
            log.error("Failed to validate the name {} of the policy {}. ", receivedPolicy.getName(), receivedPolicy.getUniqueId());
            result = Either.right(ActionStatus.INVALID_POLICY_NAME);
        }
        if (result == null && MapUtils.isNotEmpty(policies)) {
            sameNamePolicy = policies.values()
                                     .stream()
                                     .filter(p -> p.getName().equals(receivedPolicy.getName()))
                                     .findFirst();
        }
        if (sameNamePolicy.isPresent()) {
            log.error("Failed to validate the name {} of the policy {}. The policy {} with the same name already exists. ", receivedPolicy
                    .getName(), receivedPolicy.getUniqueId(), sameNamePolicy.get().getUniqueId());
            result = Either.right(ActionStatus.POLICY_NAME_ALREADY_EXIST);
        }
        if (result == null) {
            validPolicy.setName(receivedPolicy.getName());
            result = Either.left(validPolicy);
        }
        return result;
    }
}
