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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;

import org.apache.commons.lang3.StringEscapeUtils;

public class HtmlCleaner {

	private static Set<String> htmlTags = new HashSet<>();

	private static String patternHtmlFullTagStr = "</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[\\^'\">\\s]+))?)+\\s*|\\s*)/?>";

	private static String patternHtmlTagOnlyStr = "</?(\\w+)[^>]*/?>";

	private static Pattern onlyTagPattern = Pattern.compile(patternHtmlTagOnlyStr);

	private static Pattern fullTagPattern = Pattern.compile(patternHtmlFullTagStr);

	static {
		Tag[] allTags = HTML.getAllTags();
		for (Tag tag : allTags) {
			htmlTags.add(tag.toString().toLowerCase());
		}
	}

	public static String stripHtml(String input) {

		return stripHtml(input, false);

	}

	public static String stripHtml(String input, boolean toEscape) {

		if (input == null || true == input.isEmpty()) {
			return input;
		}

		Matcher matcher = onlyTagPattern.matcher(input);

		Set<String> tagsToRemove = new HashSet<>();

		while (matcher.find()) {

			int start = matcher.start();
			int end = matcher.end();

			String matchTag = input.substring(start, end);

			int groupCount = matcher.groupCount();

			if (groupCount > 0) {
				String tag = matcher.group(1);
				if (tag != null && htmlTags.contains(tag.toLowerCase())) {
					if (false == tagsToRemove.contains(matchTag)) {
						tagsToRemove.add(matchTag);
					}
				}
			}
		}

		String stripHtmlStr = removeTagsFromString(tagsToRemove, input);

		if (stripHtmlStr != null) {
			if (true == toEscape) {
				stripHtmlStr = StringEscapeUtils.escapeHtml4(stripHtmlStr);
			}
		}

		return stripHtmlStr;

	}

	private static String removeTagsFromString(Set<String> tagsToRemove, String input) {

		String stripStr = input;
		if (input == null || true == tagsToRemove.isEmpty()) {
			return input;
		}

		for (String tag : tagsToRemove) {
			stripStr = stripStr.replaceAll(tag, "");
		}
		return stripStr;
	}

}
