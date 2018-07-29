package org.openecomp.sdc.asdctool.utils;

public class ConsoleWriter {
    private static String tabbedData(String data, int min) {
        // System.out.println(); //for debug

        int tabcount = 0;
        int len = 8 * min;

        while (data.length() < len) {
            tabcount++;
            len = len - 8;
        }

        // System.out.println("debug: tabcount=" + tabcount);
        // System.out.print("debug adding tabs... ");
        for (int x = 0; x < tabcount; x++) {
            // System.out.print("tab ");
            data = data + "\t";
        }
        // System.out.println(); //for debug

        return data;
    }

    public static void dataLine(String name) {
        dataLine(name, null);
    }

    public static void dataLine(String name, Object data) {
        if (data != null) {
            System.out.println(tabbedData(name, 3) + data);
        } else {
            System.out.println(tabbedData(name, 3));
        }
    }
}
