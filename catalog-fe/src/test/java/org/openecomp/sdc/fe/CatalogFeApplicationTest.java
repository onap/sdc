package org.openecomp.sdc.fe;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CatalogFeApplicationTest {
    @Test
    void contextLoads() {
        // If the context fails to load, this test will fail automatically
        assertTrue(true, "Spring context should load for CatalogFeApplication");
    }
}
