/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

@EqualsAndHashCode(callSuper = false)
@Data
public class PropertyDataDefinition extends ToscaDataDefinition {

    private boolean definition = false;
    private Boolean hidden = Boolean.FALSE;
    private String uniqueId;
    // "boolean", "string", "float", "integer", "version" })
    private String type;
    private Boolean required = Boolean.FALSE;
    private String defaultValue;
    private String description;
    private SchemaDefinition schema;
    private boolean password;
    private String name;
    private String value;
    private String label;
    private Boolean immutable = Boolean.FALSE;
    private Boolean mappedToComponentProperty = Boolean.TRUE;
    /**
     * @deprecated use {@link #toscaGetFunction#functionType} instead
     */
    @Deprecated
    private ToscaGetFunctionType toscaGetFunctionType;
    private ToscaGetFunctionDataDefinition toscaGetFunction;

    private String inputPath;
    private String status;
    private String inputId;
    private String instanceUniqueId;
    private String model;
    private String propertyId;
    private String parentPropertyType;
    private String subPropertyInputPath;
    private List<Annotation> annotations;
    /**
     * The resource id which this property belongs to
     */
    private String parentUniqueId;
    private List<GetInputValueDataDefinition> getInputValues;
    private Boolean isDeclaredListInput = Boolean.FALSE;
    private List<GetPolicyValueDataDefinition> getPolicyValues;
    private List<String> propertyConstraints;
    private Map<String, String> metadata;
    private boolean userCreated;

    public PropertyDataDefinition() {
        super();
    }

    public PropertyDataDefinition(Map<String, Object> pr) {
        super(pr);
    }

    public PropertyDataDefinition(final PropertyDataDefinition propertyDataDefinition) {
        super();
        this.setUniqueId(propertyDataDefinition.getUniqueId());
        this.setRequired(propertyDataDefinition.isRequired());
        this.setDefaultValue(propertyDataDefinition.getDefaultValue());
        this.setDefinition(propertyDataDefinition.getDefinition());
        this.setDescription(propertyDataDefinition.getDescription());
        if (propertyDataDefinition.getSchema() != null) {
            this.setSchema(new SchemaDefinition(propertyDataDefinition.getSchema()));
        }
        this.setPassword(propertyDataDefinition.isPassword());
        this.setType(propertyDataDefinition.getType());
        this.setName(propertyDataDefinition.getName());
        this.setValue(propertyDataDefinition.getValue());
        this.setRequired(propertyDataDefinition.isRequired());
        this.setHidden(propertyDataDefinition.isHidden());
        this.setLabel(propertyDataDefinition.getLabel());
        this.setImmutable(propertyDataDefinition.isImmutable());
        this.setMappedToComponentProperty(propertyDataDefinition.isMappedToComponentProperty());
        this.setParentUniqueId(propertyDataDefinition.getParentUniqueId());
        this.setOwnerId(propertyDataDefinition.getOwnerId());
        this.setGetInputValues(propertyDataDefinition.getGetInputValues());
        this.setGetPolicyValues(propertyDataDefinition.getGetPolicyValues());
        this.setInputPath(propertyDataDefinition.getInputPath());
        this.setStatus(propertyDataDefinition.getStatus());
        this.setInputId(propertyDataDefinition.getInputId());
        this.setInstanceUniqueId(propertyDataDefinition.getInstanceUniqueId());
        this.setModel(propertyDataDefinition.getModel());
        this.setPropertyId(propertyDataDefinition.getPropertyId());
        this.setToscaGetFunctionType(propertyDataDefinition.getToscaGetFunctionType());
        this.setToscaGetFunction(propertyDataDefinition.getToscaGetFunction());
        this.parentPropertyType = propertyDataDefinition.getParentPropertyType();
        this.subPropertyInputPath = propertyDataDefinition.getSubPropertyInputPath();
        if (isNotEmpty(propertyDataDefinition.annotations)) {
            this.setAnnotations(propertyDataDefinition.annotations);
        }
        if (MapUtils.isNotEmpty(propertyDataDefinition.getMetadata())) {
            setMetadata(new HashMap<>(propertyDataDefinition.getMetadata()));
        }
        if (isNotEmpty(propertyDataDefinition.getPropertyConstraints())) {
            setPropertyConstraints(new ArrayList<>(propertyDataDefinition.getPropertyConstraints()));
        }
        this.setIsDeclaredListInput(propertyDataDefinition.getIsDeclaredListInput());
        this.setUserCreated(propertyDataDefinition.isUserCreated());
    }

    // @Override
    public boolean isDefinition() {
        return true;
    }

