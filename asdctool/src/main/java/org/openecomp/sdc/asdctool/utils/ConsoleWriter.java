/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
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
