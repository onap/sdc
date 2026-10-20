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

import React from 'react';
import { render } from '@testing-library/react';
import ModuleFile from 'sdc-app/onboarding/softwareProduct/attachments/setup/components/ModuleFile.js';

const renderModuleFile = isReadOnlyMode =>
    render(
        <ModuleFile
            module={{ name: 'base', isBase: true, yaml: 'base.yaml' }}
            files={[{ label: 'base.yaml', value: 'base.yaml' }]}
            displayVolumes={true}
            isReadOnlyMode={isReadOnlyMode}
            onModuleDelete={() => {}}
            onModuleRename={() => {}}
            onModuleFileTypeChange={() => {}}
            onToggleVolFilesDisplay={() => {}}
        />
    );

describe('ModuleFile', () => {
    it('keeps rename, delete and the file selects enabled when editable', () => {
        const { getByRole, container } = renderModuleFile(false);
        expect(getByRole('button', { name: 'Rename module' }).disabled).toBe(
            false
        );
        expect(getByRole('button', { name: 'Delete module' }).disabled).toBe(
            false
        );
        expect(
            container.querySelector('.modules-list-item-selectors .Select')
                .classList
        ).not.toContain('is-disabled');
    });

    it('disables rename, delete and every file select in read-only mode', () => {
        const { getByRole, container } = renderModuleFile(true);
        expect(getByRole('button', { name: 'Rename module' }).disabled).toBe(
            true
        );
        expect(getByRole('button', { name: 'Delete module' }).disabled).toBe(
            true
        );
        const selects = container.querySelectorAll(
            '.modules-list-item-selectors .Select'
        );
        expect(selects.length).toBe(4);
        selects.forEach(select =>
            expect(select.classList).toContain('is-disabled')
        );
    });
});
