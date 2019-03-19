/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab.yaml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.io.IOUtils;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.JsonSurferGson;
import org.yaml.snakeyaml.Yaml;

/**
 * Yaml parser and searcher which requires 3 steps:
 *
 * <br>1. Load content of Yaml file using {@link #parseContent(String)} or {@link #parseFile(String)}
 * <br>2. Provide keywords to search using {@link #filter(String)} or {@link #filter(Set)}
 * <br>3. Collect the results using {@link #collect()}
 */
public class YamlParser implements AutoCloseable {

    private static Logger LOGGER = Logger.getLogger(YamlParser.class.getName());

    private Stream<Object> parsedYamlContent;
    private InputStream inputStream;
    private Set<String> filters;

    public YamlParser() {
        this.parsedYamlContent = Stream.empty();
        filters = new HashSet<>();
    }

    /**
     * Provides yaml path for processing.
     *
     * @param path Yaml file path.
     * @return Same parser with loaded source.
     */
    YamlParser parseFile(String path) {
        filters = new HashSet<>();
        InputStream newInputStream = this.getClass().getClassLoader().getResourceAsStream(path);
        parse(path, newInputStream);
        return this;
    }

    /**
     * Provides yaml content for processing.
     *
     * @param content Yaml file content.
     * @return Same parser with loaded source.
     */
    YamlParser parseContent(String content) {
        filters = new HashSet<>();
        InputStream newInputStream = IOUtils.toInputStream(content);
        parse(content, newInputStream);
        return this;
    }

    /**
     * Adds set of filters for processing.
     *
     * @param filters correct json paths for searching resources.
     * @return Same parser with loaded filters.
     */
    YamlParser filter(Set<String> filters) {
        this.filters.addAll(filters);
        return this;
    }

    /**
     * Adds single filter for processing.
     *
     * @param filter correct json path for searching resource.
     * @return Same parser with loaded filter.
     */
    YamlParser filter(String filter) {
        filters.add(filter);
        return this;
    }

    /**
     * Collects the results from parsed yaml file and applied filters.
     *
     * @exception IOException Means that yaml file has invalid content.
     * @return List of List of simple entry 'key: collection of data'
     */
    List<List<SimpleEntry<String, ? extends Collection<Object>>>> collect() throws IOException {
        try {
            return parsedYamlContent.map(containsKeys).filter(notEmptyListPredicate()).collect(Collectors.toList());
        } catch(Exception e){
            LOGGER.log(Level.WARNING, "Unexpected document content. Please check body of the yaml file.", e);
            throw new IOException("Unexpected document content");
        }
    }

    private void parse(String yaml, InputStream newInputStream) {
        try {
            closeStream(inputStream);
            inputStream = newInputStream;
            if (Objects.isNull(inputStream)) {
                throw new IOException("Empty input stream of yaml content.");
            }
            parsedYamlContent = StreamSupport.stream(new Yaml().loadAll(inputStream).spliterator(), false);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot parse yaml: " + yaml, e);
            parsedYamlContent = Stream.empty();
        }
    }

    private Function<Object, List<SimpleEntry<String, ? extends Collection<Object>>>> containsKeys = parsedYamlSingleDocument -> {
        JsonElement jsonElement = new Gson().toJsonTree(parsedYamlSingleDocument);
        return findInJson(filters, jsonElement);
    };

    private List<SimpleEntry<String, ? extends Collection<Object>>> findInJson(Set<String> keys,
        JsonElement document) {
        return keys.stream()
            .map(getEntryForKeyFunction(document))
            .filter(notEmptyEntryPredicate())
            .collect(Collectors.toList());
    }

    private Predicate<? super List<SimpleEntry<String, ? extends Collection<Object>>>> notEmptyListPredicate() {
        return list -> !list.isEmpty();
    }

    private Predicate<SimpleEntry<String, ? extends Collection<Object>>> notEmptyEntryPredicate() {
        return entry -> !entry.getValue().isEmpty();
    }

    private Function<String, SimpleEntry<String, ? extends Collection<Object>>> getEntryForKeyFunction(
        JsonElement document) {
        return key -> {
            JsonSurfer surfer = JsonSurferGson.INSTANCE;
            try {
                return new SimpleEntry<>(key, surfer.collectAll(document.toString(), "$." + key));
            } catch (ParseCancellationException e) {
                LOGGER.log(Level.WARNING, "Invalid filter key: " + key, e);
                return new SimpleEntry<>(key, Collections.emptyList());
            }
        };
    }

    private void closeStream(InputStream stream) throws IOException {
        if (!Objects.isNull(stream)) {
            stream.close();
        }
    }

    @Override
    public void close() throws IOException {
        closeStream(inputStream);
    }
}
