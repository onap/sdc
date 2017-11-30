package org.openecomp.sdc.be.components.impl;

import org.junit.Test;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;


public class ConsumerBusinessLogicTest {

	private ConsumerBusinessLogic createTestSubject() {
		return new ConsumerBusinessLogic();
	}

	
	@Test
	public void testCreateConsumer() throws Exception {
		ConsumerBusinessLogic testSubject;
		User user = null;
		ConsumerDefinition consumer = null;
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	


	
	@Test
	public void testGetConsumer() throws Exception {
		ConsumerBusinessLogic testSubject;
		String consumerId = "";
		User user = null;
		Either<ConsumerDefinition, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		user = null;
	}

	
	@Test
	public void testGetConsumer_1() throws Exception {
		ConsumerBusinessLogic testSubject;
		String consumerId = "";
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteConsumer() throws Exception {
		ConsumerBusinessLogic testSubject;
		String consumerId = "";
		User user = null;
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateConsumer() throws Exception {
		ConsumerBusinessLogic testSubject;
		ConsumerDefinition consumer = null;
		User modifier = null;
		boolean isCreateRequest = false;
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	

}