package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.Map;

@MixinTarget(target = GroupDataDefinition.class)
public abstract  class GroupCompositionMixin  extends Mixin {
    @JsonProperty
    abstract String getName();
    @JsonProperty("members")
    abstract Map<String, String> resolveMembersList();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty
    abstract String getType();


}
