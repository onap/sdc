/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.metrics.impl;

import io.micrometer.core.instrument.binder.jetty.JettyConnectionMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmCompilationMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.openecomp.sdcrests.metrics.api.PrometheusMetricsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController("prometheusMetricsController")
public class PrometheusMetricControllerImpl implements PrometheusMetricsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusMetricControllerImpl.class);

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public PrometheusMetricControllerImpl(final PrometheusMeterRegistry prometheusMeterRegistry) {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
        new ClassLoaderMetrics().bindTo(prometheusMeterRegistry);
        try (final JvmGcMetrics jvmGcMetrics = new JvmGcMetrics()) {
            jvmGcMetrics.bindTo(prometheusMeterRegistry);
        } catch (final Exception e) {
            LOGGER.error("Could not instantiate the JVM GC Metrics", e);
        }
        new JvmMemoryMetrics().bindTo(prometheusMeterRegistry);
        new JvmCompilationMetrics().bindTo(prometheusMeterRegistry);
        new JvmThreadMetrics().bindTo(prometheusMeterRegistry);
        new ProcessorMetrics().bindTo(prometheusMeterRegistry);
        new UptimeMetrics().bindTo(prometheusMeterRegistry);
        new FileDescriptorMetrics().bindTo(prometheusMeterRegistry);
        new JettyConnectionMetrics(prometheusMeterRegistry);
    }

    @Override
    public String prometheusMetrics(final String acceptHeader) {
        return prometheusMeterRegistry.scrape(TextFormat.chooseContentType(acceptHeader));
    }

}
