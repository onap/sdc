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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.junit.jupiter.api.Test;
import org.openecomp.server.configuration.OnboardingApplicationConfig;
import org.openecomp.server.interceptors.EmptyOutputOutInterceptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class ApplicationContextWiringTest {

    @Test
    void applicationConfigDefinesTheSnapshottedBeans() throws Exception {
        WiringSnapshot.assertMatches(BeanDefinitionSnapshot.render(BeanDefinitionSnapshot.load(OnboardingApplicationConfig.class)),
            "onboarding-application-config.txt");
    }

    @Test
    void restContainerServesOnlyBeansTheConfigDefines() {
        DefaultListableBeanFactory definitions = BeanDefinitionSnapshot.load(OnboardingApplicationConfig.class);
        List<String> requested = new ArrayList<>();
        StaticListableBeanFactory stubs = new StaticListableBeanFactory() {
            @Override
            public Object getBean(String name) {
                requested.add(name);
                return new StubResource();
            }
        };
        Bus bus = BusFactory.newInstance().createBus();
        try {
            Server server = new OnboardingApplicationConfig().restContainer(bus, stubs);

            assertTrue(server.isStarted());
            assertTrue(server.getEndpoint().getOutInterceptors().stream().anyMatch(EmptyOutputOutInterceptor.class::isInstance));
            assertFalse(requested.isEmpty());
            requested.forEach(name -> assertTrue(definitions.containsBeanDefinition(name), name));
            server.destroy();
        } finally {
            bus.shutdown(true);
        }
    }

    @Test
    void jsonProviderOmitsNullFields() throws Exception {
        JacksonJsonProvider provider = new OnboardingApplicationConfig().jsonProvider();
        ByteArrayOutputStream json = new ByteArrayOutputStream();

        provider.writeTo(new Body(), Body.class, Body.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, json);

        assertEquals("{\"present\":\"value\"}", json.toString(StandardCharsets.UTF_8));
    }

    @Path("/stub")
    public static class StubResource {

        @GET
        public String get() {
            return "";
        }
    }

    public static class Body {

        public String present = "value";
        public String absent;
    }
}
