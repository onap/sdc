package org.openecomp.sdc.fe.impl;

public class MdcData {
		private String serviceInstanceID;
		private String userId;
		private String remoteAddr;
		private String localAddr;
		private Long transactionStartTime;

		public MdcData(String serviceInstanceID, String userId, String remoteAddr, String localAddr, Long transactionStartTime) {
			super();
			this.serviceInstanceID = serviceInstanceID;
			this.userId = userId;
			this.remoteAddr = remoteAddr;
			this.localAddr = localAddr;
			this.transactionStartTime = transactionStartTime;
		}

		public Long getTransactionStartTime() {
			return transactionStartTime;
		}

		public String getUserId() {
			return userId;
		}

		public String getRemoteAddr() {
			return remoteAddr;
		}

		public String getLocalAddr() {
			return localAddr;
		}

		public String getServiceInstanceID() {
			return serviceInstanceID;
		}
	}