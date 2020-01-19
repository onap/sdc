package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.List;

@MixinTarget(target = InputDefinition.class)
public abstract class InputDefinitionMixin extends PropertyDefinitionMixin {

    @JsonProperty
    abstract List<ComponentInstanceInput> getInputs();
    @JsonProperty
    abstract List<ComponentInstanceProperty> getProperties();
}
