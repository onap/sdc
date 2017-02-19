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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class HttpUtil {
	public static Either<String, IOException> readJsonStringFromRequest(HttpServletRequest request) {
		Either<String, IOException> eitherResult;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			ServletInputStream reader = request.getInputStream();
			int value;
			while ((value = reader.read()) != -1) {
				stream.write(value);
			}
			eitherResult = Either.left(new String(stream.toByteArray()));
		} catch (IOException e) {
			eitherResult = Either.right(e);
		}
		return eitherResult;

	}

	/**
	 * Builds an object from a JSON in the POST Body of the request.
	 */
	public static <T> Either<T, Exception> getObjectFromJson(HttpServletRequest request, Class<T> classOfT) {
		Either<T, Exception> eitherResult;
		try {
			Either<String, IOException> eitherReadJson = readJsonStringFromRequest(request);
			if (eitherReadJson.isLeft()) {
				eitherResult = convertJsonStringToObject(eitherReadJson.left().value(), classOfT);
			} else {
				eitherResult = Either.right((Exception) eitherReadJson.right().value());
			}
		} catch (Exception e) {
			eitherResult = Either.right(e);
		}

		return eitherResult;
	}

	public static <T> Either<T, Exception> convertJsonStringToObject(String sentJson, Class<T> classOfT) {
		Either<T, Exception> eitherResult;
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			T object = gson.fromJson(sentJson, classOfT);
			eitherResult = Either.left(object);
		} catch (Exception e) {
			eitherResult = Either.right(e);
		}
		return eitherResult;
	}
}
