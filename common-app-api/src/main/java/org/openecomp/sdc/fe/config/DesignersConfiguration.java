package org.openecomp.sdc.fe.config;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class DesignersConfiguration extends BasicConfiguration { 
	
	private Map<String,Designer> designersList;
	
	public Map<String,Designer> getDesignersList() {
		return designersList;
	}

	public void setDesignersList(Map<String,Designer> designersList) {
		this.designersList = designersList;
	}
	
	public DesignersConfiguration() {
		this.designersList = new HashMap<String, Designer>();
	}

	public static class Designer { 
		
		private String displayName;
		private String designerHost;		
		private Integer designerPort;		
		private String designerPath;
		private String designerProtocol;
		

		public String getDesignerProtocol() {
			return designerProtocol;
		}

		public void setDesignerProtocol(String designerProtocol) {
			this.designerProtocol = designerProtocol;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getDesignerHost() {
			return designerHost;
		}

		public void setDesignerHost(String designerHost) {
			this.designerHost = designerHost;
		}

		public Integer getDesignerPort() {
			return designerPort;
		}

		public void setDesignerPort(Integer designerPort) {
			this.designerPort = designerPort;
		}

		public String getDesignerPath() {
			return designerPath;
		}

		public void setDesignerPath(String designerPath) {
			this.designerPath = designerPath;
		}

	}

}


