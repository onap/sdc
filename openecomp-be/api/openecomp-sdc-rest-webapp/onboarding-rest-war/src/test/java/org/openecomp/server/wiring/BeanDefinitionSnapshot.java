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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Loads the bean definitions a production context would create - XML, component scans, {@code @Configuration}
 * classes and their imports - without instantiating any bean, so no graph database or Cassandra is needed, and
 * renders them one bean per line. Component scans only see {@code target/classes} and dependency jars: the test
 * classpath also holds test classes in production packages, which the deployed WAR does not.
 */
final class BeanDefinitionSnapshot {

    private BeanDefinitionSnapshot() {
    }

    static DefaultListableBeanFactory load(Resource... xml) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        StandardEnvironment environment = new StandardEnvironment();
        MainClassesOnly resourceLoader = new MainClassesOnly();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.setEnvironment(environment);
        reader.setResourceLoader(resourceLoader);
        reader.loadBeanDefinitions(xml);
        ConfigurationClassPostProcessor configurationClasses = new ConfigurationClassPostProcessor();
        configurationClasses.setEnvironment(environment);
        configurationClasses.setResourceLoader(resourceLoader);
        configurationClasses.postProcessBeanDefinitionRegistry(factory);
        return factory;
    }

    static String render(DefaultListableBeanFactory factory) {
        StringBuilder out = new StringBuilder();
        List<String> names = Arrays.asList(factory.getBeanDefinitionNames());
        names.stream().sorted().forEach(name -> {
            BeanDefinition definition = factory.getBeanDefinition(name);
            out.append(name).append(" : ").append(describe(definition)).append('\n');
            Arrays.stream(factory.getAliases(name)).sorted().forEach(alias -> out.append("  alias ").append(alias).append('\n'));
            renderDetails(out, definition, "  ");
        });
        return out.toString();
    }

    private static String describe(BeanDefinition definition) {
        StringBuilder text = new StringBuilder();
        if (definition.getFactoryMethodName() != null) {
            text.append(definition.getFactoryBeanName() != null ? definition.getFactoryBeanName() : definition.getBeanClassName())
                .append('#').append(definition.getFactoryMethodName());
            if (definition instanceof AnnotatedBeanDefinition
                && ((AnnotatedBeanDefinition) definition).getFactoryMethodMetadata() != null) {
                text.append(" -> ").append(((AnnotatedBeanDefinition) definition).getFactoryMethodMetadata().getReturnTypeName());
            }
        } else {
            text.append(definition.getBeanClassName());
        }
        if (!definition.getScope().isEmpty() && !BeanDefinition.SCOPE_SINGLETON.equals(definition.getScope())) {
            text.append(" scope=").append(definition.getScope());
        }
        if (definition.isPrimary()) {
            text.append(" primary");
        }
        if (definition.isLazyInit()) {
            text.append(" lazy");
        }
        if (definition.isAbstract()) {
            text.append(" abstract");
        }
        if (definition.getDependsOn() != null) {
            text.append(" depends-on=").append(String.join(",", definition.getDependsOn()));
        }
        if (definition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractDefinition = (AbstractBeanDefinition) definition;
            if (abstractDefinition.getInitMethodName() != null) {
                text.append(" init-method=").append(abstractDefinition.getInitMethodName());
            }
            if (abstractDefinition.getDestroyMethodName() != null) {
                text.append(" destroy-method=").append(abstractDefinition.getDestroyMethodName());
            }
        }
        return text.toString();
    }

    private static void renderDetails(StringBuilder out, BeanDefinition definition, String indent) {
        Map<Integer, ValueHolder> indexed = new TreeMap<>(definition.getConstructorArgumentValues().getIndexedArgumentValues());
        indexed.forEach((index, holder) -> renderValue(out, indent + "constructor-arg " + index + " =", holder.getValue(), indent));
        definition.getConstructorArgumentValues().getGenericArgumentValues()
            .forEach(holder -> renderValue(out, indent + "constructor-arg =", holder.getValue(), indent));
        for (PropertyValue property : definition.getPropertyValues().getPropertyValues()) {
            renderValue(out, indent + "property " + property.getName() + " =", property.getValue(), indent);
        }
    }

    private static void renderValue(StringBuilder out, String label, Object value, String indent) {
        if (value instanceof RuntimeBeanReference) {
            out.append(label).append(" ref ").append(((RuntimeBeanReference) value).getBeanName()).append('\n');
        } else if (value instanceof TypedStringValue) {
            out.append(label).append(' ').append(((TypedStringValue) value).getValue()).append('\n');
        } else if (value instanceof BeanDefinitionHolder || value instanceof BeanDefinition) {
            BeanDefinition inner = value instanceof BeanDefinitionHolder ? ((BeanDefinitionHolder) value).getBeanDefinition() : (BeanDefinition) value;
            out.append(label).append(" bean ").append(describe(inner)).append('\n');
            renderDetails(out, inner, indent + "    ");
        } else if (value instanceof List) {
            out.append(label).append('\n');
            for (Object element : (List<?>) value) {
                renderValue(out, indent + "  -", element, indent + "  ");
            }
        } else if (value instanceof Map) {
            out.append(label).append('\n');
            Map<String, Object> sorted = new TreeMap<>();
            ((Map<?, ?>) value).forEach((key, entry) -> sorted.put(String.valueOf(key), entry));
            sorted.forEach((key, entry) -> renderValue(out, indent + "  " + key + " =", entry, indent + "  "));
        } else {
            out.append(label).append(' ').append(value).append('\n');
        }
    }

    private static final class MainClassesOnly extends PathMatchingResourcePatternResolver {

        @Override
        public Resource[] getResources(String locationPattern) throws IOException {
            return Arrays.stream(super.getResources(locationPattern))
                .filter(resource -> !resource.getDescription().contains("test-classes"))
                .toArray(Resource[]::new);
        }
    }
}
