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

package org.openecomp.sdc.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.test.config.TestConfiguration;
import org.openecomp.sdc.common.test.config.TestNotExistConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExternalConfiguration {

	private static Logger log = LoggerFactory.getLogger(TestExternalConfiguration.class.getName());
	private static final String NEW_LINE = System.getProperty("line.separator");
	ConfigurationSource configurationSource = null;

	@Before
	public void setup() {

		ExternalConfiguration.setAppName("common");
		ExternalConfiguration.setConfigDir("src/test/resources/config");
		ExternalConfiguration.listenForChanges();

		configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName());

	}

	@Test
	public void testReadConfigurationFile() {

		ConfigurationListener configurationListener = new ConfigurationListener(TestConfiguration.class,
				new FileChangeCallback() {

					public void reconfigure(BasicConfiguration obj) {
						// TODO Auto-generated method stub
						log.debug("In reconfigure of {}", obj);
					}

				});

		TestConfiguration testConfiguration = configurationSource.getAndWatchConfiguration(TestConfiguration.class,
				configurationListener);

		assertTrue(testConfiguration != null);
		log.debug("{}", testConfiguration);
		assertEquals(testConfiguration.getBeHost(), "172.20.37.245");
		assertEquals(testConfiguration.getBeProtocol(), "http");
		assertEquals(testConfiguration.getBeContext(), "/sdc/rest/config/get");

	}

	@Test
	public void testNotExistConfigurationFile() {

		ConfigurationListener configurationListener = new ConfigurationListener(TestConfiguration.class,
				new FileChangeCallback() {

					public void reconfigure(BasicConfiguration obj) {
						// TODO Auto-generated method stub
						log.debug("In reconfigure of {}", obj);
					}

				});

		TestNotExistConfiguration testConfiguration = configurationSource
				.getAndWatchConfiguration(TestNotExistConfiguration.class, configurationListener);

		assertTrue(testConfiguration == null);

	}

	@Test
	public void testUpdateConfigurationFile() {

		ConfigurationListener configurationListener = new ConfigurationListener(TestConfiguration.class,
				new FileChangeCallback() {

					public void reconfigure(BasicConfiguration obj) {
						// TODO Auto-generated method stub
						log.debug("In reconfigure of {}", obj);
						// assertEquals(((TestConfiguration)obj).getBeSslPort(),
						// 8444);

						// assertTrue(((TestConfiguration)obj).getBeSslPort() ==
						// 8444);
					}

				});

		TestConfiguration testConfiguration = configurationSource.getAndWatchConfiguration(TestConfiguration.class,
				configurationListener);

		assertTrue(testConfiguration != null);
		log.debug("{}", testConfiguration);
		assertEquals(testConfiguration.getBeHost(), "172.20.37.245");
		assertEquals(testConfiguration.getBeProtocol(), "http");
		assertEquals(testConfiguration.getBeContext(), "/sdc/rest/config/get");

		// updateFileContent();

	}

	private void updateFileContent() {
		File file = new File(ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName()
				+ File.separator + "test-configuration.yaml");
		replaceFile(file);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void replaceFile(File f1) {
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter out = null;
		try {
			List<String> lines = new ArrayList<String>();
			String line = null;
			fr = new FileReader(f1);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				if (line.contains("beSslPort: 8443"))
					line = line.replace("8443", "8444");
				lines.add(line);
			}

			fw = new FileWriter(f1);
			out = new BufferedWriter(fw);
			for (String s : lines)
				out.write(s + NEW_LINE);
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testReadDistributionEngineConfigurationFile() {

		ConfigurationListener configurationListener = new ConfigurationListener(TestConfiguration.class,
				new FileChangeCallback() {

					public void reconfigure(BasicConfiguration obj) {
						// TODO Auto-generated method stub
						log.debug("In reconfigure of ", obj);
					}

				});

		DistributionEngineConfiguration deConfiguration = configurationSource
				.getAndWatchConfiguration(DistributionEngineConfiguration.class, configurationListener);

		assertTrue(deConfiguration != null);
		log.debug("{}", deConfiguration);
		assertEquals(deConfiguration.getDistributionNotifTopicName(), "ASDC-DISTR-NOTIF-TOPIC");
		assertEquals(deConfiguration.getDistributionStatusTopicName(), "ASDC-DISTR-STATUS-TOPIC");

		assertEquals(deConfiguration.getDistributionStatusTopic().getConsumerGroup(), "asdc");
		assertEquals(deConfiguration.getDistributionStatusTopic().getConsumerGroup(), "asdc");
		assertEquals(deConfiguration.getDistributionStatusTopic().getFetchTimeSec().intValue(), 15);
		assertEquals(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec().intValue(), 60);

		assertEquals(deConfiguration.getEnvironments().size(), 1);
		assertEquals(deConfiguration.getEnvironments().iterator().next(), "PROD");

		assertEquals(deConfiguration.getDistribNotifResourceArtifactTypes().getInfo(), null);
		assertEquals(deConfiguration.getDistribNotifResourceArtifactTypes().getLifecycle().size(), 2);
		assertTrue(deConfiguration.getDistribNotifResourceArtifactTypes().getLifecycle().contains("HEAT"));
		assertTrue(deConfiguration.getDistribNotifResourceArtifactTypes().getLifecycle().contains("DG_XML"));

		assertEquals(deConfiguration.getDistribNotifServiceArtifactTypes().getLifecycle(), null);
		assertEquals(deConfiguration.getDistribNotifServiceArtifactTypes().getInfo().size(), 1);
		assertTrue(deConfiguration.getDistribNotifServiceArtifactTypes().getInfo().contains("MURANO-PKG"));

		assertEquals(deConfiguration.getUebPublicKey(), "fff");
		assertEquals(deConfiguration.getUebSecretKey(), "ffff");
		assertEquals(deConfiguration.getUebServers().size(), 3);
		assertEquals(deConfiguration.getInitRetryIntervalSec().intValue(), 5);

	}

}
