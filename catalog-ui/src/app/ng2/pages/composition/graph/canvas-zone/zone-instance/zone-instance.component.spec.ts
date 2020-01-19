import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { PoliciesService } from 'app/ng2/services/policies.service';
import { GroupsService } from 'app/ng2/services/groups.service';
import { EventListenerService } from 'app/services';
import { Store } from '@ngxs/store';
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { ZoneInstanceComponent } from './zone-instance.component';
import { ZoneInstanceType, ZoneInstance, ZoneInstanceMode, ZoneInstanceAssignmentType, IZoneInstanceAssignment } from "app/models";
import { PolicyInstance } from "app/models/graph/zones/policy-instance";
import { Subject, of } from 'rxjs';
import { _throw } from 'rxjs/observable/throw';

describe('ZoneInstanceComponent', () => {
  let component: ZoneInstanceComponent;
  let fixture: ComponentFixture<ZoneInstanceComponent>;

  let createPolicyInstance = () => {
    let policy = new PolicyInstance();
    policy.targets = {COMPONENT_INSTANCES: [], GROUPS: []};
    return new ZoneInstance(policy, '', '');
  }

  beforeEach(() => {
    const policiesServiceStub = {updateZoneInstanceAssignments : jest.fn()};
    const groupsServiceStub = {};
    const eventListenerServiceStub = {};
    const storeStub = {};
    const compositionServiceStub = {};
    TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      declarations: [ZoneInstanceComponent],
      providers: [
        { provide: PoliciesService, useValue: policiesServiceStub },
        { provide: GroupsService, useValue: groupsServiceStub },
        { provide: EventListenerService, useValue: eventListenerServiceStub },
        { provide: Store, useValue: storeStub },
        { provide: CompositionService, useValue: compositionServiceStub }
      ]
    }).compileComponents().then(() => {
      fixture = TestBed.createComponent(ZoneInstanceComponent);
      component = fixture.componentInstance;
    });
  });

  it('can load instance',  async((done) => {
    component.zoneInstance = <ZoneInstance>{type : ZoneInstanceType.POLICY, instanceData: {name: 'test policy'}, assignments: []};
    component.forceSave = new Subject<Function>();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  }));
  

  it('if another instance is already tagging, i cannot change my mode', ()=> {
    component.zoneInstance = <ZoneInstance>{ mode: ZoneInstanceMode.NONE };
    component.isActive = false;
    component.activeInstanceMode = ZoneInstanceMode.TAG;
    component.setMode(ZoneInstanceMode.SELECTED);
    expect(component.zoneInstance.mode).toBe(ZoneInstanceMode.NONE);
  });
  
  it('if i am active(selected) and NOT in tag mode, I can set another mode', ()=> {
    component.isActive = true;
    component.zoneInstance = <ZoneInstance>{ mode: ZoneInstanceMode.SELECTED };
    jest.spyOn(component.modeChange, 'emit');
    component.setMode(ZoneInstanceMode.NONE);
    expect(component.modeChange.emit).toHaveBeenCalledWith({instance: component.zoneInstance, newMode: ZoneInstanceMode.NONE });
  });

  it('if i am active and in tag mode and i try to set mode other than tag, I am not allowed', ()=> {
    component.isActive = true;
    component.zoneInstance = <ZoneInstance>{ mode: ZoneInstanceMode.TAG };
    component.setMode(ZoneInstanceMode.SELECTED);
    expect(component.zoneInstance.mode).toBe(ZoneInstanceMode.TAG);
  });

  it('if i am active and in tag mode and click tag again and no changes, does NOT call save, but DOES turn tagging off', ()=> {
    component.isActive = true;
    component.zoneInstance = createPolicyInstance();
    component.zoneService = component.policiesService;
    component.zoneInstance.mode = ZoneInstanceMode.TAG;
    jest.spyOn(component.zoneService, 'updateZoneInstanceAssignments');
    jest.spyOn(component.modeChange, 'emit');

    component.setMode(ZoneInstanceMode.TAG);

    expect(component.zoneService.updateZoneInstanceAssignments).not.toHaveBeenCalled();
    expect(component.modeChange.emit).toHaveBeenCalledWith({instance: component.zoneInstance, newMode: ZoneInstanceMode.NONE });

  });
  it('if i am active and in tag mode and click tag again and HAVE changes, calls save AND turns tagging off', ()=> {
    component.isActive = true;
    component.zoneInstance = createPolicyInstance();
    component.zoneService = component.policiesService;
    component.zoneInstance.mode = ZoneInstanceMode.TAG;
    component.zoneInstance.assignments.push(<IZoneInstanceAssignment>{uniqueId: '123', type: ZoneInstanceAssignmentType.COMPONENT_INSTANCES});
    jest.spyOn(component.zoneService, 'updateZoneInstanceAssignments').mockReturnValue(of(true));
    jest.spyOn(component.modeChange, 'emit');

    component.setMode(ZoneInstanceMode.TAG);

    expect(component.zoneService.updateZoneInstanceAssignments).toHaveBeenCalled();
    expect(component.modeChange.emit).toHaveBeenCalledWith({instance: component.zoneInstance, newMode: ZoneInstanceMode.NONE });
  });

  it('on save error, temporary assignment list is reverted to saved assignments', ()=> {
    component.isActive = true;
    component.zoneInstance = createPolicyInstance();
    component.zoneService = component.policiesService;
    component.zoneInstance.mode = ZoneInstanceMode.TAG;
    component.zoneInstance.assignments.push(<IZoneInstanceAssignment>{uniqueId: '123', type: ZoneInstanceAssignmentType.COMPONENT_INSTANCES});
    jest.spyOn(component.zoneService, 'updateZoneInstanceAssignments').mockReturnValue(_throw({status: 404}));

    component.setMode(ZoneInstanceMode.TAG);

    expect(component.zoneInstance.assignments.length).toEqual(0);
  });

  it('on save success, all changes are saved to zoneInstance.savedAssignments', ()=> {
    component.isActive = true;
    component.zoneInstance = createPolicyInstance();
    component.zoneService = component.policiesService;
    component.zoneInstance.mode = ZoneInstanceMode.TAG;
    component.zoneInstance.assignments.push(<IZoneInstanceAssignment>{uniqueId: '123', type: ZoneInstanceAssignmentType.COMPONENT_INSTANCES});
    jest.spyOn(component.zoneService, 'updateZoneInstanceAssignments').mockReturnValue(of(true));

    component.setMode(ZoneInstanceMode.TAG);

    expect(component.zoneInstance.instanceData.getSavedAssignments().length).toEqual(1);
  });
});
