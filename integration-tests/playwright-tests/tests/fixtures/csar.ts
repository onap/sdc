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

/**
 * CSAR download and inspection, and access to the test packages shared with the TestNG suites.
 *
 *     const csar = await downloadCsar(page, () => page.locator(...).click());
 *     const template = csar.mainTemplate();
 *     expect(Object.keys(template.topology_template.node_templates)).toContain('...');
 */

import * as fs from 'fs';
import * as path from 'path';
import JSZip from 'jszip';
import { parse as parseYaml } from 'yaml';
import { expect, Page } from '@playwright/test';

/** `integration-tests/src/test/resources/Files`, the package directory the TestNG API suite also reads. */
export const FILES_DIR = path.resolve(__dirname, '../../../src/test/resources/Files');

/** Absolute path of a test package under {@link FILES_DIR}; fails the test if it is missing. */
export function fixtureFile(relative: string): string {
    const file = path.join(FILES_DIR, relative);
    expect(fs.existsSync(file), `test package ${relative} is missing under ${FILES_DIR}`).toBe(true);
    return file;
}

export class Csar {
    private constructor(private zip: JSZip, readonly fileName: string) {}

    static async fromBuffer(bytes: Buffer, fileName = 'csar'): Promise<Csar> {
        return new Csar(await JSZip.loadAsync(bytes), fileName);
    }

    entries(): string[] {
        return Object.keys(this.zip.files).filter(name => !this.zip.files[name].dir);
    }

    async text(entry: string): Promise<string> {
        return this.file(entry).async('string');
    }

    /** A nested archive (an ETSI package inside a CSAR) for {@link Csar.fromBuffer}. */
    async bytes(entry: string): Promise<Buffer> {
        return this.file(entry).async('nodebuffer');
    }

    private file(entry: string): JSZip.JSZipObject {
        const file = this.zip.file(entry);
        expect(file, `${this.fileName} has no entry ${entry}; entries: ${this.entries().join(', ')}`).toBeTruthy();
        return file!;
    }

    async yaml(entry: string): Promise<any> {
        return parseYaml(await this.text(entry));
    }

    /** The template named by `Entry-Definitions` in `TOSCA-Metadata/TOSCA.meta`. */
    async mainTemplatePath(): Promise<string> {
        const meta = await this.text('TOSCA-Metadata/TOSCA.meta');
        const entry = /^Entry-Definitions:\s*(\S+)/m.exec(meta);
        expect(entry, `${this.fileName}: TOSCA.meta has no Entry-Definitions`).toBeTruthy();
        return entry![1];
    }

    async mainTemplate(): Promise<any> {
        return this.yaml(await this.mainTemplatePath());
    }
}

/**
 * Runs `trigger` and returns the CSAR the browser downloads as a result. The download is
 * registered before the trigger runs, so a fast response cannot be missed.
 */
export async function downloadCsar(page: Page, trigger: () => Promise<unknown>, timeoutMs = 120_000): Promise<Csar> {
    const [download] = await Promise.all([
        page.waitForEvent('download', { timeout: timeoutMs }),
        trigger(),
    ]);
    const failure = await download.failure();
    expect(failure, `download of ${download.suggestedFilename()} failed`).toBeNull();
    const bytes = fs.readFileSync(await download.path());
    expect(bytes.length, `${download.suggestedFilename()} is empty`).toBeGreaterThan(0);
    return Csar.fromBuffer(bytes, download.suggestedFilename());
}
