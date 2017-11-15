package org.openecomp.core.validation;

public class ErrorMessageCode {
    private final String messageCode;

    public ErrorMessageCode(String messageCode) {

        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    @Override
    public String toString() {
        return messageCode;
    }
}
