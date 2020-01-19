package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.List;

@MixinTarget(target = PropertyDefinition.class)
public abstract class PropertyDefinitionMixin extends Mixin {

    @JsonProperty
    abstract List<Annotation> getAnnotations();
    @JsonProperty
    abstract String getDefaultValue();
    @JsonProperty
    abstract String getDescription();
    @JsonProperty
    abstract List<GetInputValueDataDefinition> getGetInputValues();
    @JsonProperty
    abstract String getInputId();
    @JsonProperty
    abstract String getInputPath();
    @JsonProperty
    abstract String getInstanceUniqueId();
    @JsonProperty
    abstract String getLabel();
    @JsonProperty
    abstract String getName();
    @JsonProperty
    abstract String getParentUniqueId();
    @JsonProperty
    abstract String getPropertyId();
    @JsonProperty
    abstract SchemaDefinition getSchema();
    @JsonProperty
    abstract SchemaDefinition getSchemaProperty();
    @JsonProperty
    abstract String getSchemaType();
    @JsonProperty
    abstract String getStatus();
    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty
    abstract String getValue();
    @JsonProperty
    abstract boolean isGetInputProperty();
    @JsonProperty
    abstract List<PropertyConstraint> getConstraints();
}
