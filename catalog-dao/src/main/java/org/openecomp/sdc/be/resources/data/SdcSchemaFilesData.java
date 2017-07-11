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

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "sdcartifact", name = "sdcschemafiles")
public class SdcSchemaFilesData {
	@PartitionKey(0)
	@Column(name = "sdcreleasenum")
	private String sdcReleaseNum;
	
	@ClusteringColumn
	@Column(name = "timestamp")
	private Date timestamp;
	
	@PartitionKey(1)
	@Column(name = "conformanceLevel")
	private String conformanceLevel;

	@Column(name = "fileName")
	private String fileName;

	@Column(name = "payload")
	private ByteBuffer payload;
	
	@Column(name = "checksum")
	private String checksum;
	
	public SdcSchemaFilesData() {
	
	}
	
	public SdcSchemaFilesData(String sdcReleaseNum, Date timestamp, String conformanceLevel, String fileName, byte[] payload, String checksum){
		this.sdcReleaseNum = sdcReleaseNum;
		this.timestamp = timestamp;
		this.conformanceLevel = conformanceLevel;
		this.fileName = fileName;
		if(payload != null) {
			this.payload = ByteBuffer.wrap(payload.clone());
		}
		this.checksum = checksum;
	}
	
	public String getSdcReleaseNum() {
		return sdcReleaseNum;
	}

	public void setSdcReleaseNum(String sdcReleaseNum) {
		this.sdcReleaseNum = sdcReleaseNum;
	}

	public String getConformanceLevel() {
		return conformanceLevel;
	}

	public void setConformanceLevel(String conformanceLevel) {
		this.conformanceLevel = conformanceLevel;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ByteBuffer getPayload() {
		return payload;
	}

	public void setPayload(ByteBuffer payload) {
		if(payload != null){
			this.payload = payload.duplicate();
		}
	}
	
	public void setPayloadAsArray(byte[] payload) {
		if(payload != null){
			this.payload = ByteBuffer.wrap(payload.clone());
		}
	}
	
	public byte[] getPayloadAsArray() {
		return payload != null ? payload.array() : null;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	public String toString() {
		return "SdcSchemaFilesData [sdcReleaseNum=" + sdcReleaseNum + ", timestamp=" + timestamp + ", conformanceLevel="
				+ conformanceLevel + ", fileName=" + fileName + ", checksum=" + checksum + "]";
	}
}
