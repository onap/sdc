package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;

public class ToscaPolicyTemplate {
	private String type;
    private IToscaMetadata metadata;
    private Map<String, Object> properties;
    private List<String> targets;
    
    public ToscaPolicyTemplate(String type, IToscaMetadata metadata, Map<String, Object> properties, List<String> targets) {
		this.type = type;
		this.metadata = metadata;
		this.properties = properties;
		this.targets = targets;
	}

	public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IToscaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(IToscaMetadata metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

	public List<String> getTargets() {
		return targets;
	}

	public void setTargets(List<String> targets) {
		this.targets = targets;
	}
    
    
    
}
