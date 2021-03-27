/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Bell Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.asdctool.impl.validator.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;

public final class Report {

    private final Map<String, Set<String>> failedVerticesPerTask = new HashMap<>();
    private final Map<String, Map<String, VertexResult>> resultsPerVertex = new HashMap<>();

    private Report() {
    }

    public static Report make() {
        return new Report();
    }

    public void addFailure(String taskName, String vertexId) {
        Set<String> failedVertices = get(failedVerticesPerTask, HashSet::new).apply(taskName);
        put(failedVerticesPerTask).apply(taskName, add(failedVertices).apply(vertexId));
    }

    public void addSuccess(String vertexId, String taskName, VertexResult result) {
        Map<String, VertexResult> vertexTasksResults = get(resultsPerVertex, HashMap::new).apply(vertexId);
        put(resultsPerVertex).apply(vertexId, put(vertexTasksResults).apply(taskName, result));
    }

    public void forEachFailure(FailureConsumer c) {
        failedVerticesPerTask.forEach(c::traverse);
    }

    public void forEachSuccess(SuccessConsumer p) {
        resultsPerVertex.forEach((vertex, tasksResults) -> tasksResults.forEach((task, result) -> p.traverse(vertex, task, result)));
    }

    <K, V> Function<K, V> get(Map<K, V> kvs, Supplier<V> fallback) {
        return k -> Optional.ofNullable(kvs.get(k)).orElseGet(fallback);
    }

    <V> Function<V, Set<V>> add(Set<V> vs) {
        return v -> {
            vs.add(v);
            return vs;
        };
    }

    <K, V> BiFunction<K, V, Map<K, V>> put(Map<K, V> kvs) {
        return (k, v) -> {
            kvs.put(k, v);
            return kvs;
        };
    }

    @FunctionalInterface
    public interface FailureConsumer {

        void traverse(String taskName, Set<String> failedVertices);
    }

    @FunctionalInterface
    public interface SuccessConsumer {

        void traverse(String vertex, String task, VertexResult result);
    }
}
