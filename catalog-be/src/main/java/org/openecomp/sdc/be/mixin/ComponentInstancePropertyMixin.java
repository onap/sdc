package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.List;

@MixinTarget(target = ComponentInstanceProperty.class)
public abstract class ComponentInstancePropertyMixin extends PropertyDefinitionMixin {

    @JsonProperty
    abstract String getComponentInstanceId();
    @JsonProperty
    abstract String getComponentInstanceName();
    @JsonProperty
    abstract List<String> getPath();
    @JsonProperty
    abstract List<PropertyRule> getRules();
    @JsonProperty
    abstract String getValueUniqueUid();
}
