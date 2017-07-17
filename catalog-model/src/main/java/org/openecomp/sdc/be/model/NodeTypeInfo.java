package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;

public class NodeTypeInfo {
	String type;
	String templateFileName;
	List<String> derivedFrom;
	boolean isNested;
	Map<String, Object> mappedToscaTemplate;
	
	public NodeTypeInfo getUnmarkedCopy(){
		NodeTypeInfo unmarked = new NodeTypeInfo();
		unmarked.type = this.type;
		unmarked.templateFileName = this.templateFileName;
		unmarked.derivedFrom = this.derivedFrom;
		unmarked.isNested = false;
		unmarked.mappedToscaTemplate = this.mappedToscaTemplate;
		return unmarked;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTemplateFileName() {
		return templateFileName;
	}
	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}
	public List<String> getDerivedFrom() {
		return derivedFrom;
	}
	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
	public boolean isNested() {
		return isNested;
	}
	public void setNested(boolean isNested) {
		this.isNested = isNested;
	}

	public Map<String, Object> getMappedToscaTemplate() {
		return mappedToscaTemplate;
	}

	public void setMappedToscaTemplate(Map<String, Object> mappedToscaTemplate) {
		this.mappedToscaTemplate = mappedToscaTemplate;
	}
}
