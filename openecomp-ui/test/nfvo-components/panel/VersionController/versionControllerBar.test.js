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
import { Provider } from 'react-redux';
import { render, fireEvent, within } from '@testing-library/react';
import ActionButtons from 'nfvo-components/panel/versionController/components/ActionButtons.jsx';
import Permissions from 'nfvo-components/panel/versionController/components/Permissions.jsx';
import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import NotificationsView from 'sdc-app/onboarding/userNotifications/NotificationsView.jsx';
import { storeCreator } from 'sdc-app/AppStore.js';
import { actionTypes as notificationActionTypes } from 'sdc-app/onboarding/userNotifications/UserNotificationsConstants.js';

const renderActionButtons = (overrides = {}) => {
    const props = {
        itemPermissions: {
            isCollaborator: true,
            isUpToDate: true,
            isDirty: false,
            isCertified: false,
            isOutOfSync: false
        },
        isFormDataValid: true,
        onSave: jest.fn(),
        onSync: jest.fn(),
        onSubmit: jest.fn(),
        onRevert: jest.fn(),
        onClickPermissions: jest.fn(),
        onClosePermissions: jest.fn(),
        onOpenCommentCommitModal: jest.fn(),
        onOpenRevisionsModal: jest.fn(),
        ...overrides
    };
    const utils = render(<ActionButtons {...props} />);
    const byTestId = id =>
        utils.container.querySelector(`[data-test-id="${id}"]`);
    return { ...utils, props, byTestId };
};

describe('Version controller action buttons', () => {
    it('renders every action as a native button named by its label', () => {
        const { getByRole } = renderActionButtons();
        ['Permissions', 'Save', 'Sync', 'Share', 'Revert', 'Submit'].forEach(
            name => {
                const button = getByRole('button', { name });
                expect(button.tagName).toBe('BUTTON');
                expect(button.getAttribute('type')).toBe('button');
            }
        );
    });

    it('carries the data-test-id on the button itself', () => {
        const { byTestId } = renderActionButtons();
        [
            'vc-permission-btn',
            'vc-save-btn',
            'vc-sync-btn',
            'vc-commit-btn',
            'vc-revert-btn',
            'vc-submit-btn'
        ].forEach(id => expect(byTestId(id).tagName).toBe('BUTTON'));
    });

    it('activates an enabled action on click', () => {
        const { byTestId, props } = renderActionButtons();
        fireEvent.click(byTestId('vc-save-btn'));
        expect(props.onSave).toHaveBeenCalledTimes(1);
    });

    it('disables an unavailable action natively and keeps it inert', () => {
        const { byTestId, props } = renderActionButtons();
        const sync = byTestId('vc-sync-btn');
        expect(sync.disabled).toBe(true);
        expect(sync.className).toContain('disabled');
        fireEvent.click(sync);
        expect(props.onSync).not.toHaveBeenCalled();
    });

    it('reports whether the permissions overlay is open', () => {
        const { byTestId, rerender, props } = renderActionButtons();
        const permissions = byTestId('vc-permission-btn');
        expect(permissions.getAttribute('aria-expanded')).toBe('false');
        fireEvent.click(permissions);
        expect(props.onClickPermissions).toHaveBeenCalledTimes(1);
        rerender(
            <ActionButtons
                {...props}
                showPermissions
                permissions={{ owner: {}, contributors: [] }}
                userInfo={{}}
            />
        );
        expect(
            byTestId('vc-permission-btn').getAttribute('aria-expanded')
        ).toBe('true');
    });

    it('keeps the Selenium "disabled" class on a disabled submit and blocks it', () => {
        const { byTestId, props } = renderActionButtons({
            itemPermissions: {
                isCollaborator: true,
                isUpToDate: false,
                isDirty: false
            }
        });
        const submit = byTestId('vc-submit-btn');
        expect(submit.className).toContain('vc-submit-button');
        expect(submit.className).toContain('disabled');
        expect(submit.disabled).toBe(true);
        fireEvent.click(submit);
        expect(props.onSubmit).not.toHaveBeenCalled();
    });

    it('submits when submit is enabled', () => {
        const { byTestId, props } = renderActionButtons();
        const submit = byTestId('vc-submit-btn');
        expect(submit.disabled).toBe(false);
        expect(submit.className).not.toContain('disabled');
        fireEvent.click(submit);
        expect(props.onSubmit).toHaveBeenCalledTimes(1);
    });
});

