package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

public class InputDataDefinition extends PropertyDataDefinition{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8913646848974511031L;
	String label;
	Boolean hidden;
	Boolean immutable;
	
	public InputDataDefinition(){
		super();
	}

	public InputDataDefinition(Map<String, Object> pr) {
		super(pr);
		
	}

	public InputDataDefinition(InputDataDefinition p) {		
		
		super(p);
		this.setLabel(p.getLabel());
		this.setHidden( p.isHidden());
		this.setImmutable( p.isImmutable());
			
		
	}
	
	public InputDataDefinition(PropertyDataDefinition p) {		
		
		super(p);
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
