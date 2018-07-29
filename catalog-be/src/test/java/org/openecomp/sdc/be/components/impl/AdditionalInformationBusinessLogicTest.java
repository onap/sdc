package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.exception.ResponseFormat;


public class AdditionalInformationBusinessLogicTest {

	private AdditionalInformationBusinessLogic createTestSubject() {
		return new AdditionalInformationBusinessLogic();
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
}