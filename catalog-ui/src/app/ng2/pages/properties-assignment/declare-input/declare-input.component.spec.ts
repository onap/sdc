import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeclareInputComponent } from './declare-input.component';

describe('DeclareInputComponent', () => {
  let component: DeclareInputComponent;
  let fixture: ComponentFixture<DeclareInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeclareInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeclareInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
