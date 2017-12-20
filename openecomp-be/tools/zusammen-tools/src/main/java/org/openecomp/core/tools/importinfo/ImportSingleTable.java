package org.openecomp.core.tools.importinfo;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.model.ColumnDefinition;
import org.openecomp.core.tools.model.TableData;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportSingleTable {

    private static final Logger logger = LoggerFactory.getLogger(ImportSingleTable.class);

    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String VALUES = " VALUES ";
    private static final Map<String, PreparedStatement> statementsCache = new HashMap<>();

    public void importFile(Path file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TableData tableData = objectMapper.readValue(file.toFile(), TableData.class);
            Session session = CassandraSessionFactory.getSession();
            PreparedStatement ps = getPrepareStatement(tableData, session);
            tableData.rows.parallelStream().forEach(row -> executeQuery(session, ps, tableData.definitions, row));
        } catch (IOException e) {
            Utils.logError(logger, e);
        }

    }

    private PreparedStatement getPrepareStatement(TableData tableData, Session session) {
        String query = createQuery(tableData);
        if (statementsCache.containsKey(query)) {
            return statementsCache.get(query);
        }
        PreparedStatement preparedStatement = session.prepare(query);
        statementsCache.put(query, preparedStatement);
        return preparedStatement;
    }

    private void executeQuery(Session session, PreparedStatement ps, List<ColumnDefinition> definitions, List<String> rows) {
        BoundStatement bind = ps.bind();
        for (int i = 0; i < definitions.size(); i++) {
            ColumnDefinition columnDefinition = definitions.get(i);
            String rowData = rows.get(i);
            Name name = dataTypesMap.get(columnDefinition.getType());
            handleByType(bind, i, rowData, name);
        }
        ResultSetFuture resultSetFuture = session.executeAsync(bind);
        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet resultSet) {
                Utils.printMessage(logger, "successful write ");
            }

            @Override
            public void onFailure(Throwable t) {
                Utils.logError(logger, t);
            }
        });
    }

    private void handleByType(BoundStatement bind, int i, String rowData, Name name) {
        switch (name) {
            case VARCHAR:
            case TEXT:
            case ASCII:
                bind.setString(i, new String(Base64.getDecoder().decode(rowData)));
                break;
            case BLOB:
                bind.setBytes(i, ByteBuffer.wrap(Base64.getDecoder().decode(rowData.getBytes())));
                break;
            case TIMESTAMP:
                bind.setDate(i, new Date(Long.parseLong(rowData)));
                break;
            case BOOLEAN:
                bind.setBool(i, Boolean.parseBoolean(rowData));
                break;
            case COUNTER:
                bind.setLong(i, Long.parseLong(rowData));
                break;
            case INT:
                bind.setInt(i, Integer.parseInt(rowData));
                break;
            case FLOAT:
                bind.setFloat(i, Float.parseFloat(rowData));
                break;
            case SET:
                byte[] decoded = Base64.getDecoder().decode(rowData);
                String decodedStr = new String(decoded);
                if (!StringUtils.isEmpty(decodedStr)) {
                    String[] splitted = decodedStr.split(ExportDataCommand.JOIN_DELIMITER_SPILTTER);
                    Set set = Sets.newHashSet(splitted);
                    set.remove("");
                    bind.setSet(i, set);
                } else {
                    bind.setSet(i, null);
                }
                break;
            case MAP:
                byte[] decodedMap = Base64.getDecoder().decode(rowData);
                String mapStr = new String(decodedMap);
                if (!StringUtils.isEmpty(mapStr)) {
                    String[] splittedMap = mapStr.split(ExportDataCommand.JOIN_DELIMITER_SPILTTER);
                    Map<String, String> map = new HashMap<>();
                    for (String keyValue : splittedMap) {
                        String[] split = keyValue.split(ExportDataCommand.MAP_DELIMITER_SPLITTER);
                        map.put(split[0], split[1]);
                    }
                    bind.setMap(i, map);
                } else {
                    bind.setMap(i, null);
                }
                break;
            default:
                throw new UnsupportedOperationException("Name is not supported :" + name);

        }
    }

    private String createQuery(TableData tableData) {
        ColumnDefinition def = tableData.definitions.iterator().next();
        StringBuilder sb = new StringBuilder();
        sb.append(INSERT_INTO).append(def.getKeyspace()).append(".").append(def.getTable());
        sb.append(tableData.definitions.stream().map(definition -> definition.getName()).collect(Collectors.joining(" , ", " ( ", " ) ")));
        sb.append(VALUES).append(tableData.definitions.stream().map(definition -> "?").collect(Collectors.joining(" , ", " ( ", " ) "))).append(";");
        return sb.toString();
    }

    public static final ImmutableMap<String, Name> dataTypesMap;

    static {
        Builder<String, Name> builder = ImmutableMap.builder();
        Name[] values = Name.values();
        for (Name name : values) {
            builder.put(name.name().toLowerCase(), name);
        }
        dataTypesMap = builder.build();
    }

}
