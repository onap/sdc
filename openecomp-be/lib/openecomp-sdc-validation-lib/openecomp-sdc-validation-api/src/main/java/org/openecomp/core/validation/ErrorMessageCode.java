package org.openecomp.core.validation;

public final class ErrorMessageCode {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorMessageCode that = (ErrorMessageCode) o;

        return messageCode != null ? messageCode.equals(that.messageCode) : that.messageCode == null;
    }

    @Override
    public int hashCode() {
        return messageCode != null ? messageCode.hashCode() : 0;
    }
}
