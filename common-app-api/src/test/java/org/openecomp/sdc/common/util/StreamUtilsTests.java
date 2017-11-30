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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import fj.data.Either;

public class StreamUtilsTests {
	@Test
	public void testTakeWhilePredicateNotMet() {
		List<Either<Integer, Boolean>> list = buildListWith10Integers();

		assertTrue(StreamUtils.takeWhile(list.stream(), p -> p.isLeft()).count() == 10);
	}

	@Test
	public void testTakeWhilePredicateIsMet() {
		List<Either<Integer, Boolean>> list = buildListWith10Integers();
		addToBooleansToList(list);

		final Stream<Either<Integer, Boolean>> takeWhileStream = StreamUtils.takeWhile(list.stream(), p -> p.isLeft());
		assertTrue(takeWhileStream.filter(p -> p.isRight()).count() == 0);
	}

	@Test
	public <T> void testTakeErrorEvalOnlyOnce() {
		List<Integer> bucket = new ArrayList<>();
		// API
		Function<Integer, Either<Integer, Boolean>> cons = num -> {
			Either<Integer, Boolean> ret;
			bucket.add(num);
			if (num > 5) {
				ret = Either.right(false);
			} else {
				ret = Either.left(num);
			}
			;
			return ret;
		};

		List<Integer> num1to10 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Stream<Either<Integer, Boolean>> streamEithers = num1to10.stream().map(e -> cons.apply(e));
		List<Either<Integer, Boolean>> collect = StreamUtils.takeWhilePlusOneNoEval(streamEithers, e -> e.isLeft())
				.collect(Collectors.toList());
		assertTrue(bucket.size() <= 6);
		assertTrue(collect.size() <= 6);
		assertTrue(collect.stream().filter(e -> e.isRight()).count() == 1);

	}

	@Test
	public void testTakeWhilePlusOnePredicateNotMet() {
		List<Either<Integer, Boolean>> list = buildListWith10Integers();

		assertTrue(StreamUtils.takeWhilePlusOne(list.stream(), p -> p.isLeft()).count() == 10);
	}

	@Test
	public void testTakeWhilePlusOnePredicateIsMet() {
		List<Either<Integer, Boolean>> list = buildListWith10Integers();
		addToBooleansToList(list);

		final Stream<Either<Integer, Boolean>> takeWhilePlusOneStream = StreamUtils.takeWhilePlusOne(list.stream(),
				p -> p.isLeft());
		assertTrue(takeWhilePlusOneStream.filter(p -> p.isRight()).count() == 1);
	}

	private void addToBooleansToList(List<Either<Integer, Boolean>> list) {
		list.add(Either.right(false));
		list.add(Either.right(false));
	}

	private List<Either<Integer, Boolean>> buildListWith10Integers() {
		List<Either<Integer, Boolean>> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(Either.left(i));
		}
		return list;
	}

	@Test
	public void myTest() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}

		List<Either<Integer, Boolean>> container = new ArrayList<Either<Integer, Boolean>>();
		list.stream().map(e -> myBusinessLogic(e, container)).filter(p -> p.isRight()).findAny();
		// Actual Results are in container
		assertTrue(container.size() == 6);

	}

	private Either<Integer, Boolean> myBusinessLogic(int e, List<Either<Integer, Boolean>> cobtainerList) {
		Either<Integer, Boolean> eitherElement = similuteDBAccess(e);
		// Keep The results in external List
		cobtainerList.add(eitherElement);

		return eitherElement;
	}

	private Either<Integer, Boolean> similuteDBAccess(int e) {
		Either<Integer, Boolean> eitherElement;
		if (e < 5) {
			// DB Success
			eitherElement = Either.left(e);
		} else {
			// DB Fail
			eitherElement = Either.right(true);
		}
		return eitherElement;
	}
}
