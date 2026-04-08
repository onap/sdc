/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 European Support Limited. All rights reserved.
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
package org.openecomp.core.tools.exportinfo;

import static org.openecomp.core.tools.exportinfo.ExportDataCommand.NULL_REPRESENTATION;
import static org.openecomp.core.tools.importinfo.ImportSingleTable.dataTypesMap;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.tools.importinfo.ImportProperties;
import org.openecomp.core.tools.model.ColumnDefinition;
import org.openecomp.core.tools.model.TableData;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ExportSerializer {

    private static final Logger logger = LoggerFactory.getLogger(ExportSerializer.class);
    private static final String ELEMENT_TABLE_NAME = "element";
    private static final String ELEMENT_INFO_COLUMN_NAME = "info";

    public void serializeResult(Iterable<Row> rows, Set<String> filteredItems, String filteredColumn, Set<String> vlms) {
    try {
        TableData tableData = new TableData();

        // Get column definitions from first row
        Iterator<Row> iterator = rows.iterator();
        if (!iterator.hasNext()) {
            Utils.printMessage(logger, "No rows returned.");
            return;
        }

            Row firstRow = iterator.next();
            firstRow.getColumnDefinitions().forEach(c -> 
                tableData.getDefinitions().add(new ColumnDefinition(
                    c.getName().asInternal(),        // column name
                    c.getType().asCql(true, true),  // type as string
                    c.getKeyspace().asInternal(),    // keyspace
                    c.getTable().asInternal()        // table
        ))
);


        boolean isElementTable = tableData.getDefinitions().get(0).getTable().equals(ELEMENT_TABLE_NAME);

        // Process first row
        processRow(firstRow, tableData, filteredItems, filteredColumn, vlms, isElementTable);

        // Process remaining rows
        iterator.forEachRemaining(row -> processRow(row, tableData, filteredItems, filteredColumn, vlms, isElementTable));

        ObjectMapper objectMapper = new ObjectMapper();
        String table = tableData.getDefinitions().get(0).getTable();
        String fileName = ImportProperties.ROOT_DIRECTORY + File.separator + table + "_" + System.currentTimeMillis() + ".json";
        objectMapper.writeValue(Paths.get(fileName).toFile(), tableData);
        Utils.printMessage(logger, "File exported is :" + fileName);

    } catch (IOException e) {
        Utils.logError(logger, e);
        System.exit(1);
    }
}

private void processRow(Row row, TableData tableData, Set<String> filteredItems, String filteredColumn, Set<String> vlms, boolean isElementTable) {
    if (!filteredItems.contains(row.getString(filteredColumn))) return;

    List<String> rowData = new ArrayList<>();
    for (int i = 0; i < tableData.getDefinitions().size(); i++) {
        ColumnDefinition columnDefinition = tableData.getDefinitions().get(i);
        com.datastax.oss.driver.api.core.type.DataType type = row.getColumnDefinitions().get(i).getType();
        boolean checkForVLM = isElementTable && columnDefinition.getName().equals(ELEMENT_INFO_COLUMN_NAME);
        Object data = convertByType(vlms, row, i, type, checkForVLM);
        rowData.add(data.toString());
    }
    tableData.getRows().add(rowData);
}


    private Object convertByType(Set<String> vlms, Row row, int i, com.datastax.oss.driver.api.core.type.DataType dataType, boolean checkForVLM) {
    Object data;
    String typeName = dataType.asCql(true, true); // get CQL representation, e.g., 'text', 'int', 'set<text>'

    switch (typeName.toLowerCase()) {
        case "text":
        case "varchar":
        case "ascii":
            String string = row.getString(i);
            if (string == null) string = NULL_REPRESENTATION;
            if (checkForVLM && vlms != null) {
                String vlm = extractVlm(string);
                if (vlm != null) vlms.add(vlm);
            }
            data = Base64.getEncoder().encodeToString(string.getBytes());
            break;
        case "blob":
            ByteBuffer bytes = row.getByteBuffer(i);
            if (bytes == null) bytes = ByteBuffer.wrap("".getBytes());
            data = Base64.getEncoder().encodeToString(bytes.array());
            break;
        case "timestamp":
            data = row.getInstant(i) != null ? Date.from(row.getInstant(i)).getTime() : "";
            break;
        case "boolean":
            data = row.getBoolean(i);
            break;
        case "counter":
            data = row.getLong(i);
            break;
        case "int":
            data = row.getInt(i);
            break;
        case "float":
            data = row.getFloat(i);
            break;
        default:
            // handle collection types like set<text>, map<text,text>
            if (typeName.startsWith("set<")) {
                Set<?> set = row.getSet(i, Object.class);
                String joined = set.stream().map(Object::toString).collect(Collectors.joining(ExportDataCommand.JOIN_DELIMITER));
                data = Base64.getEncoder().encodeToString(joined.getBytes());
            } else if (typeName.startsWith("map<")) {
                Map<?, ?> map = row.getMap(i, Object.class, Object.class);
                String mapAsString = map.entrySet().stream()
                        .map(entry -> entry.getKey().toString() + ExportDataCommand.MAP_DELIMITER + entry.getValue().toString())
                        .collect(Collectors.joining(ExportDataCommand.JOIN_DELIMITER));
                data = Base64.getEncoder().encodeToString(mapAsString.getBytes());
            } else {
                throw new UnsupportedOperationException("DataType is not supported: " + typeName);
            }
    }
    return data;
}


    protected String extractVlm(String inJson) {
        if (StringUtils.isEmpty(inJson.trim())) {
            return null;
        }
        JsonElement root;
        try {
            root = new JsonParser().parse(inJson);
        } catch (JsonSyntaxException e) {
            Utils.logError(logger, "Failed to parse json " + inJson, e);
            return null;
        }
        if (root == null) {
            return null;
        }
        JsonElement properties = root.getAsJsonObject().get("properties");
        if (properties == null) {
            return null;
        }
        JsonElement vendorId = properties.getAsJsonObject().get("vendorId");
        if (vendorId == null) {
            return null;
        }
        return vendorId.getAsString();
    }
}
