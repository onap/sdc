package org.openecomp.sdc.asdctool.enums;

public enum LifeCycleTransitionEnum {

    CHECKOUT("checkout"),
    CHECKIN("checkin"),
    CERTIFICATION_REQUEST("certificationRequest"),
    UNDO_CHECKOUT("undoCheckout"),
    CANCEL_CERTIFICATION("cancelCertification"),
    START_CERTIFICATION("startCertification"),
    FAIL_CERTIFICATION("failCertification"),
    CERTIFY("certify"),
    DISTRIBUTE("distribute");

    String displayName;

    LifeCycleTransitionEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LifeCycleTransitionEnum getFromDisplayName(String name) {
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CHECKOUT.getDisplayName())) {
            return LifeCycleTransitionEnum.CHECKOUT;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CHECKIN.getDisplayName())) {
            return LifeCycleTransitionEnum.CHECKIN;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CERTIFICATION_REQUEST.getDisplayName())) {
            return LifeCycleTransitionEnum.CERTIFICATION_REQUEST;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.UNDO_CHECKOUT.getDisplayName())) {
            return LifeCycleTransitionEnum.UNDO_CHECKOUT;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CANCEL_CERTIFICATION.getDisplayName())) {
            return LifeCycleTransitionEnum.CANCEL_CERTIFICATION;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.START_CERTIFICATION.getDisplayName())) {
            return LifeCycleTransitionEnum.START_CERTIFICATION;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.FAIL_CERTIFICATION.getDisplayName())) {
            return LifeCycleTransitionEnum.FAIL_CERTIFICATION;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CERTIFY.getDisplayName())) {
            return LifeCycleTransitionEnum.CERTIFY;
        }
        if (name.equalsIgnoreCase(LifeCycleTransitionEnum.DISTRIBUTE.getDisplayName())) {
            return LifeCycleTransitionEnum.DISTRIBUTE;
        } else
            throw new IllegalArgumentException(name + " value does not match any of LifeCycleTransitionEnum values");
    }

    public static String valuesAsString() {
        StringBuilder sb = new StringBuilder();
        for (LifeCycleTransitionEnum op : LifeCycleTransitionEnum.values()) {
            sb.append(op.getDisplayName()).append(" ");
        }
        return sb.toString();
    }
}