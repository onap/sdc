package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class ImportCsarInfo {
	String vfResourceName;
	User modifier;
	String csarUUID;
	Map<String, byte[]> csar;
	Map<String, String> createdNodesToscaResourceNames;
	Queue<String> cvfcToCreateQueue;
	
	public ImportCsarInfo(String vfResourceName, User modifier, String csarUUID, Map<String, byte[]> csar){
		this.vfResourceName = vfResourceName;
		this.modifier = modifier;
		this.csarUUID = csarUUID;
		this.csar = csar;
		this.createdNodesToscaResourceNames = new HashMap<>();
		this.cvfcToCreateQueue = new PriorityQueue<>();
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
	
}
