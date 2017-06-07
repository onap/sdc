package org.openecomp.sdc.be.dao.jsongraph.types;

public enum EdgePropertyEnum {
	
	STATE ("state");

	private String property;
	
	public String getProperty() {
		return property;
	}

	EdgePropertyEnum (String property){
		this.property = property;
	}
	
	public static EdgePropertyEnum getByProperty(String property){
		for ( EdgePropertyEnum inst : EdgePropertyEnum.values() ){
			if ( inst.getProperty().equals(property) ){
				return inst;
			}
		}
		return null;
	}
}
