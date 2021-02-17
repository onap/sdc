/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.fe.config.Configuration;

public class SerializationUtilsTest {

    @Test
    public void testSerializeAndDeserializeReturnsCorrectObject() {
        final List<String> list = Collections.singletonList("testList");
        Either<byte[], Boolean> serializeResult = SerializationUtils.serialize(list);
        assertTrue(serializeResult.isLeft());
        byte[] serializeList = serializeResult.left().value();
        Either<Object, Boolean> deserializeResult = SerializationUtils.deserialize(serializeList);
        assertTrue(deserializeResult.isLeft());
        List<String> deserializeList = (List<String>) deserializeResult.left().value();
        assertEquals(list, deserializeList);
    }

    @Test
    public void testSerializeReturnsFalseIfObjectIsNotSerializable() {
        final Configuration configuration = new Configuration();
        Either<byte[], Boolean> serializeResult = SerializationUtils.serialize(configuration);
        assertTrue(serializeResult.isRight());
        assertFalse(serializeResult.right().value());
    }

    @Test
    public void testDeserializeReturnsFalseIfObjectIsNotSerializable() {
        String testBytes = "wrongBytesToDeserialize";
        Either<Object, Boolean> serializeResult = SerializationUtils.deserialize(testBytes.getBytes());
        assertTrue(serializeResult.isRight());
        assertFalse(serializeResult.right().value());
    }

    @Test
    public void testSerializeExtAndDeserializeExtReturnsCorrectObject() {
        final List<String> list = Collections.singletonList("testList");
        Either<byte[], Boolean> serializeResult = SerializationUtils.serializeExt(list);
        assertTrue(serializeResult.isLeft());
        byte[] serializeList = serializeResult.left().value();
        Either<List, Boolean> deserializeResult = SerializationUtils.deserializeExt(serializeList, List.class, "testComponent");
        assertTrue(deserializeResult.isLeft());
        List<String> deserializeList = deserializeResult.left().value();
        assertEquals(list, deserializeList);
    }

    @Test
    public void testSerializeExtReturnsFalseIfObjectIsNotSerializable() {
        final Configuration configuration = new Configuration();
        Either<byte[], Boolean> serializeResult = SerializationUtils.serializeExt(configuration);
        assertTrue(serializeResult.isRight());
        assertFalse(serializeResult.right().value());
    }

    @Test
    public void testDeserializeExtReturnsFalseIfObjectIsNotSerializable() {
        String testBytes = "wrongBytesToDeserialize";
        Either<List, Boolean> serializeResult = SerializationUtils.deserializeExt(testBytes.getBytes(), List.class, "testComponent");
        assertTrue(serializeResult.isRight());
        assertFalse(serializeResult.right().value());
    }
}
