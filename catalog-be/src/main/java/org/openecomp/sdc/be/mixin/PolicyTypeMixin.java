package org.openecomp.sdc.be.mixin;

import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import com.fasterxml.jackson.annotation.JsonProperty;

@MixinTarget(target = PolicyTypeDataDefinition.class)
public abstract class PolicyTypeMixin extends Mixin {

    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getVersion();
    @JsonProperty
    abstract String getUniqueId();

}


