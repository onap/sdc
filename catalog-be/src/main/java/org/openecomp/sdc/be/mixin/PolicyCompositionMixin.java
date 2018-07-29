package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.List;
import java.util.Map;

@MixinTarget(target = PolicyDataDefinition.class)
public abstract  class PolicyCompositionMixin extends Mixin {
    @JsonProperty
    abstract String getName();
    @JsonProperty
    abstract Map<PolicyTargetType, List<String>> getTargets();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty("type")
    abstract String getPolicyTypeName();


}
