/**
 * Copyright © 2016-2017 European Support Limited.
 */

package org.openecomp.core.tools.exportinfo;

import static java.nio.file.Files.createDirectories;
import static org.openecomp.core.tools.commands.CommandName.EXPORT;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.commands.Command;
import org.openecomp.core.tools.commands.CommandName;
import org.openecomp.core.tools.importinfo.ImportProperties;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


public final class ExportDataCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportDataCommand.class);
    private static final String ITEM_ID_OPTION = "i";
    static final String JOIN_DELIMITER = "$#";
    public static final String JOIN_DELIMITER_SPLITTER = "\\$\\#";
    static final String MAP_DELIMITER = "!@";
    public static final String MAP_DELIMITER_SPLITTER = "\\!\\@";
    private static final int THREAD_POOL_SIZE = 6;
    public static final String NULL_REPRESENTATION = "nnuullll";

    public ExportDataCommand() {
        options.addOption(
                Option.builder(ITEM_ID_OPTION).hasArg().argName("item id").desc("id of item to export, mandatory").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);

        if (!cmd.hasOption(ITEM_ID_OPTION) || cmd.getOptionValue(ITEM_ID_OPTION) == null) {
            LOGGER.error("Argument i is mandatory");
            return false;
        }

        ExecutorService executor = null;
        try {
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            Path rootDir = Paths.get(ImportProperties.ROOT_DIRECTORY);
            initDir(rootDir);
            try (Session session = CassandraSessionFactory.getSession()) {
                final Set<String> filteredItems = Sets.newHashSet(cmd.getOptionValue(ITEM_ID_OPTION));
                Set<String> fis =
                        filteredItems.stream().map(fi -> fi.replaceAll("\\r", "")).collect(Collectors.toSet());
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
            Utils.logError(LOGGER, ex);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return EXPORT;
    }

    private static void executeQuery(final Session session, final String query, final Set<String> filteredItems,
            final String filteredColumn, final Set<String> vlms, final CountDownLatch donequerying, Executor executor) {
        ResultSetFuture resultSetFuture = session.executeAsync(query);
        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet resultSet) {
                try {
                    Utils.printMessage(LOGGER, "Start to serialize " + query);
                    new ExportSerializer().serializeResult(resultSet, filteredItems, filteredColumn, vlms);
                    donequerying.countDown();
                } catch (Exception e) {
                    Utils.logError(LOGGER, "Serialization failed :" + query, e);
                    System.exit(-1);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Utils.logError(LOGGER, "Query failed :" + query, t);
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
        Utils.printMessage(LOGGER, "Exported file :" + zipFile);
    }


    public static void initDir(Path rootDir) throws IOException {
        if (rootDir.toFile().exists()) {
            FileUtils.forceDelete(rootDir.toFile());
        }
        createDirectories(rootDir);
    }

}