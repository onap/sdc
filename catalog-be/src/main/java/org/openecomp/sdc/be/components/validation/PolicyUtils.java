package org.openecomp.sdc.be.components.validation;

import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.openecomp.sdc.common.api.Constants.GROUP_POLICY_NAME_DELIMETER;

import fj.data.Either;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;

/**
 * Provides specific functionality for policy
 */
public class PolicyUtils {

    private static final Logger log = Logger.getLogger(PolicyUtils.class);

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
        if (isNotEmpty(policies)) {
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
     * @return the set of the policies
     */
    public static Set<String> getExcludedPolicyTypesByComponent(Component component) {
        if (isEmpty(ConfigurationManager.getConfigurationManager()
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

    public static PolicyDefinition getDeclaredPolicyDefinition(String componentInstanceId, ComponentInstanceProperty property) {
        PolicyDefinition policyDefinition = new PolicyDefinition(property);
        policyDefinition.setUniqueId(UniqueIdBuilder.buildPolicyUniqueId(componentInstanceId, property.getName()));
        policyDefinition.setInstanceUniqueId(componentInstanceId);

        return policyDefinition;
    }

    private static int extractNextPolicyCounterFromUniqueId(String uniqueId) {
        int counter = 0;
        if (isNotEmpty(uniqueId)) {
            counter = extractNextPolicyCounter(uniqueId, uniqueId.lastIndexOf(Constants.POLICY_UID_POSTFIX));
        }
        return counter;
    }

    private static int extractNextPolicyCounterFromName(String policyName) {
        int counter = 0;
        if (isNotEmpty(policyName)) {
            counter = extractNextPolicyCounter(policyName, policyName.length());
        }
        return counter;
    }

    private static int extractNextPolicyCounter(String policyName, int endIndex) {
        int counter = 0;
        try {
            int beginIndex = policyName.lastIndexOf(GROUP_POLICY_NAME_DELIMETER) + GROUP_POLICY_NAME_DELIMETER.length();
            String counterStr = policyName.substring(beginIndex, endIndex);
            counter = Integer.valueOf(counterStr) + 1;
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.error("#extractNextPolicyCounter - An error occurred when attempting to extract counter from policy name [{}]. ", policyName, e);
        }
        return counter;
    }

    private static Either<PolicyDefinition, ActionStatus> validateUpdateMutablePolicyFields(PolicyDefinition recievedPolicy, PolicyDefinition validPolicy, Map<String, PolicyDefinition> policies) {
        return validateUpdatePolicyName(recievedPolicy, validPolicy, policies);
    }

    private static void validateImmutablePolicyFields(PolicyDefinition receivedPolicy, PolicyDefinition validPolicy) {
        logImmutableFieldUpdateWarning(receivedPolicy.getUniqueId(), validPolicy.getUniqueId(), JsonPresentationFields.UNIQUE_ID);
        logImmutableFieldUpdateWarning(receivedPolicy.getComponentName(), validPolicy.getComponentName(), JsonPresentationFields.CI_COMPONENT_NAME);
        logImmutableFieldUpdateWarning(receivedPolicy.getDerivedFrom(), validPolicy.getDerivedFrom(), JsonPresentationFields.DERIVED_FROM);
        logImmutableFieldUpdateWarning(receivedPolicy.getDescription(), validPolicy.getDescription(), JsonPresentationFields.DESCRIPTION);
        logImmutableFieldUpdateWarning(receivedPolicy.getInvariantName(), validPolicy.getInvariantName(), JsonPresentationFields.CI_INVARIANT_NAME);
        logImmutableFieldUpdateWarning(receivedPolicy.getInvariantUUID(), validPolicy.getInvariantUUID(), JsonPresentationFields.INVARIANT_UUID);
        logImmutableFieldUpdateWarning(receivedPolicy.getPolicyTypeName(), validPolicy.getPolicyTypeName(), JsonPresentationFields.TYPE);
        logImmutableFieldUpdateWarning(receivedPolicy.getPolicyTypeUid(), validPolicy.getPolicyTypeUid(), JsonPresentationFields.TYPE_UNIQUE_ID);
        logImmutableFieldUpdateWarning(receivedPolicy.getPolicyUUID(), validPolicy.getPolicyUUID(), JsonPresentationFields.UUID);
        logImmutableFieldUpdateWarning(receivedPolicy.getVersion(), validPolicy.getVersion(), JsonPresentationFields.VERSION);
        logImmutableFieldUpdateWarning(receivedPolicy.getIsFromCsar().toString(), validPolicy.getIsFromCsar().toString(), JsonPresentationFields.IS_FROM_CSAR);
    }

    private static boolean isUpdatedField(String oldField, String newField) {
        boolean isUpdatedField = false;
        if (isEmpty(oldField) && isNotEmpty(newField)) {
            isUpdatedField = true;
        }
        else if (isNotEmpty(oldField) && isNotEmpty(newField) && !oldField.equals(newField)) {
            isUpdatedField = true;
        }
        return isUpdatedField;
    }

    private static void logImmutableFieldUpdateWarning(String oldValue, String newValue, JsonPresentationFields field) {
        if (isUpdatedField(oldValue, newValue)) {
            log.warn("#logImmutableFieldUpdateWarning - Update of the field {} of a policy not allowed. The change will be ignored. The old value is {} , the new value is {}. ", field, oldValue, newValue);
        }
    }

    private static Either<PolicyDefinition, ActionStatus> validateUpdatePolicyName(PolicyDefinition receivedPolicy, PolicyDefinition validPolicy, Map<String, PolicyDefinition> policies) {
        Either<PolicyDefinition, ActionStatus> result = null;
        Optional<PolicyDefinition> sameNamePolicy = Optional.empty();
        if (isEmpty(receivedPolicy.getName()) || !ValidationUtils.POLICY_NAME_PATTERN.matcher(receivedPolicy
                .getName()).matches()) {
            log.error("#validateUpdatePolicyName - Failed to validate the name {} of the policy {}. ", receivedPolicy.getName(), receivedPolicy.getUniqueId());
            result = Either.right(ActionStatus.INVALID_POLICY_NAME);
        }
        if (result == null && isNotEmpty(policies)) {
            sameNamePolicy = policies.values()
                                     .stream()
                                     .filter(p -> p.getName().equals(receivedPolicy.getName()))
                                     .findFirst();
        }
        if (sameNamePolicy.isPresent()) {
            log.error("#validateUpdatePolicyName - Failed to validate the name {} of the policy {}. The policy {} with the same name already exists. ", receivedPolicy
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
