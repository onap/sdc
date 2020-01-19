import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { EventListenerService } from '../../../../../services/event-listener-service';
import { ComponentInstanceServiceNg2 } from 'app/ng2/services/component-instance-services/component-instance.service';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { GroupsService } from 'app/services-ng2';
import { PoliciesService } from 'app/services-ng2';
import { CompositionPanelHeaderComponent } from './panel-header.component';
import {SdcUiServices} from 'onap-ui-angular';
import { Capability, Requirement, RequirementsGroup, CapabilitiesGroup, ComponentInstance, Component, FullComponentInstance, PolicyInstance, GroupInstance } from "app/models";
import { of, Observable } from "rxjs";

describe('CompositionPanelHeaderComponent', () => {
  let component: CompositionPanelHeaderComponent;
  let fixture: ComponentFixture<CompositionPanelHeaderComponent>;
  const componentInstanceServiceNg2Stub = {
    updateComponentInstance: jest.fn()
  };
  const valueEditModalInstance = {
    innerModalContent : {
      instance: { name : "VF Test" }
    },
    buttons: [{id: 'saveButton', text: 'OK', size: 'xsm', callback: jest.fn(), closeModal: false}],
    closeModal : jest.fn()
  };

  beforeEach(
    () => {
      const compositionServiceStub = {};
      const eventListenerServiceStub = {};

      const workspaceServiceStub = {
        metadata: {
          componentType: "SERVICE",
          uniqueId: "123"
        }
      };
      const groupsServiceStub = {
        updateName: jest.fn()
      };
      const policiesServiceStub = {
        updateName: jest.fn()
      };

      TestBed.configureTestingModule({
        schemas: [NO_ERRORS_SCHEMA],
        declarations: [CompositionPanelHeaderComponent],
        providers: [
          { provide: CompositionService, useValue: compositionServiceStub },
          { provide: EventListenerService, useValue: eventListenerServiceStub },
          {
            provide: ComponentInstanceServiceNg2,
            useValue: componentInstanceServiceNg2Stub
          },
          { provide: WorkspaceService, useValue: workspaceServiceStub },
          { provide: GroupsService, useValue: groupsServiceStub },
          { provide: PoliciesService, useValue: policiesServiceStub },
          { provide: SdcUiServices.ModalService, useValue: {}}
        ]
      });
      fixture = TestBed.createComponent(CompositionPanelHeaderComponent);
      component = fixture.componentInstance;
    }
  );

  it('can load instance', () => {
    expect(component).toBeTruthy();
  });

  it('should close the modal without saving if the name has not changed', () => {
    component.selectedComponent = <FullComponentInstance>{name: "VF Test"};
    component.valueEditModalInstance = valueEditModalInstance;

    component.saveInstanceName();
    expect(component.componentInstanceService.updateComponentInstance).not.toHaveBeenCalled();
    expect(component.valueEditModalInstance.closeModal).toHaveBeenCalled();
  });

  it('after editing instance name, capabilities/requirements should be updated with new name', () => {
    const newName = "New VF NAME";
    component.selectedComponent = new FullComponentInstance(<ComponentInstance>{
      name: "VF Test",
      requirements: <RequirementsGroup>{"key": [<Requirement>{ownerName: "VF Test"}, <Requirement>{ownerName: "VF Test"}]},
      capabilities: new CapabilitiesGroup()
    }, <Component>{});
    component.selectedComponent.capabilities['key'] =  [<Capability>{ownerName: "VF Test"}];      
    component.valueEditModalInstance = valueEditModalInstance;
    component.valueEditModalInstance.innerModalContent.instance.name = newName;
    jest.spyOn(component.componentInstanceService, 'updateComponentInstance').mockReturnValue(of(<ComponentInstance>{name: newName}));
    component.saveInstanceName();
    
    expect(component.selectedComponent.name).toBe(newName);
    expect(component.selectedComponent.requirements['key'][0].ownerName).toEqual(newName);
    expect(component.selectedComponent.requirements['key'][1].ownerName).toEqual(newName);
    expect(component.selectedComponent.capabilities['key'][0].ownerName).toEqual(newName);
  });

  it('if update fails, name is reverted to old value', () => { 
    component.selectedComponent = new GroupInstance(<GroupInstance>{name: "GROUP NAME"});
    component.valueEditModalInstance = valueEditModalInstance;
    jest.spyOn(component.groupService, 'updateName').mockReturnValue(Observable.throw(new Error('Error')));
    component.saveInstanceName();
    expect(component.selectedComponent.name).toEqual("GROUP NAME");
  });

  it('policy instance uses policies service for update name', () => { 
    component.selectedComponent = new PolicyInstance(<PolicyInstance>{name: "Policy OLD NAME"});
    component.valueEditModalInstance = valueEditModalInstance;
    jest.spyOn(component.policiesService, 'updateName').mockReturnValue(of(true));
    component.saveInstanceName();
    expect(component.policiesService.updateName).toHaveBeenCalledTimes(1);
  });

  it('group instance uses groups service for update name', () => { 
    component.selectedComponent = new GroupInstance(<GroupInstance>{name: "GROUP NAME"});
    component.valueEditModalInstance = valueEditModalInstance;
    jest.spyOn(component.groupService, 'updateName').mockReturnValue(of(true));
    component.saveInstanceName();
    expect(component.groupService.updateName).toHaveBeenCalledTimes(1);
  });

});
