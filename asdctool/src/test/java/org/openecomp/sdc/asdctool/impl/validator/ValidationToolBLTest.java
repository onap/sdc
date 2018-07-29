package org.openecomp.sdc.asdctool.impl.validator;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceValidatorExecuter;

import java.util.LinkedList;

public class ValidationToolBLTest {

	private ValidationToolBL createTestSubject() {
		return new ValidationToolBL();
	}

	@Test(expected=NullPointerException.class)
	public void testValidateAll() throws Exception {
		ValidationToolBL testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		testSubject.validators = new LinkedList<>();
		testSubject.validators.add(new ServiceValidatorExecuter());
		result = testSubject.validateAll();
	}
}