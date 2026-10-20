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
import SoftwareProductDependenciesView from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesView.jsx';
import {
    relationTypes,
    NEW_RULE_TEMP_ID
} from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesConstants.js';

const renderDependencies = isReadOnlyMode =>
    render(
        <SoftwareProductDependenciesView
            componentsOptions={[
                { value: 'c1', label: 'VFC 1' },
                { value: 'c2', label: 'VFC 2' }
            ]}
            softwareProductDependencies={[
                {
                    id: 'dep1',
                    sourceId: 'c1',
                    targetId: 'c2',
                    relationType: relationTypes.DEPENDS_ON
                },
                {
                    id: NEW_RULE_TEMP_ID,
                    sourceId: null,
                    targetId: null,
                    relationType: relationTypes.DEPENDS_ON
                }
            ]}
            onDataChanged={() => {}}
            onAddDependency={() => {}}
            onDeleteDependency={() => {}}
            isReadOnlyMode={isReadOnlyMode}
        />
    );

describe('Software Product Dependencies - read-only', () => {
    it('keeps an existing rule deletable and editable when editable', () => {
        const { getByRole, container } = renderDependencies(false);
        expect(
            getByRole('button', { name: 'Delete Dependency' }).disabled
        ).toBe(false);
        expect(container.querySelectorAll('.Select.is-disabled').length).toBe(
            0
        );
    });

    it('disables delete and the source/target selects of an existing rule', () => {
        const { getByRole, queryByRole, container } = renderDependencies(true);
        expect(queryByRole('button', { name: 'Add Dependency' })).toBeNull();
        expect(
            getByRole('button', { name: 'Delete Dependency' }).disabled
        ).toBe(true);
        expect(container.querySelectorAll('.Select.is-disabled').length).toBe(
            2
        );
    });
});
