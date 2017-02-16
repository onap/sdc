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

package org.openecomp.sdc.common.datastructure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Extending this class enforces the objects of implementing classes to have a
 * timestamp, so that like in logstash, we can derive the index name for those
 * object from the timestamp.
 * 
 * @author paharoni
 *
 */
public class ESTimeBasedEvent {

	protected static String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";
	protected String timestamp;
	protected Map<String, Object> fields = new HashMap<String, Object>();

	public ESTimeBasedEvent() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.timestamp = simpleDateFormat.format(new Date());
		fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), this.timestamp);

	}

	public static ESTimeBasedEvent createEventFromJson(String jsonString) throws JSONException {

		ESTimeBasedEvent event = new ESTimeBasedEvent();
		JSONObject gsonObj;
		gsonObj = new JSONObject(jsonString);
		Iterator keys = gsonObj.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();
			event.fields.put(key, gsonObj.get(key));
			if (key.equals(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName())) {
				event.timestamp = (String) gsonObj.get(key);
			}
		}
		return event;
	}

	public String calculateYearIndexSuffix() {
		return timestamp.substring(0, 4);
	}

	public String calculateMonthIndexSuffix() {
		return timestamp.substring(0, 7);
	}

	public String calculateDayIndexSuffix() {
		return timestamp.substring(0, 10);
	}

	public String calculateHourIndexSuffix() {
		return new StringBuilder().append(timestamp.substring(0, 10)).append("-").append(timestamp.substring(11, 13))
				.toString();
	}

	public String calculateMinuteIndexSuffix() {
		return new StringBuilder().append(timestamp.substring(0, 10)).append("-").append(timestamp.substring(11, 13))
				.append("-").append(timestamp.substring(14, 16)).toString();
	}

	protected String getFormattedString(String template, Object... params) {
		String res = null;
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		try {
			formatter.format(template, params);
			res = formatter.toString();
		} finally {
			formatter.close();
		}
		return res;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

}
