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

package org.openecomp.sdc.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility Class For Actions On Streams
 * 
 * @author mshitrit
 *
 */
public final class StreamUtils {
	private StreamUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Breaks the stream when the predicate is not met.<br>
	 * Does not evaluate elements after the stream breaks.<br>
	 * This method evaluates the stream.<br>
	 * 
	 * @param stream
	 * @param predicate
	 * @return
	 */
	public static <T> Stream<T> takeWhilePlusOneNoEval(Stream<T> stream, Predicate<T> predicate) {
		List<T> results = new ArrayList<>();
		Consumer<T> listAdder = e -> results.add(e);
		stream.map(e -> {
			listAdder.accept(e);
			return e;
		}).filter(e -> !predicate.test(e)).findFirst();
		return results.stream();
	}

	public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
		return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
	}

	public static <T> Stream<T> takeWhilePlusOne(Stream<T> stream, Predicate<T> predicate) {
		return StreamSupport.stream(takeWhile(stream.spliterator(), new StopAfterFailPredicate<T>(predicate)), false);
	}

	private static <T> Spliterator<T> takeWhile(Spliterator<T> splitr, Predicate<T> predicate) {
		return new MySplitIterator<T>(splitr, predicate);
	}

	public static class MySplitIterator<T> extends AbstractSpliterator<T> implements Spliterator<T> {
		boolean stillGoing = true;
		private Spliterator<T> innerItr;
		private Predicate<T> innerPred;

		private MySplitIterator(Spliterator<T> splitItr, Predicate<T> pred) {
			super(splitItr.estimateSize(), 0);
			innerItr = splitItr;
			innerPred = pred;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			boolean canAdvance = true;
			if (stillGoing) {
				stillGoing = innerItr.tryAdvance(createConsumerWrapper(action));
			} else {
				canAdvance = false;
			}
			return canAdvance;
		}

		private Consumer<? super T> createConsumerWrapper(Consumer<? super T> action) {
			Consumer<? super T> cons = new Consumer<T>() {
				@Override
				public void accept(T t) {
					stillGoing = innerPred.test(t);
					if (stillGoing) {
						action.accept(t);
					}

				}
			};

			return cons;
		}

	}

	public static class StopAfterFailPredicate<T> implements Predicate<T> {
		boolean hasNotFailed;
		Predicate<T> innerPredicate;

		private StopAfterFailPredicate(Predicate<T> pred) {
			hasNotFailed = true;
			innerPredicate = pred;
		};

		@Override
		public boolean test(T t) {
			boolean isPassed;
			if (hasNotFailed) {
				isPassed = true;
				hasNotFailed = innerPredicate.test(t);
			} else {
				isPassed = false;
			}
			return isPassed;
		}

	}

}
