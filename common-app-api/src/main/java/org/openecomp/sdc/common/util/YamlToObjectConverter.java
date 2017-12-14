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

package org.openecomp.sdc.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.config.Configuration.ApplicationL1CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ApplicationL2CacheConfig;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.Configuration.BeMonitoringConfig;
import org.openecomp.sdc.be.config.Configuration.EcompPortalConfig;
import org.openecomp.sdc.be.config.Configuration.ElasticSearchConfig;
import org.openecomp.sdc.be.config.Configuration.ElasticSearchConfig.IndicesTimeFrequencyEntry;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.config.Configuration.SwitchoverDetectorConfig;
import org.openecomp.sdc.be.config.Configuration.ToscaValidatorsConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration;
import org.openecomp.sdc.fe.config.Configuration.FeMonitoringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;

public class YamlToObjectConverter {

	private static Logger log = LoggerFactory.getLogger(YamlToObjectConverter.class.getName());

	private static HashMap<String, Yaml> yamls = new HashMap<String, Yaml>();

	private static Yaml defaultYaml = new Yaml();

	static {

		org.yaml.snakeyaml.constructor.Constructor deConstructor = new org.yaml.snakeyaml.constructor.Constructor(
				DistributionEngineConfiguration.class);
		TypeDescription deDescription = new TypeDescription(DistributionEngineConfiguration.class);
		deDescription.putListPropertyType("distributionStatusTopic", DistributionStatusTopicConfig.class);
		deDescription.putListPropertyType("distribNotifServiceArtifactTypes", ComponentArtifactTypesConfig.class);
		deDescription.putListPropertyType("distribNotifResourceArtifactTypes", ComponentArtifactTypesConfig.class);
		deDescription.putListPropertyType("createTopic", CreateTopicConfig.class);
		deDescription.putListPropertyType("distributionNotificationTopic", DistributionNotificationTopicConfig.class);
		deConstructor.addTypeDescription(deDescription);
		Yaml yaml = new Yaml(deConstructor);
		yamls.put(DistributionEngineConfiguration.class.getName(), yaml);

		// FE conf
		org.yaml.snakeyaml.constructor.Constructor feConfConstructor = new org.yaml.snakeyaml.constructor.Constructor(
				org.openecomp.sdc.fe.config.Configuration.class);
		TypeDescription feConfDescription = new TypeDescription(org.openecomp.sdc.fe.config.Configuration.class);
		feConfDescription.putListPropertyType("systemMonitoring", FeMonitoringConfig.class);
		feConfConstructor.addTypeDescription(feConfDescription);
		yamls.put(org.openecomp.sdc.fe.config.Configuration.class.getName(), new Yaml(feConfConstructor));

		// BE conf
		org.yaml.snakeyaml.constructor.Constructor beConfConstructor = new org.yaml.snakeyaml.constructor.Constructor(
				org.openecomp.sdc.be.config.Configuration.class);
		TypeDescription beConfDescription = new TypeDescription(org.openecomp.sdc.be.config.Configuration.class);
		beConfConstructor.addTypeDescription(beConfDescription);

		// systemMonitoring
		beConfDescription.putListPropertyType("systemMonitoring", BeMonitoringConfig.class);

		// elasticSearch
		beConfDescription.putListPropertyType("elasticSearch", ElasticSearchConfig.class);
		TypeDescription esDescription = new TypeDescription(ElasticSearchConfig.class);
		esDescription.putListPropertyType("indicesTimeFrequency", IndicesTimeFrequencyEntry.class);
		beConfConstructor.addTypeDescription(esDescription);

		// resourceDeploymentArtifacts and serviceDeploymentArtifacts
		beConfDescription.putMapPropertyType("resourceDeploymentArtifacts", String.class,
				ArtifactTypeConfig.class);
		beConfDescription.putMapPropertyType("serviceDeploymentArtifacts", String.class,
				ArtifactTypeConfig.class);

		// onboarding
		beConfDescription.putListPropertyType("onboarding", OnboardingConfig.class);

		// ecompPortal
		beConfDescription.putListPropertyType("ecompPortal", EcompPortalConfig.class);
		// switchoverDetector
		beConfDescription.putListPropertyType("switchoverDetector", SwitchoverDetectorConfig.class);

		// ApplicationL1Cache
		beConfDescription.putListPropertyType("applicationL1Cache", ApplicationL1CacheConfig.class);

		// ApplicationL2Cache
		beConfDescription.putListPropertyType("applicationL2Cache", ApplicationL2CacheConfig.class);

		// tosca validators config
		beConfDescription.putListPropertyType("toscaValidators", ToscaValidatorsConfig.class);

		yamls.put(org.openecomp.sdc.be.config.Configuration.class.getName(), new Yaml(beConfConstructor));

		// HEAT deployment artifact
		org.yaml.snakeyaml.constructor.Constructor depArtHeatConstructor = new org.yaml.snakeyaml.constructor.Constructor(
				DeploymentArtifactHeatConfiguration.class);
		PropertyUtils propertyUtils = new PropertyUtils();
		// Skip properties which are found in YAML but not found in POJO
		propertyUtils.setSkipMissingProperties(true);
		depArtHeatConstructor.setPropertyUtils(propertyUtils);
		yamls.put(org.openecomp.sdc.be.config.validation.DeploymentArtifactHeatConfiguration.class.getName(),
				new Yaml(depArtHeatConstructor));

	}

