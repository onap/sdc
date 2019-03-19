package org.onap.sdc.gab.model;

import com.google.common.base.MoreObjects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.onap.sdc.gab.GABService;

/**
 * See GABResultEntry.{@link #path}, GABResultEntry.{@link #data}
 */
@Getter
@AllArgsConstructor
public class GABResultEntry {
    /**
     * Path of queried data.
     */
    private String path;

    /**
     * Specific events-template data served by the GABService
     * @see GABService
     */
    private Object data;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("path", path)
            .add("data", data)
            .toString();
    }
}
