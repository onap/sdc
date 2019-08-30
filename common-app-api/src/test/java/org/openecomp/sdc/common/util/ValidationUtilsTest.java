/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.common.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


public class ValidationUtilsTest {

	@Test
	public void checkValidateArtifactLabelReturnsTrueIfInputIsValid() {
		final String testLabel = "testArtifactLabel";

		boolean result = ValidationUtils.validateArtifactLabel(testLabel);

		assertTrue(result);
	}

	@Test
	public void checkValidateArtifactLabelReturnsFalseIfInputIsInvalid() {
		final String testLabel = "wrong*()ArtifactLABEL+=";

		boolean result = ValidationUtils.validateArtifactLabel(testLabel);

		assertFalse(result);
	}

	@Test
	public void checkValidateArtifactLabelReturnsFalseIfInputIsEmpty() {
		final String testLabel = "";

		boolean result = ValidationUtils.validateArtifactLabel(testLabel);

		assertFalse(result);
	}

	@Test
	public void checkValidateArtifactDisplayNameReturnsTrueIfInputIsValid() {
		final String testDisplayName = "testDisplayName";

		boolean result = ValidationUtils.validateArtifactDisplayName(testDisplayName);

		assertTrue(result);
	}

	@Test
	public void checkValidateArtifactDisplayNameReturnsFalseIfInputIsInvalid() {
		final String testDisplayName = "wrong*()DisplayNAME+=";

		boolean result = ValidationUtils.validateArtifactDisplayName(testDisplayName);

		assertFalse(result);
	}

	@Test
	public void checkValidateArtifactDisplayNameReturnsFalseIfInputIsEmpty() {
		final String testDisplayName = "";

		boolean result = ValidationUtils.validateArtifactDisplayName(testDisplayName);

		assertFalse(result);
	}

	@Test
	public void checkValidateCategoryDisplayNameFormatReturnsTrueIfInputIsValid() {
		final String testCatalogDisplayName = "testCatalogDisplayName";

		boolean result = ValidationUtils.validateCategoryDisplayNameFormat(testCatalogDisplayName);

		assertTrue(result);
	}

	@Test
	public void checkValidateCategoryDisplayNameLengthReturnsTrueIfNameIsBetweenMinAndMax() {
		final String testCatalogDisplayName = "testCatalogDisplayName";

		boolean result = ValidationUtils.validateCategoryDisplayNameLength(testCatalogDisplayName);

		assertTrue(result);
	}

	@Test
	public void checkValidateCategoryDisplayNameLengthReturnsFalseIfNameIsToLong() {
		final String testCatalogDisplayName = "testCatalogVeryLongDisplayName";

		boolean result = ValidationUtils.validateCategoryDisplayNameLength(testCatalogDisplayName);

		assertFalse(result);
	}

	@Test
	public void checkValidateCategoryDisplayNameLengthReturnsFalseIfNameIsToShort() {
		final String testCatalogDisplayName = "Na";

		boolean result = ValidationUtils.validateCategoryDisplayNameLength(testCatalogDisplayName);

		assertFalse(result);
	}

	@Test
	public void checkValidateProductFullNameLengthReturnsTrueIfNameIsBetweenMinAndMax() {
		final String testProductFullName = "testProductFullName";

		boolean result = ValidationUtils.validateProductFullNameLength(testProductFullName);

		assertTrue(result);
	}

	@Test
	public void checkValidateProductFullNameLengthReturnsTrueIfNameIsToLong() {
		final String testProductFullName =
				"testProductVeryVeryLongFullNameThatIsToLong" +
						"ToPassValidationBecauseItExceedsTheMaxLengthOfThatParameter";

		boolean result = ValidationUtils.validateProductFullNameLength(testProductFullName);

		assertFalse(result);
	}

