package org.openecomp.sdc.be.exception;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;

public class ComponentExceptionMatcher extends TypeSafeMatcher<ComponentException> {

    private String foundErrorCode;
    private final String expectedErrorCode;

    public static ComponentExceptionMatcher hasStatus(String status) {
        return new ComponentExceptionMatcher(status);
    }

    public ComponentExceptionMatcher(String expectedErrorCode) {
        this.expectedErrorCode = expectedErrorCode;
    }

    @Override
    protected boolean matchesSafely(ComponentException e) {
        foundErrorCode = e.getResponseFormat().getMessageId();
        return expectedErrorCode.equals(foundErrorCode);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(foundErrorCode)
                .appendText(" was found instead of ")
                .appendValue(expectedErrorCode);
    }
}
