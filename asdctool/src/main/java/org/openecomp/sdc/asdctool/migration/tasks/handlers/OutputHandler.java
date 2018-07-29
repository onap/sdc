package org.openecomp.sdc.asdctool.migration.tasks.handlers;

public interface OutputHandler {

	public void initiate(String name, Object... title);
	public void addRecord(Object... record);
	public boolean writeOutputAndCloseFile();
	
}
