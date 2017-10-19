package org.openecomp.sdc.common.api;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class YamlSuffixEnumTest {

	private YamlSuffixEnum createTestSubject() {
		return YamlSuffixEnum.YAML;
	}

	
	@Test
	public void testGetSuffix() throws Exception {
		YamlSuffixEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSuffix();
	}

	
	@Test
	public void testSetSuufix() throws Exception {
		YamlSuffixEnum testSubject;
		String suffix = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSuufix(suffix);
	}

	
	@Test
	public void testGetSuffixes() throws Exception {
		List<String> result;

		// default test
		result = YamlSuffixEnum.getSuffixes();
	}
}