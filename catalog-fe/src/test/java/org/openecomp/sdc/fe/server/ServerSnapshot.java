/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.fe.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Renders how {@link FeServer} configures Jetty, without starting it. Contexts are rendered in the line format the
 * snapshot of catalog-fe's {@code web.xml} used: declarations sorted by name, while listeners, filter mappings and
 * welcome files keep the order in which the container applies them.
 */
final class ServerSnapshot {

    private ServerSnapshot() {
    }

    static String render(Server server) {
        List<String> lines = new ArrayList<>();
        QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();
        lines.add("thread-pool min=" + threadPool.getMinThreads() + " max=" + threadPool.getMaxThreads() + " idle-timeout="
            + threadPool.getIdleTimeout());
        lines.add("stop-at-shutdown=" + server.getStopAtShutdown() + " stop-timeout=" + server.getStopTimeout());
        for (Connector connector : server.getConnectors()) {
            ServerConnector serverConnector = (ServerConnector) connector;
            HttpConnectionFactory http = serverConnector.getConnectionFactory(HttpConnectionFactory.class);
            HttpConfiguration config = http.getHttpConfiguration();
            lines.add("connector " + String.join("+", serverConnector.getProtocols()) + " port=" + serverConnector.getPort()
                + " idle-timeout=" + serverConnector.getIdleTimeout() + " compliance=" + http.getHttpCompliance());
            lines.add("  send-server-version=" + config.getSendServerVersion() + " send-date-header=" + config.getSendDateHeader()
                + " header-cache-size=" + config.getHeaderCacheSize() + " secure-port=" + config.getSecurePort()
                + " customizers=" + config.getCustomizers().stream().map(customizer -> customizer.getClass().getSimpleName())
                .collect(Collectors.toList()));
        }
        renderHandler(lines, server.getHandler(), "");
        return join(lines);
    }

    private static void renderHandler(List<String> lines, Handler handler, String indent) {
        StringBuilder line = new StringBuilder(indent).append("handler ").append(handler.getClass().getSimpleName());
        if (handler instanceof GzipHandler) {
            GzipHandler gzip = (GzipHandler) handler;
            line.append(" min-gzip-size=").append(gzip.getMinGzipSize())
                .append(" methods=").append(Arrays.toString(gzip.getIncludedMethods()))
                .append(" excluded-mime-types=").append(Arrays.stream(gzip.getExcludedMimeTypes()).sorted().collect(Collectors.toList()))
                .append(" excluded-agents=").append(Arrays.toString(gzip.getExcludedAgentPatterns()));
        } else if (handler instanceof DefaultHandler) {
            line.append(" show-contexts=").append(((DefaultHandler) handler).getShowContexts());
        } else if (handler instanceof ServletContextHandler) {
            line.append(' ').append(((ServletContextHandler) handler).getContextPath());
        }
        lines.add(line.toString());
        if (handler instanceof RewriteHandler) {
            RewriteHandler rewrite = (RewriteHandler) handler;
            lines.add(indent + "  rewrite-request-uri=" + rewrite.isRewriteRequestURI() + " rewrite-path-info=" + rewrite.isRewritePathInfo()
                + " original-path-attribute=" + rewrite.getOriginalPathAttribute());
            for (Rule rule : rewrite.getRules()) {
                lines.add(indent + "  rule " + rule);
            }
        }
        if (handler instanceof ServletContextHandler) {
            return;
        }
        if (handler instanceof HandlerWrapper) {
            renderHandler(lines, ((HandlerWrapper) handler).getHandler(), indent + "  ");
        } else if (handler instanceof HandlerCollection) {
            for (Handler child : ((HandlerCollection) handler).getHandlers()) {
                renderHandler(lines, child, indent + "  ");
            }
        }
    }

    static String render(ServletContextHandler context) {
        List<String> lines = new ArrayList<>();
        lines.add("context-path " + context.getContextPath());
        new TreeMap<>(context.getInitParams()).forEach((name, value) -> lines.add("context-param " + name + " = " + value));
        Arrays.stream(context.getEventListeners()).forEach(listener -> lines.add("listener " + listener.getClass().getName()));
        ServletHandler servletHandler = context.getServletHandler();
        streamOf(servletHandler.getFilters()).sorted(Comparator.comparing(FilterHolder::getName))
            .forEach(filter -> renderHolder(lines, "filter " + filter.getName(), filter, -1));
        for (FilterMapping mapping : streamOf(servletHandler.getFilterMappings()).toArray(FilterMapping[]::new)) {
            lines.add("filter-mapping " + mapping.getFilterName());
            Arrays.stream(mapping.getPathSpecs()).forEach(pathSpec -> lines.add("  url-pattern " + pathSpec));
            EnumSet<DispatcherType> dispatcherTypes = mapping.getDispatcherTypes();
            if (!EnumSet.of(DispatcherType.REQUEST).equals(dispatcherTypes)) {
                dispatcherTypes.forEach(type -> lines.add("  dispatcher " + type));
            }
        }
        Arrays.stream(servletHandler.getServlets()).sorted(Comparator.comparing(ServletHolder::getName))
            .forEach(servlet -> renderHolder(lines, "servlet " + servlet.getName(), servlet, servlet.getInitOrder()));
        Arrays.stream(servletHandler.getServletMappings()).sorted(Comparator.comparing(ServletMapping::getServletName)).forEach(mapping -> {
            lines.add("servlet-mapping " + mapping.getServletName());
            Arrays.stream(mapping.getPathSpecs()).forEach(pathSpec -> lines.add("  url-pattern " + pathSpec));
        });
        Arrays.stream(context.getWelcomeFiles()).forEach(file -> lines.add("welcome-file " + file));
        lines.add("protected-targets " + String.join(" ", context.getProtectedTargets()));
        lines.add("session-timeout " + context.getSessionHandler().getMaxInactiveInterval() / 60 + " min");
        lines.add("error-handler " + context.getErrorHandler().getClass().getSimpleName());
        for (ConstraintMapping mapping : ((ConstraintSecurityHandler) context.getSecurityHandler()).getConstraintMappings()) {
            String methods = mapping.getMethod() != null ? mapping.getMethod() : "all but " + String.join(",", mapping.getMethodOmissions());
            lines.add("constraint " + mapping.getPathSpec() + " " + methods + (mapping.getConstraint().isForbidden() ? " forbidden" : " open"));
        }
        return join(lines);
    }

    private static void renderHolder(List<String> lines, String heading, Holder<?> holder, int loadOnStartup) {
        StringBuilder line = new StringBuilder(heading).append(' ').append(holder.getClassName());
        if (loadOnStartup >= 0) {
            line.append(" load-on-startup=").append(loadOnStartup);
        }
        if (holder.isAsyncSupported()) {
            line.append(" async-supported=true");
        }
        lines.add(line.toString());
        Map<String, String> initParams = new TreeMap<>(holder.getInitParameters());
        initParams.forEach((name, value) -> lines.add("  init-param " + name + " = " + value));
    }

    private static <T> Stream<T> streamOf(T[] array) {
        return array == null ? Stream.empty() : Arrays.stream(array);
    }

    private static String join(List<String> lines) {
        return lines.stream().collect(Collectors.joining("\n", "", "\n"));
    }
}
