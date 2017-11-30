package org.openecomp.sdc.asdctool.migration.tasks.handlers;

public interface OutputHandler {

	public void initiate(Object... title);
	public void addRecord(Object... record);
	public boolean writeOutput();
	
}
