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
import java.util.Date;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "sdccomponent", name = "componentcache")
public class ComponentCacheData {
	public final static String RRESOURCE_ID_FIELD = "resourceId";

	public final static String SERVICE_NAME_FIELD = "serviceName";
	public final static String SERVICE_VERSION_FIELD = "serviceVersion";
	public final static String ARTIFACT_NAME_FIELD = "artifactName";

	public static String delim = ":";

	@PartitionKey
	@Column(name = "id")
	private String id;

	@Column
	private ByteBuffer data;

	@Column(name = "modification_time")
	private Date modificationTime;

	@Column
	private String type;

	@Column(name = "is_dirty")
	private boolean isDirty;

	@Column(name = "is_zipped")
	private boolean isZipped;

	public ComponentCacheData() {

	}

	public ComponentCacheData(String id, byte[] data, Date modificationTime, String type, boolean isDirty,
			boolean isZipped) {
		super();
		this.id = id;
		if (data != null) {
			this.data = ByteBuffer.wrap(data.clone());
		}
		this.modificationTime = modificationTime;
		this.type = type;
		this.isDirty = isDirty;
		this.isZipped = isZipped;
	}

	public ComponentCacheData(String id) {

		this.id = id;
	}

	public ComponentCacheData(String artifactId, byte[] data) {
		super();
		this.id = artifactId;
		if (data != null) {
			this.data = ByteBuffer.wrap(data.clone());
			// this.data = data.clone();
		}
	}

	public byte[] getDataAsArray() {
		if (data != null) {
			return data.array();
		}
		return null;
	}

	public void setDataAsArray(byte[] data) {
		if (data != null) {
			this.data = ByteBuffer.wrap(data.clone());
		}
	}

	public ByteBuffer getData() {
		return data;
	}

	public void setData(ByteBuffer data) {
		if (data != null) {
			this.data = data.duplicate();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean getIsDirty() {
		return isDirty;
	}

	public void setIsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public boolean getIsZipped() {
		return isZipped;
	}

	public void setIsZipped(boolean isZipped) {
		this.isZipped = isZipped;
	}

}
