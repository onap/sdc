package org.openecomp.sdc.be.ecomp.converters;

import org.junit.Test;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.sdc.be.user.Role;

public class EcompRoleConverterTest {

	@Test
	public void testConvertEcompRoleToRole() throws Exception {
		EcompRole ecompRole = new EcompRole();
		String result;

		// test 1
		for (Role iterable_element : Role.values()) {
			ecompRole.setId(new Long(iterable_element.ordinal()));
			EcompRoleConverter.convertEcompRoleToRole(ecompRole);
		}
		
		EcompRoleConverter.convertEcompRoleToRole(null);
		
		ecompRole.setId(new Long(4523535));
		EcompRoleConverter.convertEcompRoleToRole(ecompRole);
	}
}