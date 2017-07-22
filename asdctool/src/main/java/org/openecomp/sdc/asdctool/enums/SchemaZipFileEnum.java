package org.openecomp.sdc.asdctool.enums;

public enum SchemaZipFileEnum {
	
	DATA("data", "data-types", "dataTypes", "data_types", new String[]{}),
	GROUPS("groups", "group-types", "groupTypes", "group_types", new String[]{"data.yml"}),
	POLICIES("policies", "policy-types", "policyTypes","policy_types", new String[]{"data.yml"}),
	RELATIONSHIPS("relationships","relationship-types","relationshipTypes", "relationship_types", new String[]{"capabilities.yml", "data.yml", "interfaces.yml"}),
	ARTIFACTS("artifacts", "artifact-types", "artifactTypes", "artifact_types", new String[]{"data.yml"}),
	CAPABILITIES("capabilities", "capability-types", "capabilityTypes", "capability_types" ,new String[]{"data.yml"}),
	INTERFACES("interfaces", "interface-lifecycle-types", "interfaceLifecycleTypes", "interface_types", new String[]{"data.yml"});
	
	private String fileName;
	private String sourceFolderName;
	private String sourceFileName;
	private String collectionTitle;
	private String[] importFileList;

	private SchemaZipFileEnum(String fileName, String sourceFolderName, String sourceFileName, String collectionTitle,
			String[] importFileList) {
		this.fileName = fileName;
		this.sourceFolderName = sourceFolderName;
		this.sourceFileName = sourceFileName;
		this.collectionTitle = collectionTitle;
		this.importFileList = importFileList;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSourceFolderName() {
		return sourceFolderName;
	}

	public void setSourceFolderName(String sourceFolderName) {
		this.sourceFolderName = sourceFolderName;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getCollectionTitle() {
		return collectionTitle;
	}

	public void setCollectionTitle(String collectionTitle) {
		this.collectionTitle = collectionTitle;
	}

	public String[] getImportFileList() {
		return importFileList;
	}

	public void setImportFileList(String[] importFileList) {
		this.importFileList = importFileList;
	}
	
}
