package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.apache.commons.lang.enums.Enum;
import org.openecomp.sdc.asdctool.impl.migration.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MigrationUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(MigrationUtils.class);

    public static boolean handleError(String errorMsg) {
        LOGGER.error(errorMsg);
        return false;
    }

    public static <T> T handleError(T errorStatus, String errorMsg) {
        LOGGER.error(errorMsg);
        return errorStatus;
    }

    public static <A> A willThrowException(String withMsg) {
        throw new MigrationException(withMsg);
    }

}
