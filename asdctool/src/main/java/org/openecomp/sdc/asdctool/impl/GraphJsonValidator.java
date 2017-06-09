package org.openecomp.sdc.asdctool.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
