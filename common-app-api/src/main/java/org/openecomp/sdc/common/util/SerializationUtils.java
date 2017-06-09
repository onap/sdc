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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.nustaq.serialization.FSTConfiguration;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class SerializationUtils {

	private static Logger log = LoggerFactory.getLogger(SerializationUtils.class.getName());

	private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	public static Either<byte[], Boolean> serialize(Object object) {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(object);
			return Either.left(bos.toByteArray());
		} catch (Exception e) {
			log.debug("Failed to serialize object", e);
			return Either.right(false);
		}
	}

	public static Either<Object, Boolean> deserialize(byte[] bytes) {

		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
			return Either.left(in.readObject());
		} catch (Exception e) {
			log.debug("Failed to deserialize object", e);
			return Either.right(false);
		}
	}

	public static Either<byte[], Boolean> serializeExt(Object object) {
		try {
			byte[] value = conf.asByteArray(object);
			return Either.left(value);
		} catch (Exception e) {
			return Either.right(false);
		}
	}

	public static <T> Either<T, Boolean> deserializeExt(byte[] bytes, Class<T> clazz, String componentName) {
		try {
			Object object = conf.asObject(bytes);
			T castObject = clazz.cast(object);
			return Either.left(castObject);
		} catch (Exception e) {
			log.debug("Failed to deserialize object of type {} and uid {}",clazz,componentName, e);
			BeEcompErrorManager.getInstance().logInternalUnexpectedError("DeserializeObjectFromCache", "Failed to deserialize object of type " + clazz, ErrorSeverity.WARNING);
			return Either.right(false);
		}
	}

}
