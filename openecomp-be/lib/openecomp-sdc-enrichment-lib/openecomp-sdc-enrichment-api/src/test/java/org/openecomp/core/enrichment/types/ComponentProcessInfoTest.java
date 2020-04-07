/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.core.enrichment.types;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ComponentProcessInfoTest {

    @Test
    public void verifiedContentValue() throws IOException {
        ComponentProcessInfo componentProcessInfo = new ComponentProcessInfo();
        String contentData = "my test content data";
        componentProcessInfo.setContent(contentData.getBytes());
        InputStream getterContent = componentProcessInfo.getContent();
        Assert.assertEquals(contentData, IOUtils.toString(getterContent, StandardCharsets.UTF_8.name()));

    }
}
