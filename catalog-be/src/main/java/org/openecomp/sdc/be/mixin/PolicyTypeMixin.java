package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

@MixinTarget(target = PolicyTypeDataDefinition.class)
public abstract class PolicyTypeMixin extends Mixin {

    @JsonProperty
    abstract String getName();
    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getVersion();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty
    abstract String getIcon();

}


