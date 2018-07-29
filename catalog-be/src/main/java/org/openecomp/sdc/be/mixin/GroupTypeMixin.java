package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

@MixinTarget(target = GroupTypeDataDefinition.class)
public abstract class GroupTypeMixin extends Mixin {

    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getVersion();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty
    abstract String getName();
    @JsonProperty
    abstract String getIcon();

}


