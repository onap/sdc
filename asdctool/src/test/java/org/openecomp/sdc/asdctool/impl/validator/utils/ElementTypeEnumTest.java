package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ElementTypeEnumTest {

	private ElementTypeEnum createTestSubject() {
		return  ElementTypeEnum.VF;
		}

	
	@Test
	public void testGetByType() throws Exception {
		String elementType = "";
		ElementTypeEnum result;

		// default test
		result = ElementTypeEnum.getByType(elementType);
	}

	
	@Test
	public void testGetAllTypes() throws Exception {
		List<String> result;

		// default test
		result = ElementTypeEnum.getAllTypes();
	}

	
	@Test
	public void testGetElementType() throws Exception {
		ElementTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getElementType();
	}

	
	@Test
	public void testSetElementType() throws Exception {
		ElementTypeEnum testSubject;
		String elementType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setElementType(elementType);
	}

	
	@Test
	public void testGetClazz() throws Exception {
		ElementTypeEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	
	@Test
	public void testSetClazz() throws Exception {
		ElementTypeEnum testSubject;
		Class clazz = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setClazz(clazz);
	}
}