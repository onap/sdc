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

package org.openecomp.sdc.ci.tests.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

public class Config {

	private static String WINDOWS_CONFIG_FILE = "src/main/resources/ci/conf/attsdc.yaml";
	private boolean systemUnderDebug;
	private boolean rerun;
	private String reportDBhost;
	private int reportDBport;
	
	private String browser;
	private String catalogBeHost;
	private String esHost;
	private String esPort;
	private String neoHost;
	private String neoPort;
	private String disributionClientHost;
	private String disributionClientPort;
	private boolean isDistributionClientRunning;


	private String errorConfigurationFile;
	private String resourceConfigDir;
	private String componentsConfigDir;
	private String importResourceConfigDir;
	private String importResourceTestsConfigDir;
	private String importTypesConfigDir;

	private String testSuites;

	private String catalogFeHost;
	private String catalogFePort;
	private String catalogBePort;
	private String catalogBeTlsPort;

	private String neoDBusername;
	private String neoDBpassword;

	private String titanPropertiesFile;
	private List<String> packages;
	private List<String> bugs;
	private List<String> resourcesNotToDelete;
	private List<String> resourceCategoriesNotToDelete;
	private List<String> serviceCategoriesNotToDelete;
	private boolean stopOnClassFailure = false;

	private String outputFolder;
	private String reportName;
	private String url;
	private String remoteTestingMachineIP;
	private String remoteTestingMachinePort;
	private boolean remoteTesting;

	private String cassandraHost;
	private String cassandraAuditKeySpace;
	private String cassandraArtifactKeySpace;
	private boolean cassandraAuthenticate;
	private String cassandraUsername;
	private String cassandraPassword;
	private boolean cassandraSsl;
	private String cassandraTruststorePath;
	private String cassandraTruststorePassword;
	private String windowsDownloadDirectory;
	private boolean captureTraffic;
	private boolean useBrowserMobProxy;
	
	private static Config configIt = null;

	private static Yaml yaml = new Yaml();

	
	private Config() {
		super();
	}

	public static class TestPackages {

		List<String> packages;
		List<String> bugs;

		public List<String> getPackages() {
			return packages;
		}

		public void setPackages(List<String> packages) {
			this.packages = packages;
		}

		public List<String> getBugs() {
			return bugs;
		}

		public void setBugs(List<String> bugs) {
			this.bugs = bugs;
		}

		@Override
		public String toString() {
			return "TestPackages [packages=" + packages + ", bugs=" + bugs + "]";
		}

	}

