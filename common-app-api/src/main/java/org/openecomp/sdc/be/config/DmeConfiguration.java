package org.openecomp.sdc.be.config;

public class DmeConfiguration {
	private String dme2Search = "DME2SEARCH";
	private String dme2Resolve = "DME2RESOLVE";
	
	public String getDme2Search() {
		return dme2Search;
	}
	
	public void setDme2Search(String dme2Search) {
		this.dme2Search = dme2Search;
	}
	
	public String getDme2Resolve() {
		return dme2Resolve;
	}
	
	public void setDme2Resolve(String dme2Resolve) {
		this.dme2Resolve = dme2Resolve;
	}
	
	@Override
	public String toString() {
		return "DmeConfiguration [dme2Search=" + dme2Search + ", dme2Resolve=" + dme2Resolve + "]";
	}
}
