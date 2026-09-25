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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.FileSystemResource;

class BeansServicesWiringTest {

    private static final FileSystemResource BEANS_SERVICES = new FileSystemResource("src/main/webapp/WEB-INF/beans-services.xml");

    @Test
    void beansServicesDefinesTheSnapshottedBeans() throws Exception {
        WiringSnapshot.assertMatches(BeanDefinitionSnapshot.render(BeanDefinitionSnapshot.load(BEANS_SERVICES)), "beans-services.txt");
    }

    @Test
    void springMapperServletDefinesTheSnapshottedBeans() throws Exception {
        WiringSnapshot.assertMatches(BeanDefinitionSnapshot.render(BeanDefinitionSnapshot.load(
            new FileSystemResource("src/main/webapp/WEB-INF/spring-mapper-servlet.xml"))), "spring-mapper-servlet.txt");
    }

    @Test
    void jsonProviderOmitsNullFields() throws Exception {
        DefaultListableBeanFactory factory = BeanDefinitionSnapshot.load(BEANS_SERVICES);
        JacksonJsonProvider provider = factory.getBean("jsonProvider", JacksonJsonProvider.class);
        ByteArrayOutputStream json = new ByteArrayOutputStream();

        provider.writeTo(new Body(), Body.class, Body.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, json);

        assertEquals("{\"present\":\"value\"}", json.toString(StandardCharsets.UTF_8));
    }

    public static class Body {

        public String present = "value";
        public String absent;
    }
}
