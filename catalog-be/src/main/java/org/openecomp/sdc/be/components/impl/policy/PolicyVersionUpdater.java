package org.openecomp.sdc.be.components.impl.policy;





import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.instance.GroupMembersUpdateOperation;
import org.openecomp.sdc.be.components.impl.version.OnChangeVersionCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;

import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.utils.GroupUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;


import java.util.List;
import java.util.function.Consumer;

import static org.apache.commons.collections.CollectionUtils.isEmpty;


/**
 * A Helper class which handles altering the version of a group
 */
@org.springframework.stereotype.Component
public class PolicyVersionUpdater implements OnChangeVersionCommand {
    
    private static final Logger log = Logger.getLogger(PolicyVersionUpdater.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    

    public PolicyVersionUpdater(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    
    }
    
    
    @Override
    public ActionStatus onChangeVersion(Component container) {
        log.debug("#onChangeVersion - replacing all group members for component instance");
        Consumer<List<PolicyDefinition>> replaceGroupMemberTask = (policies) -> increaseVesion(policies);
        return updatePoliciesVersion(container, replaceGroupMemberTask);
    }

    public void increaseVesion(List<PolicyDefinition> policies) {
        policies.forEach(policy -> increaseMajorVersion(policy));
    }

  
    private void increaseMajorVersion(PolicyDefinition policy) {
        String version = policy.getVersion();
        
        String newVersion = GroupUtils.updateVersion(PromoteVersionEnum.MAJOR, policy.getVersion());
      
        if(!version.equals(newVersion) ){           
            String groupUUID = UniqueIdBuilder.generateUUID();
            policy.setPolicyUUID(groupUUID);
            policy.setVersion(String.valueOf(newVersion));
        }

    }    
   
    
    private ActionStatus updatePoliciesVersion(Component container, Consumer<List<PolicyDefinition>> updatePoliciesVersion) {
        List<PolicyDefinition> policies = container.resolvePoliciesList();
        if (isEmpty(policies)) {
            return ActionStatus.OK;
        }
        updatePoliciesVersion.accept(policies);
        return updatePolicies(container, policies);
    }  

    
    private ActionStatus updatePolicies(Component policiesContainer, List<PolicyDefinition> policiesToUpdate) {
        log.debug("#updatePolicies - updating {} policies for container {}", policiesToUpdate.size(), policiesContainer.getUniqueId());
        StorageOperationStatus updatePolicyResult = toscaOperationFacade.updatePoliciesOfComponent(policiesContainer.getUniqueId(), policiesToUpdate);
        return componentsUtils.convertFromStorageResponse(updatePolicyResult, policiesContainer.getComponentType());
    }

}


