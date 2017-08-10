package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.yaml.snakeyaml.Yaml;

public class CsarInfo {
	String vfResourceName;
	User modifier;
	String csarUUID;
	Map<String, byte[]> csar;
	String mainTemplateContent;
	Map<String, Object> mappedToscaMainTemplate;
	Map<String, String> createdNodesToscaResourceNames;
	Queue<String> cvfcToCreateQueue;
	boolean isUpdate;
	Map<String, Resource> createdNodes;
	
	@SuppressWarnings("unchecked")
	public CsarInfo(String vfResourceName, User modifier, String csarUUID, Map<String, byte[]> csar, String mainTemplateContent, boolean isUpdate){
		this.vfResourceName = vfResourceName;
		this.modifier = modifier;
		this.csarUUID = csarUUID;
		this.csar = csar;
		this.mainTemplateContent = mainTemplateContent;
		this.mappedToscaMainTemplate = (Map<String, Object>) new Yaml().load(mainTemplateContent);
		this.createdNodesToscaResourceNames = new HashMap<>();
		this.cvfcToCreateQueue = new PriorityQueue<>();
		this.isUpdate = isUpdate;
		this.createdNodes  = new HashMap<>(); 
	}

	public String getVfResourceName() {
		return vfResourceName;
	}

	public void setVfResourceName(String vfResourceName) {
		this.vfResourceName = vfResourceName;
	}

	public User getModifier() {
		return modifier;
	}

	public void setModifier(User modifier) {
		this.modifier = modifier;
	}

	public String getCsarUUID() {
		return csarUUID;
	}

	public void setCsarUUID(String csarUUID) {
		this.csarUUID = csarUUID;
	}

	public Map<String, byte[]> getCsar() {
		return csar;
	}

	public void setCsar(Map<String, byte[]> csar) {
		this.csar = csar;
	}

	public String getMainTemplateContent() {
		return mainTemplateContent;
	}

	public Map<String, Object> getMappedToscaMainTemplate() {
		return mappedToscaMainTemplate;
	}

	public Map<String, String> getCreatedNodesToscaResourceNames() {
		return createdNodesToscaResourceNames;
	}

	public void setCreatedNodesToscaResourceNames(Map<String, String> createdNodesToscaResourceNames) {
		this.createdNodesToscaResourceNames = createdNodesToscaResourceNames;
	}

	public Queue<String> getCvfcToCreateQueue() {
		return cvfcToCreateQueue;
	}

	public void setCvfcToCreateQueue(Queue<String> cvfcToCreateQueue) {
		this.cvfcToCreateQueue = cvfcToCreateQueue;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	public Map<String, Resource> getCreatedNodes() {
		return createdNodes;
	}

}
