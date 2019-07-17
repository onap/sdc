/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.workers;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by michael on 6/24/2016.
 */
public class Worker<T extends Job<E>, E> implements Runnable {

    private static final int QUEUE_POLL_TIMEAUT = 500;
    private String workerName;
    private LinkedBlockingQueue<T> inputQueue;

    private LinkedBlockingQueue<E> outputQueue;

    private static Logger log = Logger.getLogger(Worker.class.getName());

    public Worker(String workerName, LinkedBlockingQueue<T> inputQueue, LinkedBlockingQueue<E> outputQueue) {
        this.workerName = workerName;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {

        try {
            while (true) {
                log.trace("worker:{} doing work", workerName);
                T job = inputQueue.poll(QUEUE_POLL_TIMEAUT, TimeUnit.MILLISECONDS);
                if (job == null) {

                    log.debug("worker:{} nothing to do");
                    break;
                }
                this.outputQueue.put(job.doWork());
                log.trace("worker:{} done with work", workerName);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logInternalFlowError("executingJobFailed",
                    "failed during job execution worker" + workerName, BeEcompErrorManager.ErrorSeverity.ERROR);
            log.debug("worker: {} nothing to do stoping", workerName, e);
        }
    }

}
