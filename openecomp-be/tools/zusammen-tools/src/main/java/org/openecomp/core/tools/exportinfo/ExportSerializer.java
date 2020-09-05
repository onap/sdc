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

import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    public void serializeResult(final ResultSet resultSet, final Set<String> filteredItems, final String filteredColumn, Set<String> vlms) {
        try {
            TableData tableData = new TableData();
            tableData.getDefinitions().addAll(resultSet.getColumnDefinitions().asList().stream().map(ColumnDefinition::new).collect(Collectors.toList()));
            String table = tableData.getDefinitions().iterator().next().getTable();
            boolean isElementTable = table.equals(ELEMENT_TABLE_NAME);
            Iterator<Row> iterator = resultSet.iterator();
            iterator.forEachRemaining(row -> {
                if (!filteredItems.contains(row.getString(filteredColumn))) {
                    return;
                }
                List<String> rowData = new ArrayList<>();
                for (int i = 0; i < tableData.getDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableData.getDefinitions().get(i);
                    Name name = dataTypesMap.get(columnDefinition.getType());
                    boolean checkForVLM = isElementTable && columnDefinition.getName().equals(ELEMENT_INFO_COLUMN_NAME);
                    Object data = convertByType(vlms, row, i, name, checkForVLM);
                    rowData.add(data.toString());
                }
                tableData.getRows().add(rowData);
            });
            ObjectMapper objectMapper = new ObjectMapper();
            String fileName = ImportProperties.ROOT_DIRECTORY + File.separator + table + "_" + System.currentTimeMillis() + ".json";
            objectMapper.writeValue(Paths.get(fileName).toFile(), tableData);
            Utils.printMessage(logger, "File exported is :" + fileName);

        } catch (IOException e) {
            Utils.logError(logger, e);
            System.exit(1);
        }
    }

    private Object convertByType(Set<String> vlms, Row row, int i, Name name, boolean checkForVLM) {
        Object data;
        switch (name) {
            case VARCHAR:
            case TEXT:
            case ASCII:
                String string = row.getString(i);
                if (string == null) {
                    string = NULL_REPRESENTATION;
                }
                if (checkForVLM && vlms != null) {
                    String vlm = extractVlm(string);
                    if (vlm != null) {
                        vlms.add(vlm);
                    }
                }
                data = Base64.getEncoder().encodeToString(string.getBytes());
                break;
            case BLOB:
                ByteBuffer bytes = row.getBytes(i);
                if (bytes == null) {
                    bytes = ByteBuffer.wrap("".getBytes());
                }
                data = Base64.getEncoder().encodeToString(bytes.array());
                break;
            case TIMESTAMP:
                Date rowDate = row.getTimestamp(i);
                if (rowDate != null) {
                    data = rowDate.getTime();
                } else {
                    data = "";
                }
                break;
            case BOOLEAN:
                data = row.getBool(i);
                break;
            case COUNTER:
                data = row.getLong(i);
                break;
            case INT:
                data = row.getInt(i);
                break;
            case FLOAT:
                data = row.getFloat(i);
                break;
            case SET:
                Set<Object> set = (Set<Object>) row.getObject(i);
                Object joined = set.stream().map(Object::toString).collect(Collectors.joining(ExportDataCommand.JOIN_DELIMITER));
                data = Base64.getEncoder().encodeToString(joined.toString().getBytes());
                break;
            case MAP:
                Map<Object, Object> map = (Map<Object, Object>) row.getObject(i);
                Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
                Object mapAsString = entrySet.parallelStream().map(entry -> entry.getKey().toString() + ExportDataCommand.MAP_DELIMITER + entry.getValue().toString())
                        .collect(Collectors.joining(ExportDataCommand.JOIN_DELIMITER));
                data = Base64.getEncoder().encodeToString(mapAsString.toString().getBytes());
                break;
            default:
                throw new UnsupportedOperationException("Name is not supported :" + name);
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
