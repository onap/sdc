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
import * as fs from 'fs';
import * as path from 'path';

/**
 * onap-ui-angular re-exports its services as `import * as SdcUiServices from './services'`,
 * so `SdcUiServices` is a *module reference* in the shipped `.metadata.json`, not a class.
 * Injecting through it is fine — at runtime `SdcUiServices.ModalService === ModalService` —
 * but using it as a PROVIDER token is not: the AOT StaticReflector cannot fold the member
 * access back to the class symbol, so it emits the provider with an EMPTY dependency array
 * and no diagnostic. Because the token is the same class object at runtime, that def
 * overwrites the library's correct one and every consumer gets an instance constructed with
 * `undefined` dependencies. JIT is immune (it reads `ctorParameters` off the class at
 * instantiation time), which is why tsc, tslint and Jest all stay green.
 *
 * Import the class directly if you must re-provide it. Usually you need not: the services
 * are already provided by SdcUiComponentsModule.
 */
describe('AOT provider tokens', () => {
    const srcRoot = path.resolve(__dirname, '..');

    const collectTsFiles = (dir: string, found: string[] = []): string[] => {
        for (const entry of fs.readdirSync(dir)) {
            const full = path.join(dir, entry);
            if (fs.statSync(full).isDirectory()) {
                collectTsFiles(full, found);
                // Only production sources are AOT-compiled; a TestBed provider runs under JIT.
            } else if (entry.endsWith('.ts') && !entry.endsWith('.d.ts') && !entry.endsWith('.spec.ts')) {
                found.push(full);
            }
        }
        return found;
    };

    const stripComments = (source: string): string => source
        .replace(/\/\*[\s\S]*?\*\//g, '')
        // the `[^:]` guard keeps `http://` in string literals from being eaten
        .replace(/(^|[^:])\/\/.*$/gm, '$1');

    // Bracket-matched so nested arrays/objects inside a providers list don't cut it short.
    const providerBlocks = (source: string): string[] => {
        const blocks: string[] = [];
        const marker = /providers\s*:\s*\[/g;
        let match = marker.exec(source);
        while (match !== null) {
            let depth = 1;
            let i = match.index + match[0].length;
            const start = i;
            while (i < source.length && depth > 0) {
                if (source[i] === '[') { depth++; } else if (source[i] === ']') { depth--; }
                i++;
            }
            blocks.push(source.slice(start, i - 1));
            marker.lastIndex = i;
            match = marker.exec(source);
        }
        return blocks;
    };

    it('never uses a namespace-imported class as a provider token', () => {
        const offenders: string[] = [];
        for (const file of collectTsFiles(srcRoot)) {
            const source = stripComments(fs.readFileSync(file, 'utf8'));
            const namespaces = source.match(/import\s+\*\s+as\s+(\w+)\s+from/g) || [];
            const names = namespaces.map((n) => n.replace(/import\s+\*\s+as\s+(\w+)\s+from/, '$1'))
                // onap-ui-angular's barrel re-exports these three the same way.
                .concat(['SdcUiServices', 'SdcUiComponents', 'SdcUiCommon']);
            for (const block of providerBlocks(source)) {
                for (const ns of names) {
                    const token = new RegExp(`\\b${ns}\\.(\\w+)`).exec(block);
                    if (token) {
                        offenders.push(`${path.relative(srcRoot, file)}: ${token[0]}`);
                    }
                }
            }
        }
        expect(offenders).toEqual([]);
    });
});
