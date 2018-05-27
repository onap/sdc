package org.openecomp.sdc.onboarding.pmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PMDState {

    private static Map<String, List<Violation>> data = new HashMap<>();
    private static Map<String, List<Violation>> historicState = null;
    private static Pattern p =
            Pattern.compile("\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\",\"(.*)\"");

    public static boolean addViolation(String line, String fileLocation) {
        Matcher m = p.matcher(line);
        if (m.find()) {
            if (m.group(3).indexOf("generated-sources") != -1) {
                return true;
            }
            String mainOrTest =
                    m.group(3).indexOf(File.separator + "test" + File.separator) == -1 ? "[MAIN] " : "[TEST] ";
            List<Violation> list = data.get(fileLocation + "::" + mainOrTest + m.group(2) + "." + m.group(3).substring(
                    m.group(3).lastIndexOf(File.separatorChar) + 1));
            if (list == null) {
                list = new LinkedList<>();
                data.put(fileLocation + "::" + mainOrTest + m.group(2) + "." + m.group(3).substring(
                        m.group(3).lastIndexOf(File.separatorChar) + 1), list);
            }

            list.add(new Violation(m.group(7), m.group(8), m.group(6), Integer.parseInt(m.group(4)),
                    Integer.parseInt(m.group(5))));
            return true;
        }
        return false;
    }

    public static void reset(File mainFile, File testFile, String moduleCoordinates) throws IOException {
        data.clear();
        init(mainFile, moduleCoordinates, "[MAIN] ");
        init(testFile, moduleCoordinates, "[TEST] ");
    }

    private static void init(File file, String moduleCoordinates, String maiOrTest) throws IOException {
        if (file.exists()) {
            List<String> coll = Files.readAllLines(file.toPath());
            for (String line : coll) {
                if (line.indexOf("$") == -1) {
                    data.put(moduleCoordinates + "::" + maiOrTest + line.substring(0, line.indexOf('.'))
                                                                        .replace(File.separator, ".") + ".java",
                            new LinkedList<>());
                }
            }
        }
    }

    public static Map<String, List<Violation>> getState() {
        return data;
    }

    public static void setHistoricState(Map<String, List<Violation>> data) {
        historicState = data;
    }

    public static Map<String, List<Violation>> getHistoricState() {
        return historicState;
    }

}
