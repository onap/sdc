package org.openecomp.sdc.be.components.utils;

import org.apache.tinkerpop.shaded.minlog.Log;

import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern COUNTER_PATTERN = Pattern.compile("\\d+$");
    private static final SecureRandom random = new SecureRandom();


    private Utils() {}

    public static int getNextCounter(@NotNull List<String> existingValues) {
        if (existingValues.isEmpty()) {
            return 0;
        }
        int maxCurrentCounter = 0;
        try {
            maxCurrentCounter = existingValues.stream()
                    .map(COUNTER_PATTERN::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> matcher.group(0))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0);
        }
        catch (Exception e) {
            Log.warn("Failed in retrieivng counter from existing value: ", e);
            maxCurrentCounter = random.nextInt(100) + 50;
        }
        return ++maxCurrentCounter;
    }
}