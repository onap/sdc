package org.openecomp.sdc.be.ecomp.converters;

import org.junit.Test;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.model.User;

import fj.data.Either;

public class EcompUserConverterTest {

	@Test
	public void testConvertUserToEcompUser() throws Exception {
		User asdcUser = new User();
		Either<EcompUser, String> result;

		// test 1
		result = EcompUserConverter.convertUserToEcompUser(asdcUser);
	}

	@Test
	public void testConvertEcompUserToUser() throws Exception {
		EcompUser ecompUser = new EcompUser();
		Either<User, String> result;

		// test 1
		result = EcompUserConverter.convertEcompUserToUser(ecompUser);
	}
}