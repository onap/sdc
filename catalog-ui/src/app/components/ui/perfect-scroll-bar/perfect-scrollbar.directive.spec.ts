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

import { ElementRef } from '@angular/core';
import { PerfectScrollbarDirective } from './perfect-scrollbar.directive';

describe('PerfectScrollbarDirective', () => {
    it('keeps the scrollbar thumbs out of the tab order', () => {
        const host = document.createElement('div');
        host.innerHTML = '<p>content</p>';
        document.body.appendChild(host);
        const directive = new PerfectScrollbarDirective(new ElementRef(host));
        directive.ngOnInit();

        const thumbs = Array.from(host.querySelectorAll('.ps-scrollbar-x, .ps-scrollbar-y'));
        expect(thumbs.length).toBe(2);
        expect(thumbs.map((t) => t.getAttribute('tabindex'))).toEqual(['-1', '-1']);

        directive.ngOnDestroy();
        host.remove();
    });
});
