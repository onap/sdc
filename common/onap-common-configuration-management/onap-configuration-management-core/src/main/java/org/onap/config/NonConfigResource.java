package org.onap.config;

import com.google.common.collect.Maps;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.vavr.API.Option;

/**
 * The type Non config resource.
 */
public class NonConfigResource {

    private final Map<String, String> systemProperties;
    private Set<URL> urls;
    private Set<Path> files;

    private NonConfigResource(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
        this.files = HashSet.empty();
        this.urls = HashSet.empty();
    }

    public static NonConfigResource create() {
        HashMap<String, String> systemProperties = HashMap.ofAll(Maps.fromProperties(System.getProperties()));
        return new NonConfigResource(systemProperties);
    }

    public static NonConfigResource create(Map<String, String> systemProperties) {
        return new NonConfigResource(systemProperties);
    }

    /**
     * Add.
     *
     * @param url the url
     */
    public void add(URL url) {
        urls = urls.add(url);
    }

    /**
     * Add.
     *
     * @param file the file
     */
    public void add(File file) {
        files = files.add(file.toPath());
    }

    /**
     * Locate path.
     *
     * @param resource the resource
     * @return the path
     */
    public Path locate(String resource) {
        Path toReturn = null;
        try {
            if (resource != null) {
                toReturn = tryToLocateResource(resource).getOrNull();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return toReturn;
    }

    private Option<Path> tryToLocateResource(String resource) throws URISyntaxException {
        return new File(resource).exists() ? Option(Paths.get(resource)) : getPathForResourceAmongFiles(resource)
                .orElse(getPathForResourceBasedOnProperty(resource, "node.config.location"))
                .orElse(getPathForResourceBasedOnProperty(resource, "config.location"))
                .orElse(getPathForResourceAmongUrls(resource));
    }

    private Option<Path> getPathForResourceBasedOnProperty(String resource, String configPropertyKey) {
        return systemProperties.get(configPropertyKey)
                .flatMap(el -> Option(locate(new File(el), resource)));
    }

    private Option<Path> getPathForResourceAmongFiles(String resource) {
        return files.map(Path::toAbsolutePath)
                .filter(path -> path.toFile().exists() & path.endsWith(resource))
                .headOption();
    }

    private Option<Path> getPathForResourceAmongUrls(String resource) throws URISyntaxException {
        return urls.filter(url -> url.getFile().endsWith(resource)).headOption()
                .flatMap(url -> Option(Paths.get(url.getPath())));
    }

    private Path locate(File root, String resource) {
        return root.exists() ? ConfigurationUtils.getAllFiles(root, true, false)
                .stream().filter(file -> !ConfigurationUtils.isConfig(file))
                .peek(this::add)
                .filter(file -> file.getAbsolutePath().endsWith(resource))
                .map(file -> Paths.get(file.getAbsolutePath()))
                .findAny()
                .orElse(null) : null;
    }
}
