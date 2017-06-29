package org.openecomp.core.tools.Commands.importdata;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TreeWalker {
    private static final Logger logger = LoggerFactory.getLogger(TreeWalker.class);

    public static final void walkFiles(SessionContext sessionContext,Path rootDir, String filterItem) throws IOException {
        try (Stream<Path> walk = Files.walk(rootDir)) {
            walk.parallel().filter(path -> Files.isDirectory(path)).
                    forEach(path -> handlePath(sessionContext,path, rootDir, filterItem));
        }
    }

    private static final void handlePath(SessionContext sessionContext, Path path, Path root,String filterItem) {
        String logicalPath = path.toString().replace(root.toString()+File.separator, "");
        String[] splitted = logicalPath.split(File.separator);
        if(filterItem != null && splitted.length > 0 && !splitted[0].contains(filterItem)){
            return;
        }
        switch (splitted.length) {
            case 0:
                //root - ignore
                break;
            case 1:     // handle Item
                new ItemImport().loadPath(sessionContext,path,splitted[splitted.length -1]);
                new VersionInfoImport().loadPath(sessionContext,path,splitted[splitted.length -1]);
                break;
            case 2:
                //ignore this level
                break;
            case 3: // handle version
                new VersionImport().loadPath(sessionContext,path,splitted[splitted.length -2]);
                break;
            default:
                //handle elements
                new ElementImport().loadPath(sessionContext,path,splitted[splitted.length -1],splitted);
                break;
        }

    }

}
