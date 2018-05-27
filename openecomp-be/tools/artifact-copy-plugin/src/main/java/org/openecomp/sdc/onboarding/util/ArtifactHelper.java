package org.openecomp.sdc.onboarding.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

public class ArtifactHelper {

    private MavenProject project;

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

    String getContents(URL path) throws IOException {
        try (InputStream is = path.openStream(); Scanner scnr = new Scanner(is).useDelimiter("\\A")) {
            return scnr.hasNext() ? scnr.next() : "";
        }
    }

    String getChecksum(String filePath, String hashType) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(hashType);
        md.update(Files.readAllBytes(Paths.get(filePath)));
        byte[] hashBytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}


