/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.api;

import java.util.List;
import lombok.Data;


@Data
public class VtpTestCase {

    private String scenario;
    private String testCaseName;
    private String testSuiteName;
    private String description;
    private String author;
    private List<VtpTestCaseInput> inputs;
    private List<VtpTestCaseOutput> outputs;

    /**
     * Extends VTP test case content with location where test case is defined. This value is populated by the SDC-BE for consumption by the front end.
     * This allows the front end to tell the back end where to run the test.
     */
    private String endpoint;

}
