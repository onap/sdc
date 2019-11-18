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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.datastax.driver.mapping.annotations.Transient;

/**
 * Extending this class enforces the objects of implementing classes to have a
 * timestamp, so that like in logstash, we can derive the index name for those
 * object from the timestamp.
 *
 * @author paharoni
 */
public class ESTimeBasedEvent {

    private static final int TIMESTAMP_YEAR_SUBSTRING = 4;
    private static final int TIMESTAMP_MONTH_SUBSTRING = 7;
    private static final int TIMESTAMP_DAY_SUBSTRING = 10;
    private static final int TIMESTAMP_HOURS_START = 11;
    private static final int TIMESTAMP_HOURS_END = 13;
    private static final int TIMESTAMP_MINUTES_START = 14;
    private static final int TIMESTAMP_MINUTES_END = 16;
    @Transient
    protected SimpleDateFormat simpleDateFormat;
    protected static String dateFormatPattern = "yyyy-MM-dd HH:mm:ss.SSS z";
    @Transient
    protected String timestamp;
    @Transient
    protected Map<String, Object> fields = new HashMap<>();

    public ESTimeBasedEvent() {
        simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.timestamp = simpleDateFormat.format(new Date());
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), this.timestamp);

    }

    public static ESTimeBasedEvent createEventFromJson(String jsonString) throws JSONException {

        ESTimeBasedEvent event = new ESTimeBasedEvent();
        JSONObject gsonObj;
        gsonObj = new JSONObject(jsonString);
        Iterator keys = gsonObj.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            event.fields.put(key, gsonObj.get(key));
            if (key.equals(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName())) {
                event.timestamp = (String) gsonObj.get(key);
            }
        }
        return event;
    }

    public String calculateYearIndexSuffix() {
        return timestamp.substring(0, TIMESTAMP_YEAR_SUBSTRING);
    }

    public String calculateMonthIndexSuffix() {
        return timestamp.substring(0, TIMESTAMP_MONTH_SUBSTRING);
    }

    public String calculateDayIndexSuffix() {
        return timestamp.substring(0, TIMESTAMP_DAY_SUBSTRING);
    }

    public String calculateHourIndexSuffix() {
        return calculateBaseIndexSuffix().toString();
    }

    public String calculateMinuteIndexSuffix() {
        return calculateBaseIndexSuffix().append("-").append(timestamp, TIMESTAMP_MINUTES_START, TIMESTAMP_MINUTES_END).toString();
    }

    private StringBuilder calculateBaseIndexSuffix() {
        return new StringBuilder().append(timestamp, 0, TIMESTAMP_DAY_SUBSTRING).append("-").append(timestamp, TIMESTAMP_HOURS_START, TIMESTAMP_HOURS_END);
    }

    protected String getFormattedString(String template, Object... params) {
        String res;
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, Locale.US)) {
            formatter.format(template, params);
            res = formatter.toString();
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
