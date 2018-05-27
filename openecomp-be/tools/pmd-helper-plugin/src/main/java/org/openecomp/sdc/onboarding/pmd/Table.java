/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on a "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.onboarding.pmd;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private final int TABLEPADDING = 1;
    private final char SEPERATOR_CHAR = '-';

    private ArrayList<String> headers;
    private ArrayList<ArrayList<String>> table;
    private ArrayList<Integer> maxLength;

    public Table(ArrayList<String> headersIn, ArrayList<ArrayList<String>> content) {
        this.headers = headersIn;
        this.maxLength = new ArrayList<Integer>();
        for (int i = 0; i < headers.size(); i++) {
            maxLength.add(headers.get(i).length());
        }
        this.table = content;
        calcMaxLengthAll();
    }

    public String drawTable() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbRowSep = new StringBuilder();
        StringBuffer padder = new StringBuffer();
        String rowSeperator = "";

        for (int i = 0; i < TABLEPADDING; i++) {
            padder.append(" ");
        }

        for (int i = 0; i < maxLength.size(); i++) {
            sbRowSep.append("|");
            for (int j = 0; j < maxLength.get(i) + (TABLEPADDING * 2); j++) {
                sbRowSep.append(SEPERATOR_CHAR);
            }
        }
        sbRowSep.append("|");
        rowSeperator = sbRowSep.toString();

        sb.append(rowSeperator);
        sb.append("\n");
        sb.append("|");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(padder);
            sb.append(headers.get(i));
            for (int k = 0; k < (maxLength.get(i) - headers.get(i).length()); k++) {
                sb.append(" ");
            }
            sb.append(padder);
            sb.append("|");
        }
        sb.append("\n");
        sb.append(rowSeperator);
        sb.append("\n");

        for (int i = 0; i < table.size(); i++) {
            ArrayList<String> tempRow = table.get(i);
            sb.append("|");
            for (int j = 0; j < tempRow.size(); j++) {
                sb.append(padder);
                sb.append(tempRow.get(j));
                for (int k = 0; k < (maxLength.get(j) - tempRow.get(j).length()); k++) {
                    sb.append(" ");
                }
                sb.append(padder);
                sb.append("|");
            }
            sb.append("\n");
            sb.append(rowSeperator);
            sb.append("\n");
        }
        return sb.toString();
    }

    private void calcMaxLengthAll() {
        for (int i = 0; i < table.size(); i++) {
            List<String> temp = table.get(i);
            for (int j = 0; j < temp.size(); j++) {
                if (temp.get(j).length() > maxLength.get(j)) {
                    maxLength.set(j, temp.get(j).length());
                }
            }
        }
    }

}

