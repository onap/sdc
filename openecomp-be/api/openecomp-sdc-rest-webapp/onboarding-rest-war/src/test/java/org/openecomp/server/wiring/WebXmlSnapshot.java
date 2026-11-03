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

package org.openecomp.server.wiring;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Renders what a web application registers, independent of how it is written down: in {@code web.xml}, in the
 * {@code META-INF/web-fragment.xml} of a library on the classpath, or programmatically on the {@link ServletContext}
 * from a listener. Fragments are merged as the servlet spec merges them: a {@code web.xml} declaration of the same name
 * wins, and so do its mappings. Declarations are sorted, while listeners, filter mappings and welcome files keep the
 * order in which the container applies them. Multi-valued init-params (Jersey package and provider lists) are split
 * into one value per line.
 */
final class WebXmlSnapshot {

    private final Map<String, String> contextParams = new TreeMap<>();
    private final List<String> listeners = new ArrayList<>();
    private final Map<String, Component> filters = new TreeMap<>();
    private final List<Mapping> filterMappings = new ArrayList<>();
    private final Map<String, Component> servlets = new TreeMap<>();
    private final List<Mapping> servletMappings = new ArrayList<>();
    private final List<String> errorPages = new ArrayList<>();
    private final List<String> welcomeFiles = new ArrayList<>();
    private final Set<String> webXmlMappedFilters = new HashSet<>();
    private final Set<String> webXmlMappedServlets = new HashSet<>();
    private int programmaticFilterMappingsBefore;

    static String render(File webXml) throws Exception {
        return parse(webXml).render();
    }

    /**
     * Renders {@code webXml} together with what {@code registrations} adds to the servlet context.
     */
    static String render(File webXml, Consumer<ServletContext> registrations) throws Exception {
        WebXmlSnapshot snapshot = parse(webXml);
        registrations.accept(snapshot.recordingContext());
        return snapshot.render();
    }

