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
import * as fs from 'fs';
import * as path from 'path';
import { clickOnlyElements, describeElement } from '../../jest/a11y-helpers';

const sources = (dir: string): string[] =>
    fs.readdirSync(dir).reduce((found: string[], entry: string) => {
        const full = path.join(dir, entry);
        if (fs.statSync(full).isDirectory()) {
            return found.concat(sources(full));
        }
        return /\.(html|ts)$/.test(entry) && !/\.(spec|d)\.ts$/.test(entry) ? found.concat(full) : found;
    }, []);

const INLINE_TEMPLATE = /template:\s*`([^`]*)`/g;

const templateHtml = (file: string): string => {
    const text = fs.readFileSync(file, 'utf8');
    if (file.endsWith('.html')) {
        return text;
    }
    const inline: string[] = [];
    let match = INLINE_TEMPLATE.exec(text);
    while (match) {
        inline.push(match[1]);
        match = INLINE_TEMPLATE.exec(text);
    }
    return inline.join('\n');
};

/**
 * Containers that keep a mouse (click) next to a native button doing the same thing, keyed by
 * template path and the container's first class. A <button> may not hold their block content or
 * the other controls inside them.
 */
const CONTAINERS: { [key: string]: string } = {
    'pages/composition/graph/canvas-zone/zone-container.component.html .sdc-canvas-zone':
        'a background click deselects the zone instance; the instances inside are the buttons',
    'pages/composition/graph/canvas-zone/zone-container.component.html .sdc-canvas-zone__header':
        'button.sdc-canvas-zone__title toggles minimize',
    'pages/composition/graph/canvas-zone/zone-instance/zone-instance.component.html .zone-instance':
        'button.zone-instance__name selects the instance',
    'pages/composition/graph/connection-wizard/relationship-operations-step/interface-operation-list/interface-operation-list.component.html .interface-accordion':
        'Selenium matches the div by exact class; button.interface-name toggles',
    'pages/composition/interface-operations/interface-operations.component.html .interface-accordion':
        'Selenium matches the div by exact class; button.interface-name toggles',
    'pages/composition/interface-operations/interface-operations.component.html .data-row':
        'button.operation-name selects the operation',
    'components/logic/attributes-table/attributes-table.component.html .table-row':
        'the row holds the checkbox and value editors; the name is the select button',
    'components/logic/attributes-table/dynamic-attribute/dynamic-attribute.component.html .dynamic-property-row':
        'the row holds the checkbox, value input and add/delete/expand buttons; the name is the select button',
    'components/logic/properties-table/properties-table.component.html .table-row':
        'the row holds the checkbox and value editors; the name is the select button',
    'components/logic/properties-table/dynamic-property/dynamic-property.component.html .dynamic-property-row':
        'the row holds the checkbox, value input and add/delete/expand buttons; the name is the select button',
    'components/logic/inputs-table/inputs-table.component.html .table-row':
        'the row holds the value editors; a click on the name (edit) button also selects the row',
    'components/logic/hierarchy-navigtion/hierarchy-navigation.component.html .node-item':
        'wraps the nested hierarchy; .node-data-wrapper is the button',
    'components/logic/service-consumption/service-consumption.component.html .interface-operations-group':
        'wraps the operation buttons; .interface-title is the toggle button',
    'components/logic/service-consumption/service-consumption.component.html .operation-data':
        'button.operation-name opens the operation',
    'pages/type-workspace/type-workspace-properties/type-workspace-properties.component.html .table-col-general':
        'button.property-name-button opens the property',
    'components/ui/tile/tile.component.html .sdc-tile-content':
        'the name sits in multiline-ellipsis; the icon row is the named button',
    'pages/catalog/catalog.component.html .i-sdc-designer-leftbar-section-left-switch-header':
        'holds the selector items; the title is the toggle button',
    'pages/interface-definition/interface-definition.page.component.html .interface-accordion':
        'Selenium matches the div by exact class; a button wraps span.interface-name',
    'pages/interface-definition/interface-definition.page.component.html .cell':
        'the row\'s name button opens the operation',
    'pages/interface-operation/interface-operation.page.component.html .interface-accordion':
        'Selenium matches the div by exact class; a button wraps the toggle',
    'pages/interface-operation/interface-operation.page.component.html .data-row':
        'the name button in span.field-name edits the operation',
};

const containerKey = (file: string, el: Element) => `${file} .${(el.getAttribute('class') || '').split(/\s+/)[0]}`;

describe('click-only elements', () => {
    const appDir = path.join(__dirname, '..');

    it('makes every clickable element a native button or link', () => {
        const problems = sources(appDir).reduce((found: string[], file: string) => {
            const relative = path.relative(appDir, file);
            return found.concat(clickOnlyElements(templateHtml(file))
                .filter((el) => !(CONTAINERS[containerKey(relative, el)] && el.querySelector('button')))
                .map((el) => `${relative}: ${describeElement(el)}`));
        }, []);
        expect(problems).toEqual([]);
    });

    it('lists only containers that still exist', () => {
        const live = sources(appDir).reduce((found: string[], file: string) =>
            found.concat(clickOnlyElements(templateHtml(file)).map((el) => containerKey(path.relative(appDir, file), el))), []);
        expect(Object.keys(CONTAINERS).filter((key) => live.indexOf(key) === -1)).toEqual([]);
    });
});

describe('clickOnlyElements', () => {
    it('flags a clicked div, span, li and a link without a target', () => {
        expect(clickOnlyElements('<div (click)="a()"></div><span (click)="a()"></span><li (click)="a()"></li><a (click)="a()">x</a>').length).toBe(4);
    });

    it('accepts native buttons, links with a target and labels bound to a control', () => {
        expect(clickOnlyElements('<button type="button" (click)="a()"></button>')).toEqual([]);
        expect(clickOnlyElements('<a href="#x" (click)="a()">x</a><a [routerLink]="[\'/x\']" (click)="a()">x</a>')).toEqual([]);
        expect(clickOnlyElements('<label for="f" (click)="a()">x</label>')).toEqual([]);
    });

    it('leaves component hosts to their own template', () => {
        expect(clickOnlyElements('<sdc-button (click)="a()"></sdc-button><svg-icon (click)="a()"></svg-icon>')).toEqual([]);
    });
});