	private static <T> Yaml getYamlByClassName(Class<T> className) {

		Yaml yaml = yamls.get(className.getName());
		if (yaml == null) {
			yaml = defaultYaml;
		}

		return yaml;
	}

	public <T> T convert(String dirPath, Class<T> className, String configFileName) {

		T config = null;

		try {

			String fullFileName = dirPath + File.separator + configFileName;

			config = convert(fullFileName, className);

		} catch (Exception e) {
			log.error("Failed to convert yaml file {} to object.", configFileName,e);
		}

		return config;
	}

	public class MyYamlConstructor extends org.yaml.snakeyaml.constructor.Constructor {
		private HashMap<String, Class<?>> classMap = new HashMap<String, Class<?>>();

		public MyYamlConstructor(Class<? extends Object> theRoot) {
			super(theRoot);
			classMap.put(DistributionEngineConfiguration.class.getName(), DistributionEngineConfiguration.class);
			classMap.put(DistributionStatusTopicConfig.class.getName(), DistributionStatusTopicConfig.class);
		}

		/*
		 * This is a modified version of the Constructor. Rather than using a
		 * class loader to get external classes, they are already predefined
		 * above. This approach works similar to the typeTags structure in the
		 * original constructor, except that class information is pre-populated
		 * during initialization rather than runtime.
		 *
		 * @see
		 * org.yaml.snakeyaml.constructor.Constructor#getClassForNode(org.yaml.
		 * snakeyaml.nodes.Node)
		 */
		protected Class<?> getClassForNode(Node node) {
			String name = node.getTag().getClassName();
			Class<?> cl = classMap.get(name);
			if (cl == null)
				throw new YAMLException("Class not found: " + name);
			else
				return cl;
		}
	}

	public <T> T convert(String fullFileName, Class<T> className) {

		T config = null;

		Yaml yaml = getYamlByClassName(className);

		InputStream in = null;
		try {

			File f = new File(fullFileName);
			if (false == f.exists()) {
				log.warn("The file " + fullFileName + " cannot be found. Ignore reading configuration.");
				return null;
			}
			in = Files.newInputStream(Paths.get(fullFileName));

			config = yaml.loadAs(in, className);

			// System.out.println(config.toString());
		} catch (Exception e) {
			log.error("Failed to convert yaml file {} to object.", fullFileName, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.debug("Failed to close input stream {} ", e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}

		return config;
	}

	public <T> T convert(byte[] fileContents, Class<T> className) {

		T config = null;

		Yaml yaml = getYamlByClassName(className);

		InputStream in = null;
		try {

			in = new ByteArrayInputStream(fileContents);

			config = yaml.loadAs(in, className);

		} catch (Exception e) {
			log.error("Failed to convert yaml file to object", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.debug("Failed to close input stream {} ", e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}

		return config;
	}

	public boolean isValidYamlEncoded64(byte[] fileContents) {
		log.trace("Received Base64 data - decoding before validating...");
		byte[] decodedFileContents = Base64.decodeBase64(fileContents);
		
		return isValidYaml(decodedFileContents);
	}
	
	public boolean isValidYaml(byte[] fileContents) {
		try {
			
			Iterable<Object> mappedToscaTemplateIt =  defaultYaml.loadAll(new ByteArrayInputStream(fileContents));
			
			 for (Object o : mappedToscaTemplateIt) {
	                Map<String, Object> map = (Map<String, Object>) o;
			 }
			
		} catch (Exception e) {
			log.error("Failed to convert yaml file to object - yaml is invalid", e);
			return false;
		}
		return true;
	}
}