	@Test
	public void checkValidateProductFullNameLengthReturnsTrueIfNameIsToShort() {
		final String testProductFullName = "tes";

		boolean result = ValidationUtils.validateProductFullNameLength(testProductFullName);

		assertFalse(result);
	}

	@Test
	public void checkValidateArtifactLabelLengthReturnsTrueIfNameIsBetweenMinAndMax() {
		final String testArtifactLabel = "testArtifactLabel";

		boolean result = ValidationUtils.validateArtifactLabelLength(testArtifactLabel);

		assertTrue(result);
	}

	@Test
	public void checkValidateArtifactLabelLengthReturnsFalseIfNameIsToLong() {
		final String testArtifactLabel = generateLongString(300);

		boolean result = ValidationUtils.validateArtifactLabelLength(testArtifactLabel);

		assertFalse(result);
	}

	@Test
	public void checkValidateResourceInstanceNameLengthReturnsTrueIfNameIsBetweenMinAndMax() {
		final String testResourceInstanceName = "testResourceInstanceName";

		boolean result = ValidationUtils.validateResourceInstanceNameLength(testResourceInstanceName);

		assertTrue(result);
	}

	@Test
	public void checkValidateResourceInstanceNameReturnsTrueIfNameIsCorrect() {
		final String testResourceInstanceName = "testResourceInstanceName";

		boolean result = ValidationUtils.validateResourceInstanceName(testResourceInstanceName);

		assertTrue(result);
	}

	@Test
	public void checkValidateResourceInstanceNameReturnsFalseIfNameIsNotCorrect() {
		final String testResourceInstanceName = "wrong!@#resourceInstance\nName=+";

		boolean result = ValidationUtils.validateResourceInstanceName(testResourceInstanceName);

		assertFalse(result);
	}

	@Test
	public void checkValidateUrlLengthReturnsTrueIfUrlLengthIsBetweenMinAndMax() {
		final String testURL = "test/url/";

		boolean result = ValidationUtils.validateUrlLength(testURL);

		assertTrue(result);
	}

	@Test
	public void checkValidateUrlLengthReturnsFalseIfUrlLengthIsToLong() {
		final String testURL = generateLongString(120);

		boolean result = ValidationUtils.validateUrlLength(testURL);

		assertFalse(result);
	}

	@Test
	public void checkValidateArtifactNameLengthReturnsTrueIfUrlLengthIsBetweenMinAndMax() {
		final String testArtifactNameLength = "testArtifact";

		boolean result = ValidationUtils.validateArtifactNameLength(testArtifactNameLength);

		assertTrue(result);
	}

	@Test
	public void checkValidateArtifactNameLengthReturnsFalseIfUrlLengthIsToLong() {
		final String testArtifactNameLength = generateLongString(260);

		boolean result = ValidationUtils.validateArtifactNameLength(testArtifactNameLength);

		assertFalse(result);
	}

	@Test
	public void checkValidateComponentNamePatternReturnsTrueIfNameMatchesPattern() {
		final String testComponentName = "testComponent";

		boolean result = ValidationUtils.validateComponentNamePattern(testComponentName);

		assertTrue(result);
	}

	@Test
	public void checkValidateComponentNamePatternReturnsFalseIfNameDoesNotMatchesPattern() {
		final String testComponentName = "testWRONG!@#Component+!";

		boolean result = ValidationUtils.validateComponentNamePattern(testComponentName);

		assertFalse(result);
	}

	@Test
	public void checkValidateComponentNameLengthReturnsTrueIfNameLengthIsBetweenMinAndMax() {
		final String testComponentName = "testComponent";

		boolean result = ValidationUtils.validateComponentNameLength(testComponentName);

		assertTrue(result);
	}

	@Test
	public void checkValidateComponentNameLengthReturnsFalseIfNameLengthIsToLong() {
		final String testComponentName = generateLongString(1100);

		boolean result = ValidationUtils.validateComponentNameLength(testComponentName);

		assertFalse(result);
	}

