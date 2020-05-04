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

package org.openecomp.sdc.be.components.impl.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

/**
 * This class provides some utility functions to generate close to randomized data for testing purpose.
 */
public class TestDataUtils {

    private TestDataUtils() {
    }

    /**
     * Generates a random {@code Component}
     */
    public static Component aComponent() {
        return oneOf(new Resource(), new Service(), new Product());
    }

    /**
     * Generates a random map of {@code ArtifactDefinition}
     */
    public static Map<String, ArtifactDefinition> someArtifacts(ArtifactDefinition a, ArtifactDefinition... as) {
        return some(ArtifactDataDefinition::getUniqueId, a, as);
    }

    /**
     * Generates a random {@code ArtifactDefinition} provided with a unique id
     */
    public static ArtifactDefinition anArtifactDefinition(String uniqueId) {
        ArtifactDefinition ad = new ArtifactDefinition();
        ad.setUniqueId(uniqueId);
        return ad;
    }

    /**
     * Converts  n {@code ComponentInstance} to {@code List<ComponentInstance>}
     */
    public static List<ComponentInstance> someComponentInstances(ComponentInstance ci, ComponentInstance... cis) {
        return varargs(ci, cis);
    }

    /**
     * Generates a random {@code ComponentInstance} provided with a unique id
     */
    public static ComponentInstance aComponentInstance(String uniqueId) {
        ComponentInstance ci = new ComponentInstance();
        ci.setUniqueId(uniqueId);
        return ci;
    }

    /**
     * Converts  n {@code Operation} to {@code List<Operation>}
     */
    public static Map<String, Operation> someOperations(Operation o, Operation... os) {
        return some(Operation::getUniqueId, o, os);
    }

    /**
     * Generates a random {@code Operation} provided with a unique id and an implementation
     */
    public static Operation anOperation(String uniqueId, ArtifactDefinition implementation) {
        Operation o = new Operation();
        o.setUniqueId(uniqueId);
        o.setImplementation(implementation);
        return o;
    }

    /**
     * Converts  n {@code InterfaceDefinition} to a {@code Map<String, InterfaceDefinition>}
     */
    public static Map<String, InterfaceDefinition> someInterfaces(InterfaceDefinition a, InterfaceDefinition... as) {
        return some(InterfaceDefinition::getUniqueId, a, as);
    }

    /**
     * Generates a {@code Map} given a function to generate a key from a value, along with the values to put in the
     * resulting {@code Map}.
     *
     * @param keyMapper A function to generate a key from a value
     * @param v0        The first element to put in the {@code Map}
     * @param vs        The other elements to put on the {@code Map}
     * @param <K>       The type of a key
     * @param <V>       The type of a value
     */
    @SafeVarargs
    public static <K, V> Map<K, V> some(Function<V, K> keyMapper, V v0, V... vs) {
        HashMap<K, V> m = new HashMap<>();
        for (V v : varargs(v0, vs)) {
            m.put(keyMapper.apply(v), v);
        }
        return m;
    }

    /**
     * Alias for {@code aUniqueId}
     */
    public static String anArtifactId() {
        return alphaNum(10);
    }

    /**
     * Alias for {@code aUniqueId}
     */
    public static String aResourceInstanceId() {
        return alphaNum(10);
    }

    /**
     * Generates a uniqueId
     */
    public static String aUniqueId() {
        return alphaNum(10);
    }

    /**
     * Generates an alphanumeric string
     *
     * @param size The size of the {@code String} returned
     */
    public static String alphaNum(int size) {
        return alphaNum("", size, new Random());
    }

    private static String alphaNum(String acc, int rem, Random r) {
        if (rem == 0) {
            return acc;
        } else {
            String alphaNum = "0123456789abcdefghijklmnopqrstuvwxyz";
            return alphaNum(acc + alphaNum.charAt(r.nextInt(alphaNum.length())), rem - 1, r);
        }
    }

    /**
     * Randomly selects one value among the ones provided
     */
    @SafeVarargs
    public static <A> A oneOf(A a, A... as) {
        List<A> as0 = varargs(a, as);
        Random r = new Random();
        return as0.get(r.nextInt(as0.size()));
    }

    @SafeVarargs
    private static <A> List<A> varargs(A a, A... as) {
        ArrayList<A> as0 = new ArrayList<>(Arrays.asList(as));
        as0.add(a);
        return as0;
    }
}
