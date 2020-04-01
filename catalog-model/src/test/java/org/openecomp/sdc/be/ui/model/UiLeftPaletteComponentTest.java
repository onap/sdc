/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.ui.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

public class UiLeftPaletteComponentTest {

    private UiLeftPaletteComponent createTestSubjectService() {
        final Service service = new Service();
        service.addCategory("category", "subCategory");
        service.setTags(new ArrayList<>());
        return new UiLeftPaletteComponent(service);
    }

    private UiLeftPaletteComponent createTestSubjectResource() {
        final Resource resource = new Resource();
        resource.addCategory("category", "subCategory");
        resource.setTags(new ArrayList<>());
        return new UiLeftPaletteComponent(resource);
    }

    @Test
    public void getComponentTypeAsString() {
        final UiLeftPaletteComponent testSubject = createTestSubjectResource();
        assertThat(testSubject).isNotNull().isInstanceOf(UiLeftPaletteComponent.class);
        assertThat(testSubject.getComponentTypeAsString()).isNotNull().isInstanceOf(String.class);
    }

    @Test
    public void getCategoryName() {
        final UiLeftPaletteComponent testSubject = createTestSubjectResource();
        assertThat(testSubject).isNotNull().isInstanceOf(UiLeftPaletteComponent.class);
        assertThat(testSubject.getCategoryName()).isNotNull().isInstanceOf(String.class);
    }

    @Test
    public void getSubcategoryName_whenResource() {
        final UiLeftPaletteComponent testSubject = createTestSubjectResource();
        assertThat(testSubject).isNotNull().isInstanceOf(UiLeftPaletteComponent.class);
        assertThat(testSubject.getSubcategoryName()).isNotNull().isInstanceOf(String.class);
    }

    @Test
    public void getSubcategoryName_whenService() {
        final UiLeftPaletteComponent testSubject = createTestSubjectService();
        assertThat(testSubject).isNotNull().isInstanceOf(UiLeftPaletteComponent.class);
        assertThat(testSubject.getSubcategoryName()).isNull();
    }
}
