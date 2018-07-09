package org.onap.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
            return locateByResource(resource);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Path locateByResource(String resource) throws URISyntaxException {
        if (resource != null) {
            File file = new File(resource);
            if (file.exists()) {
                return Paths.get(resource);
            }
            Optional<Path> pathForResource = getPathForResourceAmongFiles(resource);
            if (pathForResource.isPresent()) {
                return pathForResource.get();
            }
            pathForResource = getPathForResourceBasedOnProperty(resource, "node.config.location");
            if (pathForResource.isPresent()) {
                return pathForResource.get();
            }
            pathForResource = getPathForResourceBasedOnProperty(resource, "config.location");
            if (pathForResource.isPresent()) {
                return pathForResource.get();
            }
            pathForResource = getPathForResourceAmongUrls(resource);
            if (pathForResource.isPresent()) {
                return pathForResource.get();
            }

        }
        return null;
    }

    private static Optional<Path> getPathForResourceBasedOnProperty(String resource, String configPropertyKey) {
        String configLocationProperty = System.getProperty(configPropertyKey);
        return configLocationProperty != null ? Optional.ofNullable(locate(new File(configLocationProperty), resource)) : Optional.empty();
    }

    private static Optional<Path> getPathForResourceAmongFiles(String resource) {
        Optional<String> filePathForResource = files.stream().filter(file -> file.getAbsolutePath().endsWith(resource) && file.exists()).map(File::getAbsolutePath).findAny();
        return filePathForResource.map(s -> Paths.get(s));
    }

    private static Optional<Path> getPathForResourceAmongUrls(String resource) throws URISyntaxException {
        Optional<URL> urlForResource = urls.stream().filter(url -> url.getFile().endsWith(resource)).findAny();
        return urlForResource.isPresent() ? Optional.of(Paths.get(urlForResource.get().toURI())) : Optional.empty();
    }

    private static Path locate(File root, String resource) {
        if (root.exists()) {
            Collection<File> fileSystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Set<File> configFiles = new HashSet<>(fileSystemResources).stream().filter(file -> !ConfigurationUtils.isConfig(file)).collect(Collectors.toSet());
            configFiles.forEach(NonConfigResource::add);
            return configFiles.stream().filter(file -> file.getAbsolutePath().endsWith(resource)).map(file -> Paths.get(file.getAbsolutePath())).findFirst().orElseGet(null);
        }
        return null;
    }
}
