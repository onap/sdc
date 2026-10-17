/*
 * Copyright (C) 2026 Deutsche Telekom AG.
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

/*
 * svg-sprite-loader's default sprite module, plus aria-hidden/focusable on the
 * sprite <svg>: the loader's prebuilt runtime injects it into <body> without
 * them, so assistive tech announced an unnamed image on every page that loads
 * this library (the onboarding punch-out).
 */
import BrowserSprite from 'svg-baker-runtime/browser-sprite';

const spriteNodeId = '__SVG_SPRITE_NODE__';
const spriteGlobalVarName = '__SVG_SPRITE__';

const sprite =
    window[spriteGlobalVarName] ||
    new BrowserSprite({
        attrs: { id: spriteNodeId, 'aria-hidden': 'true', focusable: 'false' }
    });
window[spriteGlobalVarName] = sprite;

const loadSprite = () => {
    const existing = document.getElementById(spriteNodeId);
    if (existing) {
        existing.setAttribute('aria-hidden', 'true');
        existing.setAttribute('focusable', 'false');
        sprite.attach(existing);
    } else {
        sprite.mount(document.body, true);
    }
};

if (document.body) {
    loadSprite();
} else {
    document.addEventListener('DOMContentLoaded', loadSprite);
}

export default sprite;
