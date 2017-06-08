package org.openecomp.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The type Non config resource.
 */
public class NonConfigResource {

  private static Set<URL> urls = new HashSet<>();
  private static Set<File> files = new HashSet<>();

  /**
   * Add.
   *
   * @param url the url
   */
  public static void add(URL url) {
    urls.add(url);
  }

  /**
   * Add.
   *
   * @param file the file
   */
  public static void add(File file) {
    files.add(file);
  }

  /**
   * Locate path.
   *
   * @param resource the resource
   * @return the path
   */
  public static Path locate(String resource) {
    try {
      if (resource != null) {
        File file = new File(resource);
        if (file.exists()) {
          return Paths.get(resource);
        }
        for (File availableFile : files) {
          if (availableFile.getAbsolutePath().endsWith(resource) && availableFile.exists()) {
            return Paths.get(availableFile.getAbsolutePath());
          }
        }
        if (System.getProperty("node.config.location") != null) {
          Path path = locate(new File(System.getProperty("node.config.location")), resource);
          if (path != null) {
            return path;
          }
        }
        if (System.getProperty("config.location") != null) {
          Path path = locate(new File(System.getProperty("config.location")), resource);
          if (path != null) {
            return path;
          }
        }
        for (URL url : urls) {
          if (url.getFile().endsWith(resource)) {
            return Paths.get(url.toURI());
          }
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }

  private static Path locate(File root, String resource) {
    if (root.exists()) {
      Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
      Predicate<File> f1 = ConfigurationUtils::isConfig;
      for (File file : filesystemResources) {
        if (!f1.test(file)) {
          add(file);
          if (file.getAbsolutePath().endsWith(resource)) {
            return Paths.get(file.getAbsolutePath());
          }
        }
      }
    }
    return null;
  }
}