	@Test
	public void checkValidateIconReturnsTrueIfIconMatchesPattern() {
		final String testIcon = "icon";

		boolean result = ValidationUtils.validateIcon(testIcon);

		assertTrue(result);
	}

	@Test
	public void checkValidateIconReturnsFalseIfIconDoesNotMatchesPattern() {
		final String testIcon = "icon,";

		boolean result = ValidationUtils.validateIcon(testIcon);

		assertFalse(result);
	}

	@Test
	public void checkValidateIconLengthReturnsTrueIfILengthIsBetweenMinAndMax() {
		final String testIcon = "icon";

		boolean result = ValidationUtils.validateIconLength(testIcon);

		assertTrue(result);
	}

	@Test
	public void checkValidateIconLengthReturnsTrueFalseIfILengthIsToLong() {
		final String testIcon = generateLongString(30);

		boolean result = ValidationUtils.validateIconLength(testIcon);

		assertFalse(result);
	}

	@Test
	public void checkValidateProjectCodeReturnsTrueIfCodeMatchesPattern() {
		final String testProjectCode = "testProjectCode";

		boolean result = ValidationUtils.validateProjectCode(testProjectCode);

		assertTrue(result);
	}

	@Test
	public void checkValidateProjectCodeReturnsFalseIfCodeDoesNotMatchesPattern() {
		final String testProjectCode = "testWRONG!@#ProjectCode";

		boolean result = ValidationUtils.validateProjectCode(testProjectCode);

		assertFalse(result);
	}

	@Test
	public void checkValidateProjectCodeLengthReturnsTrueIfCodeMatchesPattern() {
		final String testProjectCode = "testProjectCode";

		boolean result = ValidationUtils.validateProjectCodeLegth(testProjectCode);

		assertTrue(result);
	}

	@Test
	public void checkValidateContactIdReturnsTrueIfIdMatchesPattern() {
		final String testContactId = "testContactId";

		boolean result = ValidationUtils.validateContactId(testContactId);

		assertTrue(result);
	}

	@Test
	public void checkValidateCostReturnsTrueIfIdMatchesPattern() {
		final String testCost = "120.15";

		boolean result = ValidationUtils.validateCost(testCost);

		assertTrue(result);
	}

	@Test
	public void validateRemoveHtmlTagsReturnsStringWithNoHTMLTags() {
		final String htmlString = "<div>test with <p>tags</p></div>";

		String result = ValidationUtils.removeHtmlTags(htmlString);

		assertEquals(result, "test with tags");
	}

	@Test
	public void validateRemoveAllTagsReturnsStringWithNoHTMLTags() {
		final String htmlString = "<div>test with <p>tags</p></div>";

		String result = ValidationUtils.removeAllTags(htmlString);

		assertEquals(result, "test with tags");
	}

	@Test
	public void validateNormalizeWhitespaceReturnsStringWithNormalizedWhitespace() {
		final String whitespaceString = "test   normalize  whitespace";

		String result = ValidationUtils.normaliseWhitespace(whitespaceString);

		assertEquals(result, "test normalize whitespace");
	}

	@Test
	public void validateStripOctetsReturnsStringWithNormalizedWhitespace() {
		final String octedString = "%2Dtest strip octets text";

		String result = ValidationUtils.stripOctets(octedString);

		assertEquals(result, "test strip octets text");
	}

	@Test
	public void validateRemoveNoneUtf8CharsRemovesCharacterThatAreNotFromUtf8() {
		final String nonUtf8String = "test קקUtf8 קק textקק";

		String result = ValidationUtils.removeNoneUtf8Chars(nonUtf8String);

		assertEquals(result, "test Utf8  text");
	}

	@Test
	public void validateIsEnglishReturnsTrueIfStringContainsOnlyEnglishCharacters() {
		final String nonUtf8String = "test english text";

		boolean result = ValidationUtils.validateIsEnglish(nonUtf8String);

		assertTrue(result);
	}

