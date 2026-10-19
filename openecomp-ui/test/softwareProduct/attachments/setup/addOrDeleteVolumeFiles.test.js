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
import AddOrDeleteVolumeFiles from 'sdc-app/onboarding/softwareProduct/attachments/setup/components/AddOrDeleteVolumeFiles.js';

describe('AddOrDeleteVolumeFiles', () => {
    it('draws "Add Volume Files" as a tertiary button with a plus icon', () => {
        const { getByRole } = render(<AddOrDeleteVolumeFiles add={true} />);
        const button = getByRole('button', { name: 'Add Volume Files' });
        expect(button.classList.contains('sdc-button__tertiary')).toBe(true);
        expect(button.classList.contains('plus')).toBe(true);
    });

    it('draws "Delete Volume Files" as a tertiary button without the bootstrap close class', () => {
        const { getByRole } = render(<AddOrDeleteVolumeFiles add={false} />);
        const button = getByRole('button', { name: 'Delete Volume Files' });
        expect(button.classList.contains('sdc-button__tertiary')).toBe(true);
        expect(button.classList.contains('close')).toBe(false);
        expect(button.classList.contains('trashO')).toBe(true);
    });
});
