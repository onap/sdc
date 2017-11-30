package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.components.ResourceTestUtils;
import org.openecomp.sdc.be.components.impl.ServiceComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.VFComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.impl.ComponentInstanceOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class ToscaExportUtilsTest {
	private static Logger log = LoggerFactory.getLogger(ToscaExportUtilsTest.class.getName());
	@javax.annotation.Resource
	private VFComponentInstanceBusinessLogic componentInstanceBusinessLogic;
	@javax.annotation.Resource
	private ServiceComponentInstanceBusinessLogic serviceInstanceBusinessLogic;
	@Autowired
	private ToscaExportHandler exportUtils;
	@Autowired
	private ComponentInstanceOperation componentInstanceOperation;

	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@Test
	public void testExportService() {
	/*	Resource resource1 = ResourceTestUtils.prepareResource(0);
		resource1.setResourceType(ResourceTypeEnum.VF);
		Either<Resource, ResponseFormat> createResource1 = resourceBusinessLogic.createResource(resource1, getAdminUser(), null, null);
		assertTrue(createResource1.isLeft());
		Resource certifiedVFC1 = changeResourceStateToCertify(createResource1.left().value());

		Resource resource2 = ResourceTestUtils.prepareResource(1);
		resource2.setResourceType(ResourceTypeEnum.VF);
		Either<Resource, ResponseFormat> createResource2 = resourceBusinessLogic.createResource(resource2, getAdminUser(), null, null);
		assertTrue(createResource2.isLeft());
		Resource certifiedVFC2 = changeResourceStateToCertify(createResource2.left().value());

		Service service = ResourceTestUtils.prepareService(0);
		Either<Service, ResponseFormat> createService = serviceBusinessLogic.createService(service, getAdminUser());
		assertTrue(createService.isLeft());

		// add VFC instance to VF
		ComponentInstance vfcResourceInstance1 = new ComponentInstance();
		vfcResourceInstance1.setDescription("VFC instance 1");
		vfcResourceInstance1.setName(certifiedVFC1.getName());
		vfcResourceInstance1.setComponentUid(certifiedVFC1.getUniqueId());

		Either<ComponentInstance, ResponseFormat> createResourceVfcInstance1 = serviceInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, createService.left().value().getUniqueId(), adminUser.getAttuid(),
				vfcResourceInstance1);
		assertTrue(createResourceVfcInstance1.isLeft());

		ComponentInstance vfcResourceInstance2 = new ComponentInstance();
		vfcResourceInstance2.setDescription("VFC instance 2");
		vfcResourceInstance2.setName(certifiedVFC2.getName());
		vfcResourceInstance2.setComponentUid(certifiedVFC2.getUniqueId());
		Either<ComponentInstance, ResponseFormat> createResourceVfcInstance2 = serviceInstanceBusinessLogic.createComponentInstance(ComponentTypeEnum.SERVICE_PARAM_NAME, createService.left().value().getUniqueId(), adminUser.getAttuid(),
				vfcResourceInstance2);
		assertTrue(createResourceVfcInstance2.isLeft());

		Either<Service, ResponseFormat> serviceFetch = serviceBusinessLogic.getService(createService.left().value().getUniqueId(), adminUser);
		assertTrue(serviceFetch.isLeft());

		List<ComponentInstance> componentInstances = serviceFetch.left().value().getComponentInstances();
		String ciname1 = null;
		String ciname2 = null;

		for (ComponentInstance ci : componentInstances) {
			if (ci.getComponentUid().equals(certifiedVFC1.getUniqueId())) {
				ciname1 = ci.getName();
			}
			if (ci.getComponentUid().equals(certifiedVFC2.getUniqueId())) {
				ciname2 = ci.getName();
			}
		}

		Either<ToscaRepresentation, ToscaError> result = exportUtils.exportComponent(serviceFetch.left().value());
		assertTrue(result.isLeft());

		String mainYaml = result.left().value().getMainYaml();
		assertNotNull(mainYaml);

		YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
		assertTrue(yamlToObjectConverter.isValidYaml(mainYaml.getBytes()));
		log.debug(mainYaml);

		Yaml yaml = new Yaml();

		InputStream inputStream = new ByteArrayInputStream(mainYaml.getBytes());
		Map<String, Object> load = (Map<String, Object>) yaml.load(inputStream);
		Map<String, Object> imports = (Map<String, Object>) load.get("imports");
		assertNotNull(imports);
		assertEquals("Validate imports size in yml", 2, imports.size());

		Map<String, Object> metadata = (Map<String, Object>) load.get("metadata");
		assertNotNull(metadata);
		validateMetadata(metadata, serviceFetch.left().value(), false);

		Map<String, Object> vf1 = (Map<String, Object>) imports.get(certifiedVFC1.getName());
		String fileName = (String) vf1.get(ToscaExportHandler.IMPORTS_FILE_KEY);
		ArtifactDefinition artifactDefinition = certifiedVFC1.getToscaArtifacts().get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		assertEquals("Validate 1 file name", artifactDefinition.getArtifactName(), fileName);

		Map<String, Object> topology_template = (Map<String, Object>) load.get("topology_template");
		Map<String, Object> node_templates = (Map<String, Object>) topology_template.get("node_templates");
		Map<String, Object> inst1 = (Map<String, Object>) node_templates.get(ciname1);
		Map<String, Object> inst2 = (Map<String, Object>) node_templates.get(ciname2);

		Map<String, Object> inst1MD = (Map<String, Object>) inst1.get("metadata");
		Map<String, Object> inst2MD = (Map<String, Object>) inst2.get("metadata");

		validateMetadata(inst1MD, certifiedVFC1, true);

		Map<String, Object> vf2 = (Map<String, Object>) imports.get(certifiedVFC2.getName());
		fileName = (String) vf2.get(ToscaExportHandler.IMPORTS_FILE_KEY);
		artifactDefinition = certifiedVFC2.getToscaArtifacts().get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		assertEquals("Validate 2 file name", artifactDefinition.getArtifactName(), fileName);

		validateMetadata(inst2MD, certifiedVFC2, true);*/
	}

}