    public boolean getDefinition() {
        return definition;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setSchemaType(String schemaType) {
        if (schema != null && schema.getProperty() != null) {
            schema.getProperty().setType(schemaType);
        }
    }

    public String getSchemaType() {
        if (schema != null && schema.getProperty() != null) {
            return schema.getProperty().getType();
        }
        return null;
    }

    public PropertyDataDefinition getSchemaProperty() {
        if (schema != null) {
            return schema.getProperty();
        }
        return null;
    }

    public ToscaGetFunctionType getToscaGetFunctionType() {
        if (toscaGetFunction != null) {
            return toscaGetFunction.getFunctionType();
        }
        return toscaGetFunctionType;
    }

    public Boolean isHidden() {
        return hidden;
    }

    public Boolean isImmutable() {
        return immutable;
    }

    public Boolean isMappedToComponentProperty() {
        return mappedToComponentProperty;
    }

    public String getParentUniqueId() {
        return getOwnerId();
    }

    public void setParentUniqueId(String parentUniqueId) {
        setOwnerId(parentUniqueId);
    }

    public List<GetPolicyValueDataDefinition> safeGetGetPolicyValues() {
        return CollectionUtils.isEmpty(getPolicyValues) ? new ArrayList<>() : getPolicyValues;
    }

    public boolean typeEquals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyDataDefinition other = (PropertyDataDefinition) obj;
        if (this.getType() == null) {
            return other.getType() == null;
        }
        if (!this.type.equals(other.type)) {
            return false;
        }
        String thisSchemaType = this.getSchemaType();
        String otherSchemaType = other.getSchemaType();
        if (thisSchemaType == null) {
            return otherSchemaType == null;
        }
        return thisSchemaType.equals(otherSchemaType);
    }

    @Override
    public Object getToscaPresentationValue(JsonPresentationFields field) {
        switch (field) {
            case NAME:
                return name;
            case UNIQUE_ID:
                return uniqueId;
            case PASSWORD:
                return password;
            case TYPE:
                return type;
            case DEFINITION:
                return definition;
            case VALUE:
                return value;
            case DEFAULT_VALUE:
                return defaultValue;
            default:
                return super.getToscaPresentationValue(field);
        }
    }

    @Override
    public void setToscaPresentationValue(JsonPresentationFields name, Object value) {
        switch (name) {
            case NAME:
                setName((String) value);
                break;
            case UNIQUE_ID:
                setUniqueId((String) value);
                break;
            case PASSWORD:
                setPassword((Boolean) value);
                break;
            case TYPE:
                setType((String) value);
                break;
            case DEFINITION:
                setDefinition((Boolean) value);
                break;
            case VALUE:
                setValue((String) value);
                break;
            case DEFAULT_VALUE:
                setDefaultValue((String) value);
                break;
            default:
                super.setToscaPresentationValue(name, value);
                break;
        }
    }

    private <T extends ToscaDataDefinition> boolean compareSchemaType(T other) {
        return !"list".equals(type) && !"map".equals(type) || this.getSchema().getProperty().getType()
            .equals(((PropertyDataDefinition) other).getSchema().getProperty().getType());
    }

    @Override
    public <T extends ToscaDataDefinition> T mergeFunction(T other, boolean allowDefaultValueOverride) {
        if (this.getType() != null && this.getType().equals(other.getToscaPresentationValue(JsonPresentationFields.TYPE)) && compareSchemaType(
            other)) {
            other.setOwnerId(getOwnerId());
            if (allowDefaultValueOverride && getDefaultValue() != null && !getDefaultValue().isEmpty()) {
                other.setToscaPresentationValue(JsonPresentationFields.DEFAULT_VALUE, getDefaultValue());
            }
            return other;
        }
        return null;
    }

    public void convertPropertyDataToInstancePropertyData() {
        if (null != value) {
            defaultValue = value;
        }
    }

    public boolean isGetInputProperty() {
        return this.getGetInputValues() != null && !this.getGetInputValues().isEmpty();
    }

    public void setAnnotations(List<Annotation> newAnnotations) {
        Set<Annotation> annotationSet = isNotEmpty(newAnnotations) ? new HashSet<>(newAnnotations) : new HashSet<>();
        //We would to prioritize the new valid annotations over the old ones if the same one existed.
        if (this.annotations != null) {
            annotationSet.addAll(this.annotations);
        }
        this.annotations = new ArrayList<>(annotationSet);
        setToscaPresentationValue(JsonPresentationFields.ANNOTATIONS, this.annotations);
    }

    public List<Annotation> getAnnotations() {
        return (List<Annotation>) getToscaPresentationValue(JsonPresentationFields.ANNOTATIONS);
    }

    public boolean isGetFunction() {
        return this.toscaGetFunctionType != null || this.toscaGetFunction != null;
    }

    public boolean hasGetFunction() {
        return this.toscaGetFunction != null;
    }

}
