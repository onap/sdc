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

import java.nio.ByteBuffer;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "sdcartifact", name = "resources")
public class ESArtifactData {
	public static final String RRESOURCE_ID_FIELD = "resourceId";

	public static final String SERVICE_NAME_FIELD = "serviceName";
	public static final String SERVICE_VERSION_FIELD = "serviceVersion";
	public static final String ARTIFACT_NAME_FIELD = "artifactName";

	public static String delim = ":";

	@PartitionKey
	@Column(name = "id")
	private String id;

	/*
	 * Base64 encoded Artifact file data
	 */

	@Column
	private ByteBuffer data;

	// private byte[] data;

	public ESArtifactData() {

	}

	public ESArtifactData(String id) {

		this.id = id;
	}

	public ESArtifactData(String artifactId, byte[] data) {
		super();
		this.id = artifactId;
		if (data != null) {
			this.data = ByteBuffer.wrap(data.clone());
			// this.data = data.clone();
		}

	}

	public byte[] getDataAsArray() {
		// return data;
		if (data != null) {
			return data.array();
		}
		return null;
	}

	public void setDataAsArray(byte[] data) {
		if (data != null) {
			// this.data = data.clone();
			this.data = ByteBuffer.wrap(data.clone());
		}
	}

	public ByteBuffer getData() {
		// return data;
		return data;
	}

	public void setData(ByteBuffer data) {
		if (data != null) {
			// this.data = data.clone();
			this.data = data.duplicate();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
