package org.openecomp.sdc.be.model.tosca.converters;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import com.google.gson.JsonElement;

public class ToscaMapValueConverterTest {

	private ToscaMapValueConverter createTestSubject() {
		return ToscaMapValueConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaMapValueConverter result;

		// default test
		result = ToscaMapValueConverter.getInstance();
	}


}