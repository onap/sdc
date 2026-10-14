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

const clientPoint = (event: MouseEvent) => event.x !== undefined ? {x: event.x, y: event.y} : {x: event.clientX, y: event.clientY};

/**
 * The point a popover opened from a click should start at. A keyboard-activated icon is clicked
 * through element.click(), which carries no pointer coordinates, so the centre of the clicked
 * element stands in for the mouse position.
 * 'page' matches event.pageX/pageY, 'client' matches event.x/y.
 */
export function popoverAnchor(event: MouseEvent, coordinates: 'page' | 'client'): { x: number, y: number } {
    const pointer = coordinates === 'page' ? {x: event.pageX, y: event.pageY} : clientPoint(event);
    const fromKeyboard = event.detail === 0 || (!pointer.x && !pointer.y);
    const element = (event.currentTarget || event.target) as Element;
    if (!fromKeyboard || !element || !element.getBoundingClientRect) {
        return pointer;
    }
    const rect = element.getBoundingClientRect();
    const scroll = coordinates === 'page' ? {x: window.pageXOffset, y: window.pageYOffset} : {x: 0, y: 0};
    return {x: rect.left + rect.width / 2 + scroll.x, y: rect.top + rect.height / 2 + scroll.y};
}
