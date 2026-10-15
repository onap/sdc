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

import fs from 'fs';
import path from 'path';
import { parse } from '@babel/parser';

const SRC = path.join(__dirname, '..', '..', 'src');

const CLICK_ONLY_TAGS = [
    'div',
    'span',
    'li',
    'ul',
    'em',
    'i',
    'b',
    'strong',
    'small',
    'p',
    'td',
    'th',
    'tr',
    'img',
    'section',
    'label',
    'a'
];

/**
 * Containers that keep a mouse onClick next to a native button doing the same thing, keyed by
 * source path and the container's first className. A <button> may not hold their block content or
 * the other controls inside them.
 */
const CONTAINERS = {
    'nfvo-components/datepicker/Datepicker.jsx .datepicker-custom-input':
        'the wrapper keeps its mouse click; the date text button opens the picker',
    'nfvo-components/listEditor/ListEditorItemView.jsx .list-editor-item-view-content':
        'wraps block content; a visually hidden button selects the item',
    'sdc-app/onboarding/licenseModel/overview/listItems/FeatureGroup.jsx .':
        'wraps block content; a visually hidden button toggles the item',
    'sdc-app/onboarding/licenseModel/overview/listItems/LicenseAgreement.jsx .':
        'wraps block content; a visually hidden button toggles the item',
    'sdc-app/onboarding/onboard/onboardingCatalog/VSPOverlay.jsx .vsp-overlay-wrapper':
        'only stops clicks inside the overlay reaching the tile; its rows are buttons',
    'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidationView.jsx .':
        'the header row; its title is the button',
    'sdc-app/onboarding/softwareProduct/vnfMarketPlace/VNFImportView.jsx .':
        'the row; the name cell is the select button',
    'sdc-app/onboarding/versionsPage/components/VersionList.jsx .':
        'the row; Go to this Version is the button'
};

const sources = dir =>
    fs.readdirSync(dir).reduce((found, entry) => {
        const full = path.join(dir, entry);
        if (fs.statSync(full).isDirectory()) {
            return found.concat(sources(full));
        }
        return /\.jsx?$/.test(entry) ? found.concat(full) : found;
    }, []);

const attrNamed = (opening, name) =>
    opening.attributes.find(
        a => a.type === 'JSXAttribute' && a.name.name === name
    );

const stringAttr = (opening, name) => {
    const a = attrNamed(opening, name);
    return a && a.value && a.value.type === 'StringLiteral'
        ? a.value.value
        : '';
};

const walk = (node, visit) => {
    if (!node || typeof node.type !== 'string') {
        return;
    }
    visit(node);
    Object.keys(node).forEach(key => {
        const child = node[key];
        if (Array.isArray(child)) {
            child.forEach(c => walk(c, visit));
        } else if (child && typeof child.type === 'string' && key !== 'loc') {
            walk(child, visit);
        }
    });
};

const containsButton = element => {
    let found = false;
    walk(element, n => {
        if (
            n !== element &&
            n.type === 'JSXOpeningElement' &&
            n.name.name === 'button'
        ) {
            found = true;
        }
    });
    return found;
};

/** JSX elements of a plain HTML tag whose onClick is their only way to be activated. */
export function clickOnlyElements(code) {
    const ast = parse(code, {
        sourceType: 'module',
        plugins: [
            'jsx',
            'classProperties',
            'objectRestSpread',
            'decorators-legacy',
            'exportDefaultFrom'
        ]
    });
    const found = [];
    walk(ast, node => {
        if (node.type !== 'JSXElement') {
            return;
        }
        const opening = node.openingElement;
        const tag = opening.name.name;
        if (
            CLICK_ONLY_TAGS.indexOf(tag) === -1 ||
            !attrNamed(opening, 'onClick')
        ) {
            return;
        }
        if (tag === 'a' && attrNamed(opening, 'href')) {
            return;
        }
        if (tag === 'label' && attrNamed(opening, 'htmlFor')) {
            return;
        }
        found.push({
            tag,
            line: opening.loc.start.line,
            className: stringAttr(opening, 'className'),
            testId: stringAttr(opening, 'data-test-id'),
            hasButton: containsButton(node)
        });
    });
    return found;
}

const containerKey = (file, el) => `${file} .${el.className.split(/\s+/)[0]}`;

describe('click-only elements', () => {
    const all = sources(SRC).map(file => ({
        file: path.relative(SRC, file),
        elements: clickOnlyElements(fs.readFileSync(file, 'utf8'))
    }));

    it('reads the sources', () => {
        expect(all.length).toBeGreaterThan(100);
    });

    it('makes every clickable element a native button or link', () => {
        const problems = all.reduce(
            (found, { file, elements }) =>
                found.concat(
                    elements
                        .filter(
                            el =>
                                !(
                                    CONTAINERS[containerKey(file, el)] &&
                                    el.hasButton
                                )
                        )
                        .map(
                            el =>
                                `${file}:${el.line} <${el.tag} className="${
                                    el.className
                                }" data-test-id="${el.testId}">`
                        )
                ),
            []
        );
        expect(problems).toEqual([]);
    });

    it('lists only containers that still exist', () => {
        const live = all.reduce(
            (found, { file, elements }) =>
                found.concat(elements.map(el => containerKey(file, el))),
            []
        );
        expect(
            Object.keys(CONTAINERS).filter(key => live.indexOf(key) === -1)
        ).toEqual([]);
    });
});

describe('clickOnlyElements', () => {
    it('flags a clicked div, span and a link without an href', () => {
        expect(
            clickOnlyElements(
                'const x = <div onClick={f}><span onClick={f} /><a onClick={f}>x</a></div>;'
            ).length
        ).toBe(3);
    });

    it('accepts buttons, links with an href, labels bound to a control and components', () => {
        expect(
            clickOnlyElements(
                'const x = <p><button type="button" onClick={f} /><a href="#x" onClick={f}>x</a><label htmlFor="i" onClick={f} /><SVGIcon onClick={f} /></p>;'
            )
        ).toEqual([]);
    });
});
