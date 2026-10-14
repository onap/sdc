/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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
const SKIP_TYPES = ['hidden', 'button', 'submit', 'file'];

const attr = (el: Element, plain: string) => el.getAttribute(plain) || el.getAttribute('[attr.' + plain + ']') || '';

/** Form controls in an Angular template source that no <label> names. Bound ids must match textually. */
export function unlabelledControls(html: string): string[] {
    const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
    const labelled = Array.from(doc.querySelectorAll('label')).map((l) => attr(l, 'for')).filter((f) => !!f);
    return Array.from(doc.querySelectorAll('input, select, textarea'))
        .filter((el) => SKIP_TYPES.indexOf((el.getAttribute('type') || '').toLowerCase()) === -1)
        .filter((el) => !el.closest('label'))
        .filter((el) => !attr(el, 'aria-label'))
        .filter((el) => labelled.indexOf(attr(el, 'id')) === -1)
        .map((el) => `${el.tagName.toLowerCase()} ${attr(el, 'data-tests-id') || el.getAttribute('formcontrolname') || ''}`);
}
