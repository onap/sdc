package org.openecomp.sdc.common.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class ValidationUtilsTest {

	private ValidationUtils createTestSubject() {
		return new ValidationUtils();
	}

	
	@Test
	public void testValidateArtifactLabel() throws Exception {
		String label = "";
		boolean result;

		// default test
		result = ValidationUtils.validateArtifactLabel(label);
	}

	
	@Test
	public void testValidateArtifactDisplayName() throws Exception {
		String displayName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateArtifactDisplayName(displayName);
	}

	

	
	@Test
	public void testNormalizeCategoryName4Display() throws Exception {
		String str = "";
		String result;

		// test 1
		str = "123";
		result = ValidationUtils.normalizeCategoryName4Display(str);
		Assert.assertEquals("123", result);

		// test 2
		str = "123#123";
		result = ValidationUtils.normalizeCategoryName4Display(str);
		Assert.assertEquals("123#123", result);
	}

	
	@Test
	public void testNormalizeCategoryName4Uniqueness() throws Exception {
		String str = "";
		String result;

		// default test
		result = ValidationUtils.normalizeCategoryName4Uniqueness(str);
	}

	
	@Test
	public void testValidateCategoryDisplayNameLength() throws Exception {
		String label = "";
		boolean result;

		// default test
		result = ValidationUtils.validateCategoryDisplayNameLength(label);
	}

	
	@Test
	public void testValidateProductFullNameLength() throws Exception {
		String fullName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateProductFullNameLength(fullName);
	}

	
	@Test
	public void testValidateArtifactLabelLength() throws Exception {
		String label = "";
		boolean result;

		// default test
		result = ValidationUtils.validateArtifactLabelLength(label);
	}

	
	@Test
	public void testValidateResourceInstanceNameLength() throws Exception {
		String resourceInstanceName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateResourceInstanceNameLength(resourceInstanceName);
	}

	
	@Test
	public void testValidateResourceInstanceName() throws Exception {
		String resourceInstanceName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateResourceInstanceName(resourceInstanceName);
	}

	
	@Test
	public void testValidateUrlLength() throws Exception {
		String url = "";
		boolean result;

		// default test
		result = ValidationUtils.validateUrlLength(url);
	}

	
	@Test
	public void testValidateArtifactNameLength() throws Exception {
		String artifactName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateArtifactNameLength(artifactName);
	}

	
	@Test
	public void testValidateComponentNamePattern() throws Exception {
		String componentName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateComponentNamePattern(componentName);
	}

	
	@Test
	public void testValidateComponentNameLength() throws Exception {
		String componentName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateComponentNameLength(componentName);
	}

	
	@Test
	public void testValidateIcon() throws Exception {
		String icon = "";
		boolean result;

		// default test
		result = ValidationUtils.validateIcon(icon);
	}

	
	@Test
	public void testValidateIconLength() throws Exception {
		String icon = "";
		boolean result;

		// default test
		result = ValidationUtils.validateIconLength(icon);
	}

	
	@Test
	public void testValidateProjectCode() throws Exception {
		String projectCode = "";
		boolean result;

		// default test
		result = ValidationUtils.validateProjectCode(projectCode);
	}

	
	@Test
	public void testValidateProjectCodeLegth() throws Exception {
		String projectCode = "";
		boolean result;

		// default test
		result = ValidationUtils.validateProjectCodeLegth(projectCode);
	}

	
	@Test
	public void testValidateContactId() throws Exception {
		String contactId = "";
		boolean result;

		// default test
		result = ValidationUtils.validateContactId(contactId);
	}

	
	@Test
	public void testValidateCost() throws Exception {
		String cost = "";
		boolean result;

		// default test
		result = ValidationUtils.validateCost(cost);
	}

	
	@Test
	public void testRemoveHtmlTags() throws Exception {
		String str = "";
		String result;

		// default test
		result = ValidationUtils.removeHtmlTags(str);
	}

	
	@Test
	public void testRemoveAllTags() throws Exception {
		String htmlText = "";
		String result;

		// default test
		result = ValidationUtils.removeAllTags(htmlText);
	}

	
	@Test
	public void testNormaliseWhitespace() throws Exception {
		String str = "";
		String result;

		// default test
		result = ValidationUtils.normaliseWhitespace(str);
	}

	
	@Test
	public void testStripOctets() throws Exception {
		String str = "";
		String result;

		// default test
		result = ValidationUtils.stripOctets(str);
	}

	
	@Test
	public void testRemoveNoneUtf8Chars() throws Exception {
		String input = "";
		String result;

		// default test
		result = ValidationUtils.removeNoneUtf8Chars(input);
	}

	
	@Test
	public void testValidateIsEnglish() throws Exception {
		String input = "";
		boolean result;

		// default test
		result = ValidationUtils.validateIsEnglish(input);
	}

	
	@Test
	public void testValidateIsAscii() throws Exception {
		String input = "";
		boolean result;

		// default test
		result = ValidationUtils.validateIsAscii(input);
	}

	
	@Test
	public void testConvertHtmlTagsToEntities() throws Exception {
		String input = "";
		String result;

		// default test
		result = ValidationUtils.convertHtmlTagsToEntities(input);
	}

	


	


	
	@Test
	public void testValidateTagListLength() throws Exception {
		int tagListLength = 0;
		boolean result;

		// default test
		result = ValidationUtils.validateTagListLength(tagListLength);
	}

	
	@Test
	public void testValidateDescriptionLength() throws Exception {
		String description = "";
		boolean result;

		// default test
		result = ValidationUtils.validateDescriptionLength(description);
	}

	
	@Test
	public void testValidateStringNotEmpty() throws Exception {
		String value = "";
		boolean result;

		// test 1
		value = null;
		result = ValidationUtils.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);

		// test 2
		value = "";
		result = ValidationUtils.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testValidateListNotEmpty() throws Exception {
		List<?> list = null;
		boolean result;

		// test 1
		list = null;
		result = ValidationUtils.validateListNotEmpty(list);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testValidateVendorName() throws Exception {
		String vendorName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateVendorName(vendorName);
	}

	
	@Test
	public void testValidateVendorNameLength() throws Exception {
		String vendorName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateVendorNameLength(vendorName);
	}

	
	@Test
	public void testValidateResourceVendorModelNumberLength() throws Exception {
		String resourceVendorModelNumber = "";
		boolean result;

		// default test
		result = ValidationUtils.validateResourceVendorModelNumberLength(resourceVendorModelNumber);
	}

	
	@Test
	public void testValidateVendorRelease() throws Exception {
		String vendorRelease = "";
		boolean result;

		// default test
		result = ValidationUtils.validateVendorRelease(vendorRelease);
	}

	
	@Test
	public void testValidateVendorReleaseLength() throws Exception {
		String vendorRelease = "";
		boolean result;

		// default test
		result = ValidationUtils.validateVendorReleaseLength(vendorRelease);
	}

	
	@Test
	public void testValidateServiceTypeLength() throws Exception {
		String serviceType = "";
		boolean result;

		// default test
		result = ValidationUtils.validateServiceTypeLength(serviceType);
	}

	
	@Test
	public void testValidateServiceRoleLength() throws Exception {
		String serviceRole = "";
		boolean result;

		// default test
		result = ValidationUtils.validateServiceRoleLength(serviceRole);
	}

	
	@Test
	public void testHasBeenCertified() throws Exception {
		String version = "";
		boolean result;

		// default test
		result = ValidationUtils.hasBeenCertified(version);
	}

	
	@Test
	public void testNormaliseComponentName() throws Exception {
		String name = "";
		String result;

		// default test
		result = ValidationUtils.normaliseComponentName(name);
	}

	
	@Test
	public void testNormalizeComponentInstanceName() throws Exception {
		String name = "";
		String result;

		// default test
		result = ValidationUtils.normalizeComponentInstanceName(name);
	}

	


	
	@Test
	public void testConvertToSystemName() throws Exception {
		String name = "";
		String result;

		// default test
		result = ValidationUtils.convertToSystemName(name);
	}

	
	@Test
	public void testNormalizeFileName() throws Exception {
		String filename = "";
		String result;

		// default test
		result = ValidationUtils.normalizeFileName(filename);
	}

	


	
	@Test
	public void testValidateUrl() throws Exception {
		String url = "";
		boolean result;

		// default test
		result = ValidationUtils.validateUrl(url);
	}

	
	@Test
	public void testCleanArtifactDisplayName() throws Exception {
		String strIn = "";
		String result;

		// default test
		result = ValidationUtils.cleanArtifactDisplayName(strIn);
	}

	
	@Test
	public void testNormalizeArtifactLabel() throws Exception {
		String strIn = "";
		String result;

		// default test
		result = ValidationUtils.normalizeArtifactLabel(strIn);
	}

	
	@Test
	public void testValidateAdditionalInformationKeyName() throws Exception {
		String str = "";
		boolean result;

		// default test
		result = ValidationUtils.validateAdditionalInformationKeyName(str);
	}

	


	


	
	@Test
	public void testValidateConsumerName() throws Exception {
		String consumerName = "";
		boolean result;

		// default test
		result = ValidationUtils.validateConsumerName(consumerName);
	}

	
	@Test
	public void testIsUTF8Str() throws Exception {
		String str = "";
		boolean result;

		// default test
		result = ValidationUtils.isUTF8Str(str);
	}

	
	@Test
	public void testValidateConsumerPassSalt() throws Exception {
		String consumerSalt = "";
		boolean result;

		// default test
		result = ValidationUtils.validateConsumerPassSalt(consumerSalt);
	}

	
	@Test
	public void testIsFloatNumber() throws Exception {
		String number = "";
		boolean result;

		// default test
		result = ValidationUtils.isFloatNumber(number);
	}

	
	@Test
	public void testValidateCertifiedVersion() throws Exception {
		String version = "";
		boolean result;

		// default test
		result = ValidationUtils.validateCertifiedVersion(version);
	}

	
	@Test
	public void testValidateMinorVersion() throws Exception {
		String version = "";
		boolean result;

		// default test
		result = ValidationUtils.validateMinorVersion(version);
	}

	
	@Test
	public void testNormaliseProductName() throws Exception {
		String name = "";
		String result;

		// default test
		result = ValidationUtils.normaliseProductName(name);
	}

	


	
	@Test
	public void testRemoveHtmlTagsOnly() throws Exception {
		String htmlText = "";
		String result;

		// default test
		result = ValidationUtils.removeHtmlTagsOnly(htmlText);
	}
}