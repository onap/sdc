package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;

import java.util.List;


public class ElementTypeEnumTest {

	private ElementTypeEnum createTestSubject() {
		return  ElementTypeEnum.VF;
		}

	
	@Test
	public void testGetByType() {
		String elementType = "";
		ElementTypeEnum result;

		// default test
		result = ElementTypeEnum.getByType(elementType);
		result = ElementTypeEnum.getByType(ElementTypeEnum.VF.getElementType());
	}

	
	@Test
	public void testGetAllTypes() {
		List<String> result;

		// default test
		result = ElementTypeEnum.getAllTypes();
	}

	
	@Test
	public void testGetElementType() {
		ElementTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getElementType();
	}

	
	@Test
	public void testSetElementType() {
		ElementTypeEnum testSubject;
		String elementType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setElementType(elementType);
	}

	
	@Test
	public void testGetClazz() {
		ElementTypeEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	
	@Test
	public void testSetClazz() {
		ElementTypeEnum testSubject;
		Class clazz = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setClazz(clazz);
	}
}