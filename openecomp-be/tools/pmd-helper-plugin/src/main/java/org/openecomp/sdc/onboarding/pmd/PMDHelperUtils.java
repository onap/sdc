package org.openecomp.sdc.onboarding.pmd;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PMDHelperUtils {

    private PMDHelperUtils() {
        // default constructor. donot remove.
    }

    private static Map<String, String> pmdLink = new HashMap<>();

    static {
        pmdLink.put("one", "errorprone");
        pmdLink.put("ign", "design");
        pmdLink.put("ing", "multithreading");
        pmdLink.put("nce", "performance");
        pmdLink.put("ity", "security");
        pmdLink.put("yle", "codestyle");
        pmdLink.put("ces", "bestpractices");
    }

    static String readInputStream(InputStream is) {
        try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    static File getStateFile(String moduleCoordinate, MavenProject proj, String filePath) {
        return new File(getTopParentProject(moduleCoordinate, proj).getBasedir(),
                filePath.substring(filePath.indexOf('/') + 1));
    }

    private static MavenProject getTopParentProject(String moduleCoordinate, MavenProject proj) {
        if (getModuleCoordinate(proj).equals(moduleCoordinate) || proj.getParent() == null) {
            return proj;
        } else {
            return getTopParentProject(moduleCoordinate, proj.getParent());
        }
    }

    private static String getModuleCoordinate(MavenProject project) {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    private static <T> T readState(String fileName, Class<T> clazz) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return clazz.cast(ois.readObject());
        } catch (Exception ioe) {
            return null;
        }
    }

    static <T> T readState(File file, Class<T> clazz) {
        try (InputStream is = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(is)) {
            return clazz.cast(ois.readObject());
        } catch (Exception ioe) {
            return null;
        }
    }

    static boolean evaluateCodeQuality(Map<String, List<Violation>> stats, Map<String, List<Violation>> current,
            File file, Log logger) {
        boolean qualityCheckPassed = true;
        Map<String, String> table = new HashMap<>();
        Set<String> classes = current.keySet();
        int counter = 0;
        for (String clazz : classes) {
            List<Violation> orgViolation = stats.get(clazz) == null ? new ArrayList<>() : stats.get(clazz);
            List<Violation> currViolation = current.get(clazz) == null ? new ArrayList<>() : current.get(clazz);
            if (diffViolation(orgViolation, currViolation) > 0) {
                Map<String, Integer> lDetails = diffCategory(orgViolation, currViolation);
                for (String cat : lDetails.keySet()) {
                    String lineNo = getLineNumbers(currViolation, cat);
                    table.put(++counter + clazz, cat + ":" + lDetails.get(cat) + ":" + lineNo);
                }
            }
        }
        if (!table.isEmpty()) {
            qualityCheckPassed = false;
            try {
                Files.write(file.toPath(),
                        new Table(getTableHeaders(true), getContents(table, true)).drawTable().getBytes(),
                        StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            logger.error(new Table(getTableHeaders(false), getContents(table, false)).drawTable());
        }
        return qualityCheckPassed;
    }

    private static ArrayList<String> getTableHeaders(boolean addLink) {
        ArrayList<String> list = new ArrayList<>();
        list.add("Class Name");
        list.add("Rule Category");
        list.add("Rule Name");
        list.add("Fix");
        list.add("Source Line No");
        if (addLink) {
            list.add("Help Link");
        }
        return list;
    }

    private static ArrayList<ArrayList<String>> getContents(Map<String, String> data, boolean addLink) {
        ArrayList<ArrayList<String>> list = new ArrayList<>();
        Pattern p = Pattern.compile("(.*):(.*):(.*):(.*)");
        for (String s : data.keySet()) {
            ArrayList<String> l = new ArrayList<>();
            l.add(s.substring(s.indexOf("::") + 2));
            Matcher m = p.matcher(data.get(s));
            if (m.find()) {
                l.add(m.group(1));
                l.add(m.group(2));
                l.add(m.group(3) + " at least");
                l.add(m.group(4));
                if (addLink) {
                    l.add("http://pmd.sourceforge.net/snapshot/pmd_rules_java_" + getLinkCategory(m.group(1)) + ".html#"
                                  + m.group(2).toLowerCase());
                }
            }
            list.add(l);
        }
        return list;
    }

    private static String getLinkCategory(String cat) {
        for (String category : pmdLink.keySet()) {
            if (cat.contains(category)) {
                return pmdLink.get(category);
            }
        }
        return "ERROR";
    }

    private static int diffViolation(List<Violation> org, List<Violation> curr) {
        int diff = 0;
        if (org == null || org.isEmpty()) {
            if (curr != null && !curr.isEmpty()) {
                diff = curr.size();
            }
        } else {
            if (curr != null && !curr.isEmpty()) {
                diff = curr.size() - org.size();
            }
        }
        return diff;
    }

    private static Map<String, Integer> diffCategory(List<Violation> org, List<Violation> curr) {
        Map<String, AtomicInteger> currData = new HashMap<>();
        Map<String, AtomicInteger> orgData = new HashMap<>();
        countViolations(curr, currData);
        countViolations(org, orgData);
        Map<String, Integer> val = new HashMap<>();
        for (String cat : currData.keySet()) {
            if (orgData.get(cat) == null) {
                val.put(cat, currData.get(cat).intValue());
            } else if (currData.get(cat).intValue() > orgData.get(cat).intValue()) {
                val.put(cat, currData.get(cat).intValue() - orgData.get(cat).intValue());
            }
        }
        return val;
    }

    private static void countViolations(List<Violation> violations,  Map<String, AtomicInteger> store){
        for (Violation v : violations) {
            if (store.get(v.getCategory() + ":" + v.getRule()) == null) {
                store.put(v.getCategory() + ":" + v.getRule(), new AtomicInteger(1));
            } else {
                store.get(v.getCategory() + ":" + v.getRule()).incrementAndGet();
            }
        }
    }

    private static void processOriginalViolations(List<Violation> org){

    }
    private static String getLineNumbers(List<Violation> vList, String category) {
        String val = "";
        boolean firstOver = false;
        for (Violation v : vList) {
            if (category.equals(v.getCategory() + ":" + v.getRule())) {
                if (firstOver) {
                    val += ",";
                }
                val += v.getLine();
                firstOver = true;
            }
        }
        return val;
    }

    static Map<String, List<Violation>> readCurrentPMDState(String fileName) {
        Map<String, List<Violation>> val = readState(fileName, HashMap.class);
        return val == null ? new HashMap<>() : val;
    }

    static Map<String, List<Violation>> readCurrentPMDState(File file) {
        Map<String, List<Violation>> val = readState(file, HashMap.class);
        return val == null ? new HashMap<>() : val;
    }

    static void writeCurrentPMDState(File file, Map<String, List<Violation>> data) {
        try (OutputStream os = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(data);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    static boolean isReportEmpty(File reportFile){
        try {
            return !reportFile.exists() || Files.readAllLines(reportFile.toPath()).size()<=1;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
