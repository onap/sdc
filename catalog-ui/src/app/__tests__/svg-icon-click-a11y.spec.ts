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
import { unnamedClickableIcons } from '../../jest/a11y-helpers';

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

describe('svg-icon click sites', () => {
    const appDir = path.join(__dirname, '..');

    it('reads the template files and the inline templates', () => {
        const files = sources(appDir);
        expect(files.filter((file) => file.endsWith('.html')).length).toBeGreaterThan(100);
        expect(templateHtml(path.join(appDir, 'components/ui/download-artifact/download-artifact.component.ts'))).toContain('<svg-icon');
    });

    it('make every clicked icon a named keyboard button', () => {
        const problems = sources(appDir).reduce((found: string[], file: string) =>
            found.concat(unnamedClickableIcons(templateHtml(file)).map((p) => `${path.relative(appDir, file)}: ${p}`)), []);
        expect(problems).toEqual([]);
    });
});

describe('unnamedClickableIcons', () => {
    it('accepts a clickable, named svg-icon', () => {
        expect(unnamedClickableIcons('<svg-icon name="x" [clickable]="true" ariaLabel="Close" (click)="close()"></svg-icon>')).toEqual([]);
        expect(unnamedClickableIcons('<svg-icon [clickable]="!readonly" [ariaLabel]="label" (click)="close()"></svg-icon>')).toEqual([]);
    });

    it('rejects an icon that is not clickable or has no name', () => {
        expect(unnamedClickableIcons('<svg-icon (click)="close()"></svg-icon>').length).toBe(1);
        expect(unnamedClickableIcons('<svg-icon [clickable]="false" ariaLabel="Close" (click)="close()"></svg-icon>').length).toBe(1);
        expect(unnamedClickableIcons('<svg-icon [clickable]="true" (click)="close()"></svg-icon>').length).toBe(1);
        expect(unnamedClickableIcons('<svg-icon-label clickable="false" label="Add" (click)="add()"></svg-icon-label>').length).toBe(1);
    });

    it('lets a clickable svg-icon-label be named by its label', () => {
        expect(unnamedClickableIcons('<svg-icon-label clickable="true" label="Add" (click)="add()"></svg-icon-label>')).toEqual([]);
        expect(unnamedClickableIcons('<svg-icon-label clickable="{{!!selected}}" label="Add" (click)="add()"></svg-icon-label>')).toEqual([]);

        expect(unnamedClickableIcons('<svg-icon-label [clickable]="true" (click)="add()"></svg-icon-label>').length).toBe(1);
        expect(unnamedClickableIcons('<svg-icon-label [clickable]="true" [label]="" (click)="add()"></svg-icon-label>').length).toBe(1);

    });

    it('ignores icons without a click handler', () => {
        expect(unnamedClickableIcons('<svg-icon name="x"></svg-icon>')).toEqual([]);
    });
});
