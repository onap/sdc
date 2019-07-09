/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;

/**
 * This class was generated.
 */
public class MapFilesDataStructureToDtoTest {

    @Test()
    public void testConversion() {

        final FilesDataStructure source = new FilesDataStructure();

        final List<Module> modules = Collections.emptyList();
        source.setModules(modules);

        final List<String> unassigned = Collections.emptyList();
        source.setUnassigned(unassigned);

        final List<String> artifacts = Collections.emptyList();
        source.setArtifacts(artifacts);

        final List<String> nested = Collections.emptyList();
        source.setNested(nested);

        final FileDataStructureDto target = new FileDataStructureDto();

        final MapFilesDataStructureToDto mapper = new MapFilesDataStructureToDto();
        mapper.doMapping(source, target);

        assertSame(modules, target.getModules());
        assertSame(unassigned, target.getUnassigned());
        assertSame(artifacts, target.getArtifacts());
        assertSame(nested, target.getNested());
    }
}
