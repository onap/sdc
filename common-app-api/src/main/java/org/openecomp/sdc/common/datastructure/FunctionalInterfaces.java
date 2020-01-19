/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.datastructure;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openecomp.sdc.common.log.enums.EcompErrorSeverity;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;

import fj.F;
import fj.data.Either;

/**
 * Class For Functional interfaces And Functional Methods
 * 
 * @author mshitrit
 *
 */
public class FunctionalInterfaces {
	private static final int DEFAULT_REDO_INTERVAL_TIME_MS = 50;
	private static final int DEFAULT_MAX_WAIT_TIME_MS = 10000;
	private static final Logger LOGGER = Logger.getLogger(FunctionalInterfaces.class.getName());

	/**
	 * This is an interface of a List that implements Serializable
	 * 
	 * @author mshitrit
	 *
	 * @param <T>
	 */
	public interface SerializableList<T> extends List<T>, Serializable {
	}

	/**
	 * @author mshitrit Consumer that takes two parameters
	 * @param <T1>
	 * @param <T2>
	 */
	public interface ConsumerTwoParam<T1, T2> {
		/**
		 * Same Accept method, but takes two parameters
		 * 
		 * @param t1
		 * @param t2
		 */
		void accept(T1 t1, T2 t2);
	}

	/**
	 * @author mshitrit Function that takes two parameters
	 * @param <T1>
	 * @param <T2>
	 * @param <R>
	 */
	public interface FunctionTwoParam<T1, T2, R> {
		/**
		 * Same apply method, but takes two parameters
		 * 
		 * @param t1
		 * @param t2
		 * @return
		 */
		R apply(T1 t1, T2 t2);
	}

	/**
	 * @author mshitrit Function that throws an exception
	 * @param <T>
	 * @param <R>
	 * @param <E>
	 */
	public interface FunctionThrows<T, R, E extends Exception> {
		/**
		 * Same apply method, but throws an exception
		 * 
		 * @param t
		 * @return
		 */
		R apply(T t) throws E;
	}

    public interface FunctionTwoParamThrows<T1, T2, R, E extends Exception> {
        /**
         * Same apply method, but throws an exception
         * 
         * @param t1
         * @param t2
         * @return
         */
        R apply(T1 t1, T2 t2) throws E;
    }

	/**
	 * @author mshitrit Supplier that throws an exception
	 * @param <R>
	 * @param <E>
	 */
	public interface SupplierThrows<R, E extends Exception> {
		/**
		 * Same get method, but throws an exception
		 * 
		 * @return
		 * @throws E
		 */
		R get() throws E;
	}

	/**
	 * @author mshitrit Consumer that throws an exception
	 * @param <T>
	 * @param <E>
	 */
	public interface ConsumerThrows<T, E extends Exception> {
		/**
		 * Same accept, but throws an exception
		 * 
		 * @param t
		 * @throws E
		 */
		void accept(T t) throws E;
	}

	/**
	 * @author mshitrit Runnable that throws an exception
	 * @param <E>
	 */
	public interface RunnableThrows<E extends Exception> {
		/**
		 * Same run, but throws an exception
		 * 
		 * @throws E
		 */
		void run() throws E;
	}

	/**
	 * Runs a method that declares throwing an Exception and has a return value.
	 * <br>
	 * In case Exception Occurred replaces it with FunctionalAttException. <br>
	 * This is useful for two cases:<br>
	 * 1.using methods that throws exceptions in streams.<br>
	 * 2.replacing declared exception with undeclared exception (Runtime).<br>
	 * See below Use Case:<br>
	 * Instead of: intList.stream().map(e -> fooThrowsAndReturnsBoolean(e)); -
	 * does not compile !<br>
	 * Use This : intList.stream().map(e -> swallowException( () ->
	 * fooThrowsAndReturnsBoolean(e))); - compiles !<br>
	 * 
	 * @param methodToRun
	 * @return
	 */
	public static <R, E extends Exception> R swallowException(SupplierThrows<R, E> methodToRun) {
		try {
            return methodToRun.get();
		} catch (Exception e) {
			throw new FunctionalAttException(e);
		}
	}

