package org.openecomp.sdc.uici.tests.verificator;

import static org.testng.AssertJUnit.assertTrue;

import java.util.function.Function;
import java.util.function.Supplier;

import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

/**
 * Util Class For Verificators
 * 
 * @author mshitrit
 *
 */
public final class VerificatorUtil {

	private VerificatorUtil() {
		throw new IllegalAccessError();
	}

	public static void verifyWithRetry(Supplier<Boolean> verificator) {
		Function<Boolean, Boolean> retryVerificationLogic = isVerified -> isVerified;
		Boolean isVerifiedAfterRetries = FunctionalInterfaces.retryMethodOnResult(verificator, retryVerificationLogic);
		assertTrue(isVerifiedAfterRetries);
	}
}
