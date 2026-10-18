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
/**
 * Every acronym the UI can explain. `aliases` are the exact strings the UI renders (resource-type
 * codes from the backend, category names, hard-coded labels); `abbr` is what the tooltip shows in
 * brackets after the long name, null when the alias is not an acronym (Service, shown as "S").
 */
export interface GlossaryTermDef {
    key: string;
    abbr: string;
    aliases: string[];
}

export const GLOSSARY_TERMS: GlossaryTermDef[] = [
    {key: 'VF', abbr: 'VF', aliases: ['VF']},
    {key: 'VFC', abbr: 'VFC', aliases: ['VFC']},
    {key: 'CVFC', abbr: 'CVFC', aliases: ['CVFC']},
    {key: 'VFCMT', abbr: 'VFCMT', aliases: ['VFCMT']},
    {key: 'CR', abbr: 'CR', aliases: ['CR']},
    {key: 'CP', abbr: 'CP', aliases: ['CP']},
    {key: 'VL', abbr: 'VL', aliases: ['VL']},
    {key: 'PNF', abbr: 'PNF', aliases: ['PNF']},
    {key: 'SERVICE', abbr: null, aliases: ['S', 'Service', 'SERVICE']},
    {key: 'VSP', abbr: 'VSP', aliases: ['VSP']},
    {key: 'VLM', abbr: 'VLM', aliases: ['VLM']},
    {key: 'VNF', abbr: 'VNF', aliases: ['VNF']},
    {key: 'CSAR', abbr: 'CSAR', aliases: ['CSAR']},
    {key: 'TOSCA', abbr: 'TOSCA', aliases: ['TOSCA']},
    {key: 'HEAT', abbr: 'HEAT', aliases: ['HEAT', 'Heat']},
    {key: 'ENV', abbr: 'ENV', aliases: ['ENV']},
    {key: 'DCAE', abbr: 'DCAE', aliases: ['DCAE']},
    {key: 'SDC_AID', abbr: 'SDC AID', aliases: ['SDC AID']},
    {key: 'BRG', abbr: 'BRG', aliases: ['BRG']},
    {key: 'BBU', abbr: 'BBU', aliases: ['BBU']},
    {key: 'ENB', abbr: 'eNB', aliases: ['eNB']},
    {key: 'GNB', abbr: 'gNB', aliases: ['gNB']},
    {key: 'CUCP', abbr: 'CUCP', aliases: ['CUCP']},
    {key: 'CUUP', abbr: 'CUUP', aliases: ['CUUP']},
    {key: 'DU', abbr: 'DU', aliases: ['DU']},
    {key: 'RU', abbr: 'RU', aliases: ['RU']}
];

export function nameKey(key: string): string {
    return 'GLOSSARY_' + key + '_NAME';
}

export function descKey(key: string): string {
    return 'GLOSSARY_' + key + '_DESC';
}