	@Test
	public void validateIsEnglishReturnsFalseIfStringContainsNoEnglishCharacters() {
		final String nonUtf8String = "test noEnglish text文";

		boolean result = ValidationUtils.validateIsEnglish(nonUtf8String);

		assertFalse(result);
	}

	@Test
	public void validateIsAsciiReturnsTrueIfStringContainsOnlyAsciiCharacters() {
		final String testAsciiText = "ascii text";

		boolean result = ValidationUtils.validateIsAscii(testAsciiText);

		assertTrue(result);
	}

	@Test
	public void validateIsAsciiReturnsFalseIfStringContainsNotAsciiCharacter() {
		final String testAsciiText = "no ascii text ┬á";

		boolean result = ValidationUtils.validateIsAscii(testAsciiText);

		assertFalse(result);
	}

	@Test
	public void validateConvertHtmlTagsToEntitiesReturnsStringWithReplacedTags() {
		final String testAsciiText = "<div>ascii text</div>";

		String result = ValidationUtils.convertHtmlTagsToEntities(testAsciiText);

		assertEquals(result, "&lt;div&gt;ascii text&lt;/div&gt;");
	}


	@Test
	public void validateRemoveDuplicateFromListReturnsListWithoutDuplicates() {
		List<String> listOfDuplicates =
				Lists.newArrayList("text01","text01","text02","text02","text02","text03");

		List<String> result = ValidationUtils.removeDuplicateFromList(listOfDuplicates);

		assertTrue(result.containsAll(Lists.newArrayList("text01","text03","text03")));
		assertEquals(result.size(), 3);
	}

	@Test
	public void checkValidateTagLengthReturnsTrueIfTagIsBetweenMaxAndMin() {
		final String testTag = "testTag";

		boolean result = ValidationUtils.validateTagLength(testTag);

		assertTrue(result);
	}

	@Test
	public void checkValidateTagLengthReturnsFalseIfTagIsToLong() {
		final String testTag = generateLongString(1200);

		boolean result = ValidationUtils.validateTagLength(testTag);

		assertFalse(result);
	}

	@Test
	public void checkValidateTagLengthReturnsFalseIfTagIsNull() {
		boolean result = ValidationUtils.validateTagLength(null);

		assertFalse(result);
	}

	@Test
	public void validateValidateTagListLengthReturnsTrueIfListIsBetweenMaxAndMin() {
		boolean result = ValidationUtils.validateTagListLength(5);

		assertTrue(result);
	}

	@Test
	public void validateValidateTagListLengthReturnsFalseIfListIsToLong() {
		boolean result = ValidationUtils.validateTagListLength(1250);

		assertFalse(result);
	}

	@Test
	public void checkCalidateListNotEmptyReturnsTrueIfListIsNotEmpty() {
		boolean result = ValidationUtils.validateListNotEmpty(Collections.singletonList("testItem"));

		assertTrue(result);
	}

	@Test
	public void checkCalidateListNotEmptyReturnsFalseIfListIsEmpty() {
		boolean result = ValidationUtils.validateListNotEmpty(Collections.emptyList());

		assertFalse(result);
	}

	@Test
	public void checkValidateDescriptionLengthTestReturnsTrueIfTagIsBetweenMaxAndMin() {
		final String testDescription = "testDescription";

		boolean result = ValidationUtils.validateDescriptionLength(testDescription);

		assertTrue(result);
	}

	@Test
	public void checkValidateDescriptionLengthTestReturnsFalseIfTagIsToLong() {
		final String testDescription =  generateLongString(1200);

		boolean result = ValidationUtils.validateDescriptionLength(testDescription);

		assertFalse(result);
	}

	@Test
	public void checkValidateStringNotEmptyReturnsFalseIfStringIsNotEmpty() {
		final String testString =  "test";

		boolean result = ValidationUtils.validateStringNotEmpty(testString);

		assertTrue(result);
	}