    private static WebXmlSnapshot parse(File webXml) throws Exception {
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(webXml).getDocumentElement();
        WebXmlSnapshot snapshot = new WebXmlSnapshot();
        children(root, "context-param").forEach(param -> snapshot.contextParams.put(text(param, "param-name"), text(param, "param-value")));
        children(root, "listener").forEach(listener -> snapshot.listeners.add(text(listener, "listener-class")));
        snapshot.addDeclarations(root, "");
        for (Element errorPage : children(root, "error-page")) {
            String trigger = children(errorPage, "exception-type").isEmpty() ? text(errorPage, "error-code") : text(errorPage, "exception-type");
            snapshot.errorPages.add(trigger + " -> " + text(errorPage, "location"));
        }
        for (Element welcomeFileList : children(root, "welcome-file-list")) {
            children(welcomeFileList, "welcome-file").forEach(file -> snapshot.welcomeFiles.add(file.getTextContent().trim()));
        }
        Enumeration<URL> fragments = WebXmlSnapshot.class.getClassLoader().getResources("META-INF/web-fragment.xml");
        List<URL> sorted = Collections.list(fragments);
        sorted.sort(Comparator.comparing(URL::toString));
        for (URL fragment : sorted) {
            try (InputStream in = fragment.openStream()) {
                snapshot.addDeclarations(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in).getDocumentElement(), " [web-fragment]");
            }
        }
        return snapshot;
    }

    private void addDeclarations(Element root, String origin) {
        children(root, "filter").forEach(filter -> filters.putIfAbsent(text(filter, "filter-name"), Component.parse(filter, "filter-class", origin)));
        children(root, "filter-mapping").stream().map(mapping -> Mapping.parse(mapping, "filter-name"))
            .filter(mapping -> !webXmlMappedFilters.contains(mapping.name)).forEach(filterMappings::add);
        children(root, "servlet").forEach(servlet -> servlets.putIfAbsent(text(servlet, "servlet-name"), Component.parse(servlet, "servlet-class", origin)));
        children(root, "servlet-mapping").stream().map(mapping -> Mapping.parse(mapping, "servlet-name"))
            .filter(mapping -> !webXmlMappedServlets.contains(mapping.name)).forEach(servletMappings::add);
        if (origin.isEmpty()) {
            filterMappings.forEach(mapping -> webXmlMappedFilters.add(mapping.name));
            servletMappings.forEach(mapping -> webXmlMappedServlets.add(mapping.name));
        }
    }

    private String render() {
        List<String> lines = new ArrayList<>();
        contextParams.forEach((name, value) -> lines.add("context-param " + name + " = " + value));
        listeners.forEach(listener -> lines.add("listener " + listener));
        filters.forEach((name, filter) -> filter.render(lines, "filter " + name));
        filterMappings.forEach(mapping -> mapping.render(lines, "filter-mapping"));
        servlets.forEach((name, servlet) -> servlet.render(lines, "servlet " + name));
        servletMappings.stream().sorted(Comparator.comparing(mapping -> mapping.name)).forEach(mapping -> mapping.render(lines, "servlet-mapping"));
        errorPages.forEach(errorPage -> lines.add("error-page " + errorPage));
        welcomeFiles.forEach(file -> lines.add("welcome-file " + file));
        return lines.stream().collect(Collectors.joining("\n", "", "\n"));
    }

    /**
     * A servlet context that records addServlet/addFilter and their registrations, and fails on any other call, so a
     * registration this snapshot cannot render is not silently dropped. Like the container, it returns null for a name
     * that is already declared.
     */
    private ServletContext recordingContext() {
        return proxy(ServletContext.class, (method, args) -> {
            String name = (String) args[0];
            String className = args[1] instanceof Class ? ((Class<?>) args[1]).getName()
                : args[1] instanceof String ? (String) args[1] : args[1].getClass().getName();
            Component component = new Component(className, "");
            if ("addServlet".equals(method)) {
                return servlets.putIfAbsent(name, component) == null ? servletRegistration(name, component) : null;
            }
            return filters.putIfAbsent(name, component) == null ? filterRegistration(name, component) : null;
        }, "addServlet", "addFilter");
    }

    private ServletRegistration.Dynamic servletRegistration(String name, Component servlet) {
        return proxy(ServletRegistration.Dynamic.class, (method, args) -> {
            switch (method) {
                case "addMapping":
                    Mapping mapping = new Mapping(name);
                    Arrays.stream((String[]) args[0]).forEach(pattern -> mapping.targets.add("url-pattern " + pattern));
                    servletMappings.add(mapping);
                    return null;
                case "setLoadOnStartup":
                    servlet.loadOnStartup = String.valueOf(args[0]);
                    return null;
                default:
                    return servlet.record(method, args);
            }
        }, "addMapping", "setLoadOnStartup", "setAsyncSupported", "setInitParameter");
    }

    private FilterRegistration.Dynamic filterRegistration(String name, Component filter) {
        return proxy(FilterRegistration.Dynamic.class, (method, args) -> {
            if ("addMappingForUrlPatterns".equals(method)) {
                Mapping mapping = new Mapping(name);
                Arrays.stream((String[]) args[2]).forEach(pattern -> mapping.targets.add("url-pattern " + pattern));
                if (args[0] != null) {
                    ((EnumSet<?>) args[0]).forEach(dispatcher -> mapping.targets.add("dispatcher " + ((DispatcherType) dispatcher).name()));
                }
                if ((Boolean) args[1]) {
                    filterMappings.add(mapping);
                } else {
                    filterMappings.add(programmaticFilterMappingsBefore++, mapping);
                }
                return null;
            }
            return filter.record(method, args);
        }, "addMappingForUrlPatterns", "setAsyncSupported", "setInitParameter");
    }

    private interface Call {

        Object handle(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, Call call, String... supported) {
        List<String> methods = Arrays.asList(supported);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (!methods.contains(method.getName())) {
                throw new UnsupportedOperationException(type.getSimpleName() + "." + method.getName() + " is not snapshotted");
            }
            return call.handle(method.getName(), args);
        }));
    }

    private static final class Component {

        private final String className;
        private final String origin;
        private final Map<String, String> initParams = new TreeMap<>();
        private String loadOnStartup;
        private String asyncSupported;

        private Component(String className, String origin) {
            this.className = className;
            this.origin = origin;
        }

        private static Component parse(Element element, String classTag, String origin) {
            Component component = new Component(text(element, classTag), origin);
            if (!children(element, "load-on-startup").isEmpty()) {
                component.loadOnStartup = text(element, "load-on-startup");
            }
            if (!children(element, "async-supported").isEmpty()) {
                component.asyncSupported = text(element, "async-supported");
            }
            children(element, "init-param").forEach(param -> component.initParams.put(text(param, "param-name"), text(param, "param-value")));
            return component;
        }

        private Object record(String method, Object[] args) {
            if ("setAsyncSupported".equals(method)) {
                asyncSupported = String.valueOf(args[0]);
                return null;
            }
            return initParams.putIfAbsent((String) args[0], (String) args[1]) == null;
        }

        private void render(List<String> lines, String heading) {
            StringBuilder line = new StringBuilder(heading).append(' ').append(className);
            if (loadOnStartup != null) {
                line.append(" load-on-startup=").append(loadOnStartup);
            }
            if (asyncSupported != null) {
                line.append(" async-supported=").append(asyncSupported);
            }
            lines.add(line.append(origin).toString());
            initParams.forEach((name, value) -> {
                List<String> values = Arrays.stream(value.split("[,\\s]+")).filter(part -> !part.isEmpty()).collect(Collectors.toList());
                if (values.size() == 1) {
                    lines.add("  init-param " + name + " = " + values.get(0));
                } else {
                    lines.add("  init-param " + name + " =");
                    values.forEach(part -> lines.add("    " + part));
                }
            });
        }
    }

    private static final class Mapping {

        private final String name;
        private final List<String> targets = new ArrayList<>();

        private Mapping(String name) {
            this.name = name;
        }

        private static Mapping parse(Element element, String nameTag) {
            Mapping mapping = new Mapping(text(element, nameTag));
            NodeList nodes = element.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element && !nameTag.equals(node.getNodeName())) {
                    mapping.targets.add(node.getNodeName() + " " + node.getTextContent().trim());
                }
            }
            return mapping;
        }

        private void render(List<String> lines, String heading) {
            lines.add(heading + " " + name);
            targets.forEach(target -> lines.add("  " + target));
        }
    }

    private static List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element && tag.equals(nodes.item(i).getNodeName())) {
                result.add((Element) nodes.item(i));
            }
        }
        return result;
    }

    private static String text(Element parent, String tag) {
        return children(parent, tag).get(0).getTextContent().trim();
    }
}
