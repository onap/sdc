package org.openecomp.sdc.be.tosca.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class ToscaTemplateTest {

	private ToscaTemplate createTestSubject() {
		return new ToscaTemplate("");
	}

	
	@Test
	public void testGetNode_types() throws Exception {
		ToscaTemplate testSubject;
		Map<String, ToscaNodeType> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode_types();
	}

	
	@Test
	public void testSetNode_types() throws Exception {
		ToscaTemplate testSubject;
		Map<String, ToscaNodeType> node_types = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNode_types(node_types);
	}

	
	@Test
	public void testGetImports() throws Exception {
		ToscaTemplate testSubject;
		List<Map<String, Map<String, String>>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImports();
	}

	
	@Test
	public void testSetImports() throws Exception {
		ToscaTemplate testSubject;
		List<Map<String, Map<String, String>>> imports = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImports(imports);
	}

	
	@Test
	public void testGetTosca_definitions_version() throws Exception {
		ToscaTemplate testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTosca_definitions_version();
	}

	
	@Test
	public void testSetTosca_definitions_version() throws Exception {
		ToscaTemplate testSubject;
		String tosca_definitions_version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTosca_definitions_version(tosca_definitions_version);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		ToscaTemplate testSubject;
		ToscaMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		ToscaTemplate testSubject;
		ToscaMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetTopology_template() throws Exception {
		ToscaTemplate testSubject;
		ToscaTopolgyTemplate result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTopology_template();
	}

	
	@Test
	public void testSetTopology_template() throws Exception {
		ToscaTemplate testSubject;
		ToscaTopolgyTemplate topology_template = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTopology_template(topology_template);
	}


	@Test
	public void testGetSetData_types() throws Exception {
		ToscaTemplate testSubject = createTestSubject();
		Map<String, ToscaDataType> dataTypes = new HashMap<>();
		dataTypes.put("datatype", new ToscaDataType());
		testSubject.setData_types(dataTypes);
		assertEquals(dataTypes, testSubject.getData_types());
	}


	@Test
	public void testGetSetInterface_types() throws Exception {
		ToscaTemplate testSubject = createTestSubject();
		Map<String, Object> interfaceTypes = new HashMap<>();
		interfaceTypes.put("id", new Object());
		testSubject.setInterface_types(interfaceTypes);
		assertEquals(interfaceTypes, testSubject.getInterface_types());
	}

}