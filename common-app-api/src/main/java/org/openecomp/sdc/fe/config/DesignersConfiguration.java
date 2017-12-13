package org.openecomp.sdc.fe.config;

import java.util.List;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class DesignersConfiguration extends BasicConfiguration { 
	
	private List <Designer> designersList;
	
	public List<Designer> getDesignersList() {
		return designersList;
	}

	public void setDesignersList(List<Designer> designersList) {
		this.designersList = designersList;
	}

	public static class Designer { 
		
		private String displayName;
		private String designerHost;		
		private Integer designerPort;		
		private String designerPath;
		

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


