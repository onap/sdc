package org.openecomp.sdc.dmaap;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.InvalidPathException;

public class Util {

    public static String toPath(String path , String filename) throws InvalidPathException{
        if (StringUtils.isNotBlank(path) ){
            if (path.trim().endsWith("/") || path.trim().endsWith("/")){
                return path+(filename!=null ? filename : "");
            }
            return path+"/"+(filename!=null ? filename : "");

        }
        throw new InvalidPathException("wrong path configuration cannot find path -> ",path);
    }
}
