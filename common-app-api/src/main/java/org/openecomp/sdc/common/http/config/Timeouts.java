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

package org.openecomp.sdc.common.http.config;

public class Timeouts {

    private static final int DEFAULT_TIMEOUT_MS = 15000;
    private int connectTimeoutMs = DEFAULT_TIMEOUT_MS;
    private int readTimeoutMs = DEFAULT_TIMEOUT_MS;
    private int connectPoolTimeoutMs = DEFAULT_TIMEOUT_MS;

    public static final Timeouts DEFAULT;
    static {
        DEFAULT = new Timeouts(); 
    }

    private Timeouts() {
    }

    public Timeouts(int connectTimeoutMs, int readTimeoutMs) {
        setConnectTimeoutMs(connectTimeoutMs);
        setReadTimeoutMs(readTimeoutMs);
    }

    public Timeouts(Timeouts timeouts) {
        setReadTimeoutMs(timeouts.readTimeoutMs);
        setConnectTimeoutMs(timeouts.connectTimeoutMs);
        setConnectPoolTimeoutMs(timeouts.connectPoolTimeoutMs);
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        validate(connectTimeoutMs);
        this.connectTimeoutMs = connectTimeoutMs;
    }
    
    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
    
    public void setReadTimeoutMs(int readTimeoutMs) {
        validate(readTimeoutMs);
        this.readTimeoutMs = readTimeoutMs;
    }
    
    public int getConnectPoolTimeoutMs() {
        return connectPoolTimeoutMs;
    }
    
    public void setConnectPoolTimeoutMs(int connectPoolTimeoutMs) {
        validate(connectPoolTimeoutMs);
        this.connectPoolTimeoutMs = connectPoolTimeoutMs;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + connectPoolTimeoutMs;
        result = prime * result + connectTimeoutMs;
        result = prime * result + readTimeoutMs;
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Timeouts other = (Timeouts) obj;
        if (connectPoolTimeoutMs != other.connectPoolTimeoutMs)
            return false;
        if (connectTimeoutMs != other.connectTimeoutMs)
            return false;
        if (readTimeoutMs != other.readTimeoutMs)
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Timeouts [connectTimeoutMs=");
        builder.append(connectTimeoutMs);
        builder.append(", readTimeoutMs=");
        builder.append(readTimeoutMs);
        builder.append(", connectPoolTimeoutMs=");
        builder.append(connectPoolTimeoutMs);
        builder.append("]");
        return builder.toString();
    }
    
    private void validate(int timeout) {
        if(timeout <= 0) {
            throw new IllegalArgumentException("Timeout values cannot be less than zero");
        }
    }
}
