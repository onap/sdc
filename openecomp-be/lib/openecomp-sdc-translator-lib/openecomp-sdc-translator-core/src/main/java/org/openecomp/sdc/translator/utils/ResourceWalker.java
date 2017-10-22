package org.openecomp.sdc.translator.utils;

import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author EVITALIY.
 * @since 02 Apr 17
 */
public class ResourceWalker {

  public static Map<String, String> readResourcesFromDirectory(String resourceDirectoryToStart)
      throws
      Exception {
    Map<String, String> filesContent = new HashMap<>();
    traverse(resourceDirectoryToStart, (fileName, stream) -> {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        filesContent.put(fileName, IOUtils.toString(reader));
      } catch (IOException exception) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
            LoggerTragetServiceName.READ_RESOURCE_FILE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(),
            LoggerErrorDescription.RESOURCE_FILE_READ_ERROR
                + " File name = " + fileName);
        throw new CoreException((new ErrorCode.ErrorCodeBuilder())
            .withMessage(LoggerErrorDescription.RESOURCE_FILE_READ_ERROR
                + " File name = " + fileName)
            .withId("Resource Read Error").withCategory(ErrorCategory.APPLICATION).build(),
            exception);
      }
    });
    return filesContent;
  }

  private static void traverse(String start, BiConsumer<String, InputStream> handler) throws
      Exception {

    URL url = ResourceWalker.class.getClassLoader().getResource(start);
    if (url == null) {
      throw new FileNotFoundException("Resource not found: " + start);
    }

    switch (url.getProtocol().toLowerCase()) {

      case "file":
        traverseFile(new File(url.getPath()), handler);
        break;
      case "zip":
      case "jar":
        String path = url.getPath();
        int resourcePosition = path.lastIndexOf("!/" + start);
        traverseArchive(path.substring(0, resourcePosition), start, handler);
        break;
      default:
        throw new IllegalArgumentException("Unknown protocol");
    }
  }

  private static void traverseArchive(String file, String resource, BiConsumer<String, InputStream>
      handler)
      throws URISyntaxException, IOException {

    // There is what looks like a bug in Java:
    // if "abc" is a directory in an archive,
    // both "abc" and "abc/" will be found successfully.
    // However, calling isDirectory() will return "true" for "abc/",
    // but "false" for "abc".
    try (ZipFile zip = new ZipFile(new URI(file).getPath())) {

      Predicate<ZipEntry> predicate = buildPredicate(resource);
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        handleZipEntry(predicate, zip, entries.nextElement(), handler);
      }
    }
  }

  private static Predicate<ZipEntry> buildPredicate(String resource) {

    if (resource.endsWith("/")) {
      return zipEntry ->
          zipEntry.getName().startsWith(resource) && !zipEntry.isDirectory();
    } else {
      return zipEntry -> {
        String name = zipEntry.getName();
        return (name.equals(resource) || name.startsWith(resource + "/"))
            && !zipEntry.isDirectory();
      };
    }
  }

  private static void handleZipEntry(Predicate<ZipEntry> predicate, ZipFile zip, ZipEntry zipEntry,
                                     BiConsumer<String, InputStream> handler)
      throws IOException {

    if (predicate.test(zipEntry)) {

      try (InputStream input = zip.getInputStream(zipEntry)) {
        handler.accept(zipEntry.getName(), input);
      }
    }
  }

  private static void traverseFile(File file, BiConsumer<String, InputStream> handler) throws
      IOException {

    if (file.isDirectory()) {
      for (File sub : file.listFiles()) {
        traverseFile(sub, handler);
      }
    } else {
      try (FileInputStream stream = new FileInputStream(file)) {
        handler.accept(file.getPath(), stream);
      }
    }
  }
}
