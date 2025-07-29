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

package org.openecomp.sdcrests.externaltesting.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.List;
import org.openecomp.core.externaltesting.api.ClientConfiguration;
import org.openecomp.core.externaltesting.api.RemoteTestingEndpointDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/v1.0/externaltesting")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "External-Testing")})
@Validated

public interface ExternalTesting {

    @GetMapping("/config")
    ResponseEntity getConfig();

    @PutMapping("/config")
    ResponseEntity setConfig(ClientConfiguration config);

    @GetMapping("/testcasetree")
    ResponseEntity getTestCasesAsTree();

    @GetMapping("/endpoints")
    ResponseEntity getEndpoints();

    @PutMapping("/endpoints")
    ResponseEntity setEndpoints(@RequestBody List<RemoteTestingEndpointDefinition> endpoints);

    @GetMapping("/endpoints/{endpointId}/scenarios")
    ResponseEntity getScenarios(@PathVariable("endpointId") String endpointId);

    @GetMapping("/endpoints/{endpointId}/scenarios/{scenario}/testsuites")
    ResponseEntity getTestsuites(@PathVariable("endpointId") String endpointId, @PathVariable("scenario") String scenario);

    @GetMapping("/endpoints/{endpointId}/scenarios/{scenario}/testcases")
    ResponseEntity getTestcases(@PathVariable("endpointId") String endpointId, @PathVariable("scenario") String scenario);

    @GetMapping("/endpoints/{endpointId}/scenarios/{scenario}/testsuites/{testsuite}/testcases/{testcase}")
    ResponseEntity getTestcase(@PathVariable("endpointId") String endpointId, @PathVariable("scenario") String scenario,
                               @PathVariable("testsuite") String testsuite, @PathVariable("testcase") String testcase);

    @PostMapping("/endpoints/{endpointId}/executions/{executionId}")
    ResponseEntity getExecution(@PathVariable("endpointId") String endpointId, @PathVariable("executionId") String executionId);


    @PostMapping(value = "/executions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity execute(@RequestParam("vspId") String vspId, @RequestParam("vspVersionId") String vspVersionId,
                           @RequestParam("requestId") String requestId,
                           @RequestPart(value = "files", required = false) List<MultipartFile> files,
                           @RequestPart(value = "testdata", required = false) String testData);

    @GetMapping("/executions")
    ResponseEntity getValidationResult(@RequestParam("requestId") String requestId,
                                       @RequestParam("endPoint") List<String> endPoints);
}
