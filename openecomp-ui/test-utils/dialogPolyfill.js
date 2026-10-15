/*
 * Copyright (C) 2026 Deutsche Telekom AG
 *
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
 */

// onap-ui-react's Modal is a <dialog> it opens with showModal(). jsdom has no dialog API, so
// dialogs created in tests get a minimal open/close implementation (no top layer, inertness or
// focus trap).
export function polyfillDialog(el) {
    Object.defineProperty(el, 'open', {
        configurable: true,
        get() {
            return this.hasAttribute('open');
        }
    });
    el.show = function() {
        this.setAttribute('open', '');
    };
    el.showModal = el.show;
    el.close = function() {
        if (this.hasAttribute('open')) {
            this.removeAttribute('open');
            this.dispatchEvent(new Event('close'));
        }
    };
    return el;
}

export function installDialogPolyfill(doc) {
    if (typeof doc.createElement('dialog').showModal === 'function') {
        return false;
    }
    const createElement = doc.createElement;
    doc.createElement = function(tagName, ...rest) {
        const el = createElement.call(this, tagName, ...rest);
        return String(tagName).toLowerCase() === 'dialog'
            ? polyfillDialog(el)
            : el;
    };
    return true;
}

installDialogPolyfill(document);
