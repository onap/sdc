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

package org.openecomp.sdc.common.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.safety.Whitelist;

import com.google.common.base.CharMatcher;

public class ValidationUtils {
	public final static Integer COMPONENT_NAME_MAX_LENGTH = 1024;
	public final static Pattern COMPONENT_NAME_PATTERN = Pattern
			.compile("^[\\w][\\w \\.\\-\\_\\:\\+]{0," + (COMPONENT_NAME_MAX_LENGTH-1) + "}$");
	public final static Integer ADDITIONAL_INFORMATION_KEY_MAX_LENGTH = 50;
	public final static Pattern ADDITIONAL_INFORMATION_KEY_PATTERN = Pattern
			.compile("^[\\w\\s\\.\\-\\_]{1," + COMPONENT_NAME_MAX_LENGTH + "}$");
	public final static Integer RSI_NAME_MAX_LENGTH = 1024;
	public final static Pattern RSI_NAME_PATTERN = Pattern
			.compile("^[\\w \\s\\.\\-\\_\\:\\+]{1," + RSI_NAME_MAX_LENGTH + "}$");
	public final static Integer COMMENT_MAX_LENGTH = 256;

	public final static Integer ICON_MAX_LENGTH = 25;
	public final static Pattern ICON_PATTERN = Pattern.compile("^[\\w\\-]{1," + ICON_MAX_LENGTH + "}$");
	public final static Integer PROJECT_CODE_MAX_LEGTH = 50;
	public final static Pattern PROJECT_CODE_PATTERN = Pattern.compile("^[\\s\\w_.-]{5,50}$");

	// USER_ID format : aannnX (where a=a-z or A-Z, n=0-9, and X=a-z,A-Z, or 0-9)
	public final static Integer CONNTACT_ID_MAX_LENGTH = 50;
//	public final static Pattern CONTACT_ID_PATTERN = Pattern
//			.compile("[mM]{1}[0-9]{5}|[a-zA-Z]{2}[0-9]{4}|[a-zA-Z]{2}[0-9]{3}[a-zA-Z]{1}");
	public final static Pattern CONTACT_ID_PATTERN = Pattern.compile("^[\\s\\w_.-]{1,50}$");
	public final static Pattern OCTET_PATTERN = Pattern.compile("%[a-fA-F0-9]{2}");
	public final static Pattern NONE_UTF8_PATTERN = Pattern.compile("[^\\x00-\\x7F]+");
	public final static Pattern URL_INVALIDE_PATTERN = Pattern.compile("[,#?&@$<>~^`\\\\\\[\\]{}|\")(*!+=;%]+");// ,#?&@$<>~^`\\[]{}|")(*!

