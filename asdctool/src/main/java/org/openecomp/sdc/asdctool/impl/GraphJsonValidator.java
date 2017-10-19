/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.impl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * simple util class to verify that the titan export json graph is not corrupted
 */
public class GraphJsonValidator {

    private static Logger log = LoggerFactory.getLogger(GraphJsonValidator.class.getName());

    public boolean verifyTitanJson(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Integer> invalidRows = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(1);
        Files.lines(Paths.get(filePath)).forEach(line -> {
            try {
                verifyJsonLine(objectMapper, atomicInteger, line);
            } catch (RuntimeException  | IOException e) {
                logInvalidJsonRow(atomicInteger, line, e);
                invalidRows.add(atomicInteger.get());
            }
        });
        return verificationResult(invalidRows);
    }

    private void verifyJsonLine(ObjectMapper objectMapper, AtomicInteger atomicInteger, String line) throws IOException {
        log.info("verifying line: " +  atomicInteger.get());
        objectMapper.readTree(line);
        atomicInteger.incrementAndGet();
    }

    private void logInvalidJsonRow(AtomicInteger atomicInteger, String line, Exception e) {
        log.error("Invalid Json!!!!!!!!!!!!!!!!!!!!", e);
        log.info("line number: " +  atomicInteger.get());
        log.info("line value: " + line);
    }

    private boolean verificationResult(List<Integer> invalidRows) {
        if (!invalidRows.isEmpty()) {
            log.info("the following lines are not valid: " + invalidRows);
            return false;
        }
        return true;
    }

}