describe('Version controller permissions overlay', () => {
    it('renders Manage Permissions as a native button for the owner', () => {
        const onClosePermissions = jest.fn();
        const onManagePermissions = jest.fn();
        const { getByRole } = render(
            <Permissions
                userInfo={{ userId: 'u1' }}
                permissions={{
                    owner: { userId: 'u1', fullName: 'Owner' },
                    contributors: []
                }}
                onClosePermissions={onClosePermissions}
                onManagePermissions={onManagePermissions}
            />
        );
        const button = getByRole('button', { name: 'Manage Permissions' });
        expect(button.getAttribute('type')).toBe('button');
        fireEvent.click(button);
        expect(onClosePermissions).toHaveBeenCalledTimes(1);
        expect(onManagePermissions).toHaveBeenCalledTimes(1);
    });
});

describe('Version controller bar', () => {
    const renderBar = (overrides = {}) => {
        const props = {
            version: { id: '1.1', name: '1.1', status: 'Draft' },
            viewableVersions: [{ id: '1.1', name: '1.1', status: 'Draft' }],
            itemPermission: { isCollaborator: true },
            onMoreVersionsClick: jest.fn(),
            onClose: jest.fn(),
            ...overrides
        };
        const utils = render(
            <Provider store={storeCreator()}>
                <VersionController {...props} />
            </Provider>
        );
        return { ...utils, props };
    };

    it('names the version select', () => {
        const { container } = renderBar();
        const select = container.querySelector(
            '[data-test-id="vc-versions-select-box"]'
        );
        expect(select.getAttribute('aria-label')).toBe('Version');
    });

    it('renders Versions Page as a native button', () => {
        const { getByRole, props } = renderBar();
        const button = getByRole('button', { name: 'Versions Page' });
        expect(button.getAttribute('data-test-id')).toBe(
            'vc-versions-page-link'
        );
        fireEvent.click(button);
        expect(props.onMoreVersionsClick).toHaveBeenCalledTimes(1);
    });

    it('renders the close X as a named native button', () => {
        const { getByRole, props } = renderBar();
        const button = getByRole('button', { name: 'Close' });
        expect(button.getAttribute('data-test-id')).toBe('vc-cancel-btn');
        fireEvent.click(button);
        expect(props.onClose).toHaveBeenCalledTimes(1);
    });
});

describe('Notifications bell', () => {
    const renderBell = () => {
        const store = storeCreator();
        const utils = render(
            <Provider store={store}>
                <NotificationsView />
            </Provider>
        );
        const bell = () =>
            utils.container.querySelector('.notifications-icon');
        return { ...utils, store, bell };
    };

    it('is a named native button that reports the overlay state', () => {
        const { bell } = renderBell();
        expect(bell().tagName).toBe('BUTTON');
        expect(bell().getAttribute('type')).toBe('button');
        expect(bell().getAttribute('aria-label')).toBe('Notifications');
        expect(bell().getAttribute('aria-expanded')).toBe('false');
        fireEvent.click(bell());
        expect(bell().getAttribute('aria-expanded')).toBe('true');
    });

    it('includes the unread count in its name', () => {
        const { bell, store } = renderBell();
        store.dispatch({
            type: notificationActionTypes.LOAD_NOTIFICATIONS,
            result: { notifications: [], numOfNotSeenNotifications: 2 }
        });
        expect(bell().getAttribute('aria-label')).toBe(
            'Notifications, 2 unread'
        );
    });

    it('closes the overlay on Escape and returns focus to the bell', () => {
        const { bell, container } = renderBell();
        fireEvent.click(bell());
        expect(bell().getAttribute('aria-expanded')).toBe('true');
        const overlay = container.querySelector('.onboarding-overlay');
        expect(overlay).toBeTruthy();
        fireEvent.keyDown(overlay, { key: 'Escape' });
        expect(bell().getAttribute('aria-expanded')).toBe('false');
        expect(document.activeElement).toBe(bell());
    });
});
