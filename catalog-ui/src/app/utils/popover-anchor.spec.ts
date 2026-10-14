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
import { popoverAnchor } from './popover-anchor';

describe('popoverAnchor', () => {
    const icon = document.createElement('svg-icon');
    icon.getBoundingClientRect = () => ({left: 100, top: 40, width: 20, height: 10, right: 120, bottom: 50} as ClientRect);
    document.body.appendChild(icon);

    const fire = (init: MouseEventInit, coordinates: 'page' | 'client') => {
        let point;
        const listener = (event: MouseEvent) => point = popoverAnchor(event, coordinates);
        icon.addEventListener('click', listener);
        icon.dispatchEvent(new MouseEvent('click', init));
        icon.removeEventListener('click', listener);
        return point;
    };

    it('keeps the pointer position of a mouse click', () => {
        expect(fire({detail: 1, clientX: 7, clientY: 9}, 'client')).toEqual({x: 7, y: 9});
        expect(popoverAnchor({detail: 1, pageX: 300, pageY: 200} as MouseEvent, 'page')).toEqual({x: 300, y: 200});
    });

    it('anchors a keyboard activation at the centre of the clicked element', () => {
        expect(fire({detail: 0}, 'client')).toEqual({x: 110, y: 45});
    });

    it('adds the scroll offset when page coordinates are expected', () => {
        Object.defineProperty(window, 'pageXOffset', {value: 5, configurable: true});
        Object.defineProperty(window, 'pageYOffset', {value: 500, configurable: true});
        expect(fire({detail: 0}, 'page')).toEqual({x: 115, y: 545});
        Object.defineProperty(window, 'pageXOffset', {value: 0, configurable: true});
        Object.defineProperty(window, 'pageYOffset', {value: 0, configurable: true});
    });

    it('treats a click without coordinates as a keyboard activation', () => {
        expect(fire({}, 'client')).toEqual({x: 110, y: 45});
    });
});
