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

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.sdc.gab.model.GABResult;
import org.onap.sdc.gab.model.GABResultEntry;
import org.onap.sdc.gab.model.GABResults;

/**
 * Yaml parser and searcher for GAB. Requires 3 steps:
 *
 * <br>1. Load content of Yaml file using {@link #parseContent(String)} or {@link #parseFile(String)}
 * <br>2. Provide keywords to search using {@link #filter(String)} or {@link #filter(Set)}
 * <br>3. Collect the results using {@link #collect()}
 */
public class GABYamlParser implements AutoCloseable {

    private YamlParser yamlParser;

    public GABYamlParser(YamlParser yamlParser) {
        this.yamlParser = yamlParser;
    }

    /**
     * Provides yaml path for processing.
     *
     * @param path Yaml file path.
     * @return Same parser with loaded source.
     */
    public GABYamlParser parseFile(String path) {
        yamlParser.parseFile(path);
        return this;
    }

    /**
     * Provides yaml content for processing.
     *
     * @param content Yaml file content.
     * @return Same parser with loaded source.
     */
    public GABYamlParser parseContent(String content) {
        yamlParser.parseContent(content);
        return this;
    }

    /**
     * Adds set of filters for processing.
     *
     * @param filters correct json paths for searching resources.
     * @return Same parser with loaded filters.
     */
    public GABYamlParser filter(Set<String> filters) {
        yamlParser.filter(filters);
        return this;
    }

    /**
     * Adds single filter for processing.
     *
     * @param filter correct json path for searching resource.
     * @return Same parser with loaded filter.
     */
    public GABYamlParser filter(String filter) {
        yamlParser.filter(filter);
        return this;
    }

    /**
     * Collects the results from parsed yaml file and applied filters.
     *
     * @exception IOException Means that yaml file has invalid content.
     * @return {@link GABResults}
     */
    public GABResults collect() throws IOException {
        return new GABResults(yamlParser.collect().stream()
            .map(results -> new GABResult(createGabResultEntryList(results)))
            .collect(Collectors.toList()));
    }

    private List<GABResultEntry> createGabResultEntryList(List<SimpleEntry<String, ? extends Collection<Object>>> parsedContent) {
        return Objects.isNull(parsedContent) ? Collections.emptyList() : parsedContent.stream()
                .map(result -> result.getValue().stream()
                    .map(entry -> new GABResultEntry(result.getKey(), entry))
                    .collect(Collectors.toList())).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        yamlParser.close();
    }
}
