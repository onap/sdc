/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.exception;

public final class NsdSignatureExceptionSupplier {

    private NsdSignatureExceptionSupplier() {
    }

    public static NsdSignatureException invalidCertificate(final String certificateName) {
        return new NsdSignatureException(String.format("The certificate '%s' is invalid", certificateName));
    }

    public static NsdSignatureException certificateNotConfigured() {
        return new NsdSignatureException("No certificate configured");
    }

    public static NsdSignatureException unableToCreateSignature(final Exception e) {
        return new NsdSignatureException("Could create file signature", e);
    }

}