	@Test
	public void checkValidateStringNotEmptyReturnsFTrueIfStringIsEmpty() {
		final String testString =  "";

		boolean result = ValidationUtils.validateStringNotEmpty(testString);

		assertFalse(result);
	}

	@Test
	public void checkValidateVendorNameReturnsTrueIfNameFitsPattern() {
		final String testVendorName =  "testVendor";

		boolean result = ValidationUtils.validateVendorName(testVendorName);

		assertTrue(result);
	}

	@Test
	public void checkValidateVendorNameReturnsFalseIfNameDoesNotFitsPattern() {
		final String testVendorName =  "test:Vendor";

		boolean result = ValidationUtils.validateVendorName(testVendorName);

		assertFalse(result);
	}

	@Test
	public void checkValidateVendorNameLengthReturnsTrueIfNameIsBetweenMaxAndMin() {
		final String testVendorName =  "testVendor";

		boolean result = ValidationUtils.validateVendorNameLength(testVendorName);

		assertTrue(result);
	}

	@Test
	public void checkValidateVendorNameLengthReturnsFalseIfNameIsToLong() {
		final String testVendorName =  generateLongString(90);

		boolean result = ValidationUtils.validateVendorNameLength(testVendorName);

		assertFalse(result);
	}

	@Test
	public void checkValidateResourceVendorModelNumberLengthReturnsTrueIfNameIsBetweenMaxAndMin() {
		final String testVendorName =  "testVendor";

		boolean result = ValidationUtils.validateResourceVendorModelNumberLength(testVendorName);

		assertTrue(result);
	}

	@Test
	public void checkValidateResourceVendorModelNumberLengthReturnsFalseIfNameIsToLong() {
		final String testVendorName =  generateLongString(90);

		boolean result = ValidationUtils.validateResourceVendorModelNumberLength(testVendorName);

		assertFalse(result);
	}

	@Test
	public void checkValidateVendorReleaseReturnsTrueIfReleaseFitsPattern() {
		final String testVendorRelease =  "testVendorRelease";

		boolean result = ValidationUtils.validateVendorRelease(testVendorRelease);

		assertTrue(result);
	}

	@Test
	public void checkValidateVendorReleaseReturnsFalseIfReleaseDoesNotFitsPattern() {
		final String testVendorRelease =  "testVendor:Release";

		boolean result = ValidationUtils.validateVendorRelease(testVendorRelease);

		assertFalse(result);
	}

	@Test
	public void checkValidateVendorReleaseLengthReturnsTrueIfReleaseIsBetweenMaxAndMin() {
		final String testVendorRelease =  "testVendorRelease";

		boolean result = ValidationUtils.validateVendorReleaseLength(testVendorRelease);

		assertTrue(result);
	}

	@Test
	public void checkValidateVendorReleaseLengthReturnsFalseIfReleaseIsToLong() {
		final String testVendorRelease =  generateLongString(30);

		boolean result = ValidationUtils.validateVendorReleaseLength(testVendorRelease);

		assertFalse(result);
	}

	@Test
	public void checkValidateServiceTypeLengthReturnsTrueIfReleaseIsBetweenMaxAndMin() {
		final String testServiceType =  "testServiceType";

		boolean result = ValidationUtils.validateServiceTypeLength(testServiceType);

		assertTrue(result);
	}

	@Test
	public void checkValidateServiceTypeLengthReturnsFalseIfReleaseIsToLong() {
		final String testServiceType =  generateLongString(500);

		boolean result = ValidationUtils.validateServiceTypeLength(testServiceType);

		assertFalse(result);
	}

	@Test
	public void checkValidateServiceRoleLengthReturnsTrueIfReleaseIsBetweenMaxAndMin() {
		final String testServiceRoleLength =  "testServiceType";

		boolean result = ValidationUtils.validateServiceRoleLength(testServiceRoleLength);

		assertTrue(result);
	}

