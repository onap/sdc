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
import { AttributesComponent } from './attributes.component';

describe('attributes component row details', () => {
    let component: AttributesComponent;
    let toggleExpandRow: jest.Mock;

    beforeEach(() => {
        component = new AttributesComponent({metadata: {}} as any, null, null, null, null, null);
        toggleExpandRow = jest.fn();
        (component as any).table = {rowDetail: {toggleExpandRow}};
    });

    it('expands a clicked row that has a description', () => {
        const row = {name: 'a1', description: 'some text'};
        component.onExpandRow({type: 'click', row});
        expect(toggleExpandRow).toHaveBeenCalledWith(row);
    });

    it('does not expand a clicked row without a description', () => {
        component.onExpandRow({type: 'click', row: {name: 'tosca_id', description: null}});
        expect(toggleExpandRow).not.toHaveBeenCalled();
    });
});
