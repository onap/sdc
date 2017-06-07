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

package org.openecomp.sdc.be.resources.data;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import com.google.gson.reflect.TypeToken;

public class ArtifactData extends GraphNode {

	private ArtifactDataDefinition artifactDataDefinition;

	public ArtifactData() {
		super(NodeTypeEnum.ArtifactRef);
		artifactDataDefinition = new ArtifactDataDefinition();
	}

	public ArtifactData(ArtifactDataDefinition artifactDataDefinition) {
		super(NodeTypeEnum.ArtifactRef);
		this.artifactDataDefinition = artifactDataDefinition;

	}

	public ArtifactData(Map<String, Object> properties) {
		this();
		artifactDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		artifactDataDefinition.setArtifactType((String) properties.get(GraphPropertiesDictionary.ARTIFACT_TYPE.getProperty()));
		artifactDataDefinition.setArtifactRef((String) properties.get(GraphPropertiesDictionary.ARTIFACT_REF.getProperty()));
		artifactDataDefinition.setArtifactName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		artifactDataDefinition.setArtifactRepository((String) properties.get(GraphPropertiesDictionary.ARTIFACT_REPOSITORY.getProperty()));
		artifactDataDefinition.setArtifactChecksum((String) properties.get(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
		artifactDataDefinition.setArtifactCreator((String) properties.get(GraphPropertiesDictionary.CREATOR.getProperty()));
		artifactDataDefinition.setUserIdCreator((String) properties.get(GraphPropertiesDictionary.CREATOR_ID.getProperty()));
		artifactDataDefinition.setUserIdLastUpdater((String) properties.get(GraphPropertiesDictionary.LAST_UPDATER.getProperty()));
		artifactDataDefinition.setCreatorFullName((String) properties.get(GraphPropertiesDictionary.CREATOR_FULL_NAME.getProperty()));
		artifactDataDefinition.setUpdaterFullName((String) properties.get(GraphPropertiesDictionary.UPDATER_FULL_NAME.getProperty()));
		artifactDataDefinition.setCreationDate((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));
		artifactDataDefinition.setLastUpdateDate((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));
		artifactDataDefinition.setDescription((String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty()));
		artifactDataDefinition.setEsId((String) properties.get(GraphPropertiesDictionary.ES_ID.getProperty()));
		artifactDataDefinition.setArtifactLabel((String) properties.get(GraphPropertiesDictionary.ARTIFACT_LABEL.getProperty()));
		artifactDataDefinition.setMandatory((Boolean) properties.get(GraphPropertiesDictionary.IS_ABSTRACT.getProperty()));
		artifactDataDefinition.setArtifactChecksum((String) properties.get(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
		artifactDataDefinition.setArtifactDisplayName((String) properties.get(GraphPropertiesDictionary.ARTIFACT_DISPLAY_NAME.getProperty()));
		artifactDataDefinition.setApiUrl((String) properties.get(GraphPropertiesDictionary.API_URL.getProperty()));
		artifactDataDefinition.setServiceApi((Boolean) properties.get(GraphPropertiesDictionary.SERVICE_API.getProperty()));
		artifactDataDefinition.setArtifactVersion((String) properties.get(GraphPropertiesDictionary.ARTIFACT_VERSION.getProperty()));
		artifactDataDefinition.setArtifactUUID((String) properties.get(GraphPropertiesDictionary.ARTIFACT_UUID.getProperty()));
		artifactDataDefinition.setPayloadUpdateDate((Long) properties.get(GraphPropertiesDictionary.PAYLOAD_UPDATE_DATE.getProperty()));
		artifactDataDefinition.setHeatParamsUpdateDate((Long) properties.get(GraphPropertiesDictionary.HEAT_PARAMS_UPDATE_DATE.getProperty()));
		artifactDataDefinition.setGenerated((Boolean) properties.get(GraphPropertiesDictionary.GENERATED.getProperty()));
		Type listType = new TypeToken<List<String>>() {
		}.getType();
		List<String> requiredArtifactsfromJson = getGson().fromJson((String) properties.get(GraphPropertiesDictionary.REQUIRED_ARTIFACTS.getProperty()), listType);
		artifactDataDefinition.setRequiredArtifacts(requiredArtifactsfromJson);

		String groupType = (String) properties.get(GraphPropertiesDictionary.ARTIFACT_GROUP_TYPE.getProperty());
		if (groupType != null && !groupType.isEmpty()) {

			artifactDataDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.findType(groupType));
		}

		Integer timeout = (Integer) properties.get(GraphPropertiesDictionary.ARTIFACT_TIMEOUT.getProperty());
		if (timeout != null) {

			artifactDataDefinition.setTimeout(timeout);
		}

	}

	public ArtifactDataDefinition getArtifactDataDefinition() {
		return artifactDataDefinition;
	}

	public void setArtifactDataDefinition(ArtifactDataDefinition artifactDataDefinition) {
		this.artifactDataDefinition = artifactDataDefinition;
	}

	@Override
	public Object getUniqueId() {
		return artifactDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, artifactDataDefinition.getUniqueId());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_TYPE, artifactDataDefinition.getArtifactType());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_REF, artifactDataDefinition.getArtifactRef());
		addIfExists(map, GraphPropertiesDictionary.NAME, artifactDataDefinition.getArtifactName());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_REPOSITORY, artifactDataDefinition.getArtifactRepository());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_CHECKSUM, artifactDataDefinition.getArtifactChecksum());
		addIfExists(map, GraphPropertiesDictionary.CREATOR, artifactDataDefinition.getArtifactCreator());
		addIfExists(map, GraphPropertiesDictionary.CREATOR_ID, artifactDataDefinition.getUserIdCreator());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATER, artifactDataDefinition.getUserIdLastUpdater());
		addIfExists(map, GraphPropertiesDictionary.CREATOR_FULL_NAME, artifactDataDefinition.getCreatorFullName());
		addIfExists(map, GraphPropertiesDictionary.UPDATER_FULL_NAME, artifactDataDefinition.getUpdaterFullName());
		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, artifactDataDefinition.getCreationDate());
		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, artifactDataDefinition.getLastUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, artifactDataDefinition.getDescription());
		addIfExists(map, GraphPropertiesDictionary.ES_ID, artifactDataDefinition.getEsId());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_LABEL, artifactDataDefinition.getArtifactLabel());
		addIfExists(map, GraphPropertiesDictionary.IS_ABSTRACT, artifactDataDefinition.getMandatory());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_CHECKSUM, artifactDataDefinition.getArtifactChecksum());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_DISPLAY_NAME, artifactDataDefinition.getArtifactDisplayName());
		addIfExists(map, GraphPropertiesDictionary.API_URL, artifactDataDefinition.getApiUrl());
		addIfExists(map, GraphPropertiesDictionary.SERVICE_API, artifactDataDefinition.getServiceApi());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_TIMEOUT, artifactDataDefinition.getTimeout());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_VERSION, artifactDataDefinition.getArtifactVersion());
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_UUID, artifactDataDefinition.getArtifactUUID());
		addIfExists(map, GraphPropertiesDictionary.PAYLOAD_UPDATE_DATE, artifactDataDefinition.getPayloadUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.HEAT_PARAMS_UPDATE_DATE, artifactDataDefinition.getHeatParamsUpdateDate());
		addIfExists(map, GraphPropertiesDictionary.REQUIRED_ARTIFACTS, artifactDataDefinition.getRequiredArtifacts());
		addIfExists(map, GraphPropertiesDictionary.GENERATED, artifactDataDefinition.getGenerated());
		
		String groupType = null;
		ArtifactGroupTypeEnum groupTypeEnum = artifactDataDefinition.getArtifactGroupType();
		if (groupTypeEnum != null) {
			groupType = groupTypeEnum.getType();
		}
		addIfExists(map, GraphPropertiesDictionary.ARTIFACT_GROUP_TYPE, groupType);

		return map;
	}

	@Override
	public String toString() {
		return "ArtifactData [artifactDataDefinition=" + artifactDataDefinition + "]";
	}

}
