package org.openecomp.sdc.be.components.impl.instance;

import org.openecomp.sdc.be.components.impl.policy.PolicyTargetsUpdateHandler;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class PolicyTargetsUpdateOperation implements OnComponentInstanceChangeOperation {

    private static final Logger log = Logger.getLogger(PolicyTargetsUpdateOperation.class);
    private final PolicyTargetsUpdateHandler policyTargetsUpdateHandler;

    public PolicyTargetsUpdateOperation(PolicyTargetsUpdateHandler policyTargetsUpdateHandler) {
        this.policyTargetsUpdateHandler = policyTargetsUpdateHandler;
    }

    @Override
    public ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#onChangeVersion - replacing all policy targets referencing component instance {} with component instance {}", prevVersion.getUniqueId(), newVersion.getUniqueId());
        return policyTargetsUpdateHandler.replacePoliciesTargets(container, prevVersion.getUniqueId(), newVersion.getUniqueId(), PolicyTargetType.COMPONENT_INSTANCES);
    }

    @Override
    public ActionStatus onDelete(Component container, String deletedEntityId) {
        log.debug("#onDelete - removing all component {} policy targets referencing component instance {}", container.getUniqueId(), deletedEntityId);
        return policyTargetsUpdateHandler.removePoliciesTargets(container, deletedEntityId, PolicyTargetType.COMPONENT_INSTANCES);
    }


}
