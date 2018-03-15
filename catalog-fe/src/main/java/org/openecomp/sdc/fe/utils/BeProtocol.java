package org.openecomp.sdc.fe.utils;

public enum BeProtocol {
		HTTP("http"), SSL("ssl");
		private String protocolName;

		public String getProtocolName() {
			return protocolName;
		}

		BeProtocol(String protocolName) {
			this.protocolName = protocolName;
		}
	};