	@Test
	public void checkValidateServiceRoleLengthReturnsFalseIfReleaseIsToLong() {
		final String testServiceRoleLength =  generateLongString(500);

		boolean result = ValidationUtils.validateServiceRoleLength(testServiceRoleLength);

		assertFalse(result);
	}

	@Test
	public void validateHasBeenCertifiedReturnsTrueIfVersionIsEqualOrBiggerThan1() {
		final String testVersion = "1.0";

		boolean result = ValidationUtils.hasBeenCertified(testVersion);

		assertTrue(result);
	}

	@Test
	public void validateHasBeenCertifiedReturnsFalseIfVersionIsSmallerThan1() {
		final String testVersion = "0.6";

		boolean result = ValidationUtils.hasBeenCertified(testVersion);

		assertFalse(result);
	}

	@Test
	public void validateNormaliseComponentNameReturnsNormalizedName() {
		final String testName = "test-Component-Service";

		String result = ValidationUtils.normaliseComponentName(testName);

		assertEquals(result, "testcomponentservice");
	}

	@Test
	public void validateNormaliseComponentInstanceNameReturnsNormalizedName() {
		final String testName = "test-Component-Service";

		String result = ValidationUtils.normalizeComponentInstanceName(testName);

		assertEquals(result, "testcomponentservice");
	}

	@Test
	public void validateConvertToSystemNameReturnsProperSystemName() {
		final String testName = "test-Component-Service";

		String result = ValidationUtils.convertToSystemName(testName);

		assertEquals(result, "TestComponentService");
	}

	@Test
	public void validateNormalizeFileNameReturnsNormalizedName() {
		final String testName = "test File Name";

		String result = ValidationUtils.normalizeFileName(testName);

		assertEquals(result, "test-File-Name");
	}

	@Test
	public void checkValidateUrlReturnsTrueIfURLIsValid() {
		final String testUrl = "http://test.co/valid/url/";

		boolean result = ValidationUtils.validateUrl(testUrl);

		assertTrue(result);
	}

	@Test
	public void checkValidateUrlReturnsFalseIfURLIsNotValid() {
		final String testUrl = "http//notvalid!#url";

		boolean result = ValidationUtils.validateUrl(testUrl);

		assertFalse(result);
	}

	@Test
	public void checkValidateUrlReturnsFalseIfURLIsNotUtf8() {
		final String testUrl = "http://test.co/notutf/קקurl/";

		boolean result = ValidationUtils.validateUrl(testUrl);

		assertFalse(result);
	}

	@Test
	public void validateNormalizeArtifactLabelReturnsNormalizeArtifactLabel() {
		final String testArtifactLabel = "test-File-Name";

		String result = ValidationUtils.normalizeArtifactLabel(testArtifactLabel);

		assertEquals(result, "testfilename");
	}

	@Test
	public void validateAdditionalInformationKeyNameReturnsTrueIfAdditionalInformationAreValid() {
		final String testAdditionalInformationKeyName = "KeyName";

		boolean result = ValidationUtils.validateAdditionalInformationKeyName(testAdditionalInformationKeyName);

		assertTrue(result);
	}

	@Test
	public void validateNormalizeAdditionalInformationReturnsNormalizeArtifactLabel() {
		final String testArtifactLabel = "additional--Information__Testing";

		String result = ValidationUtils.normalizeAdditionalInformation(testArtifactLabel);

		assertEquals(result, "additional-Information_Testing");
	}

	@Test
	public void checkValidateLengthReturnsTrueIfStringIsShorterThenGivenLength() {
		final String testString = "testString";

		boolean result = ValidationUtils.validateLength(testString,50);

		assertTrue(result);
	}

	@Test
	public void checkValidateLengthReturnsTrueIfStringIsNull() {
		boolean result = ValidationUtils.validateLength(null,50);

		assertTrue(result);
	}

