/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import i18n from 'nfvo-utils/i18n/i18n';

export default class VspUploadStatus {
    static UPLOADING = 'UPLOADING';
    static VALIDATING = 'VALIDATING';
    static PROCESSING = 'PROCESSING';
    static SUCCESS = 'SUCCESS';
    static ERROR = 'ERROR';

    complete;
    created;
    lockId;
    status;
    updated;
    vspId;
    vspVersionId;

    constructor(vspUploadStatusResponse) {
        this.status = vspUploadStatusResponse.status;
        this.complete = vspUploadStatusResponse.complete;
        this.created = vspUploadStatusResponse.created;
        this.lockId = vspUploadStatusResponse.lockId;
        this.updated = vspUploadStatusResponse.updated;
        this.vspId = vspUploadStatusResponse.vspId;
        this.vspVersionId = vspUploadStatusResponse.vspVersionId;
    }

    statusToString() {
        if (!this.status) {
            return '';
        }
        switch (this.status) {
            case VspUploadStatus.UPLOADING: {
                return i18n('upload.status.uploading');
            }
            case VspUploadStatus.VALIDATING: {
                return i18n('upload.status.validating');
            }
            case VspUploadStatus.PROCESSING: {
                return i18n('upload.status.processing');
            }
            case VspUploadStatus.SUCCESS: {
                return i18n('upload.status.success');
            }
            case VspUploadStatus.ERROR: {
                return i18n('upload.status.error');
            }
            default:
                return this.status;
        }
    }
}
