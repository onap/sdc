package org.openecomp.sdc.onboarding.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class ArtifactHelper {

    private MavenProject project;
    private MavenSession session;
    private static Map<String, byte[]> store = new HashMap<>();
    private static Set<String> terminalModuleCoordinates = new HashSet<>();
    private String unicornRoot = null;
    private static File unicornMetaLocation = null;
    private File tempLocation = Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile();

    void init(String terminalModuleCoordinate) {
        unicornMetaLocation = getUnicornRootFile(unicornRoot.substring(0, unicornRoot.indexOf('/')), project);
        setTerminalModuleCoordinates(session.getProjects().get(session.getProjects().size() - 1));
        terminalModuleCoordinates.add(terminalModuleCoordinate);
    }

    List<ArtifactRepository> getRepositories(boolean snapshotRepo) {
        List<ArtifactRepository> list = new ArrayList<>();
        for (ArtifactRepository artRepo : project.getRemoteArtifactRepositories()) {
            if (snapshotRepo) {
                if (artRepo.getSnapshots().isEnabled()) {
                    list.add(artRepo);
                }
            } else {
                if (artRepo.getReleases().isEnabled()) {
                    list.add(artRepo);
                }
            }
        }
        return list;
    }

    private void setTerminalModuleCoordinates(MavenProject project) {
        terminalModuleCoordinates.add(getModuleCoordinate(project));
    }

    private boolean isModuleTerminal(MavenProject project) {
        return terminalModuleCoordinates.contains(getModuleCoordinate(project));
    }

    File getUnicornMetaLocation() {
        return unicornMetaLocation;
    }

    String getContents(URL path) throws IOException {
        try (InputStream is = path.openStream(); Scanner scnr = new Scanner(is).useDelimiter("\\A")) {
            return scnr.hasNext() ? scnr.next() : "";
        }
    }

    String getChecksum(String filePath, String hashType) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(hashType);
        md.update(Files.readAllBytes(Paths.get(filePath)));
        byte[] hashBytes = md.digest();

        StringBuilder buffer = new StringBuilder();
        for (byte hashByte : hashBytes) {
            buffer.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return buffer.toString();
    }

    void store(String artifactId, byte[] data) {
        store.put(artifactId, data);
    }

    byte[] getArtifact(String artifactId) {
        return store.get(artifactId);
    }

    void deleteAll(File f) {
        if (!f.exists()) {
            return;
        }
        for (File file : f.listFiles()) {
            if (file.isDirectory() && file.listFiles().length > 0) {
                deleteAll(file);
            } else {
                file.delete();
            }
        }
    }

    String getModuleCoordinate(MavenProject project) {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    private File getUnicornRootFile(String moduleCoordinate, MavenProject proj) {
        return getStateFile(moduleCoordinate, proj, unicornRoot);
    }

    private File getStateFile(String moduleCoordinate, MavenProject proj, String filePath) {
        return new File(getTopParentProject(moduleCoordinate, proj).getBasedir(),
                filePath.substring(filePath.indexOf('/') + 1));
    }

    MavenProject getTopParentProject(String moduleCoordinate, MavenProject proj) {
        if (getModuleCoordinate(proj).equals(moduleCoordinate) || proj.getParent() == null) {
            return proj;
        } else {
            return getTopParentProject(moduleCoordinate, proj.getParent());
        }
    }

    void shutDown(MavenProject project) throws IOException, ClassNotFoundException {
        File file = new File(unicornMetaLocation, "compileState.dat");
        Map dataStore = null;
        if (isModuleTerminal(project) && file.exists()) {
            try (InputStream is = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(is)) {
                dataStore = HashMap.class.cast(ois.readObject());
                dataStore.put("shutdownTime", System.currentTimeMillis());
            }
            try (OutputStream os = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(dataStore);
            }
            Files.copy(file.toPath(), Paths.get(tempLocation.getAbsolutePath(), file.getName()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }
}