	public final static Pattern ENGLISH_PATTERN = Pattern.compile("^[\\p{Graph}\\x20]+$");
	public final static Integer COMPONENT_DESCRIPTION_MAX_LENGTH = 1024;
	public final static Integer TAG_MAX_LENGTH = 1024;
	public final static Integer TAG_LIST_MAX_LENGTH = 1024;
	public final static Integer VENDOR_NAME_MAX_LENGTH = 25;
	public final static Pattern VENDOR_NAME_PATTERN = Pattern
			.compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]+$");
	public final static Integer VENDOR_RELEASE_MAX_LENGTH = 25;
	public final static Pattern VENDOR_RELEASE_PATTERN = Pattern
			.compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]+$");

	public final static Pattern CLEAN_FILENAME_PATTERN = Pattern.compile("[\\x00-\\x1f\\x80-\\x9f\\x5c/<?>\\*:|\"/]+");

	public final static Pattern DASH_PATTERN = Pattern.compile("[-]+");
	public final static Pattern UNDERSCORE_PATTERN = Pattern.compile("[_]+");
	public final static Pattern PLUS_PATTERN = Pattern.compile("[+]+");
	public final static Pattern SPACE_PATTERN = Pattern.compile("[ ]+");
	public final static Pattern AMP_PATTERN = Pattern.compile("[&]+");
	public final static Pattern DOT_PATTERN = Pattern.compile("[\\.]+");
	public final static Pattern APOST_PATTERN = Pattern.compile("[']+");
	public final static Pattern HASHTAG_PATTERN = Pattern.compile("[#]+");
	public final static Pattern EQUAL_PATTERN = Pattern.compile("[=]+");
	public final static Pattern COLON_PATTERN = Pattern.compile("[:]+");
	public final static Pattern AT_PATTERN = Pattern.compile("[@]+");
	public final static Pattern AND_PATTERN = Pattern.compile(" [aA][Nn][Dd] ");
	public final static Set<String> CATEGORY_CONJUNCTIONS = new HashSet<String>(
			Arrays.asList("of", "to", "for", "as", "a", "an", "the"));

	public final static Pattern COST_PATTERN = Pattern.compile("^[0-9]{1,5}\\.[0-9]{1,3}$");
	public final static Pattern ARTIFACT_LABEL_PATTERN = Pattern.compile("^[a-zA-Z0-9 \\-+]+$");
	public final static Integer ARTIFACT_LABEL_LENGTH = 255;
	public final static Pattern ARTIFACT_DISPLAY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9 &\\.'#=:@_\\-+]+$");
	public final static Pattern CATEGORY_LABEL_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9 &\\.'#=:@_\\-+]+$");
	public final static Integer CATEGORY_LABEL_MIN_LENGTH = 4;
	public final static Integer CATEGORY_LABEL_MAX_LENGTH = 25;

	public final static Pattern COMPONENT_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_]+");
	public final static Pattern COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-]+");
	public final static Pattern PRODUCT_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_&=#@':\\[\\]\\+]+");
	public final static Integer CONSUMER_NAME_MAX_LENGTH = 255;
	// public final static Pattern CONSUMER_NAME_PATTERN =
	// Pattern.compile("^[\\w]{1}?[\\w\\.\\-]{0," + CONSUMER_NAME_MAX_LENGTH +
	// "}?$");
	public final static Pattern CONSUMER_NAME_PATTERN = Pattern.compile("^[\\w]+[\\w\\.\\-]*$");
	public final static Integer CONSUMER_SALT_LENGTH = 32;
	public final static Integer CONSUMER_PASSWORD_LENGTH = 64;
	public final static Pattern CONSUMER_PASS_SALT_PATTERN = Pattern.compile("^[a-z0-9]+$");
	public final static Pattern FLOAT_PATTERN = Pattern.compile("^[\\d]+[\\.]{1}[\\d]+$");
	public final static Pattern CERTIFIED_VERSION_PATTERN = Pattern.compile("^[1-9][0-9]*\\.0$");
	public final static Pattern MINOR_VERSION_PATTERN = Pattern.compile("^0\\.[1-9][0-9]*$");
	public final static Pattern TAGS_PATTERN = Pattern.compile("<[^><]*>");

	public final static Integer ARTIFACT_NAME_LENGTH = 255;
	public final static Integer API_URL_LENGTH = 100;
	public final static Integer ARTIFACT_DESCRIPTION_MAX_LENGTH = 256;

	public final static Integer PRODUCT_FULL_NAME_MIN_LENGTH = 4;
	public final static Integer PRODUCT_FULL_NAME_MAX_LENGTH = 100;

	public static boolean validateArtifactLabel(String label) {
		return ARTIFACT_LABEL_PATTERN.matcher(label).matches();
	}
	
	public static boolean validateArtifactDisplayName(String displayName) {
		return ARTIFACT_DISPLAY_NAME_PATTERN.matcher(displayName).matches();
	}

	public static boolean validateCategoryDisplayNameFormat(String label) {
		boolean res = true;
		if (label != null) {
			label = label.trim();
			res = CATEGORY_LABEL_PATTERN.matcher(label).matches();
		}
		return res;
	}

	public static String normalizeCategoryName4Display(String str) {
		if (str != null) {
			str = str.trim();
			str = DASH_PATTERN.matcher(str).replaceAll("-");
			str = UNDERSCORE_PATTERN.matcher(str).replaceAll("_");
			str = AMP_PATTERN.matcher(str).replaceAll("&");
			str = PLUS_PATTERN.matcher(str).replaceAll("+");
			str = DOT_PATTERN.matcher(str).replaceAll(".");
			str = APOST_PATTERN.matcher(str).replaceAll("'");
			str = HASHTAG_PATTERN.matcher(str).replaceAll("#");
			str = EQUAL_PATTERN.matcher(str).replaceAll("=");
			str = COLON_PATTERN.matcher(str).replaceAll(":");
			str = AT_PATTERN.matcher(str).replaceAll("@");
			str = normaliseWhitespace(str);
			str = AND_PATTERN.matcher(str).replaceAll(" & ");

			// Case normalizing
			StringBuilder sb = new StringBuilder();
			String[] split = str.split(" ");
			for (int i = 0; i < split.length; i++) {
				String splitted = split[i];
				String lowerCase = splitted.toLowerCase();
				// BANK OF AMERICA --> BANK of AMERICA ("of" is lowercased), but
				// OF BANK OF AMERICA --> OF BANK of AMERICA (first "OF" is not
				// lowercased because it's first word)
				// Agreed with Ella, 26/11/15
				if ((i > 0) && CATEGORY_CONJUNCTIONS.contains(lowerCase)) {
					sb.append(lowerCase);
				} else {
					sb.append(WordUtils.capitalize(splitted));
				}
				sb.append(" ");
			}
			str = sb.toString().trim();
		}
		return str;
	}

	public static String normalizeCategoryName4Uniqueness(String str) {
		return str.toLowerCase();
	}

	public static boolean validateCategoryDisplayNameLength(String label) {
		return (label != null && label.length() >= CATEGORY_LABEL_MIN_LENGTH
				&& label.length() <= CATEGORY_LABEL_MAX_LENGTH);
	}

	public static boolean validateProductFullNameLength(String fullName) {
		return (fullName != null && fullName.length() >= PRODUCT_FULL_NAME_MIN_LENGTH
				&& fullName.length() <= PRODUCT_FULL_NAME_MAX_LENGTH);
	}

	public static boolean validateArtifactLabelLength(String label) {
		return label.length() > 0 && label.length() <= ARTIFACT_LABEL_LENGTH;
	}

	public static boolean validateResourceInstanceNameLength(String resourceInstanceName) {
		return resourceInstanceName.length() <= RSI_NAME_MAX_LENGTH;
	}

	public static boolean validateResourceInstanceName(String resourceInstanceName) {
		return RSI_NAME_PATTERN.matcher(resourceInstanceName).matches();
	}

	public static boolean validateUrlLength(String url) {
		return url.length() <= API_URL_LENGTH;
	}

	public static boolean validateArtifactNameLength(String artifactName) {
		return (artifactName.length() <= ARTIFACT_NAME_LENGTH && artifactName.length() > 0);
	}

	public static boolean validateComponentNamePattern(String componentName) {
		return COMPONENT_NAME_PATTERN.matcher(componentName).matches();
	}

	public static boolean validateComponentNameLength(String componentName) {
		return componentName.length() <= COMPONENT_NAME_MAX_LENGTH;
	}

	public static boolean validateIcon(String icon) {
		return ICON_PATTERN.matcher(icon).matches();
	}

	public static boolean validateIconLength(String icon) {
		return icon.length() <= ICON_MAX_LENGTH;
	}

	public static boolean validateProjectCode(String projectCode) {
		return PROJECT_CODE_PATTERN.matcher(projectCode).matches();
	}

	public static boolean validateProjectCodeLegth(String projectCode) {
		return projectCode.length() <= PROJECT_CODE_MAX_LEGTH;
	}

	public static boolean validateContactId(String contactId) {
		return CONTACT_ID_PATTERN.matcher(contactId).matches();
	}

	public static boolean validateCost(String cost) {
		return COST_PATTERN.matcher(cost).matches();
	}

	public static String removeHtmlTags(String str) {
		return Jsoup.clean(str, Whitelist.none());
	}

	public static String removeAllTags(String htmlText) {

		String stripped = TAGS_PATTERN.matcher(htmlText).replaceAll("").trim();
		return stripped;
	}

	public static String normaliseWhitespace(String str) {
		return StringUtil.normaliseWhitespace(str);
	}

	public static String stripOctets(String str) {
		return OCTET_PATTERN.matcher(str).replaceAll("");
	}

	public static String removeNoneUtf8Chars(String input) {
		return NONE_UTF8_PATTERN.matcher(input).replaceAll("");
	}

	public static boolean validateIsEnglish(String input) {
		return ENGLISH_PATTERN.matcher(input).matches();
	}

	public static boolean validateIsAscii(String input) {

		boolean isAscii = CharMatcher.ASCII.matchesAllOf(input);

		return isAscii;
	}

	public static String convertHtmlTagsToEntities(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}

	public static List<String> removeDuplicateFromList(List<String> list) {
		Set<String> hs = new LinkedHashSet<>(list);
		list.clear();
		list.addAll(hs);
		return list;

	}

	public static boolean validateTagLength(String tag) {
		if (tag != null) {
			return tag.length() <= TAG_MAX_LENGTH;
		}
		return false;
	}

	public static boolean validateTagListLength(int tagListLength) {
		return tagListLength <= TAG_LIST_MAX_LENGTH;
	}

	public static boolean validateDescriptionLength(String description) {
		return description.length() <= COMPONENT_DESCRIPTION_MAX_LENGTH;
	}

	public static boolean validateStringNotEmpty(String value) {
		if ((value == null) || (value.isEmpty())) {
			return false;
		}
		return true;
	}

	public static boolean validateListNotEmpty(List<?> list) {
		if ((list == null) || (list.isEmpty())) {
			return false;
		}
		return true;
	}

	public static boolean validateVendorName(String ventorName) {
		return VENDOR_NAME_PATTERN.matcher(ventorName).matches();
	}

	public static boolean validateVendorNameLength(String ventorName) {
		return ventorName.length() <= VENDOR_NAME_MAX_LENGTH;
	}

	public static boolean validateVendorRelease(String vendorRelease) {
		return VENDOR_RELEASE_PATTERN.matcher(vendorRelease).matches();
	}

	public static boolean validateVendorReleaseLength(String vendorRelease) {
		return vendorRelease.length() <= VENDOR_RELEASE_MAX_LENGTH;
	}

	public static boolean hasBeenCertified(String version) {
		return NumberUtils.toDouble(version) >= 1;
	}

	public static String normaliseComponentName(String name) {
		String[] split = splitComponentName(name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			sb.append(splitElement);
		}
		return sb.toString();

	}

	public static String normalizeComponentInstanceName(String name) {
		String[] split = splitComponentInctanceName(name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			sb.append(splitElement);
		}
		return sb.toString();

	}

	private static String[] splitComponentName(String name) {
		String normalizedName = name.toLowerCase();
		normalizedName = COMPONENT_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
		String[] split = normalizedName.split(" ");
		return split;
	}

	private static String[] splitComponentInctanceName(String name) {
		String normalizedName = name.toLowerCase();
		normalizedName = COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
		String[] split = normalizedName.split(" ");
		return split;
	}

	public static String convertToSystemName(String name) {
		String[] split = splitComponentName(name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			String capitalize = WordUtils.capitalize(splitElement);
			sb.append(capitalize);
		}
		return sb.toString();
	}

	public static String normalizeFileName(String filename) {
		// String[] split = filename.split(Pattern.quote(File.separator));
		// String name = "";
		//
		// name = split[split.length - 1];
		return cleanFileName(filename);

	}

	private static String cleanFileName(String str) {
		str = CLEAN_FILENAME_PATTERN.matcher(str).replaceAll("");
		str = normaliseWhitespace(str);
		str = SPACE_PATTERN.matcher(str).replaceAll("-");
		str = DASH_PATTERN.matcher(str).replaceAll("-");
		str = StringUtils.strip(str, "-_ .");

		return str;
	}

	public static boolean validateUrl(String url) {

		UrlValidator urlValidator = new UrlValidator();
		if (!urlValidator.isValid(url)) {
			return false;
		}
		if (NONE_UTF8_PATTERN.matcher(url).find()) {
			return false;
		}

		if (URL_INVALIDE_PATTERN.matcher(url).find()) {
			return false;
		}
		return true;

	}

	public static String cleanArtifactDisplayName(String strIn) {
		String str = DASH_PATTERN.matcher(strIn).replaceAll("-");
		str = UNDERSCORE_PATTERN.matcher(str).replaceAll("_");
		str = PLUS_PATTERN.matcher(str).replaceAll("+");
		str = normaliseWhitespace(str);
		str = str.trim();
		// str = str.replaceAll(" ", "");

		return str;
	}

	public static String normalizeArtifactLabel(String strIn) {

		String str = DASH_PATTERN.matcher(strIn).replaceAll("");
		str = UNDERSCORE_PATTERN.matcher(str).replaceAll("");
		str = PLUS_PATTERN.matcher(str).replaceAll("");
		str = SPACE_PATTERN.matcher(str).replaceAll("");
		str = DOT_PATTERN.matcher(str).replaceAll("");
		str = str.toLowerCase();

		return str;
	}

	public static boolean validateAdditionalInformationKeyName(String str) {
		return ADDITIONAL_INFORMATION_KEY_PATTERN.matcher(str).matches();
	}

	public static String normalizeAdditionalInformation(String str) {
		if (str != null) {
			str = DASH_PATTERN.matcher(str).replaceAll("-");
			str = UNDERSCORE_PATTERN.matcher(str).replaceAll("_");
			str = normaliseWhitespace(str);
		}
		return str;
	}

	public static boolean validateLength(String str, int length) {
		if (str == null) {
			return true;
		}
		return str.length() <= length;
	}

	public static boolean validateConsumerName(String consumerName) {
		return CONSUMER_NAME_PATTERN.matcher(consumerName).matches();
	}

	public static boolean isUTF8Str(String str) {
		if (NONE_UTF8_PATTERN.matcher(str).find()) {
			return false;
		}
		return true;
	}

	public static boolean validateConsumerPassSalt(String consumerSalt) {
		return CONSUMER_PASS_SALT_PATTERN.matcher(consumerSalt).matches();
	}

	public static boolean isFloatNumber(String number) {
		return FLOAT_PATTERN.matcher(number).matches();
	}

	public static boolean validateCertifiedVersion(String version) {
		return (version != null && CERTIFIED_VERSION_PATTERN.matcher(version).matches());
	}

	public static boolean validateMinorVersion(String version) {
		return (version != null && MINOR_VERSION_PATTERN.matcher(version).matches());
	}

	public static String normaliseProductName(String name) {
		String[] split = splitComponentName(PRODUCT_NAME_DELIMETER_PATTERN, name);
		StringBuffer sb = new StringBuffer();
		for (String splitElement : split) {
			sb.append(splitElement);
		}
		return sb.toString();

	}

	private static String[] splitComponentName(Pattern pattern, String name) {
		String normalizedName = name.toLowerCase();
		normalizedName = pattern.matcher(normalizedName).replaceAll(" ");
		String[] split = normalizedName.split(" ");
		return split;
	}

	public static String removeHtmlTagsOnly(String htmlText) {
		String stripped = HtmlCleaner.stripHtml(htmlText, false);
		return stripped;
	}

}
