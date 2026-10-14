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
import { render, fireEvent } from '@testing-library/react';
import VersionList from 'sdc-app/onboarding/versionsPage/components/VersionList.jsx';
import PermissionsView from 'sdc-app/onboarding/versionsPage/components/PermissionsView.jsx';
import VersionsPage from 'sdc-app/onboarding/versionsPage/VersionsPage.jsx';

jest.mock('nfvo-components/tree/Tree.jsx', () => {
    const React = require('react');
    return class MockTree extends React.Component {
        componentDidMount() {
            this.props.onRenderedBeyondWidth();
        }
        render() {
            return <div className="tree-view" />;
        }
    };
});

const version = {
    id: '1',
    name: '1.0',
    status: 'Draft',
    description: 'first',
    additionalInfo: { OptionalCreationMethods: ['major'] }
};

const renderList = () => {
    const props = {
        versions: [version],
        onSelectVersion: jest.fn(),
        onNavigateToVersion: jest.fn(),
        onCreateVersion: jest.fn(),
        isCollaborator: true
    };
    const utils = render(<VersionList {...props} />);
    return { ...utils, props };
};

describe('Versions page - version list', () => {
    it('renders the in-row actions as named native buttons', () => {
        const { getByRole, container } = renderList();
        const goTo = getByRole('button', { name: 'Go to this Version' });
        const create = getByRole('button', { name: 'Create New Version' });
        expect(goTo.getAttribute('type')).toBe('button');
        expect(goTo.getAttribute('data-test-id')).toBe(
            'versions-page-select-version'
        );
        expect(create.getAttribute('data-test-id')).toBe(
            'versions-page-create-version'
        );
        const row = container.querySelector(
            '.version-item-row:not(.header-row)'
        );
        expect(row.hasAttribute('role')).toBe(false);
        expect(row.hasAttribute('tabindex')).toBe(false);
    });

    it('navigates from the in-row button without triggering the row', () => {
        const { getByRole, props } = renderList();
        fireEvent.click(getByRole('button', { name: 'Go to this Version' }));
        expect(props.onNavigateToVersion).toHaveBeenCalledTimes(1);
        expect(props.onSelectVersion).not.toHaveBeenCalled();
    });

    it('creates a version from the in-row button without triggering the row', () => {
        const { getByRole, props } = renderList();
        fireEvent.click(getByRole('button', { name: 'Create New Version' }));
        expect(props.onCreateVersion).toHaveBeenCalledTimes(1);
        expect(props.onNavigateToVersion).not.toHaveBeenCalled();
    });

    it('keeps the row itself mouse-activatable', () => {
        const { container, props } = renderList();
        fireEvent.click(
            container.querySelector('.version-item-row:not(.header-row)')
        );
        expect(props.onSelectVersion).toHaveBeenCalledTimes(1);
        expect(props.onNavigateToVersion).toHaveBeenCalledTimes(1);
    });
});

describe('Versions page - permissions', () => {
    const renderPermissions = (currentUser, isManual = false) => {
        const onManagePermissions = jest.fn();
        const utils = render(
            <PermissionsView
                owner={{ userId: 'owner', fullName: 'Owner' }}
                contributors={[]}
                currentUser={currentUser}
                isManual={isManual}
                onManagePermissions={onManagePermissions}
            />
        );
        return { ...utils, onManagePermissions };
    };

    it('renders Manage Permissions as a native button for the owner', () => {
        const { getByRole, onManagePermissions } = renderPermissions({
            userId: 'owner'
        });
        const button = getByRole('button', { name: 'Manage Permissions' });
        expect(button.getAttribute('data-test-id')).toBe(
            'versions-page-manage-permissions'
        );
        fireEvent.click(button);
        expect(onManagePermissions).toHaveBeenCalledTimes(1);
    });

    it('hides Manage Permissions from users other than the owner', () => {
        expect(
            renderPermissions({ userId: 'someone' }).queryByRole('button', {
                name: 'Manage Permissions'
            })
        ).toBeNull();
    });
});

describe('Versions page - header and tree', () => {
    const renderPage = (overrides = {}) => {
        const props = {
            versions: [version],
            owner: { userId: 'owner', fullName: 'Owner' },
            contributors: [],
            viewers: [],
            currentUser: { userId: 'owner' },
            isCollaborator: true,
            itemName: 'vlm',
            onSelectVersion: jest.fn(),
            onNavigateToVersion: jest.fn(),
            onTreeFullScreen: jest.fn(),
            onManagePermissions: jest.fn(),
            onCreateVersion: jest.fn(),
            onModalNodeClick: jest.fn(),
            onArchive: jest.fn(),
            onRestore: jest.fn(),
            onDelete: jest.fn(),
            ...overrides
        };
        return { ...render(<VersionsPage {...props} />), props };
    };

    it('renders Archive as a named native button', () => {
        const { getByRole, props } = renderPage();
        const archive = getByRole('button', { name: 'Archive' });
        expect(archive.getAttribute('type')).toBe('button');
        expect(archive.getAttribute('data-test-id')).toBe(
            'deprecate-action-btn'
        );
        fireEvent.click(archive);
        expect(props.onArchive).toHaveBeenCalledTimes(1);
    });

    it('renders the tree full-screen control as a named native button', () => {
        const { getByRole, props } = renderPage();
        const expand = getByRole('button', { name: 'Expand version tree' });
        expect(expand.getAttribute('type')).toBe('button');
        fireEvent.click(expand);
        expect(props.onTreeFullScreen).toHaveBeenCalledTimes(1);
    });
});
