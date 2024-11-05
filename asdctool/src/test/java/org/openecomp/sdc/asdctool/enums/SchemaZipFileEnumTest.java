/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchemaZipFileEnumTest {

    @Test
    public void testSchemaZipConstants() throws Exception {
        SchemaZipFileEnum[] schemaFileList = SchemaZipFileEnum.values();
        for (SchemaZipFileEnum schemaZipFileEnum : schemaFileList) {
            switch (schemaZipFileEnum.getFileName()) {
                case "data" :
                    Assertions.assertArrayEquals(schemaZipFileEnum.getImportFileList(),SchemaZipFileEnum.SchemaZipConstants.EMPTY_IMPORT_LIST);
                    break;
                case "relationships" :
                    Assertions.assertArrayEquals(schemaZipFileEnum.getImportFileList(),SchemaZipFileEnum.SchemaZipConstants.RELATIONSHIPS_TYPES_IMPORT_LIST);
                    break;
                default:
                    Assertions.assertArrayEquals(schemaZipFileEnum.getImportFileList(),SchemaZipFileEnum.SchemaZipConstants.DATA_IMPORT_LIST);
                    break;
            }
        }
    }

}
