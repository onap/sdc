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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class PropertyDataDefinition extends ToscaDataDefinition {

	private String uniqueId;

	// "boolean", "string", "float", "integer", "version" })
	private String type;

	private Boolean required = Boolean.FALSE;

	protected boolean definition = false;

	private String defaultValue;

	private String description;

	private SchemaDefinition schema;

	private boolean password;

	private String name;

	private String value;

	private String label;
	protected Boolean hidden = Boolean.FALSE;
	private Boolean immutable = Boolean.FALSE;

	private String inputPath;
	private String status;
	private String inputId;
	private String instanceUniqueId;
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

	public PropertyDataDefinition() {
		super();
	}

	public PropertyDataDefinition(Map<String, Object> pr) {
		super(pr);

	}

	public PropertyDataDefinition(PropertyDataDefinition p) {
		super();
		this.setUniqueId(p.getUniqueId());
		this.setRequired(p.isRequired());
		this.setDefaultValue(p.getDefaultValue());
		this.setDescription(p.getDescription());
		this.setSchema(p.getSchema());
		this.setPassword(p.isPassword());
		this.setType(p.getType());
		this.setName(p.getName());
		this.setValue(p.getValue());
		this.setRequired(p.isRequired());
		this.setHidden(p.isHidden());
		this.setLabel(p.getLabel());
		this.setImmutable(p.isImmutable());
		this.setParentUniqueId(p.getParentUniqueId());
		this.setOwnerId(p.getOwnerId());
		this.setGetInputValues(p.getGetInputValues());
		this.setGetPolicyValues(p.getGetPolicyValues());
		this.setInputPath(p.getInputPath());
		this.setStatus(p.getStatus());
		this.setInputId(p.getInputId());
		this.setInstanceUniqueId(p.getInstanceUniqueId());
		this.setPropertyId(p.getPropertyId());
		this.parentPropertyType = p.getParentPropertyType();
		this.subPropertyInputPath = p.getSubPropertyInputPath();
		if(isNotEmpty(p.annotations)){
		    this.setAnnotations(p.annotations);
        }
		this.setIsDeclaredListInput(p.getIsDeclaredListInput());
	}

	public String getParentPropertyType() {
	    return parentPropertyType;
	}

	public void setParentPropertyType(String parentPropertyType) {
	    this.parentPropertyType = parentPropertyType;
	}

	public String getSubPropertyInputPath() {
	    return subPropertyInputPath;
	}

	public void setSubPropertyInputPath(String subPropertyInputPath) {
	    this.subPropertyInputPath = subPropertyInputPath;
	}

	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	// @Override
	public boolean isDefinition() {
		return true;
	}

	public void setDefinition(boolean definition) {
		this.definition = definition;
	}

	public String getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public SchemaDefinition getSchema() {
		return schema;
	}

	public void setSchema(SchemaDefinition entrySchema) {
		this.schema = entrySchema;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean isHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean isImmutable() {
		return immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public String getParentUniqueId() {
		return getOwnerId();
	}

	public void setParentUniqueId(String parentUniqueId) {
		setOwnerId(parentUniqueId);
	}

	public List<GetInputValueDataDefinition> getGetInputValues() {
		return getInputValues;
	}

	public void setGetInputValues(List<GetInputValueDataDefinition> getInputValues) {
		this.getInputValues = getInputValues;
	}

	public List<GetPolicyValueDataDefinition> safeGetGetPolicyValues() {
		return CollectionUtils.isEmpty(getPolicyValues) ? new ArrayList<>() : getPolicyValues;
	}

	public List<GetPolicyValueDataDefinition> getGetPolicyValues() {
		return getPolicyValues;
	}

	public void setGetPolicyValues(List<GetPolicyValueDataDefinition> getPolicyValues) {
		this.getPolicyValues = getPolicyValues;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInputId() {
		return inputId;
	}

	public void setInputId(String inputId) {
		this.inputId = inputId;
	}

	public String getInstanceUniqueId() {
		return instanceUniqueId;
	}

	public void setInstanceUniqueId(String instanceUniqueId) {
		this.instanceUniqueId = instanceUniqueId;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public Boolean getIsDeclaredListInput() {
		return isDeclaredListInput;
	}

	public void setIsDeclaredListInput(Boolean isDeclaredListInput) {
		this.isDeclaredListInput = isDeclaredListInput;
	}

	@Override
	public String toString() {
		return "PropertyDataDefinition [uniqueId=" + uniqueId + ", type=" + type + ", required=" + required + ", definition=" + definition + ", defaultValue=" + defaultValue + ", description=" + description + ", schema=" + schema + ", password="
				+ password + ", name=" + name + ", value=" + value + ", label=" + label + ", hidden=" + hidden + ", immutable=" + immutable + ", inputPath=" + inputPath + ", status=" + status + ", inputId=" + inputId + ", instanceUniqueId="
				+ instanceUniqueId + ", propertyId=" + propertyId + ", parentUniqueId=" + parentUniqueId + ", getInputValues=" + getInputValues + ", isDeclaredListInput=" + isDeclaredListInput
				+  "parentPropertyType" + parentPropertyType
				+  "subPropertyInputPath" + subPropertyInputPath +"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (definition ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (password ? 1231 : 1237);
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((parentUniqueId == null) ? 0 : parentUniqueId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
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
	public boolean equals(Object obj) {
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
		if (defaultValue == null) {
			if (other.defaultValue != null) {
                return false;
            }
		} else if (!defaultValue.equals(other.defaultValue)) {
            return false;
        }
		if (definition != other.definition) {
            return false;
        }
		if (description == null) {
			if (other.description != null) {
                return false;
            }
		} else if (!description.equals(other.description)) {
            return false;
        }
		if (password != other.password) {
            return false;
        }
		if (required == null) {
			if (other.required != null) {
                return false;
            }
		} else if (!required.equals(other.required)) {
            return false;
        }
		if (schema == null) {
			if (other.schema != null) {
                return false;
            }
		} else if (!schema.equals(other.schema)) {
            return false;
        }
		if (type == null) {
			if (other.type != null) {
                return false;
            }
		} else if (!type.equals(other.type)) {
            return false;
        }
		if (parentUniqueId == null) {
			if (other.parentUniqueId != null) {
                return false;
            }
		} else if (!parentUniqueId.equals(other.parentUniqueId)) {
            return false;
        }
		if (uniqueId == null) {
			if (other.uniqueId != null) {
                return false;
            }
		} else if (!uniqueId.equals(other.uniqueId)) {
            return false;
        }
		if (status == null) {
			if (other.status != null) {
                return false;
            }
		} else if (!status.equals(other.status)) {
            return false;
        }
		return true;
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
                setName((String)value);
                break;
            case UNIQUE_ID:
                setUniqueId((String)value);
                break;
            case PASSWORD:
                setPassword((Boolean)value);
                break;
            case TYPE:
                setType((String)value);
                break;
            case DEFINITION:
                setDefinition((Boolean)value);
                break;
            case VALUE:
                setValue((String)value);
                break;
            case DEFAULT_VALUE:
                setDefaultValue((String)value);
                break;
            default:
                super.setToscaPresentationValue(name, value);
                break;
        }
    }

	private <T extends ToscaDataDefinition> boolean compareSchemaType(T other) {
		return !"list".equals(type) && !"map".equals(type) || this.getSchema().getProperty().getType().equals(((PropertyDataDefinition) other).getSchema().getProperty().getType());
	}

	@Override
	public <T extends ToscaDataDefinition> T mergeFunction(T other, boolean allowDefaultValueOverride) {
		if (this.getType() != null && 
				this.getType().equals(other.getToscaPresentationValue(JsonPresentationFields.TYPE)) && 
				compareSchemaType(other)) {
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
		if(this.annotations != null){
			for(Annotation oldInputAnnotation : this.annotations)
				annotationSet.add(oldInputAnnotation);
		}

		this.annotations = new ArrayList<>(annotationSet);
		setToscaPresentationValue(JsonPresentationFields.ANNOTATIONS, this.annotations);
	}
	
	public List<Annotation> getAnnotations() {
		return (List<Annotation>) getToscaPresentationValue(JsonPresentationFields.ANNOTATIONS);
	}

}
