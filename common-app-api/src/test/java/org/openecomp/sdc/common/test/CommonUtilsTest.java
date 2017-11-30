/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.HtmlCleaner;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class CommonUtilsTest {
	private static Logger log = LoggerFactory.getLogger(CommonUtilsTest.class.getName());

	/*
	 * Validation utils start
	 */
	@Test
	public void testValidateServiceName() {

		assertTrue(ValidationUtils.validateComponentNamePattern("1111222"));
		assertTrue(ValidationUtils.validateComponentNamePattern("sfE4444"));
		assertTrue(ValidationUtils.validateComponentNamePattern("1111sfd222"));
		assertTrue(ValidationUtils.validateComponentNamePattern("11sdf 1124_22"));
		assertTrue(ValidationUtils.validateComponentNamePattern("111----1222"));
		assertTrue(ValidationUtils.validateComponentNamePattern("1111f .222"));
		assertTrue(ValidationUtils.validateComponentNamePattern("1111222"));
		assertFalse(ValidationUtils.validateComponentNamePattern("11!11222"));
		assertFalse(ValidationUtils.validateComponentNamePattern("111|`1222"));
		assertFalse(ValidationUtils.validateComponentNamePattern("."));
		assertFalse(ValidationUtils.validateComponentNamePattern(""));
		assertTrue(ValidationUtils.validateComponentNamePattern("s"));
	}

	@Test
	public void validateServiceNameLengthTest() {
		assertTrue(ValidationUtils.validateComponentNameLength("fsdlfsdlksdsd;"));
		// assertFalse(ValidationUtils.validateComponentNameLength("ddddddddddddddddddddddsdfsddddddddddddddddddddddsdfs"));
	}

	@Test
	public void testValidateIcon() {

		assertTrue(ValidationUtils.validateIcon("something"));
		assertTrue(ValidationUtils.validateIcon("sfE4444"));
		assertTrue(ValidationUtils.validateIcon("1111sfd222"));
		assertTrue(ValidationUtils.validateIcon("11sdf1124_22"));
		assertTrue(ValidationUtils.validateIcon("111----1222"));
		assertFalse(ValidationUtils.validateIcon("1111f.222"));
		assertTrue(ValidationUtils.validateIcon("1111222"));
		assertFalse(ValidationUtils.validateIcon("1111 222"));
		assertFalse(ValidationUtils.validateIcon("11!11222"));
		assertFalse(ValidationUtils.validateIcon("111|`1222"));

	}

	@Test
	public void testFloatParsing() {
		assertTrue(ValidationUtils.isFloatNumber("15555.45"));
		assertTrue(ValidationUtils.isFloatNumber("0.5"));
		assertFalse(ValidationUtils.isFloatNumber("15555"));
		assertFalse(ValidationUtils.isFloatNumber("1"));
		assertFalse(ValidationUtils.isFloatNumber("jk532"));
		assertFalse(ValidationUtils.isFloatNumber("12..6"));

	}

	@Test
	public void testValidateIconLength() {
		assertTrue(ValidationUtils.validateIconLength("fsdlfsdlksdsd"));
		assertFalse(ValidationUtils.validateIconLength("ddddddddddddddddddddddsdfsddddddddddddddddddddddsdfs"));
	}

	@Test
	public void testValidateProjectCode() {

		assertTrue(ValidationUtils.validateProjectCode("15555"));
		assertTrue(ValidationUtils.validateProjectCode("12434501"));
		assertTrue(ValidationUtils.validateProjectCode("00000"));
		assertTrue(ValidationUtils.validateProjectCode("something"));
		assertTrue(ValidationUtils.validateProjectCode("som ething"));
		assertTrue(ValidationUtils.validateProjectCode("3255 656"));
		assertTrue(ValidationUtils.validateProjectCode("43535t636"));
		assertFalse(ValidationUtils.validateProjectCode("098&656"));
	}

	@Test
	public void testValidateProjectCodeLength() {

		assertTrue(ValidationUtils.validateProjectCodeLegth("00000"));
		assertFalse(ValidationUtils.validateProjectCodeLegth("ddddddddddddddddddddddsdfsddddddddddddddddddddddsdfs"));

	}

	@Test
	public void testValidateContactId() {

		assertTrue(ValidationUtils.validateContactId("ml7889"));
		assertTrue(ValidationUtils.validateContactId("Ml7889"));
		assertTrue(ValidationUtils.validateContactId("ml788r"));
		assertTrue(ValidationUtils.validateContactId("something"));
		assertTrue(ValidationUtils.validateContactId("mlk111"));
		assertTrue(ValidationUtils.validateContactId("12ml89"));
		assertFalse(ValidationUtils.validateContactId("!!78900"));
	}

	@Test
	public void testRemoveHtml() {

		assertTrue("gooboo".equals(ValidationUtils.removeHtmlTags("<b>goo<b></b></b><b>boo</b>")));
		assertTrue("goo&lt;boo".equals(ValidationUtils.removeHtmlTags("<b>goo<b></b><</b><b>boo</b>")));
		assertTrue("goo boo".equals(ValidationUtils.removeHtmlTags("goo boo")));
		assertTrue("goo# . boo12".equals(ValidationUtils.removeHtmlTags("goo# . boo12")));
	}

	@Test
	public void testnormaliseWhitespace() {

		assertTrue("goo boo".equals(ValidationUtils.normaliseWhitespace("goo boo")));
		assertTrue("goo boo ".equals(ValidationUtils.normaliseWhitespace("goo boo	")));
		assertTrue("goo boo".equals(ValidationUtils.normaliseWhitespace("goo    boo")));
	}

	@Test
	public void teststripOctets() {
		assertTrue("goo boo".equals(ValidationUtils.stripOctets("goo%1F boo")));
		assertTrue("goo boo ".equals(ValidationUtils.stripOctets("goo boo %1F")));
		assertTrue("goo boo".equals(ValidationUtils.stripOctets("%1Fgoo boo")));
	}

	@Test
	public void testRemoveNoneUtf8Chars() {
		assertTrue("goo boo".equals(ValidationUtils.removeNoneUtf8Chars("goo boo")));
		assertTrue("goo boo!!._".equals(ValidationUtils.removeNoneUtf8Chars("goo boo!!._")));
		assertTrue("goo 	boo".equals(ValidationUtils.removeNoneUtf8Chars("goo 	boo")));
		assertTrue("goo  bo123o".equals(ValidationUtils.removeNoneUtf8Chars("goo  bo123o")));
		assertTrue("goo  bo123o".equals(ValidationUtils.removeNoneUtf8Chars("goo  קקbo123oגכקק")));
		assertTrue("goo  bo123o".equals(ValidationUtils.removeNoneUtf8Chars("goo  bo1������23o")));
	}

	@Test
	public void validateEnglishTest() {
		assertTrue(ValidationUtils.validateIsEnglish("ml7889"));
		assertFalse(ValidationUtils.validateIsEnglish("ml7889קר"));
		assertFalse(ValidationUtils.validateIsEnglish("ml7889文"));
	}

	@Test
	public void removeDuplicateFromListTest() {
		List<String> tagsBefore = new ArrayList<>();
		tagsBefore.add("tag1");
		tagsBefore.add("tag7");
		tagsBefore.add("tag3");
		tagsBefore.add("tag4");
		tagsBefore.add("tag1");

		List<String> tagsAfter = new ArrayList<>();
		tagsAfter.add("tag1");
		tagsAfter.add("tag7");
		tagsAfter.add("tag3");
		tagsAfter.add("tag4");
		assertTrue(tagsAfter.containsAll(ValidationUtils.removeDuplicateFromList(tagsBefore)));
		tagsBefore = new ArrayList<>();
		tagsBefore.add("tag1");
		tagsBefore.add("tag7");
		tagsBefore.add("tag3");
		tagsBefore.add("tag4");
		tagsBefore.add("Tag1");

		tagsAfter = new ArrayList<>();
		tagsAfter.add("tag1");
		tagsAfter.add("tag7");
		tagsAfter.add("tag3");
		tagsAfter.add("tag4");
		tagsAfter.add("Tag1");
		assertTrue(tagsAfter.containsAll(ValidationUtils.removeDuplicateFromList(tagsBefore)));
	}

	@Test
	public void validateTagLengthTest() {
		assertTrue(ValidationUtils.validateTagLength("fsdlfsdlkfjkljsdf"));
		// assertFalse(ValidationUtils.validateTagLength("ddddddddddddddddddddddsdfsddddddddddddddddddddddsdfs"));

	}

	@Test
	public void validateTagListLengthTest() {
		assertTrue(ValidationUtils.validateTagListLength("fsdlfsdlkfjkljsdf,dsfsdfsdf".length()));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= 1024; i++) {
			sb.append("a");
		}
		assertFalse(ValidationUtils.validateTagListLength(sb.toString().length()));

	}

	@Test
	public void validateDescriptionLengthTest() {
		assertTrue(ValidationUtils.validateDescriptionLength("fsdlfsdlkfjkljsddgfgdfgdfgdfgff"));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= 1024; i++) {
			sb.append("a");
		}
		assertFalse(ValidationUtils.validateDescriptionLength(sb.toString()));

	}

	@Test
	public void validateStringNotEmptyTest() {
		assertTrue(ValidationUtils.validateStringNotEmpty("fsdlfsdlk"));
		assertFalse(ValidationUtils.validateStringNotEmpty(""));
		assertFalse(!ValidationUtils.validateStringNotEmpty("  "));
		assertFalse(!ValidationUtils.validateStringNotEmpty("	"));
	}

	@Test
	public void validateVendorNameTest() {
		assertTrue(ValidationUtils.validateVendorName("fsdlfsdlk"));
		assertTrue(ValidationUtils.validateVendorName("fsdlfsdlk.sdsd;"));
		assertFalse(ValidationUtils.validateVendorName("sadf:"));
		assertFalse(ValidationUtils.validateVendorName("sadf/"));
		assertFalse(ValidationUtils.validateVendorName("sadf?"));
	}

	@Test
	public void validateVendorNameLengthTest() {
		assertTrue(ValidationUtils.validateVendorNameLength("fsdlfsdlk.sdsd;"));
		assertFalse(ValidationUtils.validateVendorNameLength("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddsdfs"));
	}

	@Test
	public void validateVendorReleaseTest() {
		assertTrue(ValidationUtils.validateVendorRelease("fsdlfsdlk"));
		assertTrue(ValidationUtils.validateVendorRelease("fsdlfsdlk.sdsd;"));
		assertFalse(ValidationUtils.validateVendorRelease("sadf:"));
		assertFalse(ValidationUtils.validateVendorRelease("sadf/"));
		assertFalse(ValidationUtils.validateVendorRelease("sadf?"));
	}

	@Test
	public void validateVendorReleaseLengthTest() {
		assertTrue(ValidationUtils.validateVendorReleaseLength("fsdlfsdlk.sdsd;"));
		assertFalse(ValidationUtils.validateVendorReleaseLength("ddddddddddddddddddddddsdfs"));
	}

	@Test
	public void hasBeenCertifiedTest() {
		assertTrue(ValidationUtils.hasBeenCertified("1.2"));
		assertTrue(ValidationUtils.hasBeenCertified("2.2"));
		assertTrue(ValidationUtils.hasBeenCertified("1.0"));
		assertFalse(ValidationUtils.hasBeenCertified("0.1"));

	}

	@Test
	public void normalizedNameTest() {
		String input = "MyNewSysName";
		String outputNorm = ValidationUtils.normaliseComponentName(input);
		String outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "My New Sys Name";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "My.New-Sys_Name";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "My..New-Sys_Name";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "My.New--sys_NAme";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "Layer 3 Connectivity";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "Layer 3 VPN";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "Layer-3      Connectivity";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

		input = "IP-connectivity";
		outputNorm = ValidationUtils.normaliseComponentName(input);
		outputSys = ValidationUtils.convertToSystemName(input);
		log.debug("{} <> {} <> {}", input, outputNorm, outputSys);

	}

	@Test
	public void normalizeFileNameTest() {
		assertTrue("too.jpeg".equals(ValidationUtils.normalizeFileName("too.jpeg")));
		assertTrue("too..jpeg".equals(ValidationUtils.normalizeFileName("too..jpeg")));
		assertTrue("too..jpeg".equals(ValidationUtils.normalizeFileName("t*o:o..jpe<>g")));
		assertTrue("goo.too..jpeg".equals(ValidationUtils.normalizeFileName("goo.t*o:o..jpe<>g")));
		assertTrue("goo.too..jpeg".equals(ValidationUtils.normalizeFileName("   goo.t*o:o..jpe<>g  ")));
		assertTrue("goo-too-mo.jpeg".equals(ValidationUtils.normalizeFileName("goo   too----mo.jpeg")));
		assertTrue("goo-too-mo.jpeg".equals(ValidationUtils.normalizeFileName(".\\..\\goo   too----mo.jpeg")));
		assertTrue("goo-too-mo.jpeg".equals(ValidationUtils.normalizeFileName("__--goo   too----mo.jpeg--__")));
		assertTrue("goo-too-mo.jpeg".equals(ValidationUtils.normalizeFileName("_ -goo   too----mo.jpeg _--  _-")));

	}

	@Test
	public void validateUrlTest() {
		assertTrue(ValidationUtils.validateUrl("http://google.co.il/"));
		assertTrue(ValidationUtils.validateUrl("https://google.co.il/"));
		assertTrue(ValidationUtils.validateUrl("https://google.co.il/go/go"));
		assertTrue(ValidationUtils.validateUrl("https://google.co.il/go/go"));
		assertTrue(ValidationUtils.validateUrl("http://google.co.il/go/go"));
		assertFalse(ValidationUtils.validateUrl("google.co.il/go/go"));
		assertFalse(ValidationUtils.validateUrl("https://google.co.il/go/go!"));
		assertFalse(ValidationUtils.validateUrl("https://g;oogle.co.il/go/go"));

	}

	@Test
	public void normalizeArtifactLabel() {
		assertEquals(ValidationUtils.normalizeArtifactLabel("Test--3    134++"), "test3134");
	}

	@Test
	public void cleanArtifactLabel() {
		assertEquals(ValidationUtils.cleanArtifactDisplayName("Test--3    134++"), "Test-3 134+");
	}

	@Test
	public void validateArtifactLabel() {
		assertTrue(ValidationUtils.validateArtifactLabel("dsflkjsdf345JKL"));
		assertTrue(ValidationUtils.validateArtifactLabel("dsfsd lkj  "));
		assertTrue(ValidationUtils.validateArtifactLabel("sdfdsf---+"));
		assertTrue(ValidationUtils.validateArtifactLabel("   -  +"));
		assertFalse(ValidationUtils.validateArtifactLabel("sfsdfhkj111="));
		assertFalse(ValidationUtils.validateArtifactLabel("sfsdfhkj111=dfsf%"));
		assertFalse(ValidationUtils.validateArtifactLabel("sdfsdfljghgklsdg908*"));

	}

	@Test
	public void validateConsumerNameTest() {
		assertTrue(ValidationUtils.validateConsumerName("ab037cd"));
		assertFalse(ValidationUtils.validateConsumerName(" "));
		assertTrue(ValidationUtils.validateConsumerName("_dD.d9"));
		assertTrue(ValidationUtils.validateConsumerName("_dd.G9-"));
		assertFalse(ValidationUtils.validateConsumerName(".dA.d9-"));
		assertFalse(ValidationUtils.validateConsumerName("-d"));
		assertFalse(ValidationUtils.validateConsumerName("d?"));
		assertTrue(ValidationUtils.validateConsumerName("9"));
	}

	@Test
	public void validateConsumerPassSaltTest() {
		assertTrue(ValidationUtils.validateConsumerPassSalt("ad35fg2"));
		assertTrue(ValidationUtils.validateConsumerPassSalt("12s"));
		assertTrue(ValidationUtils.validateConsumerPassSalt("9"));
		assertFalse(ValidationUtils.validateConsumerPassSalt("dA.d9-"));
		assertFalse(ValidationUtils.validateConsumerPassSalt("dASQe"));
		assertFalse(ValidationUtils.validateConsumerPassSalt("_d"));
		assertFalse(ValidationUtils.validateConsumerPassSalt("?"));
		assertFalse(ValidationUtils.validateConsumerPassSalt(""));
		assertFalse(ValidationUtils.validateConsumerPassSalt(" "));
	}

	@Test
	public void validateCategoryNameFormatTest() {
		assertTrue(ValidationUtils.validateCategoryDisplayNameFormat("Net           ele-2_3#456&+.'=:@@@@@#####"));
		// this will fail at length
		assertTrue(ValidationUtils.validateCategoryDisplayNameFormat(null));
		// * is not allowed
		assertFalse(ValidationUtils.validateCategoryDisplayNameFormat("Net ele-2_3#456&*+.'=:@"));
		assertFalse(ValidationUtils.validateCategoryDisplayNameFormat(""));
		// should start with alphanumeric
		assertFalse(ValidationUtils.validateCategoryDisplayNameFormat("#abcdef"));
	}

	@Test
	public void validateCategoryNameLengthTest() {
		assertTrue(ValidationUtils.validateCategoryDisplayNameLength("Netele-2_3#456&+.'=:@@@@@"));
		assertTrue(ValidationUtils.validateCategoryDisplayNameLength("Nete"));
		assertFalse(ValidationUtils.validateCategoryDisplayNameLength("Netele-2_3#456&+.'=:@@@@@1"));
		assertFalse(ValidationUtils.validateCategoryDisplayNameLength("Net"));
		assertFalse(ValidationUtils.validateCategoryDisplayNameLength(null));
	}

	@Test
	public void normalizeCategoryNameTest() {
		assertEquals("NeteLE-2_3 of #456&+. & Goal a Abc'=:@ AT & T", ValidationUtils.normalizeCategoryName4Display(
				"   neteLE---2___3 OF ###456&&&+++...     aNd goal A abc'''==::@@@@@     AT and T  "));
		assertEquals("The Bank of America", ValidationUtils.normalizeCategoryName4Display("The Bank OF America"));
	}

	@Test
	public void normalizeCategoryLabelTest() {
		assertEquals("netele-2_3 of #456&+.&goal a abc'=:@ at&t",
				ValidationUtils.normalizeCategoryName4Uniqueness("NeteLE-2_3 of #456&+.&Goal a Abc'=:@ AT&T"));
	}

	/*
	 * Validation utils end
	 */

	/*
	 * General utility start
	 */

	@Test
	public void validateFileExtension() {
		assertEquals(Constants.EMPTY_STRING, GeneralUtility.getFilenameExtension("lalatru"));
		assertEquals(Constants.EMPTY_STRING, GeneralUtility.getFilenameExtension("aa."));
		assertEquals(Constants.EMPTY_STRING, GeneralUtility.getFilenameExtension(null));
		assertEquals("yaml", GeneralUtility.getFilenameExtension("lala.tru.yaml"));
		assertEquals("txt", GeneralUtility.getFilenameExtension("kuku.txt"));
	}

	@Test
	public void yamlTest() {

		log.debug("\"kuku\"");
		DumperOptions options = new DumperOptions();
		// options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.FOLDED);
		Yaml yaml = new Yaml(options);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("k1", "val");
		parameters.put("k2", "\"val\"");

		String str = yaml.dump(parameters);
		log.debug(str);
	}
	
	@Test
	public void yamlValidTest() {

		StringBuffer sb = new StringBuffer();
		sb.append("key: \"!@;/?:&=+$,_.~*'()[]\"");
		byte[] payload = sb.toString().getBytes();

		YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

		assertTrue(yamlToObjectConverter.isValidYaml(payload));
	}

	@Test
	public void testRemoveOnlyHtmlTags() {

		assertEquals("gooboo", HtmlCleaner.stripHtml("<b>goo<b></b></b><b>boo</b>"));
		/*String str = HtmlCleaner.stripHtml("<esofer><b>goo<b></b><</b><b>boo</b>");*/

		String stripHtmlAndEscape = HtmlCleaner.stripHtml("<esofer><b>goo<b></b><</b><b>boo</b>");
		assertEquals("<esofer>goo<boo", stripHtmlAndEscape);

		stripHtmlAndEscape = HtmlCleaner.stripHtml("<esofer><b>goo<b></b><</b><b>boo</b>", true);
		assertEquals("&lt;esofer&gt;goo&lt;boo", stripHtmlAndEscape);

		stripHtmlAndEscape = HtmlCleaner.stripHtml("<esofer><b>goo<b></b><&</b><b>boo</b>dvc&", true);
		assertEquals("&lt;esofer&gt;goo&lt;&amp;boodvc&amp;", stripHtmlAndEscape);

		assertEquals("esofer&gt;&gt;&lt;&lt;", HtmlCleaner.stripHtml("esofer>><<", true));
		assertEquals("esofer>><<", HtmlCleaner.stripHtml("esofer>><<", false));

		assertEquals("<esofer1>><<esofer2>", HtmlCleaner.stripHtml("<esofer1>><<esofer2>"));

		assertEquals("<esofer1 a= b>><<esofer2>", HtmlCleaner.stripHtml("<esofer1 a= b><h1>><<esofer2><br>"));

		assertEquals("&lt;esofer1 a= 'b'&gt;&gt;&lt;&lt;esofer2&gt;",
				HtmlCleaner.stripHtml("<esofer1 a= 'b'>><<esofer2>", true));
		assertEquals("<esofer1 a= 'b'>><<esofer2>", HtmlCleaner.stripHtml("<esofer1 a= 'b'>><H6><<esofer2>"));

		assertEquals("<esofer1 a= b>><<esofer2>", HtmlCleaner.stripHtml("<esofer1 a= b>><<esofer2>"));

		assertEquals("<esofer1 sd sa= b>><<esofer2>", HtmlCleaner.stripHtml("<esofer1 sd sa= b>><<esofer2>"));

		assertEquals("&lt;esofer1 sd sa= b&gt;&gt;&lt;&lt;esofer2&gt;",
				HtmlCleaner.stripHtml("<esofer1 sd sa= b>><<esofer2>", true));
		assertEquals("<esofer1 sd sa= b>><<esofer2>", HtmlCleaner.stripHtml("<esofer1 sd sa= b>><<esofer2>", false));
		assertEquals("&lt;esofer1 sd sa= b&gt;&gt;&lt;&lt;esofer2&gt;",
				HtmlCleaner.stripHtml("<esofer1 sd sa= b>><<esofer2>", true));
		assertEquals("<esofer1 sd sa= b>><<esofer2>",
				HtmlCleaner.stripHtml("<esofer1 sd sa= b>><br><H1><<esofer2>", false));
		assertEquals("&lt;esofer&gt;goo&lt;&amp;boodvc&amp;",
				HtmlCleaner.stripHtml("<esofer><b>goo<b></b><&</b><b>boo</b>dvc&", true));
		assertEquals("<esofer>goo<&boodvc&", HtmlCleaner.stripHtml("<esofer><b>goo<b></b><&</b><b>boo</b>dvc&", false));
		assertEquals("<<<>>>;\"", HtmlCleaner.stripHtml("<<<>>>;\"", false));
		assertEquals("&lt;&lt;&lt;&gt;&gt;&gt;;&quot;", HtmlCleaner.stripHtml("<<<>>>;\"", true));
		assertEquals("<<<>>>;\"", HtmlCleaner.stripHtml("<<<>>>;\"", false));
		assertEquals("abc ab a", HtmlCleaner.stripHtml("abc ab a", true));
		assertEquals("abc ab a", HtmlCleaner.stripHtml("abc ab a", false));
		assertEquals("< esofer1 sd &<>sa= b>><<esofer2>",
				HtmlCleaner.stripHtml("< esofer1 sd &<>sa= b>><br><H1><<esofer2>", false));
		assertEquals("sa= b>><<esofer2>", HtmlCleaner.stripHtml("<br sd &<>sa= b>><br><H1><<esofer2>", false));
		assertEquals("< br sd &<>sa= b>><<esofer2>",
				HtmlCleaner.stripHtml("< br sd &<>sa= b>><br><H1><<esofer2>", false));
		assertEquals("sa= b>><<esofer2>", HtmlCleaner.stripHtml("</br sd &<>sa= b>><br><H1><<esofer2>", false));
		assertEquals("sa= b>><<esofer2>", HtmlCleaner.stripHtml("<br sd &</>sa= b>><br><H1><<esofer2>", false));

	}

	/*
	 * General utility end
	 */
}