	@Test
	public void checkValidateLengthReturnsTrueIfStringIsExitsTheGivenLength() {
		final String testString = "testString";

		boolean result = ValidationUtils.validateLength(testString,5);

		assertFalse(result);
	}

	@Test
	public void validateIsUTF8StrReturnsFalseIfGivenStringContainsUtf8Character() {
		final String testString = "testקString";

		boolean result = ValidationUtils.isUTF8Str(testString);

		assertFalse(result);
	}

	@Test
	public void validateIsUTF8StrReturnsTrueIfGivenStringDoesNotContainsUtf8Character() {
		final String testString = "testString";

		boolean result = ValidationUtils.isUTF8Str(testString);

		assertTrue(result);
	}

	@Test
	public void validateIsFloatNumberReturnsTrueIfGivenStringRepresentsFloatNumber() {
		final String testString = "12.45";

		boolean result = ValidationUtils.isFloatNumber(testString);

		assertTrue(result);
	}

	@Test
	public void validateIsFloatNumberReturnsFalseIfGivenStringDoesNotRepresentsFloatNumber() {
		final String testString = "notFloatingPoint";

		boolean result = ValidationUtils.isFloatNumber(testString);

		assertFalse(result);
	}

	@Test
	public void validateCertifiedVersionReturnsTrueIfGivenStringRepresentsVersion() {
		final String testString = "1.0";

		boolean result = ValidationUtils.validateCertifiedVersion(testString);

		assertTrue(result);
	}

	@Test
	public void validateCertifiedVersionReturnsFalseIfGivenStringDoesNotRepresentsVersion() {
		final String testString = "notVersion";

		boolean result = ValidationUtils.validateCertifiedVersion(testString);

		assertFalse(result);
	}

	@Test
	public void validateMinorVersionReturnsTrueIfGivenStringRepresentsMinorVersion() {
		final String testString = "0.1";

		boolean result = ValidationUtils.validateMinorVersion(testString);

		assertTrue(result);
	}

	@Test
	public void validateMinorVersionReturnsFalseIfGivenStringDoesNotRepresentsMinorVersion() {
		final String testString = "notMinorVersion";

		boolean result = ValidationUtils.validateMinorVersion(testString);

		assertFalse(result);
	}

	@Test
	public void validateCleanArtifactDisplayNameReturnsCleanedArtifactName() {
		final String testArtifactDisplayName = "  test-File   Name";

		String result = ValidationUtils.cleanArtifactDisplayName(testArtifactDisplayName);

		assertEquals(result, "test-File Name");
	}

	@Test
	public void checkValidateArtifactLabelReturnsTrueIfLabelIsValid() {
		final String testArtifactDisplayName = "testLabel";

		boolean result = ValidationUtils.validateArtifactLabel(testArtifactDisplayName);

		assertTrue(result);
	}

	@Test
	public void checkValidateArtifactLabelReturnsFalseIfLabelIsNotValid() {
		final String testArtifactDisplayName = "test=notValid=Label";

		boolean result = ValidationUtils.validateArtifactLabel(testArtifactDisplayName);

		assertFalse(result);
	}

	@Test
	public void checkValidateConsumerNameReturnsTrueIfLabelIsValid() {
		final String testConsumerName = "testConsumerName";

		boolean result = ValidationUtils.validateConsumerName(testConsumerName);

		assertTrue(result);
	}

	@Test
	public void checkValidateConsumerNameReturnsFalseIfLabelIsNotValid() {
		final String testConsumerName = "test=notValid=ConsumerName";

		boolean result = ValidationUtils.validateConsumerName(testConsumerName);

		assertFalse(result);
	}

	@Test
	public void checkValidateConsumerPassSaltReturnsTrueIfLabelIsValid() {
		final String testPassSalt = "123qwe";

		boolean result = ValidationUtils.validateConsumerPassSalt(testPassSalt);

		assertTrue(result);
	}

