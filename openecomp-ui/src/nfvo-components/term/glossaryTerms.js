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
import i18n from 'nfvo-utils/i18n/i18n.js';

export const GLOSSARY_TERMS = [
    { key: 'VSP', abbr: 'VSP', aliases: ['VSP'] },
    { key: 'VLM', abbr: 'VLM', aliases: ['VLM'] },
    { key: 'VNF', abbr: 'VNF', aliases: ['VNF'] },
    { key: 'VFC', abbr: 'VFC', aliases: ['VFC'] },
    { key: 'CSAR', abbr: 'CSAR', aliases: ['CSAR'] },
    { key: 'HEAT', abbr: 'HEAT', aliases: ['HEAT', 'Heat'] },
    { key: 'NIC', abbr: 'NIC', aliases: ['NIC'] }
];

export const nameKey = key => 'glossary.' + key + '.name';
export const descKey = key => 'glossary.' + key + '.description';

// catalog-ui renders its own hidden descriptions as sdc-glossary-<KEY> in the same document.
export const descriptionId = key => 'sdc-onboarding-glossary-' + key;

export const lookup = term => {
    const def = GLOSSARY_TERMS.find(t => t.aliases.indexOf(term) !== -1);
    if (!def) {
        return null;
    }
    return {
        key: def.key,
        abbr: def.abbr,
        name: i18n(nameKey(def.key)),
        description: i18n(descKey(def.key)),
        descriptionId: descriptionId(def.key)
    };
};

export const label = entry =>
    entry.abbr ? `${entry.name} (${entry.abbr})` : entry.name;
export const tooltipText = entry => `${label(entry)}: ${entry.description}`;
