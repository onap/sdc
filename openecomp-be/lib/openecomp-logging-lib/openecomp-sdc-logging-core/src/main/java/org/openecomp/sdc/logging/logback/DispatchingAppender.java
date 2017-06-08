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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.sift.Discriminator;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Allows to use EELF logging configuration almost as is, by using a custom routing function, but pre-configured
 * appenders attached to the standard EELF loggers.</p>
 *
 * <p>Changes that must be made in <i>logback.xml</i> supplied with EELF:</p>
 *
 * <pre>
 *     &lt;appender name="DISPATCHER" class="org.openecomp.sdc.logging.logback.DispatchingAppender"&gt;
 *          &lt;discriminator class="org.openecomp.sdc.logging.logback.EventTypeDiscriminator"/&gt;
 *          &lt;appenderNamePattern&gt;asyncEELF%s&lt;/appenderNamePattern&gt;
 *     &lt;/appender&gt;
 *
 *     &lt;root level="INFO" additivity="false"&gt;
 *          &lt;appender-ref ref="DISPATCHER" /&gt;
 *      &lt;/root&gt;
 * </pre>
 *
 * @author EVITALIY
 * @since 17/08/2016.
 */
public class DispatchingAppender extends AppenderBase<ILoggingEvent> {

    // "magic" appender to indicate a missing appender
    private static final Appender<ILoggingEvent> NO_APPENDER = new DispatchingAppender();

    private Map<String, Appender<ILoggingEvent>> appenders = new ConcurrentHashMap<>();

    private Discriminator<ILoggingEvent> discriminator;
    private String appenderNamePattern;

    @DefaultClass(MDCBasedDiscriminator.class)
    public void setDiscriminator(Discriminator<ILoggingEvent> discriminator) {
        this.discriminator = discriminator;
    }

    public Discriminator<ILoggingEvent> getDiscriminator() {
        return this.discriminator;
    }

    public void setAppenderNamePattern(String pattern) {
        this.appenderNamePattern = pattern;
    }

    public String getAppenderNamePattern() {
        return this.appenderNamePattern;
    }

    @Override
    protected void append(ILoggingEvent event) {

        if (this.isStarted()) {

            String discriminatingValue = this.discriminator.getDiscriminatingValue(event);
            String appenderName = String.format(this.appenderNamePattern, discriminatingValue);
            Appender<ILoggingEvent> appender = this.lookupAppender(appenderName);
            if (appender == NO_APPENDER) {
                this.addError(String.format("Appender %s does not exist", appenderName));
            } else {
                appender.doAppend(event);
            }
        }
    }

    private Appender<ILoggingEvent> lookupAppender(String key) {

        Appender<ILoggingEvent> appender = appenders.get(key);
        if (appender != null) {
            return appender;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger log : context.getLoggerList()) {

            Iterator<Appender<ILoggingEvent>> iterator = log.iteratorForAppenders();
            while (iterator.hasNext()) {

                Appender<ILoggingEvent> element = iterator.next();
                if (key.equals(element.getName())) {
                    this.appenders.putIfAbsent(key, element);
                    return element;
                }
            }
        }

        // to avoid consecutive lookups if the required appender does not exist
        this.appenders.putIfAbsent(key, NO_APPENDER);
        return NO_APPENDER;
    }

    @Override
    public void start() {

        int errors = 0;
        if (this.discriminator == null) {
            this.addError("Missing discriminator. Aborting");
        }

        if (!this.discriminator.isStarted()) {
            this.addError("Discriminator has not started successfully. Aborting");
            ++errors;
        }

        if (this.appenderNamePattern == null) {
            this.addError("Missing name pattern. Aborting");
            ++errors;
        }

        if (errors == 0) {
            super.start();
        }
    }
}
