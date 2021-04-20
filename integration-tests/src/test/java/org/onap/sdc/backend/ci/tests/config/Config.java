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

package org.onap.sdc.backend.ci.tests.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

@Getter
@Setter
public class Config {

    private static final String SDC_DEFAULT_CONFIG_FILE = "src/test/resources/ci/conf/sdc-conf.yaml";
    private static Config configIt = null;
    private String configurationFile;
    private String downloadAutomationFolder;
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
    private String importResourceConfigDir;
    private String importResourceTestsConfigDir;
    private String catalogFeHost;
    private String catalogFePort;
    private String catalogBePort;
    private String catalogBeTlsPort;
    private String onboardingBeHost;
    private String onboardingBePort;
    private String neoDBusername;
    private String neoDBpassword;
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
    private boolean captureTraffic;
    private boolean useBrowserMobProxy;
    private String sdcHttpMethod;
    private String localDataCenter;
    private boolean uiSimulator;

    private Config() {
        super();
    }

    public synchronized static Config instance() {
        if (configIt == null) {
            try {
                configIt = init();
            } catch (final IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return configIt;
    }

    private static Config init() throws IOException {

        String configFile = System.getProperty("config.resource");
        if (configFile == null) {
            configFile = SDC_DEFAULT_CONFIG_FILE;
        }

        if (!((new File(configFile)).exists())) {
            throw new RuntimeException("The config file " + configFile + " cannot be found.");
        }

        final Config config;

        try (final InputStream in = Files.newInputStream(Paths.get(configFile));) {
            config = new Yaml().loadAs(in, Config.class);
            setPackagesAndBugs(configFile, config);
        }

        return config;
    }

    private static void setPackagesAndBugs(final String path, final Config config) throws IOException {

        final int separator = Math.max(path.lastIndexOf("\\"), path.lastIndexOf("/"));
        final String dirPath = path.substring(0, separator + 1);
        final String packagesFile = dirPath + File.separator + "sdc-packages.yaml";

        if (!((new File(packagesFile)).exists())) {
            throw new RuntimeException("The config file " + packagesFile + " cannot be found.");
        }

        try (final InputStream in = Files.newInputStream(Paths.get(packagesFile));) {
            final TestPackages testPackages = new Yaml().loadAs(in, TestPackages.class);
            config.setBugs(testPackages.getBugs());
            config.setPackages(testPackages.getPackages());
        }

    }

    public boolean getIsDistributionClientRunning() {
        return isDistributionClientRunning;
    }

    public void setIsDistributionClientRunning(final boolean isDistributionClientRunning) {
        this.isDistributionClientRunning = isDistributionClientRunning;
    }

    @Override
    public String toString() {
        return "Config [systemUnderDebug=" + systemUnderDebug + ", rerun=" + rerun + ", reportDBhost=" + reportDBhost
            + ", reportDBport=" + reportDBport + ", browser=" + browser + ", catalogBeHost=" + catalogBeHost
            + ", esHost=" + esHost + ", esPort=" + esPort + ", neoHost=" + neoHost + ", neoPort=" + neoPort
            + ", disributionClientHost=" + disributionClientHost + ", disributionClientPort="
            + disributionClientPort + ", isDistributionClientRunning=" + isDistributionClientRunning
            + ", errorConfigurationFile=" + errorConfigurationFile + ", resourceConfigDir=" + resourceConfigDir +
            ", importResourceConfigDir=" + importResourceConfigDir + ", importResourceTestsConfigDir="
            + importResourceTestsConfigDir + ", catalogFeHost="
            + catalogFeHost + ", catalogFePort=" + catalogFePort + ", catalogBePort=" + catalogBePort
            + ", catalogBeTlsPort=" + catalogBeTlsPort + ", neoDBusername=" + neoDBusername + ", neoDBpassword="
            + neoDBpassword + ", packages=" + packages + ", bugs="
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
            + ", captureTraffic=" + captureTraffic
            + ", useBrowserMobProxy=" + useBrowserMobProxy + ", configurationFile=" + configurationFile
            + ", downloadAutomationFolder=" + downloadAutomationFolder + "]";
    }

    @Getter
    @Setter
    @ToString
    public static class TestPackages {

        private List<String> packages;
        private List<String> bugs;

    }

}
