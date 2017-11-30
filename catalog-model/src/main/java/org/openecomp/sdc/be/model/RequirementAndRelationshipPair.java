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

package org.openecomp.sdc.be.model;

import java.io.Serializable;

import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;

public class RequirementAndRelationshipPair implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5763126570618602135L;

	private String requirement;
	private String capabilityOwnerId;
	private String requirementOwnerId;
	private String id;

	private RelationshipImpl relationship;

	private String capability;

	private String capabilityUid;
	private String requirementUid;

	public RequirementAndRelationshipPair() {
		super();
	}

	public RequirementAndRelationshipPair(String requirement, RelationshipImpl relationship) {
		super();
		this.requirement = requirement;
		this.relationship = relationship;
	}

	public RequirementAndRelationshipPair(String requirement, RelationshipImpl relationship, String capability) {
		super();
		this.requirement = requirement;
		this.relationship = relationship;
		this.capability = capability;
	}

	public String getRequirement() {
		return requirement;
	}

	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}

	public String getCapabilityOwnerId() {
		return capabilityOwnerId;
	}

	public void setCapabilityOwnerId(String capabilityOwnerId) {
		this.capabilityOwnerId = capabilityOwnerId;
	}

	public String getRequirementOwnerId() {
		return requirementOwnerId;
	}

	public void setRequirementOwnerId(String requirementOwnerId) {
		this.requirementOwnerId = requirementOwnerId;
	}

	public RelationshipImpl getRelationship() {
		return relationship;
	}

	public void setRelationships(RelationshipImpl relationship) {
		this.relationship = relationship;
	}

	public String getCapability() {
		return capability;
	}

	public void setCapability(String capability) {
		this.capability = capability;
	}

	public String getCapabilityUid() {
		return capabilityUid;
	}

	public void setCapabilityUid(String capabilityUid) {
		this.capabilityUid = capabilityUid;
	}

	public String getRequirementUid() {
		return requirementUid;
	}

	public void setRequirementUid(String requirementUid) {
		this.requirementUid = requirementUid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "RequirementAndRelationshipPair [requirement=" + requirement + ", relationship=" + relationship
				+ ", capability=" + capability + "]";
	}

	public boolean equalsTo( RelationshipInstDataDefinition savedRelation){
		if ( savedRelation == null ){
			return false;
		}
		if(getRelationship().getType() == null ){
			if(savedRelation.getType() != null){
				return false;
			}
		}
		else { 
			if ( !savedRelation.getType().equals(this.getRelationship().getType()) ){
				return false;
			}	
		}
		if ( !savedRelation.getCapabilityOwnerId().equals(this.getCapabilityOwnerId()) ){
			return false;
		}
		if ( !savedRelation.getRequirementOwnerId().equals(this.getRequirementOwnerId()) ){
			return false;
		}
		if ( !savedRelation.getRequirementId().equals(this.getRequirementUid()) ){
			return false;
		}
		if ( !savedRelation.getCapabilityId().equals(this.getCapabilityUid()) ){
			return false;
		}
		if ( !savedRelation.getRequirement().equals(this.getRequirement()) ){
			return false;
		}
		return true;
	}
}
