package org.openecomp.core.tools.util;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void createZip(String zipFileName, Path dir, Set<String> filterItem) throws Exception {
        File dirObj = dir.toFile();
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
                ZipOutputStream out = new ZipOutputStream(fileOutputStream)) {
            addDir(dirObj, out, dir.toString(), filterItem);
        }
    }

    public static final Set<String> cleanStr(Set<String> inFilterStrs) {
        return inFilterStrs.stream().map(inFilterStr -> {
                    if (Objects.isNull(inFilterStr)) {
                        return inFilterStr;
                    }
                    Scanner scan = new Scanner(inFilterStr);
                    while (scan.hasNextLine()) {
                        inFilterStr = scan.nextLine().replaceAll("[^a-zA-Z0-9]", "");
                    }
                    return inFilterStr;
                }
        ).collect(Collectors.toSet());
    }

    static void addDir(File dirObj, ZipOutputStream out, String root, Set<String> filterItem) throws IOException {
        File[] files = dirObj.listFiles();
        filterItem = cleanStr(filterItem);

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out, root, filterItem);
                String filePath = files[i].getAbsolutePath().replace(root + File.separator, "") + "/";
                out.putNextEntry(new ZipEntry(filePath));
                continue;
            }
            try (FileInputStream in = new FileInputStream((files[i].getAbsolutePath()))) {
                String filePath = files[i].getAbsolutePath().replace(root + File.separator, "");
                if (filterItem.isEmpty() || filterItem.stream().anyMatch(s -> filePath.contains(s))) {
                    out.putNextEntry(new ZipEntry(filePath));
                    try {
                        ByteStreams.copy(in, out);

                    } finally {
                        out.closeEntry();
                    }
                }

            }
        }
    }

    public static void unzip(Path zipFile, Path outputFolder) throws IOException {
        if (zipFile == null || outputFolder == null) {
            return;
        }
        if (!Files.exists(outputFolder)) {
            Files.createDirectories(outputFolder);
        }

        try (FileInputStream fileInputStream = new FileInputStream(zipFile.toFile());
             ZipInputStream zis = new ZipInputStream(fileInputStream)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder.toString() + File.separator + fileName);
                if (ze.isDirectory()) {
                    Path path = newFile.toPath();
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        ByteStreams.copy(zis, fos);
                    }
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
        }

    }
}

