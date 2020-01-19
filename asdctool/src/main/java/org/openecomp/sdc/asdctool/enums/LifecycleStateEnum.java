package org.openecomp.sdc.asdctool.enums;

public enum LifecycleStateEnum {

    READY_FOR_CERTIFICATION,

    CERTIFICATION_IN_PROGRESS,

    CERTIFIED,

    NOT_CERTIFIED_CHECKIN,

    NOT_CERTIFIED_CHECKOUT;

    public static LifecycleStateEnum findState(String state) {

        for (LifecycleStateEnum lifecycleStateEnum : LifecycleStateEnum.values()) {
            if (lifecycleStateEnum.name().equals(state)) {
                return lifecycleStateEnum;
            }
        }
        return null;
    }
}