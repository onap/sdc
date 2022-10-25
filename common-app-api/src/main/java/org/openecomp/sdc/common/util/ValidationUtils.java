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

import com.google.common.base.CharMatcher;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class ValidationUtils {

    public static final Integer COMPONENT_NAME_MAX_LENGTH = 1024;
    public static final Pattern COMPONENT_NAME_PATTERN = Pattern.compile("^[\\w][\\w \\.\\-\\_\\:\\+]{0," + (COMPONENT_NAME_MAX_LENGTH - 1) + "}$");
    public static final Integer ADDITIONAL_INFORMATION_KEY_MAX_LENGTH = 50;
    public static final Pattern ADDITIONAL_INFORMATION_KEY_PATTERN = Pattern.compile("^[\\w\\s\\.\\-\\_]{1," + COMPONENT_NAME_MAX_LENGTH + "}$");
    public static final Integer RSI_NAME_MAX_LENGTH = 1024;
    public static final Pattern RSI_NAME_PATTERN = Pattern.compile("^[\\w \\s\\.\\-\\_\\:\\+]{1," + RSI_NAME_MAX_LENGTH + "}$");
    public static final Integer COMMENT_MAX_LENGTH = 256;
    public static final Integer ICON_MAX_LENGTH = 25;
    public static final Pattern ICON_PATTERN = Pattern.compile("^[\\w\\-]{1," + ICON_MAX_LENGTH + "}$");
    public static final Integer PROJECT_CODE_MAX_LEGTH = 50;
    public static final Pattern PROJECT_CODE_PATTERN = Pattern.compile("^[\\s\\w_.-]{5,50}$");
    // USER_ID format : aannnX (where a=a-z or A-Z, n=0-9, and X=a-z,A-Z, or 0-9)
    public static final Integer CONNTACT_ID_MAX_LENGTH = 50;
    public static final Pattern CONTACT_ID_PATTERN = Pattern.compile("^[\\s\\w_.-]{1,50}$");
    public static final Pattern OCTET_PATTERN = Pattern.compile("%[a-fA-F0-9]{2}");
    public static final Pattern NONE_UTF8_PATTERN = Pattern.compile("[^\\x00-\\x7F]+");
    public static final Pattern URL_INVALIDE_PATTERN = Pattern.compile("[,#?&@$<>~^`\\\\\\[\\]{}|\")(*!+=;%]+");// ,#?&@$<>~^`\\[]{}|")(*!
    public static final Pattern ENGLISH_PATTERN = Pattern.compile("^[\\p{Graph}\\x20]+$");
    public static final Pattern COMMENT_PATTERN = Pattern.compile("^[\\u0000-\\u00BF]{1,1024}$");
    public static final Pattern SERVICE_METADATA_PATTERN = Pattern
        .compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]{1,256}");
    public static final Integer COMPONENT_DESCRIPTION_MAX_LENGTH = 1024;
    public static final Integer SERVICE_TYPE_MAX_LENGTH = 256;
    public static final Integer SERVICE_ROLE_MAX_LENGTH = 256;
    public static final Integer SERVICE_FUNCTION_MAX_LENGTH = 256;
    public static final Integer SERVICE_NAMING_POLICY_MAX_SIZE = 100;
    public static final Integer TAG_MAX_LENGTH = 1024;
    public static final Integer TAG_LIST_MAX_LENGTH = 1024;
    public static final Integer VENDOR_NAME_MAX_LENGTH = 60;
    public static final Pattern VENDOR_NAME_PATTERN = Pattern
        .compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]+$");
    public static final Integer VENDOR_RELEASE_MAX_LENGTH = 25;
    public static final Pattern VENDOR_RELEASE_PATTERN = Pattern
        .compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]+$");
    public static final Integer RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH = 65;
    public static final Pattern CLEAN_FILENAME_PATTERN = Pattern.compile("[\\x00-\\x1f\\x80-\\x9f\\x5c/<?>\\*:|\"/]+");
    public static final Pattern YANG_MODULE_3GPP_PATTERN = Pattern.compile("^(_3gpp).*$");
    public static final Pattern DASH_PATTERN = Pattern.compile("[-]+");
    public static final Pattern UNDERSCORE_PATTERN = Pattern.compile("[_]+");
    public static final Pattern PLUS_PATTERN = Pattern.compile("[+]+");
    public static final Pattern SPACE_PATTERN = Pattern.compile("[ ]+");
    public static final Pattern AMP_PATTERN = Pattern.compile("[&]+");
    public static final Pattern DOT_PATTERN = Pattern.compile("[\\.]+");
    public static final Pattern APOST_PATTERN = Pattern.compile("[']+");
    public static final Pattern HASHTAG_PATTERN = Pattern.compile("[#]+");
    public static final Pattern EQUAL_PATTERN = Pattern.compile("[=]+");
    public static final Pattern COLON_PATTERN = Pattern.compile("[:]+");
    public static final Pattern AT_PATTERN = Pattern.compile("[@]+");
    public static final Pattern AND_PATTERN = Pattern.compile(" [aA][Nn][Dd] ");
    public static final Pattern COST_PATTERN = Pattern.compile("^[0-9]{1,5}\\.[0-9]{1,3}$");
    public static final Pattern ARTIFACT_LABEL_PATTERN = Pattern.compile("^[a-zA-Z0-9 \\-@+]+$");
    public static final Integer ARTIFACT_LABEL_LENGTH = 255;
    public static final Pattern ARTIFACT_DISPLAY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9 &\\.'#=:@_\\-+]+$");
    public static final Pattern CATEGORY_LABEL_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9 &\\.'#=:@_\\-+]+$");
    public static final Integer CATEGORY_LABEL_MIN_LENGTH = 3;
    public static final Integer CATEGORY_LABEL_MAX_LENGTH = 25;
    public static final Pattern COMPONENT_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_]+");
    public static final Pattern COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-]+");
    public static final Pattern PRODUCT_NAME_DELIMETER_PATTERN = Pattern.compile("[\\.\\-\\_&=#@':\\[\\]\\+]+");
    public static final Integer CONSUMER_NAME_MAX_LENGTH = 255;
    public static final Pattern CONSUMER_NAME_PATTERN = Pattern.compile("^[\\w]+[\\w\\.\\-]*$");
    public static final Integer CONSUMER_SALT_LENGTH = 32;
    public static final Integer CONSUMER_PASSWORD_LENGTH = 64;
    public static final Pattern CONSUMER_PASS_SALT_PATTERN = Pattern.compile("^[a-z0-9]+$");
    public static final Pattern FLOAT_PATTERN = Pattern.compile("^[\\d]+[\\.]{1}[\\d]+$");
    public static final Pattern CERTIFIED_VERSION_PATTERN = Pattern.compile("^[1-9][0-9]*\\.0$");
    public static final Pattern MINOR_VERSION_PATTERN = Pattern.compile("^0\\.[1-9][0-9]*$");
    public static final Pattern TAGS_PATTERN = Pattern.compile("<[^><]*>");
    public static final Pattern TAG_PATTERN = Pattern.compile("^[\\s\\w_.-]{1,1024}$");
    public static final Integer ARTIFACT_NAME_LENGTH = 255;
    public static final Integer API_URL_LENGTH = 100;
    public static final Integer ARTIFACT_DESCRIPTION_MAX_LENGTH = 256;
    public static final Integer PRODUCT_FULL_NAME_MIN_LENGTH = 4;
    public static final Integer PRODUCT_FULL_NAME_MAX_LENGTH = 100;
    public static final Integer FORWARDING_PATH_NAME_MAX_LENGTH = 100;
    public static final Pattern FORWARDING_PATH_NAME_PATTERN = Pattern
        .compile("^[\\w][\\w \\.\\-\\_\\:\\+]{0," + (FORWARDING_PATH_NAME_MAX_LENGTH - 1) + "}$");
    public static final Integer POLICY_MAX_LENGTH = 1024;
    public static final Pattern POLICY_NAME_PATTERN = Pattern.compile("^[\\w][\\w \\.\\-\\_\\:\\+]{0," + (POLICY_MAX_LENGTH - 1) + "}$");
    private static final Set<String> CATEGORY_CONJUNCTIONS = new HashSet<>(Arrays.asList("of", "to", "for", "as", "a", "an", "the"));

    public static final Integer TENANT_NAME_MAX_LENGTH = 60;
    public static final Pattern TENANT_NAME_PATTERN = Pattern
            .compile("^[\\x20-\\x21\\x23-\\x29\\x2B-\\x2E\\x30-\\x39\\x3B\\x3D\\x40-\\x5B\\x5D-\\x7B\\x7D-\\xFF]+$");


    private ValidationUtils() {
    }

    public static boolean validateArtifactLabel(String label) {
        return ARTIFACT_LABEL_PATTERN.matcher(label).matches();
    }

    public static boolean validateArtifactDisplayName(String displayName) {
        return ARTIFACT_DISPLAY_NAME_PATTERN.matcher(displayName).matches();
    }

    public static String cleanUpText(String text) {
        text = removeNoneUtf8Chars(text);
        text = normaliseWhitespace(text);
        text = stripOctets(text);
        text = removeHtmlTagsOnly(text);
        return text;
    }

    public static boolean validateTagPattern(String tag) {
        return TAG_PATTERN.matcher(tag).matches();
    }

    public static boolean validateServiceMetadata(String metadataField) {
        return SERVICE_METADATA_PATTERN.matcher(metadataField).matches();
    }

    public static boolean validateCommentPattern(String comment) {
        return COMMENT_PATTERN.matcher(comment).matches();
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
        return (label != null && label.length() >= CATEGORY_LABEL_MIN_LENGTH && label.length() <= CATEGORY_LABEL_MAX_LENGTH);
    }

    public static boolean validateProductFullNameLength(String fullName) {
        return (fullName != null && fullName.length() >= PRODUCT_FULL_NAME_MIN_LENGTH && fullName.length() <= PRODUCT_FULL_NAME_MAX_LENGTH);
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
        return Jsoup.clean(str, Safelist.none());
    }

    public static String removeAllTags(String htmlText) {
        return TAGS_PATTERN.matcher(htmlText).replaceAll("").trim();
    }

    public static String normaliseWhitespace(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        appendNormalisedWhitespace(sb, str, false);
        return sb.toString();
    }

    private static void appendNormalisedWhitespace(StringBuilder accum, String string, boolean stripLeading) {
        boolean lastWasWhite = false;
        boolean reachedNonWhite = false;
        int len = string.length();
        int c;
        for (int i = 0; i < len; i += Character.charCount(c)) {
            c = string.codePointAt(i);
            if (isWhitespace(c)) {
                if ((stripLeading && !reachedNonWhite) || lastWasWhite) {
                    continue;
                }
                accum.append(' ');
                lastWasWhite = true;
            } else {
                accum.appendCodePoint(c);
                lastWasWhite = false;
                reachedNonWhite = true;
            }
        }
    }

    private static boolean isWhitespace(int c) {
        return c == ' ';
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
        return CharMatcher.ascii().matchesAllOf(input);
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

    public static boolean validateVendorName(String vendorName) {
        return VENDOR_NAME_PATTERN.matcher(vendorName).matches();
    }

    public static boolean validateVendorNameLength(String vendorName) {
        return vendorName.length() <= VENDOR_NAME_MAX_LENGTH;
    }



    public static boolean validateTenantName(String tenant) {
        return TENANT_NAME_PATTERN.matcher(tenant).matches();
    }

    public static boolean validateTenantNameLength(String tenant) {
        return tenant.length() <= TENANT_NAME_MAX_LENGTH;
    }



    public static boolean validateResourceVendorModelNumberLength(String resourceVendorModelNumber) {
        return resourceVendorModelNumber.length() <= RESOURCE_VENDOR_MODEL_NUMBER_MAX_LENGTH;
    }

    public static boolean validateVendorRelease(String vendorRelease) {
        return VENDOR_RELEASE_PATTERN.matcher(vendorRelease).matches();
    }

    public static boolean validateVendorReleaseLength(String vendorRelease) {
        return vendorRelease.length() <= VENDOR_RELEASE_MAX_LENGTH;
    }

    public static boolean validateServiceTypeLength(String serviceType) {
        return serviceType.length() <= SERVICE_TYPE_MAX_LENGTH;
    }

    public static boolean validateServiceRoleLength(String serviceRole) {
        return serviceRole.length() <= SERVICE_ROLE_MAX_LENGTH;
    }

    public static boolean validateServiceFunctionLength(String serviceFunction) {
        return serviceFunction.length() <= SERVICE_FUNCTION_MAX_LENGTH;
    }

    public static boolean validateServiceNamingPolicyLength(String namingPolicy) {
        return namingPolicy.length() <= SERVICE_NAMING_POLICY_MAX_SIZE;
    }

    public static boolean hasBeenCertified(String version) {
        return NumberUtils.toDouble(version) >= 1;
    }

    public static String normaliseComponentName(String name) {
        String[] split = splitComponentName(name);
        StringBuilder sb = new StringBuilder();
        for (String splitElement : split) {
            sb.append(splitElement);
        }
        return sb.toString();
    }

    public static String normalizeComponentInstanceName(String name) {
        String[] split = splitComponentInstanceName(name);
        StringBuilder sb = new StringBuilder();
        for (String splitElement : split) {
            sb.append(splitElement);
        }
        return sb.toString();
    }

    private static String[] splitComponentName(String name) {
        String normalizedName = name.toLowerCase();
        normalizedName = COMPONENT_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
        return normalizedName.split(" ");
    }

    private static String[] splitComponentInstanceName(String name) {
        String normalizedName = name.toLowerCase();
        normalizedName = COMPONENT_INCTANCE_NAME_DELIMETER_PATTERN.matcher(normalizedName).replaceAll(" ");
        return normalizedName.split(" ");
    }

    public static String convertToSystemName(String name) {
        String[] split = splitComponentName(name);
        StringBuilder sb = new StringBuilder();
        for (String splitElement : split) {
            String capitalize = WordUtils.capitalize(splitElement);
            sb.append(capitalize);
        }
        return sb.toString();
    }

    public static String normalizeFileName(String filename) {
        return cleanFileName(filename);
    }

    private static String cleanFileName(String str) {
        str = CLEAN_FILENAME_PATTERN.matcher(str).replaceAll("");
        str = normaliseWhitespace(str);
        str = SPACE_PATTERN.matcher(str).replaceAll("-");
        str = DASH_PATTERN.matcher(str).replaceAll("-");
        if (!YANG_MODULE_3GPP_PATTERN.matcher(str).matches()) {
            str = StringUtils.strip(str, "-_ .");
        }
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

    public static boolean validateCategoryIconNotEmpty(List<String> categoryIcons) {
        return CollectionUtils.isEmpty(categoryIcons);
    }

    public static String normaliseProductName(String name) {
        String[] split = splitComponentName(PRODUCT_NAME_DELIMETER_PATTERN, name);
        StringBuilder sb = new StringBuilder();
        for (String splitElement : split) {
            sb.append(splitElement);
        }
        return sb.toString();
    }

    private static String[] splitComponentName(Pattern pattern, String name) {
        String normalizedName = name.toLowerCase();
        normalizedName = pattern.matcher(normalizedName).replaceAll(" ");
        return normalizedName.split(" ");
    }

    public static String removeHtmlTagsOnly(String htmlText) {
        return HtmlCleaner.stripHtml(htmlText, false);
    }

    public static boolean validateForwardingPathNamePattern(String forwardingPathName) {
        return FORWARDING_PATH_NAME_PATTERN.matcher(forwardingPathName).matches();
    }

    public static String sanitizeInputString(String input) {
        if (StringUtils.isNotEmpty(input)) {
            input = ValidationUtils.removeNoneUtf8Chars(input);
            input = ValidationUtils.removeHtmlTags(input);
            input = ValidationUtils.normaliseWhitespace(input);
            input = ValidationUtils.stripOctets(input);
        }
        return input;
    }
}
