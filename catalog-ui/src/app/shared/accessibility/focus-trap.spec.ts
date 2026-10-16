/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import { FocusTrap } from './focus-trap';

describe('FocusTrap focus restore', () => {
    let page: HTMLElement;
    let card: HTMLElement;
    let opener: HTMLButtonElement;
    let dialog: HTMLElement;

    beforeEach(() => {
        page = document.createElement('div');
        page.innerHTML = '<div class="card" tabindex="0"><div class="actions"><button class="opener">Import Type</button></div></div>' +
            '<div class="dialog"><button class="close">Close</button></div>';
        document.body.appendChild(page);
        card = page.querySelector('.card');
        opener = page.querySelector('.opener');
        dialog = page.querySelector('.dialog');
    });

    afterEach(() => page.remove());

    const openAndClose = (whileOpen?: () => void) => {
        opener.focus();
        const trap = new FocusTrap();
        trap.activate(dialog);
        (dialog.querySelector('.close') as HTMLElement).focus();
        if (whileOpen) { whileOpen(); }
        trap.deactivate();
    };

    it('returns focus to the opener', () => {
        openAndClose();
        expect(document.activeElement).toBe(opener);
    });

    it('falls back to the nearest focusable ancestor when the opener cannot take focus', () => {
        // jsdom has no layout, so stand in for a hidden opener with one that refuses focus.
        openAndClose(() => { opener.focus = () => undefined; });
        expect(document.activeElement).toBe(card);
    });
});
