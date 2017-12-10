package org.openecomp.sdc.be.components.impl;

import java.util.List;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;


public class AdditionalInformationBusinessLogicTest {

	private AdditionalInformationBusinessLogic createTestSubject() {
		return new AdditionalInformationBusinessLogic();
	}

	
	@Test
	public void testGetElementDao() throws Exception {
	Class<IElementOperation> class1 = null;
	ServletContext context = null;
	IElementOperation result;
	
	// default test
	}

	
	@Test
	public void testCreateAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateAndConvertValue() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String context = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateAndConvertKey() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String context = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateMaxSizeNotReached() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String componentId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateValue() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		String value = "";
		Either<String, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testFindAdditionInformationKey() throws Exception {
	AdditionalInformationBusinessLogic testSubject;List<AdditionalInfoParameterInfo> parameters = null;
	String key = "";
	AdditionalInfoParameterInfo result;
	
	// default test
	}

	
	@Test
	public void testValidateAndNormalizeKey() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		String key = "";
		Either<String, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAllAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInformationDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testVerifyCanWorkOnComponent() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		String userId = "";
		ResponseFormat result;

		// default test
	}
}