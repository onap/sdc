/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import { async, ComponentFixture, fakeAsync, flushMicrotasks, TestBed } from '@angular/core/testing';
import { Component, SimpleChange } from '@angular/core';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {SvgIconModule} from "onap-ui-angular/dist/svg-icon/svg-icon.module";
import { ConstraintsComponent, ConstraintTypes } from './constraints.component';
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils/constants';

@Component({
  template: `<span>{{constraints ? 'Constraints' : 'No constraints'}}</span>
    <button [disabled]="!valid">Save</button>
    <app-constraints [propertyType]="propertyType" [propertyConstraints]="constraints"
      (onConstraintChange)="constraints = $event.constraints; valid = $event.valid"></app-constraints>`
})
class ConstraintsHostComponent {
  propertyType = PROPERTY_TYPES.LIST;
  constraints: any[] = null;
  valid = true;
}

describe('ConstraintsComponent', () => {
  let component: ConstraintsComponent;
  let fixture: ComponentFixture<ConstraintsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConstraintsComponent, ConstraintsHostComponent ],
      imports: [FormsModule, ReactiveFormsModule, SvgIconModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConstraintsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('initializes parent constraints without changing an already checked binding', fakeAsync(() => {
    const host = TestBed.createComponent(ConstraintsHostComponent);

    expect(() => host.detectChanges()).not.toThrow();
    flushMicrotasks();
    host.detectChanges();

    expect(host.componentInstance.constraints).toEqual([]);
    expect(host.componentInstance.valid).toBe(true);
  }));

  it('disables the parent Save button for an existing incompatible constraint', fakeAsync(() => {
    const host = TestBed.createComponent(ConstraintsHostComponent);
    host.componentInstance.constraints = [{ equal: '22' }];

    expect(() => host.detectChanges()).not.toThrow();
    flushMicrotasks();
    host.detectChanges();

    expect(host.nativeElement.querySelector('button').disabled).toBe(true);
    expect(host.componentInstance.constraints).toEqual([{ equal: '22' }]);
  }));

  function loadProperty(propertyType: string, constraints: any[] = []) {
    fixture = TestBed.createComponent(ConstraintsComponent);
    component = fixture.componentInstance;
    component.propertyType = propertyType;
    component.propertyConstraints = constraints;
    component.ngOnChanges({
      propertyType: new SimpleChange(undefined, propertyType, true),
      propertyConstraints: new SimpleChange(undefined, constraints, true)
    });
    fixture.detectChanges();
  }

  function offeredConstraints(): string[] {
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('[data-tests-id="constraints"]');
    return Array.from(select.options)
      .filter(option => !option.hidden)
      .map(option => option.value);
  }

  [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].forEach(propertyType => {
    it(`only offers collection constraints for ${propertyType}`, () => {
      loadProperty(propertyType);
      component.addConstraint();
      fixture.detectChanges();

      expect(offeredConstraints()).toEqual([
        ConstraintTypes.valid_values,
        ConstraintTypes.length,
        ConstraintTypes.min_length,
        ConstraintTypes.max_length
      ]);
    });
  });

  PROPERTY_DATA.COMPARABLE_TYPES.filter(propertyType => propertyType !== PROPERTY_TYPES.SCALAR_UNIT).forEach(propertyType => {
    it(`offers equality and comparison constraints for ${propertyType}`, () => {
      loadProperty(propertyType);
      component.addConstraint();
      fixture.detectChanges();

      const expected = [
        ConstraintTypes.equal,
        ConstraintTypes.greater_than,
        ConstraintTypes.greater_or_equal,
        ConstraintTypes.less_than,
        ConstraintTypes.less_or_equal,
        ConstraintTypes.in_range,
        ConstraintTypes.valid_values
      ];
      if (propertyType === PROPERTY_TYPES.STRING) {
        expected.push(ConstraintTypes.length, ConstraintTypes.min_length, ConstraintTypes.max_length, ConstraintTypes.pattern);
      }
      expect(offeredConstraints()).toEqual(expected);
    });
  });

  it('only offers equality and valid values for boolean properties', () => {
    loadProperty(PROPERTY_TYPES.BOOLEAN);
    component.addConstraint();
    fixture.detectChanges();

    expect(offeredConstraints()).toEqual([ConstraintTypes.equal, ConstraintTypes.valid_values]);
  });

  it('preserves an existing incompatible constraint until the user removes it', () => {
    loadProperty(PROPERTY_TYPES.LIST, [{ equal: '22' }]);
    const select: HTMLSelectElement = fixture.nativeElement.querySelector('[data-tests-id="constraints"]');

    expect(select.value).toBe(ConstraintTypes.equal);
    expect(component.constraintsArray.at(0).get('value').value).toBe('22');
    expect(offeredConstraints()).not.toContain(ConstraintTypes.equal);
    expect(component.constraintForm.valid).toBe(false);

    const change = jest.fn();
    component.onConstraintChange.subscribe(change);
    component.removeConstraint(0);
    expect(change).toHaveBeenLastCalledWith({ constraints: [], valid: true });
  });

  it('refreshes options and validity without deleting rows when the property type changes', () => {
    loadProperty(PROPERTY_TYPES.STRING, [{ equal: '22' }]);
    const change = jest.fn();
    component.onConstraintChange.subscribe(change);

    component.propertyType = PROPERTY_TYPES.LIST;
    component.ngOnChanges({propertyType: new SimpleChange(PROPERTY_TYPES.STRING, PROPERTY_TYPES.LIST, false)});
    fixture.detectChanges();

    expect(component.constraintsArray.length).toBe(1);
    expect(offeredConstraints()).not.toContain(ConstraintTypes.equal);
    expect(change).toHaveBeenLastCalledWith({ constraints: [{ equal: '22' }], valid: false });

    component.propertyType = PROPERTY_TYPES.STRING;
    component.ngOnChanges({propertyType: new SimpleChange(PROPERTY_TYPES.LIST, PROPERTY_TYPES.STRING, false)});
    fixture.detectChanges();

    expect(offeredConstraints()).toContain(ConstraintTypes.equal);
    expect(change).toHaveBeenLastCalledWith({ constraints: [{ equal: '22' }], valid: true });
  });

  it('keeps duplicate supported constraints disabled', () => {
    loadProperty(PROPERTY_TYPES.LIST, [{ length: '2' }]);
    component.addConstraint();
    fixture.detectChanges();

    const selects: NodeListOf<HTMLSelectElement> = fixture.nativeElement.querySelectorAll('[data-tests-id="constraints"]');
    const lengthOption = Array.from(selects[1].options).find(option => option.value === ConstraintTypes.length);
    expect(lengthOption.disabled).toBe(true);
  });

  [PROPERTY_TYPES.INTEGER, PROPERTY_TYPES.FLOAT].forEach(propertyType => {
    [ConstraintTypes.in_range, ConstraintTypes.valid_values].forEach(constraintType => {
      it(`preserves valid ${constraintType} entries for ${propertyType}`, () => {
        loadProperty(propertyType, [{ [constraintType]: ['1', '2'] }]);

        expect(component.constraintForm.valid).toBe(true);
        expect(component.valid).toBe(true);
      });
    });
  });

  it('revalidates individual valid-values entries when the property type changes', () => {
    loadProperty(PROPERTY_TYPES.INTEGER, [{ validValues: ['1', '2'] }]);
    const change = jest.fn();
    component.onConstraintChange.subscribe(change);

    component.propertyType = PROPERTY_TYPES.STRING;
    component.ngOnChanges({propertyType: new SimpleChange(PROPERTY_TYPES.INTEGER, PROPERTY_TYPES.STRING, false)});
    component.onChangeConstrainValueIndex(0, 'abc', 0);

    expect(change).toHaveBeenLastCalledWith({ constraints: [{ validValues: ['abc', '2'] }], valid: true });

    component.propertyType = PROPERTY_TYPES.INTEGER;
    component.ngOnChanges({propertyType: new SimpleChange(PROPERTY_TYPES.STRING, PROPERTY_TYPES.INTEGER, false)});

    expect(component.constraintValuesArray(0).at(0).hasError('invalidInt')).toBe(true);
    expect(change).toHaveBeenLastCalledWith({ constraints: [{ validValues: ['abc', '2'] }], valid: false });
  });

  it('renders Add Constraint as a tertiary button that does not submit the form', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('[data-tests-id="add-constraint-button"]');
    expect(button.tagName).toBe('BUTTON');
    expect(button.type).toBe('button');
    expect(button.classList).toContain('sdc-button__tertiary');
    expect(button.textContent.trim()).toBe('Add Constraint');

    const before = component.constraintsArray.length;
    button.click();
    expect(component.constraintsArray.length).toBe(before + 1);
  });
});
