package org.openecomp.sdc.be.model.utils;

import org.openecomp.sdc.common.api.Constants;

public class GroupUtils {

	public static boolean isVfModule(String type) {
		return type.equals(Constants.DEFAULT_GROUP_VF_MODULE);
	}
}
