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

package org.openecomp.sdc.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import org.openecomp.sdc.logging.Markers;
import org.slf4j.Marker;

/**
 * Can be used with {@link ch.qos.logback.classic.sift.SiftingAppender} to route events of different types to
 * separate log files. For example,
 *
 * <pre>
 *     &lt;configuration&gt;
 *         &lt;appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender"&gt;
 *             &lt;discriminator class="org.openecomp.sdc.logging.logback.EventTypeDiscriminator"/&gt;
 *             &lt;sift&gt;
 *                  &lt;appender name="{EventType}" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *                      &lt;file&gt;${logDirectory}/${eventType}.log&lt;/file&gt;
 *                      &lt;rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy"&gt;
 *                          &lt;fileNamePattern&gt;${logDirectory}/${eventType}.%i.log.zip&lt;/fileNamePattern&gt;
 *                          &lt;minIndex&gt;1&lt;/minIndex&gt;
 *                          &lt;maxIndex&gt;9&lt;/maxIndex&gt;
 *                      &lt;/rollingPolicy&gt;
 *                      &lt;triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy"&gt;
 *                          &lt;maxFileSize&gt;5MB&lt;/maxFileSize&gt;
 *                      &lt;/triggeringPolicy&gt;
 *                      &lt;encoder&gt;
 *                          &lt;pattern&gt;${defaultPattern}&lt;/pattern&gt;
 *                      &lt;/encoder&gt;
 *                  &lt;/appender&gt;
 *             &lt;/sift&gt;
 *         &lt;/appender&gt;
 *
 *         &lt;root level="INFO"&gt;
 *             &lt;appender-ref ref="SIFT" /&gt;
 *         &lt;/root&gt;
 *     &lt;/configuration&gt;
 * </pre>
 *
 * @author evitaliy
 * @since 21/07/2016.
 */
public class EventTypeDiscriminator extends AbstractDiscriminator<ILoggingEvent> {

    private static final String KEY = "eventType";

    private static final String AUDIT = "Audit";
    private static final String METRICS = "Metrics";
    private static final String ERROR = "Error";
    private static final String DEBUG = "Debug";
    private static final String DEFAULT = DEBUG;

    private static final int MIN_ERROR_LEVEL = Level.WARN_INT;
    private static final int MAX_ERROR_LEVEL = Level.ERROR_INT;
    private static final int DEFAULT_LEVEL = Level.DEBUG_INT;

    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {

        Level level = event.getLevel();
        final int levelInt = level == null ? DEFAULT_LEVEL : level.toInt();
        if ((levelInt > MIN_ERROR_LEVEL - 1) && (levelInt < MAX_ERROR_LEVEL + 1)) {
            return ERROR;
        }

        if (levelInt == Level.DEBUG_INT) {
            return DEBUG;
        }

        /*
         * After DEBUG, ERROR, and WARNING have been filtered out,
         * only TRACE and INFO are left. TRACE is less than DEBUG
         * and therefore cannot be used. So, INFO should be used for
         * custom routing like AUDIT and METRICS
         */
        if (levelInt == Level.INFO_INT) {

            final Marker marker = event.getMarker();
            if (marker != null) {

                if (marker.contains(Markers.AUDIT)) {
                    return AUDIT;
                }

                if (marker.contains(Markers.METRICS)) {
                    return METRICS;
                }
            }

            return ERROR;
        }

        return DEFAULT;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}
