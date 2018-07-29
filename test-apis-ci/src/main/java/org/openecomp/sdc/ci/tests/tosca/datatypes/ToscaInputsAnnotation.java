package org.openecomp.sdc.ci.tests.tosca.datatypes;

import org.yaml.snakeyaml.TypeDescription;

import java.util.Map;
import java.util.Objects;

public class ToscaInputsAnnotation {
	
	String name;
	String type;
	private Map<String, Object> properties;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	
	//gets Type description for Yaml snake
	public static TypeDescription getTypeDescription(){
        return new TypeDescription(ToscaInputsAnnotation.class);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ToscaInputsAnnotation that = (ToscaInputsAnnotation) o;
		return Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
}