	/**
	 * Runs a method that declares throwing an Exception without return value.
	 * <br>
	 * In case Exception Occurred replaces it with FunctionalAttException. <br>
	 * This is useful for two cases:<br>
	 * 1.using methods that throws exceptions in streams.<br>
	 * 2.replacing declared exception with undeclared exception (Runtime).<br>
	 * See below Use Case:<br>
	 * 
	 * @param methodToRun
	 */
	public static <E extends Exception> void swallowException(RunnableThrows<E> methodToRun) {

		SupplierThrows<Boolean, E> runnableWrapper = () -> {
			methodToRun.run();
			return true;
		};
		swallowException(runnableWrapper);
	}

	private static class FunctionalAttException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private FunctionalAttException(Exception e) {
			super(e);
		}
	}

	/**
	 * Runs the given method.<br>
	 * Verify the method result against the resultVerifier.<br>
	 * If verification passed returns the result.<br>
	 * If Verification failed keeps retrying until it passes or until 10 seconds
	 * pass.<br>
	 * If Exception Occurred keeps retrying until it passes or until 10 seconds
	 * pass,<br>
	 * If last retry result caused an exception - it is thrown.
	 * 
	 * @param methodToRun
	 *            given Method
	 * @param resultVerifier
	 *            verifier for the method result
	 * @return
	 */
	public static <R> R retryMethodOnResult(Supplier<R> methodToRun, Function<R, Boolean> resultVerifier) {
		return retryMethodOnResult(methodToRun, resultVerifier, DEFAULT_MAX_WAIT_TIME_MS,
				DEFAULT_REDO_INTERVAL_TIME_MS);
	}

	/**
	 * Runs the given method.<br>
	 * Verify the method result against the resultVerifier.<br>
	 * If verification passed returns the result.<br>
	 * If Verification failed keeps retrying until it passes or until maxWait
	 * pass.<br>
	 * If Exception Occurred keeps retrying until it passes or until maxWait
	 * pass,<br>
	 * If last retry result caused an exception - it is thrown.
	 * 
	 * @param methodToRun
	 *            given Method
	 * @param resultVerifier
	 *            verifier for the method result
	 * @param maxWaitMS
	 * @param retryIntervalMS
	 * @return
	 */
	public static <R> R retryMethodOnResult(Supplier<R> methodToRun, Function<R, Boolean> resultVerifier,
			long maxWaitMS, long retryIntervalMS) {
		boolean stopSearch = false;
		R ret = null;
		int timeElapsed = 0;
		FunctionalAttException functionalExceotion = null;
		boolean isExceptionInLastTry = false;
		while (!stopSearch) {
			try {
				ret = methodToRun.get();
				stopSearch = resultVerifier.apply(ret);
				isExceptionInLastTry = false;
			} catch (Exception e) {
				functionalExceotion = new FunctionalAttException(e);
				isExceptionInLastTry = true;

			} finally {
				sleep(retryIntervalMS);
				timeElapsed += retryIntervalMS;
				if (timeElapsed > maxWaitMS) {
					stopSearch = true;
				}
			}

		}
		if (isExceptionInLastTry) {
			throw functionalExceotion;
		} else {
			return ret;
		}

	}

	/**
	 * Runs the given method.<br>
	 * Verify the method result against the resultVerifier.<br>
	 * If verification passed returns the result.<br>
	 * If Verification failed keeps retrying until maxRetries reached.<br>
	 * If Exception Occurred keeps retrying until it passes or until maxRetries
	 * reached,<br>
	 * If last retry result caused an exception - it is thrown.
	 * 
	 * @param methodToRun
	 *            given Method
	 * @param resultVerifier
	 *            verifier for the method result
	 * @param maxRetries
	 * @return
	 */
	public static <R> R retryMethodOnResult(Supplier<R> methodToRun, Function<R, Boolean> resultVerifier,
			long maxRetries) {
		boolean stopSearch = false;
		R ret = null;
		int retriesCount = 0;
		FunctionalAttException functionalExceotion = null;
		boolean isExceptionInLastTry = false;
		while (!stopSearch) {
			try {
				ret = methodToRun.get();
				stopSearch = resultVerifier.apply(ret);
				isExceptionInLastTry = false;
			} catch (Exception e) {
				functionalExceotion = new FunctionalAttException(e);
				isExceptionInLastTry = true;
			} finally {
				if (++retriesCount >= maxRetries) {
					stopSearch = true;
				}
			}
		}
		if (isExceptionInLastTry) {
			throw functionalExceotion;
		} else {
			return ret;
		}
	}

    public static <R> R retryMethodOnException(SupplierThrows<R, Exception> methodToRun, 
            Function<Exception, Boolean> exceptionVerifier, long maxRetries) throws Exception {
        boolean stopSearch = false;
        R ret = null;
        int retriesCount = 0;
        Exception exception = null;
        while (!stopSearch) {
            try {
                exception = null;
                ret = methodToRun.get();
                stopSearch = true;
            }
            catch (Exception e) {
                exception = e;
                stopSearch = exceptionVerifier.apply(e);
            }
            finally {
                if (++retriesCount >= maxRetries) {
                    stopSearch = true;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        else {
            return ret;
        }
    }

	/**
	 * Runs the given method.<br>
	 * In case exception occurred runs the method again either until succeed or
	 * until 10 seconds pass.
	 * 
	 * @param methodToRun
	 *            given method
	 * @return
	 */

	public static <R> R retryMethodOnException(Supplier<R> methodToRun) {
		Function<R, Boolean> dummyVerifier = someResult -> true;
		return retryMethodOnResult(methodToRun, dummyVerifier, DEFAULT_MAX_WAIT_TIME_MS, DEFAULT_REDO_INTERVAL_TIME_MS);
	}

	/**
	 * Runs the given method.<br>
	 * In case exception occurred runs the method again either until succeed or
	 * until 10 seconds pass.
	 * 
	 * @param methodToRun
	 *            given method
	 */
	public static void retryMethodOnException(Runnable methodToRun) {
		Function<Boolean, Boolean> dummyVerifier = someResult -> true;
		Supplier<Boolean> dummySupplier = () -> {
			methodToRun.run();
			return true;
		};
		retryMethodOnResult(dummySupplier, dummyVerifier, DEFAULT_MAX_WAIT_TIME_MS, DEFAULT_REDO_INTERVAL_TIME_MS);
	}

	/**
	 * Same as Thread.sleep but throws a FunctionalAttException
	 * (RuntimeException) instead of InterruptedException.<br>
	 * 
	 * @param millis
	 */
	public static void sleep(long millis) {
		swallowException(() -> Thread.sleep(millis));

	}

	/**
	 * Converts Either containing right value to another either with different
	 * type of left value and the same type of right value.
	 * 
	 * @param eitherToConvert
	 * @return
	 */
	public static <T1, T2, T3> Either<T1, T2> convertEitherRight(Either<T3, T2> eitherToConvert) {
		if (eitherToConvert.isLeft()) {
			throw new UnsupportedOperationException("Can not convert either right value because it has left value");
		} else {
			return Either.right(eitherToConvert.right().value());
		}

	}

	/**
	 * Converts Either containing left value to another either with different
	 * type of right value and the same type of left value.
	 * 
	 * @param eitherToConvert
	 * @return
	 */
	public static <T1, T2, T3> Either<T1, T2> convertEitherLeft(Either<T1, T3> eitherToConvert) {
		if (eitherToConvert.isLeft()) {
			throw new UnsupportedOperationException("Can not convert either left value because it has right value");
		} else {
			return Either.left(eitherToConvert.left().value());
		}

	}

	/**
	 * Returns enum value for a field <br>
	 * 
	 * @param fieldValue
	 * @param values
	 * @param enumValueGetter
	 * @return
	 */
	public static <T extends Enum<T>> T getEnumValueByFieldValue(String fieldValue, T[] values,
			Function<T, String> enumValueGetter, T defaultValue) {
		return getEnumValueByFieldValue(fieldValue, values, enumValueGetter, defaultValue, true);

	}
	
	
	public static <T extends Enum<T>> T getEnumValueByFieldValue(String fieldValue, T[] values,
			Function<T, String> enumValueGetter, T defaultValue, boolean isCaseSensetive ){

		final Predicate<? super T> predicate;
		if( isCaseSensetive ){
			predicate = e -> fieldValue.equals(enumValueGetter.apply(e));
		}
		else{
			predicate = e -> fieldValue.equalsIgnoreCase(enumValueGetter.apply(e));
		}
		Optional<T> optionalFound =
				// Stream of values of enum
				Arrays.asList(values).stream().
				// Filter in the one that match the field
						filter(predicate).
						// collect
						findAny();
		T ret;
		ret = optionalFound.isPresent() ? optionalFound.get() : defaultValue;
		return ret;

	}

	/**
	 * This method runs the given method.<br>
	 * In case given method finished running within timeoutInMs limit it returns
	 * Either which left value is the result of the method that ran.<br>
	 * In case given method did not finish running within timeoutInMs limit it
	 * returns Either which right value is false. <br>
	 * 
	 * @param supplier
	 * @param timeoutInMs
	 *            - if 0 or lower no timeout is used
	 * @return
	 */
	public static <T> Either<T, Boolean> runMethodWithTimeOut(Supplier<T> supplier, long timeoutInMs) {
		Either<T, Boolean> result;
		if (timeoutInMs <= NumberUtils.LONG_ZERO) {
			result = Either.left(supplier.get());
		} else {
			ExecutorService pool = Executors.newSingleThreadExecutor();
			Future<T> future = pool.submit(supplier::get);
			try {
				T calcValue = future.get(timeoutInMs, TimeUnit.MILLISECONDS);
				result = Either.left(calcValue);
			} catch (InterruptedException e) {
				LOGGER.debug("InterruptedException in runMethodWithTimeOut", e);
				Thread.currentThread().interrupt();
				result = Either.right(false);
			} catch (ExecutionException | TimeoutException e) {
				LOGGER.debug("method run was canceled because it has passed its time limit of {} MS", timeoutInMs, e);
				result = Either.right(false);
			} finally {
				pool.shutdownNow();
			}
		}
		return result;
	}

	public static <T> F<T, Boolean> convertToFunction(Consumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
                return true;
            } catch (Exception e) {
                return false;
            }
        };
	}
	
	/**
	 * Wraps the execution of the Runnable with try catch.<br>
	 * In case exception occurred returns Optional containing the
	 * resultOnException.<br>
	 * Otherwise returns an Empty optional.
	 * 
	 * @param runnable
	 * @param resultOnException
	 * @return
	 */
	public static <T, E extends Exception> Optional<T> wrapWithTryCatch(RunnableThrows<E> runnable,
			T resultOnException) {
		Optional<T> optionalError;
		try {
			runnable.run();
			optionalError = Optional.empty();
		} catch (Exception e) {
			logException(e);
			optionalError = Optional.of(resultOnException);
		}
		return optionalError;
	}

		/**
	 * Runs the given method.<br>
	 * In case the method passes without any Assertion Errors finishes.<br>
	 * In case there is an assertion error keeps running the method every  retryIntervalMS.<br> until there is no Errors or maxWaitTimeMs has passed. <br>
	 * If there are still Assertion Errors in the last Run they are returned to the user.<br>
	 *
	 * @param methodToRun
	 * @param maxWaitTimeMs
	 * @param retryIntervalMS
	 */
	public  static <T extends Throwable> void retryMethodOnException (Runnable methodToRun, long maxWaitTimeMs, long retryIntervalMS) {
		if (maxWaitTimeMs <= 0) {
			throw new UnsupportedOperationException("Number maxWaitTimeMs be greater than 0");
		}
		StopWatch watch = new StopWatch();
		watch.start();

		boolean isLastTry = false;
		while (!isLastTry) {
			isLastTry = watch.getTime() + retryIntervalMS > maxWaitTimeMs;
			if (isLastTry) {
				methodToRun.run();
			}
			else {
				try {
					methodToRun.run();
					break;
				} catch (Exception e) {
					wrapWithTryCatch(() -> Thread.sleep(retryIntervalMS));
				}
			}
		}


	}

	/**
	 * Wraps the execution of the Runnable with try catch.<br>
	 * In case exception occurred logs the Exception.<br>
	 * resultOnException.<br>
	 * Otherwise returns an Empty optional.
	 * 
	 * @param runnable
	 * @return
	 */
	public static <E extends Exception> void wrapWithTryCatch(RunnableThrows<E> runnable) {
		wrapWithTryCatch(runnable, null);
	}
	
	private static void logException(Exception e) {
		LOGGER.error(EcompErrorSeverity.ERROR, EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, EMPTY, EMPTY, EMPTY, EMPTY);
		LOGGER.debug("Error was caught ", e);
	}

}
