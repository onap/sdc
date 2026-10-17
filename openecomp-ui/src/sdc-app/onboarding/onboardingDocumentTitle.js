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
import { enums } from './OnboardingConstants.js';

const TITLE_SUFFIX = 'SDC';

const selectedText = ({ selectedKey, menuItems = [] }) => {
    const item = menuItems.find(({ key }) => key === selectedKey);
    return item && item.displayText;
};

/**
 * "<page> - <item> - SDC" from the punch-out breadcrumbs, most specific
 * level first. Returns null when no item is open (onboarding home), so the
 * host page keeps its own title.
 */
export const documentTitleFor = (breadcrumbs = []) => {
    const typeIndex = breadcrumbs.findIndex(
        ({ selectedKey }) =>
            selectedKey === enums.BREADCRUMS.LICENSE_MODEL ||
            selectedKey === enums.BREADCRUMS.SOFTWARE_PRODUCT
    );
    let levels = breadcrumbs;
    if (typeIndex !== -1) {
        // A VSP's levels are vendor, type, VSP, page: the vendor is context,
        // not the open item. A VLM's are vendor (the item itself), type, page.
        levels =
            breadcrumbs[typeIndex].selectedKey ===
            enums.BREADCRUMS.SOFTWARE_PRODUCT
                ? breadcrumbs.slice(typeIndex + 1)
                : breadcrumbs.filter((level, index) => index !== typeIndex);
    }
    const parts = levels
        .map(selectedText)
        .filter(Boolean)
        .reverse();
    return parts.length ? [...parts, TITLE_SUFFIX].join(' - ') : null;
};

export class DocumentTitleKeeper {
    hostTitle = null;
    ownTitle = null;

    update(breadcrumbs) {
        const title = documentTitleFor(breadcrumbs);
        if (title) {
            if (this.ownTitle === null) {
                this.hostTitle = document.title;
            }
            this.ownTitle = title;
            document.title = title;
        } else {
            this.restore();
        }
    }

    restore() {
        if (this.ownTitle !== null && document.title === this.ownTitle) {
            document.title = this.hostTitle;
        }
        this.ownTitle = null;
    }
}
