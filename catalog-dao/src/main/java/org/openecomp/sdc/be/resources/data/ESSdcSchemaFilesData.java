package org.openecomp.sdc.be.resources.data;

import java.nio.ByteBuffer;
import java.util.Date;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "sdcartifact", name = "sdcschemafiles")
public class ESSdcSchemaFilesData {
		
	@Column(name = "sdcreleasenum")
	private String sdcReleaseNum;

	@Column(name = "timestamp")
	private Date timestamp;

	@Column(name = "conformanceLevel")
	private String conformanceLevel;

	@Column(name = "fileName")
	private String fileName;

	@Column(name = "payload")
	private ByteBuffer payload;
	
	@Column(name = "checksum")
	private String checksum;
	
	public ESSdcSchemaFilesData() {
	
	}
	
	public ESSdcSchemaFilesData(String sdcReleaseNum, String conformanceLevel, String fileName, byte[] payload){
		this.sdcReleaseNum = sdcReleaseNum;
		this.conformanceLevel = conformanceLevel;
		this.fileName = fileName;
		if(payload != null) {
			this.payload = ByteBuffer.wrap(payload.clone());
		}
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
}
