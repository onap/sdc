package org.openecomp.sdc.be.mixin;

import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import com.fasterxml.jackson.annotation.JsonProperty;

@MixinTarget(target = GroupTypeDataDefinition.class)
public abstract class GroupTypeMixin extends Mixin {

    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getVersion();
    @JsonProperty
    abstract String getUniqueId();

}


