package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;


public class ComponentInstanceDataDefinitionTest {

	private ComponentInstanceDataDefinition createTestSubject() {
		return new ComponentInstanceDataDefinition();
	}

	
	@Test
	public void testGetIcon() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIcon();
	}

	
	@Test
	public void testSetIcon() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String icon = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setIcon(icon);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetPosX() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPosX();
	}

	
	@Test
	public void testSetPosX() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String posX = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPosX(posX);
	}

	
	@Test
	public void testGetPosY() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPosY();
	}

	
	@Test
	public void testSetPosY() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String posY = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPosY(posY);
	}

	
	@Test
	public void testGetComponentUid() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentUid();
	}

	
	@Test
	public void testSetComponentUid() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String resourceUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentUid(resourceUid);
	}

	
	@Test
	public void testGetName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetInvariantName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantName();
	}

	
	@Test
	public void testSetInvariantName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String invariantName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantName(invariantName);
	}

	
	@Test
	public void testGetPropertyValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyValueCounter();
	}

	
	@Test
	public void testSetPropertyValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer propertyValueCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyValueCounter(propertyValueCounter);
	}

	
	@Test
	public void testGetNormalizedName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}

	
	@Test
	public void testSetNormalizedName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String normalizedName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNormalizedName(normalizedName);
	}

	
	@Test
	public void testGetOriginType() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		OriginTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOriginType();
	}

	
	@Test
	public void testSetOriginType() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		OriginTypeEnum originType = null;

		// test 1
		testSubject = createTestSubject();
		originType = null;
		testSubject.setOriginType(originType);
	}

	
	@Test
	public void testGetAttributeValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAttributeValueCounter();
	}

	
	@Test
	public void testSetAttributeValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer attributeValueCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setAttributeValueCounter(attributeValueCounter);
	}

	
	@Test
	public void testGetInputValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputValueCounter();
	}

	
	@Test
	public void testSetInputValueCounter() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		Integer inputValueCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputValueCounter(inputValueCounter);
	}

	
	@Test
	public void testGetCustomizationUUID() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCustomizationUUID();
	}

	
	@Test
	public void testSetCustomizationUUID() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String customizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCustomizationUUID(customizationUUID);
	}

	
	@Test
	public void testGetComponentName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentName();
	}

	
	@Test
	public void testSetComponentName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentName(resourceName);
	}

	
	@Test
	public void testGetComponentVersion() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentVersion();
	}

	
	@Test
	public void testGetToscaComponentName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaComponentName();
	}

	
	@Test
	public void testSetToscaComponentName() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String toscaComponentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaComponentName(toscaComponentName);
	}

	
	@Test
	public void testSetComponentVersion() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentVersion(resourceVersion);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentInstanceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}