	public synchronized static Config instance() {
		if (configIt == null) {
			try {
				configIt = init();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return configIt;
	}

	private static Config init() throws IOException {

		Config config = null;

		String configFile = System.getProperty("config.resource");
		if (configFile == null) {
			if (System.getProperty("os.name").contains("Windows")) {
				configFile = WINDOWS_CONFIG_FILE;
			} else {
				throw new RuntimeException("Please Add Jvm Argument config.resource");
			}
		}

		File file = new File(configFile);
		if (false == file.exists()) {
			throw new RuntimeException("The config file " + configFile + " cannot be found.");
		}

		InputStream in = null;
		try {

			in = Files.newInputStream(Paths.get(configFile));

			config = yaml.loadAs(in, Config.class);

			setPackagesAndBugs(configFile, config);

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// JsonReader jsonReader = new JsonReader(new FileReader(configFile));
		// Config configAttOdlIt = new Gson().fromJson(jsonReader,
		// Config.class);

		return config;
	}

	private static void setPackagesAndBugs(String path, Config config) throws IOException {

		int separator = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
		String dirPath = path.substring(0, separator + 1);
		String packagesFile = dirPath + File.separator + "attsdc-packages.yaml";
		File file = new File(packagesFile);
		if (false == file.exists()) {
			throw new RuntimeException("The config file " + packagesFile + " cannot be found.");
		}

		TestPackages testPackages = null;
		InputStream in = null;
		try {

			in = Files.newInputStream(Paths.get(packagesFile));

			testPackages = yaml.loadAs(in, TestPackages.class);

			List<String> bugs = testPackages.getBugs();
			List<String> packages = testPackages.getPackages();

			config.setBugs(bugs);
			config.setPackages(packages);

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// public Config(String catalogBeHost, String esHost, String esPort, String
	// resourceConfigDir, String componentsConfigDir, String catalogFeHost,
	// String catalogFePort, String catalogBePort) {
	// super();
	// this.catalogBeHost = catalogBeHost;
	// this.esHost = esHost;
	// this.esPort = esPort;
	// this.resourceConfigDir = resourceConfigDir;
	// this.componentsConfigDir = componentsConfigDir;
	// this.catalogFeHost = catalogFeHost;
	// this.catalogFePort = catalogFePort;
	// this.catalogBePort = catalogBePort;
	// }

	String configurationFile;
	
	public boolean getSystemUnderDebug() {
		return systemUnderDebug;
	}

	public void setSystemUnderDebug(boolean systemUnderDebug) {
		this.systemUnderDebug = systemUnderDebug;
	}

	public boolean getRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		this.rerun = rerun;
	}
	
	public String getReportDBhost() {
		return reportDBhost;
	}

	public void setReportDBhost(String reportDBhost) {
		this.reportDBhost = reportDBhost;
	}

	public int getReportDBport() {
		return reportDBport;
	}

	public void setReportDBport(int reportDBport) {
		this.reportDBport = reportDBport;
	}
//	public boolean isUsingBrowserMobProxy() {
//		return useBrowserMobProxy;
//	}
//
//	public void setUsingBrowserMobProxy(boolean usingBrowserMobProxy) {
//		this.useBrowserMobProxy = usingBrowserMobProxy;
//	} 

	
	
	
	public String getBrowser() {
		return browser;
	}
	
	public boolean getUseBrowserMobProxy() {
		return useBrowserMobProxy;
	}

	public void setUseBrowserMobProxy(boolean useBrowserMobProxy) {
		this.useBrowserMobProxy = useBrowserMobProxy;
	}



	public boolean getCaptureTraffic() {
		return captureTraffic;
	}

	public void setCaptureTraffic(boolean captureTraffic) {
		this.captureTraffic = captureTraffic;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getConfigurationFile() {
		return configurationFile;
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	public boolean getIsDistributionClientRunning() {
		return isDistributionClientRunning;
	}

	public void setIsDistributionClientRunning(boolean isDistributionClientRunning) {
		this.isDistributionClientRunning = isDistributionClientRunning;
	}
	
	public String getCatalogBePort() {
		return catalogBePort;
	}

	public String getDisributionClientHost() {
		return disributionClientHost;
	}

	public void setDisributionClientHost(String disributionClientHost) {
		this.disributionClientHost = disributionClientHost;
	}

	public String getDisributionClientPort() {
		return disributionClientPort;
	}

	public void setDisributionClientPort(String disributionClientPort) {
		this.disributionClientPort = disributionClientPort;
	}

	public void setCatalogBePort(String catalogBePort) {
		this.catalogBePort = catalogBePort;
	}

	public String getCatalogFeHost() {
		return catalogFeHost;
	}

	public void setCatalogFeHost(String catalogFeHost) {
		this.catalogFeHost = catalogFeHost;
	}

	public String getCatalogFePort() {
		return catalogFePort;
	}

	public void setCatalogFePort(String catalogFePort) {
		this.catalogFePort = catalogFePort;
	}

	public String getCatalogBeHost() {
		return catalogBeHost;
	}

	public void setCatalogBeHost(String catalogBeHost) {
		this.catalogBeHost = catalogBeHost;
	}

	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public String getEsPort() {
		return esPort;
	}

	public void setEsPort(String esPort) {
		this.esPort = esPort;
	}

	public String getResourceConfigDir() {
		return resourceConfigDir;
	}

	public void setResourceConfigDir(String resourceConfigDir) {
		this.resourceConfigDir = resourceConfigDir;
	}

	public String getComponentsConfigDir() {
		return componentsConfigDir;
	}

	public void setComponentsConfigDir(String componentsConfigDir) {
		this.componentsConfigDir = componentsConfigDir;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getNeoPort() {
		return neoPort;
	}

	public void setNeoPort(String neoPort) {
		this.neoPort = neoPort;
	}

	public String getNeoHost() {
		return neoHost;
	}

	public void setNeoHost(String neoHost) {
		this.neoHost = neoHost;
	}

	public String getNeoDBpassword() {
		return neoDBpassword;
	}

	public String getNeoDBusername() {
		return neoDBusername;
	}

	public void setNeoDBusername(String neoDBusername) {
		this.neoDBusername = neoDBusername;
	}

	public void setNeoDBpassword(String neoDBpassword) {
		this.neoDBpassword = neoDBpassword;
	}

	public String getTitanPropertiesFile() {
		return titanPropertiesFile;
	}

	public void setTitanPropertiesFile(String titanPropertiesFile) {
		this.titanPropertiesFile = titanPropertiesFile;
	}

	public List<String> getPackages() {
		return packages;
	}

	public void setPackages(List<String> packages) {
		this.packages = packages;
	}

	public List<String> getBugs() {
		return bugs;
	}

	public void setBugs(List<String> bugs) {
		this.bugs = bugs;
	}

	public boolean isStopOnClassFailure() {
		return stopOnClassFailure;
	}

	public void setStopOnClassFailure(boolean stopOnClassFailure) {
		this.stopOnClassFailure = stopOnClassFailure;
	}

	public String getImportResourceConfigDir() {
		return importResourceConfigDir;
	}

	public void setImportResourceConfigDir(String importResourceConfigDir) {
		this.importResourceConfigDir = importResourceConfigDir;
	}

	public String getImportResourceTestsConfigDir() {
		return importResourceTestsConfigDir;
	}

	public void setImportResourceTestsConfigDir(String importResourceTestsConfigDir) {
		this.importResourceTestsConfigDir = importResourceTestsConfigDir;
	}

	public String getErrorConfigurationFile() {
		return errorConfigurationFile;
	}

	public void setErrorConfigurationFile(String errorConfigurationFile) {
		this.errorConfigurationFile = errorConfigurationFile;
	}

	public String getCatalogBeTlsPort() {
		return catalogBeTlsPort;
	}

	public void setCatalogBeTlsPort(String catalogBeTlsPort) {
		this.catalogBeTlsPort = catalogBeTlsPort;
	}

	public List<String> getResourcesNotToDelete() {
		return resourcesNotToDelete;
	}

	public void setResourcesNotToDelete(List<String> resourcesNotToDelete) {
		this.resourcesNotToDelete = resourcesNotToDelete;
	}

	public List<String> getResourceCategoriesNotToDelete() {
		return resourceCategoriesNotToDelete;
	}

	public void setResourceCategoriesNotToDelete(List<String> resourceCategoriesNotToDelete) {
		this.resourceCategoriesNotToDelete = resourceCategoriesNotToDelete;
	}

	public List<String> getServiceCategoriesNotToDelete() {
		return serviceCategoriesNotToDelete;
	}

	public void setServiceCategoriesNotToDelete(List<String> serviceCategoriesNotToDelete) {
		this.serviceCategoriesNotToDelete = serviceCategoriesNotToDelete;
	}

	public String getImportTypesConfigDir() {
		return importTypesConfigDir;
	}

	public void setImportTypesConfigDir(String importTypesConfigDir) {
		this.importTypesConfigDir = importTypesConfigDir;
	}

	public String getCassandraHost() {
		return cassandraHost;
	}

	public void setCassandraHost(String cassandraHost) {
		this.cassandraHost = cassandraHost;
	}

	public String getCassandraAuditKeySpace() {
		return cassandraAuditKeySpace;
	}

	public void setCassandraAuditKeySpace(String cassandraAuditKeySpace) {
		this.cassandraAuditKeySpace = cassandraAuditKeySpace;
	}

	public String getCassandraArtifactKeySpace() {
		return cassandraArtifactKeySpace;
	}

	public void setCassandraArtifactKeySpace(String cassandraArtifactKeySpace) {
		this.cassandraArtifactKeySpace = cassandraArtifactKeySpace;
	}

	
	public String getWindowsDownloadDirectory() {
		return windowsDownloadDirectory;
	}
	
	public void setWindowsDownloadDirectory(String windowsDownloadDirectory) {
		this.windowsDownloadDirectory = windowsDownloadDirectory;
	}
	
	@Override
	public String toString() {
		return "Config [systemUnderDebug=" + systemUnderDebug + ", rerun=" + rerun + ", reportDBhost=" + reportDBhost
				+ ", reportDBport=" + reportDBport + ", browser=" + browser + ", catalogBeHost=" + catalogBeHost
				+ ", esHost=" + esHost + ", esPort=" + esPort + ", neoHost=" + neoHost + ", neoPort=" + neoPort
				+ ", disributionClientHost=" + disributionClientHost + ", disributionClientPort="
				+ disributionClientPort + ", isDistributionClientRunning=" + isDistributionClientRunning
				+ ", errorConfigurationFile=" + errorConfigurationFile + ", resourceConfigDir=" + resourceConfigDir
				+ ", componentsConfigDir=" + componentsConfigDir + ", importResourceConfigDir="
				+ importResourceConfigDir + ", importResourceTestsConfigDir=" + importResourceTestsConfigDir
				+ ", importTypesConfigDir=" + importTypesConfigDir + ", testSuites=" + testSuites + ", catalogFeHost="
				+ catalogFeHost + ", catalogFePort=" + catalogFePort + ", catalogBePort=" + catalogBePort
				+ ", catalogBeTlsPort=" + catalogBeTlsPort + ", neoDBusername=" + neoDBusername + ", neoDBpassword="
				+ neoDBpassword + ", titanPropertiesFile=" + titanPropertiesFile + ", packages=" + packages + ", bugs="
				+ bugs + ", resourcesNotToDelete=" + resourcesNotToDelete + ", resourceCategoriesNotToDelete="
				+ resourceCategoriesNotToDelete + ", serviceCategoriesNotToDelete=" + serviceCategoriesNotToDelete
				+ ", stopOnClassFailure=" + stopOnClassFailure + ", outputFolder=" + outputFolder + ", reportName="
				+ reportName + ", url=" + url + ", remoteTestingMachineIP=" + remoteTestingMachineIP
				+ ", remoteTestingMachinePort=" + remoteTestingMachinePort + ", remoteTesting=" + remoteTesting
				+ ", cassandraHost=" + cassandraHost + ", cassandraAuditKeySpace=" + cassandraAuditKeySpace
				+ ", cassandraArtifactKeySpace=" + cassandraArtifactKeySpace + ", cassandraAuthenticate="
				+ cassandraAuthenticate + ", cassandraUsername=" + cassandraUsername + ", cassandraPassword="
				+ cassandraPassword + ", cassandraSsl=" + cassandraSsl + ", cassandraTruststorePath="
				+ cassandraTruststorePath + ", cassandraTruststorePassword=" + cassandraTruststorePassword
				+ ", windowsDownloadDirectory=" + windowsDownloadDirectory + ", captureTraffic=" + captureTraffic
				+ ", useBrowserMobProxy=" + useBrowserMobProxy + ", configurationFile=" + configurationFile + "]";
	}

	public boolean isRemoteTesting() {
		return remoteTesting;
	}

	public void setRemoteTesting(boolean remoteTesting) {
		this.remoteTesting = remoteTesting;
	}

	public String getUrl() {
		try {
			return url;
		} catch (Exception e) {
			return null;
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRemoteTestingMachineIP() {
		return remoteTestingMachineIP;
	}

	public void setRemoteTestingMachineIP(String remoteTestingMachineIP) {
		this.remoteTestingMachineIP = remoteTestingMachineIP;
	}

	public String getRemoteTestingMachinePort() {
		return remoteTestingMachinePort;
	}

	public void setRemoteTestingMachinePort(String remoteTestingMachinePort) {
		this.remoteTestingMachinePort = remoteTestingMachinePort;
	}

	public boolean getCassandraAuthenticate() {
		return cassandraAuthenticate;
	}

	public void setCassandraAuthenticate(boolean cassandraAuthenticate) {
		this.cassandraAuthenticate = cassandraAuthenticate;
	}

	public String getCassandraUsername() {
		return cassandraUsername;
	}

	public void setCassandraUsername(String cassandraUsername) {
		this.cassandraUsername = cassandraUsername;
	}

	public String getCassandraPassword() {
		return cassandraPassword;
	}

	public void setCassandraPassword(String cassandraPassword) {
		this.cassandraPassword = cassandraPassword;
	}

	public boolean getCassandraSsl() {
		return cassandraSsl;
	}

	public void setCassandraSsl(boolean cassandraSsl) {
		this.cassandraSsl = cassandraSsl;
	}

	public String getCassandraTruststorePath() {
		return cassandraTruststorePath;
	}

	public void setCassandraTruststorePath(String cassandraTruststorePath) {
		this.cassandraTruststorePath = cassandraTruststorePath;
	}

	public String getCassandraTruststorePassword() {
		return cassandraTruststorePassword;
	}

	public void setCassandraTruststorePassword(String cassandraTruststorePassword) {
		this.cassandraTruststorePassword = cassandraTruststorePassword;
	}

}
