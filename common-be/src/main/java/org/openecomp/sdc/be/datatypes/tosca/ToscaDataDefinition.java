package org.openecomp.sdc.be.datatypes.tosca;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonValue;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

public abstract class ToscaDataDefinition {
	
	protected Map<String, Object> toscaPresentation;

	
	public ToscaDataDefinition(){
		toscaPresentation = new HashMap<String, Object>();
	}
	@JsonCreator
	public ToscaDataDefinition(Map<String, Object> art){
		toscaPresentation = art;
	}
	@JsonValue
	public Object getToscaPresentationValue(JsonPresentationFields name) {
		if (toscaPresentation != null && toscaPresentation.containsKey(name.getPresentation())) {
			return toscaPresentation.get(name.getPresentation());
		}
		return null;
	}
	
	public void setToscaPresentationValue(JsonPresentationFields name, Object value) {
		if (toscaPresentation == null && value !=null) {
			toscaPresentation = new HashMap<String, Object>();			
		}
		toscaPresentation.put(name.getPresentation(), value);
		
	}
	public void setOwnerIdIfEmpty(String ownerId){
		if (  getOwnerId() == null ){
			setOwnerId(ownerId);
		}
	}
	public void setOwnerId(String ownerId){
		setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
	}

	public String getOwnerId(){
		return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
	}
}
