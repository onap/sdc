package org.openecomp.sdc.dmaap;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RequestManager {

    private Executor executor;

    public RequestManager(int poolSize ){
        int sz = Math.max( poolSize , 1);
        int recommendedMaxSz = Runtime.getRuntime().availableProcessors() * 2;
        executor = Executors.newFixedThreadPool( Math.min( sz , recommendedMaxSz ) );
    }

    public Executor getExecutor() {
        return executor;
    }
}