	@Test
	public void checkValidateConsumerPassSaltReturnsFalseIfLabelIsNotValid() {
		final String testPassSalt = "_123qweLO";

		boolean result = ValidationUtils.validateConsumerPassSalt(testPassSalt);

		assertFalse(result);
	}

	@Test
	public void checkValidateCategoryNameFormatReturnsTrueIfLabelIsValid() {
		final String testDisplayNameFormat = "DisplayNameFormat";

		boolean result = ValidationUtils.validateCategoryDisplayNameFormat(testDisplayNameFormat);

		assertTrue(result);
	}

	@Test
	public void checkValidateCategoryNameFormatReturnsFalseIfLabelIsNotValid() {
		final String testDisplayNameFormat = "Display{NotValid}NameFormat";

		boolean result = ValidationUtils.validateCategoryDisplayNameFormat(testDisplayNameFormat);

		assertFalse(result);
	}

	@Test
	public void checkValidateCategoryNameFormatReturnsFalseIfLabelIsStartingWihNonAlphabetical() {
		final String testDisplayNameFormat = "@DisplayNameFormat";

		boolean result = ValidationUtils.validateCategoryDisplayNameFormat(testDisplayNameFormat);

		assertFalse(result);
	}

	@Test
	public void checkValidateCategoryNameLengthReturnsTrueIfLabelLengthIsBetweenMinaAndMax() {
		final String testDisplayNameFormat = "DisplayNameFormat";

		boolean result = ValidationUtils.validateCategoryDisplayNameLength(testDisplayNameFormat);

		assertTrue(result);
	}

	@Test
	public void checkValidateCategoryNameLengthReturnsFalseIfLabelLengthIsToLong() {
		final String testDisplayNameFormat = generateLongString(28);

		boolean result = ValidationUtils.validateCategoryDisplayNameLength(testDisplayNameFormat);

		assertFalse(result);
	}

	@Test
	public void validateNormalizeCategoryNameReturnsNormalizeName() {
		final String testCatalogName = "not Normalize OF CatalogName";

		String result = ValidationUtils.normalizeCategoryName4Display(testCatalogName);

		assertEquals(result, "Not Normalize of CatalogName");
	}

	@Test
	public void validateNormalizeCategoryLabelReturnsNormalizeLabel() {
		final String testCatalogLabel = "not Normalize OF CatalogLabel";

		String result = ValidationUtils.normalizeCategoryName4Uniqueness(testCatalogLabel);

		assertEquals(result, "not normalize of cataloglabel");
	}

	@Test
	public void validateNormaliseProductNameReturnsNormalizedName() {
		final String testProductName = "Product Name";

		String result = ValidationUtils.normaliseProductName(testProductName);

		assertEquals(result, "productname");

	}

	@Test
	public void validateRemoveHtmlTagsOnlyReturnsStringWithRemovedHtmlTags() {
		final String testHtml = "<div>Product <p>Name</p> <not html tag></div>";

		String result = ValidationUtils.removeHtmlTagsOnly(testHtml);

		assertEquals(result, "Product Name <not html tag>");

	}

	@Test
	public void checkValidateForwardingPathNamePatternReturnsTrueIfPathIsValid() {
		final String testForwardingPath = "test.forwarding.path";

		boolean result = ValidationUtils.validateForwardingPathNamePattern(testForwardingPath);

		assertTrue(result);
	}

	@Test
	public void checkValidateForwardingPathNamePatternReturnsFalseIfPathIsNotValid() {
		final String testForwardingPath = "test/notValid/forwarding//path";

		boolean result = ValidationUtils.validateForwardingPathNamePattern(testForwardingPath);

		assertFalse(result);
	}

	private String generateLongString(int length) {
		StringBuilder toLongLabelBuilder = new StringBuilder();
		for(int i=0 ; i<=length ; i++) {
			toLongLabelBuilder.append("t");
		}
		return toLongLabelBuilder.toString();
	}
}
