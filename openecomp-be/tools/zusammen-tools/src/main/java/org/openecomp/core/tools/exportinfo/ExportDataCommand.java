/**
 * Copyright © 2016-2017 European Support Limited.
 */
package org.openecomp.core.tools.exportinfo;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.apache.commons.io.FileUtils;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.importinfo.ImportProperties;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.nio.file.Files.createDirectories;


public final class ExportDataCommand {
    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);
    public static final String JOIN_DELIMITER = "$#";
    public static final String JOIN_DELIMITER_SPLITTER = "\\$\\#";
    public static final String MAP_DELIMITER = "!@";
    public static final String MAP_DELIMITER_SPLITTER = "\\!\\@";
    public static final int THREAD_POOL_SIZE = 6;

    private ExportDataCommand() {
    }

    public static void exportData(String filterItem) {
        ExecutorService executor = null;
        try {
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            Path rootDir = Paths.get(ImportProperties.ROOT_DIRECTORY);
            initDir(rootDir);
            try (Session session = CassandraSessionFactory.getSession()) {
                final Set<String> filteredItems = Sets.newHashSet(filterItem);
                Set<String> fis = filteredItems.stream().map(fi -> fi.replaceAll("\\r", "")).collect(Collectors.toSet());
                Map<String, List<String>> queries;
                Yaml yaml = new Yaml();
                try (InputStream is = ExportDataCommand.class.getResourceAsStream("/queries.yaml")) {
                    queries = (Map<String, List<String>>) yaml.load(is);
                }
                List<String> queriesList = queries.get("queries");
                List<String> itemsColumns = queries.get("item_columns");
                Set<String> vlms = new HashSet<>();
                CountDownLatch doneQueries = new CountDownLatch(queriesList.size());
                executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                for (int i = 0; i < queriesList.size(); i++) {
                    executeQuery(session, queriesList.get(i), fis, itemsColumns.get(i), vlms, doneQueries, executor);
                }
                doneQueries.await();
                if (!vlms.isEmpty()) {
                    CountDownLatch doneVmls = new CountDownLatch(queriesList.size());
                    for (int i = 0; i < queriesList.size(); i++) {
                        executeQuery(session, queriesList.get(i), vlms, itemsColumns.get(i), null, doneVmls, executor);
                    }

                    doneVmls.await();
                }
            }
            zipPath(rootDir);
            FileUtils.forceDelete(rootDir.toFile());
        } catch (Exception ex) {
            Utils.logError(logger, ex);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }

    }


    private static void executeQuery(final Session session, final String query, final Set<String> filteredItems, final String filteredColumn,
                                        final Set<String> vlms, final CountDownLatch donequerying, Executor executor) {
        ResultSetFuture resultSetFuture = session.executeAsync(query);
        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet resultSet) {
                try {
                    Utils.printMessage(logger, "Start to serialize " + query);
                    new ExportSerializer().serializeResult(resultSet, filteredItems, filteredColumn, vlms);
                    donequerying.countDown();
                } catch (Exception e){
                    Utils.logError(logger, "Serialization failed :" + query, e);
                    System.exit(-1);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Utils.logError(logger, "Query failed :" + query, t);
                System.exit(-1);
            }
        }, executor);
    }

    private static void zipPath(Path rootDir) throws IOException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String dateStr = date.format(formatter);
        dateStr = dateStr.replaceAll(":", "_");
        String zipFile = System.getProperty("user.home") + File.separatorChar + "onboarding_import" + dateStr + ".zip";
        ZipUtils.createZip(zipFile, rootDir);
        Utils.printMessage(logger, "Exported file :" + zipFile);
    }


    public static void initDir(Path rootDir) throws IOException {
        if (rootDir.toFile().exists()) {
            FileUtils.forceDelete(rootDir.toFile());
        }
        createDirectories(rootDir);
    }

}