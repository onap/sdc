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
// Type definitions for js-md5 v0.3.0
// Project: https://github.com/emn178/js-md5
// Definitions by: Roland Greim <https://github.com/tigerxy>
// Definitions: https://github.com/DefinitelyTyped/DefinitelyTyped/

/// <reference path="../jquery/jquery.d.ts"/>

interface JQuery {
    md5(value: string): string;
    md5(value: Array<any>): string;
    md5(value: Uint8Array): string;
}

interface JQueryStatic {
    md5(value: string): string;
    md5(value: Array<any>): string;
    md5(value: Uint8Array): string;
}

interface md5 {
    (value: string): string;
    (value: Array<any>): string;
    (value: Uint8Array): string;
}

interface String {
    md5(value: string): string;
    md5(value: Array<any>): string;
    md5(value: Uint8Array): string;
}

declare module "js-md5" {
    export = md5;
}

declare var md5: md5;
