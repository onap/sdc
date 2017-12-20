/**
 * Copyright © 2016-2017 European Support Limited.
 */
package org.openecomp.core.tools.exportinfo;

import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.core.tools.importinfo.ImportProperties;
import org.openecomp.core.tools.model.ColumnDefinition;
import org.openecomp.core.tools.model.TableData;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.core.tools.importinfo.ImportSingleTable.dataTypesMap;

public class ExportSerializer {

    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);
    private static final String ELEMENT_TABLE_NAME = "element";
    private static final String ELEMENT_INFO_COLUMN_NAME = "info";

    public void serializeResult(final ResultSet resultSet, final Set<String> filteredItems, final String filteredColumn, Set<String> vlms) {
        try {
            TableData tableData = new TableData();
            tableData.definitions = resultSet.getColumnDefinitions().asList().stream().map(column -> new ColumnDefinition(column)).collect(Collectors.toList());
            String table = tableData.definitions.iterator().next().getTable();
            boolean isElementTable = table.equals(ELEMENT_TABLE_NAME);
            Iterator<Row> iterator = resultSet.iterator();
            iterator.forEachRemaining(row -> {
                if (!filteredItems.contains(row.getString(filteredColumn))) {
                    return;
                }
                List<String> rowData = new ArrayList<>();
                for (int i = 0; i < tableData.definitions.size(); i++) {
                    ColumnDefinition columnDefinition = tableData.definitions.get(i);
                    Name name = dataTypesMap.get(columnDefinition.getType());
                    boolean checkForVLM = isElementTable && columnDefinition.getName().equals(ELEMENT_INFO_COLUMN_NAME);
                    Object data;
                    data = convertByType(vlms, row, i, name, checkForVLM);
                    rowData.add(data.toString());
                }
                tableData.rows.add(rowData);
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
                    string = "";
                }
                if (checkForVLM && vlms != null){
                    String vlm = extractVlm(string);
                    if (vlm!= null) {
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
                data = row.getDate(i).getTime();
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
                Set set = row.getSet(i, Object.class);
                Object joined = set.stream().map(o -> o.toString()).collect(Collectors.joining(ExportDataCommand.JOIN_DELIMITER));
                data = Base64.getEncoder().encodeToString(joined.toString().getBytes());
                break;
            case MAP:
                Map<Object,Object> map = row.getMap(i, Object.class, Object.class);
                Set<Map.Entry<Object,Object>> entrySet = map.entrySet();
                Object mapAsString = entrySet.parallelStream().map(entry -> entry.getKey().toString() + ExportDataCommand.MAP_DELIMITER +entry.getValue().toString())
                        .collect(Collectors.joining(ExportDataCommand.MAP_DELIMITER));
                data = Base64.getEncoder().encodeToString(mapAsString.toString().getBytes());
                break;
            default:
                throw new UnsupportedOperationException("Name is not supported :" + name);
        }
        return data;
    }

    protected String extractVlm(String injson) {
        try {
            if (injson == null){
                return null;
            }
            JsonElement root = new JsonParser().parse(injson);
            if (root == null){
                return null;
            }
            JsonElement properties = root.getAsJsonObject().get("properties");
            if (properties == null){
                return null;
            }
            JsonElement vendorId = properties.getAsJsonObject().get("vendorId");
            if (vendorId == null){
                return null;
            }
            return vendorId.getAsString();
        } catch (Exception ex){
            return null;
        }
    }
}
