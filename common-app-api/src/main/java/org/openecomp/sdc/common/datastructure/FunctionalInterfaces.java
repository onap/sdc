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

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
	 
	/**
	 * This is an interface of a List that implements Serializable
	 * @author mshitrit
	 *
	 * @param <T>
	 */
	public interface SerializableList<T> extends List<T> , Serializable {
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
			final R result = methodToRun.get();
			return result;
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
	public static <T1,T2,T3> Either<T1,T2> convertEitherRight(Either<T3,T2> eitherToConvert){
		if( eitherToConvert.isLeft() ){
			throw new UnsupportedOperationException("Can not convert either right value because it has left value");
		}
		else{
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
	public static <T1,T2,T3> Either<T1,T2> convertEitherLeft(Either<T1,T3> eitherToConvert){
		if( eitherToConvert.isLeft() ){
			throw new UnsupportedOperationException("Can not convert either left value because it has right value");
		}
		else{
			return Either.left(eitherToConvert.left().value());
		}
			
	}

}
