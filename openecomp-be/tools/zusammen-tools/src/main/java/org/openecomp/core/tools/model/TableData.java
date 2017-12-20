/**
 * Copyright © 2016-2017 European Support Limited.
 */
package org.openecomp.core.tools.model;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    public List<ColumnDefinition> definitions = new ArrayList<>();
    public List<List<String>> rows = new ArrayList<>();
}
