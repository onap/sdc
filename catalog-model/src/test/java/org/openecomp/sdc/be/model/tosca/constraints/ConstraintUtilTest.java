package org.openecomp.sdc.be.model.tosca.constraints;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil.ConstraintInformation;

public class ConstraintUtilTest {

	@Test
	public void testCheckStringType() throws Exception {
		ToscaType propertyType = ToscaType.STRING;

		// default test
		ConstraintUtil.checkStringType(propertyType);
	}

	
	@Test
	public void testCheckComparableType() throws Exception {
		ToscaType propertyType = ToscaType.INTEGER;

		// default test
		ConstraintUtil.checkComparableType(propertyType);
	}

	
	@Test
	public void testConvertToComparable() throws Exception {
		ToscaType propertyType = ToscaType.BOOLEAN;
		String value = "";
		Comparable result;

		// default test
		result = ConstraintUtil.convertToComparable(propertyType, value);
	}

	

}