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

package org.openecomp.server.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class OnboardingTenantTokenFilterWiringTest {

    private static final File WEB_XML = new File("src/main/webapp/WEB-INF/web.xml");

    @Test
    void tenantFilterGuardsTheTenantAwareEndpoints() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(WEB_XML);
        String filterName = null;
        NodeList filters = doc.getElementsByTagName("filter");
        for (int i = 0; i < filters.getLength(); i++) {
            Element filter = (Element) filters.item(i);
            if (OnboardingTenantTokenFilter.class.getName().equals(text(filter, "filter-class"))) {
                filterName = text(filter, "filter-name");
            }
        }
        assertEquals("TenantTokenFilter", filterName);
        Set<String> patterns = new HashSet<>();
        NodeList mappings = doc.getElementsByTagName("filter-mapping");
        for (int i = 0; i < mappings.getLength(); i++) {
            Element mapping = (Element) mappings.item(i);
            if (filterName.equals(text(mapping, "filter-name"))) {
                NodeList urls = mapping.getElementsByTagName("url-pattern");
                for (int j = 0; j < urls.getLength(); j++) {
                    String raw = urls.item(j).getTextContent();
                    assertEquals(raw.trim(), raw, "url-pattern has surrounding whitespace");
                    patterns.add(raw);
                }
            }
        }
        assertEquals(new HashSet<>(Arrays.asList("/v1.0/vendor-license-models/*", "/v1.0/vendor-software-products",
            "/v1.0/vendor-software-products/", "/v1.0/items/*")), patterns);
        assertFalse(new String(Files.readAllBytes(WEB_XML.toPath())).contains("Keycloak"));
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }
}
