package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;


public class UiResourceDataTransferTest {

	private UiResourceDataTransfer createTestSubject() {
		return new UiResourceDataTransfer();
	}

	
	@Test
	public void testGetAdditionalInformation() throws Exception {
		UiResourceDataTransfer testSubject;
		List<AdditionalInformationDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAdditionalInformation();
	}

	
	@Test
	public void testSetAdditionalInformation() throws Exception {
		UiResourceDataTransfer testSubject;
		List<AdditionalInformationDefinition> additionalInformation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAdditionalInformation(additionalInformation);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		UiResourceDataTransfer testSubject;
		UiResourceMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		UiResourceDataTransfer testSubject;
		UiResourceMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testGetDerivedList() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedList();
	}

	
	@Test
	public void testSetDerivedList() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> derivedList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedList(derivedList);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		UiResourceDataTransfer testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		UiResourceDataTransfer testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetAttributes() throws Exception {
		UiResourceDataTransfer testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAttributes();
	}

	
	@Test
	public void testSetAttributes() throws Exception {
		UiResourceDataTransfer testSubject;
		List<PropertyDefinition> attributes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAttributes(attributes);
	}

	
	@Test
	public void testGetInterfaces() throws Exception {
		UiResourceDataTransfer testSubject;
		Map<String, InterfaceDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInterfaces();
	}

	
	@Test
	public void testSetInterfaces() throws Exception {
		UiResourceDataTransfer testSubject;
		Map<String, InterfaceDefinition> interfaces = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaces(interfaces);
	}

	
	@Test
	public void testGetDefaultCapabilities() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultCapabilities();
	}

	
	@Test
	public void testSetDefaultCapabilities() throws Exception {
		UiResourceDataTransfer testSubject;
		List<String> defaultCapabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultCapabilities(defaultCapabilities);
	}
}