import {NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture} from '@angular/core/testing';
import {SdcUiServices} from 'onap-ui-angular';
import 'rxjs/add/observable/of';
import {ConfigureFn, configureTests} from '../../../../../jest/test-config.helper';
import {CompositionGraphComponent} from "./composition-graph.component";
import {WorkspaceService} from "../../workspace/workspace.service";
import {ComponentInstance, GroupInstance, NodesFactory, ZoneInstance, ZoneInstanceMode} from "../../../../models";
import {EventListenerService} from "../../../../services";
import {
    CompositionGraphGeneralUtils,
    CompositionGraphNodesUtils,
    CompositionGraphZoneUtils,
    MatchCapabilitiesRequirementsUtils, ServicePathGraphUtils
} from "./utils";
import {CompositionGraphLinkUtils} from "./utils/composition-graph-links-utils";
import {ConnectionWizardService} from "./connection-wizard/connection-wizard.service";
import {CommonGraphUtils} from "./common/common-graph-utils";
import {CompositionGraphPaletteUtils} from "./utils/composition-graph-palette-utils";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {ComponentInstanceServiceNg2} from "../../../services/component-instance-services/component-instance.service";
import {CompositionService} from "../composition.service";
import {ModalService} from '../../../services/modal.service';
import {Store} from '@ngxs/store';
import {PoliciesService} from '../../../services/policies.service';
import {GroupsService} from '../../../services/groups.service';
import {PolicyInstance} from "../../../../models/graph/zones/policy-instance";
import {ZoneInstanceType} from "../../../../models/graph/zones/zone-instance";
import {GRAPH_EVENTS} from "../../../../utils/constants";
import * as cytoscape from "cytoscape";
import {ComponentMetadata} from "../../../../models/component-metadata";
import {Zone} from "../../../../models/graph/zones/zone";
import {SelectedComponentType, SetSelectedComponentAction} from "../common/store/graph.actions";

describe('composition graph component', () => {

    let fixture: ComponentFixture<CompositionGraphComponent>;
    let instance: CompositionGraphComponent;
    let eventServiceMock: Partial<EventListenerService>;
    let compositionGraphZoneUtils: Partial<CompositionGraphZoneUtils>;
    let generalGraphUtils: Partial<CompositionGraphGeneralUtils>;
    let workspaceServiceMock: Partial<WorkspaceService>;
    let policyService: Partial<PoliciesService>;
    let storeStub;
    let compositionGraphLinkUtils: Partial<CompositionGraphLinkUtils>;
    let nodesGraphUtils: Partial<CompositionGraphNodesUtils>;

    let createPolicyInstance = () => {
        let policy = new PolicyInstance();
        policy.targets = {COMPONENT_INSTANCES: [], GROUPS: []};
        return new ZoneInstance(policy, '', '');
    }

    beforeEach(
        async(() => {

            eventServiceMock = {
                notifyObservers: jest.fn(),
                unRegisterObserver: jest.fn()
            }

            compositionGraphZoneUtils = {
                endCyTagMode: jest.fn(),
                showZoneTagIndications: jest.fn(),
                hideZoneTagIndications: jest.fn(),
                hideGroupZoneIndications: jest.fn(),
                showGroupZoneIndications: jest.fn(),
                startCyTagMode: jest.fn()
            }

            workspaceServiceMock = {
                metadata: <ComponentMetadata>{
                    uniqueId: 'service_unique_id',
                    componentType: 'SERVICE'
                }
            }

            compositionGraphLinkUtils = {
                handleLinkClick: jest.fn(),
                getModifyLinkMenu: jest.fn()
            }

            storeStub = {
                dispatch: jest.fn()
            }
            policyService = {
                getSpecificPolicy: jest.fn()
            }

            generalGraphUtils = {
                zoomGraphTo: jest.fn()
            }

            nodesGraphUtils = {
                onNodesPositionChanged: jest.fn()
            }

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [CompositionGraphComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: NodesFactory, useValue: {}},
                        {provide: EventListenerService, useValue: eventServiceMock},
                        {provide: CompositionGraphZoneUtils, useValue: compositionGraphZoneUtils},
                        {provide: CompositionGraphGeneralUtils, useValue: generalGraphUtils},
                        {provide: CompositionGraphLinkUtils, useValue: compositionGraphLinkUtils},
                        {provide: CompositionGraphNodesUtils, useValue: nodesGraphUtils},
                        {provide: ConnectionWizardService, useValue: {}},
                        {provide: CommonGraphUtils, useValue: {}},
                        {provide: CompositionGraphPaletteUtils, useValue: {}},
                        {provide: TopologyTemplateService, useValue: {}},
                        {provide: ComponentInstanceServiceNg2, useValue: {}},
                        {provide: MatchCapabilitiesRequirementsUtils, useValue: {}},
                        {provide: CompositionService, useValue: {}},
                        {provide: SdcUiServices.LoaderService, useValue: {}},
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: SdcUiServices.NotificationsService, useValue: {}},
                        {provide: SdcUiServices.simplePopupMenuService, useValue: {}},
                        {provide: ServicePathGraphUtils, useValue: {}},
                        {provide: ModalService, useValue: {}},
                        {provide: PoliciesService, useValue: policyService},
                        {provide: GroupsService, useValue: {}},
                        {provide: Store, useValue: storeStub},
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(CompositionGraphComponent);
                instance = fixture.componentInstance;
                instance._cy = cytoscape({});
            });
        })
    );

    it('composition graph component should be defined', () => {
        expect(fixture).toBeDefined();
    });

    describe('on zone instance mode changed', () => {
        let newZoneInstance: ZoneInstance;

        beforeEach(
            async(() => {
                newZoneInstance = createPolicyInstance();
                instance.zoneTagMode = null;
                instance.zones = [];
                instance.zones[ZoneInstanceType.POLICY] = new Zone('Policies', 'P', ZoneInstanceType.POLICY);
                instance.zones[ZoneInstanceType.GROUP] = new Zone('Groups', 'G', ZoneInstanceType.GROUP);
                instance.activeZoneInstance = createPolicyInstance();
            }))

        it('zone instance in tag mode and we want to turn tag mode off', () => {
            instance.zoneTagMode = 'some_zone_id';
            instance.activeZoneInstance = newZoneInstance;
            instance.zoneInstanceModeChanged(ZoneInstanceMode.NONE, newZoneInstance, ZoneInstanceType.POLICY);
            expect(instance.eventListenerService.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_CANVAS_TAG_END, newZoneInstance);
            expect(instance.activeZoneInstance.mode).toBe(ZoneInstanceMode.SELECTED)
        })

        it('we are not in tag mode and policy instance mode changed to NONE - group and zone tag indication need to be removed', () => {
            instance.zoneInstanceModeChanged(ZoneInstanceMode.NONE, newZoneInstance, ZoneInstanceType.POLICY);
            expect(instance.compositionGraphZoneUtils.hideZoneTagIndications).toHaveBeenCalledWith(instance._cy);
            expect(instance.compositionGraphZoneUtils.hideGroupZoneIndications).toHaveBeenCalledWith(instance.zones[ZoneInstanceType.GROUP].instances);
        })

        it('we are not in tag mode and active zone instance gets hover/none - we dont actually change mode', () => {
            let newMode = ZoneInstanceMode.SELECTED;
            instance.zoneInstanceModeChanged(newMode, newZoneInstance, ZoneInstanceType.POLICY);
            expect(newZoneInstance.mode).toBe(newMode);
        })

        it('we are not in tag mode and zone instance mode changed to HOVER mode', () => {
            instance.zoneInstanceModeChanged(ZoneInstanceMode.HOVER, newZoneInstance, ZoneInstanceType.POLICY);
            expect(instance.compositionGraphZoneUtils.showZoneTagIndications).toHaveBeenCalledWith(instance._cy, newZoneInstance);
            expect(instance.compositionGraphZoneUtils.showGroupZoneIndications).toHaveBeenCalledWith(instance.zones[ZoneInstanceType.GROUP].instances, newZoneInstance);
            expect(instance.eventListenerService.notifyObservers).not.toHaveBeenCalled();
        })

        it('we are not in tag mode and mode changed to SELECTED', () => {
            instance.zoneInstanceModeChanged(ZoneInstanceMode.SELECTED, newZoneInstance, ZoneInstanceType.POLICY);
            expect(instance.compositionGraphZoneUtils.showZoneTagIndications).toHaveBeenCalledWith(instance._cy, newZoneInstance);
            expect(instance.compositionGraphZoneUtils.showGroupZoneIndications).toHaveBeenCalledWith(instance.zones[ZoneInstanceType.GROUP].instances, newZoneInstance);
            expect(instance.activeZoneInstance).toBe(newZoneInstance);
            expect(instance.eventListenerService.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_ZONE_INSTANCE_SELECTED, newZoneInstance);
            expect(instance.store.dispatch).toHaveBeenCalledWith(new SetSelectedComponentAction({
                component: newZoneInstance.instanceData,
                type: SelectedComponentType[ZoneInstanceType[newZoneInstance.type]]
            }));
            expect(instance.eventListenerService.notifyObservers).not.toHaveBeenCalledWith(GRAPH_EVENTS.ON_CANVAS_TAG_START, ZoneInstanceType.POLICY);
        })


        it('we are not in tag mode and and zone instance mode changed to TAG', () => {
            instance.zoneInstanceModeChanged(ZoneInstanceMode.TAG, newZoneInstance, ZoneInstanceType.POLICY);
            expect(instance.compositionGraphZoneUtils.showZoneTagIndications).toHaveBeenCalledWith(instance._cy, newZoneInstance);
            expect(instance.compositionGraphZoneUtils.showGroupZoneIndications).toHaveBeenCalledWith(instance.zones[ZoneInstanceType.GROUP].instances, newZoneInstance);
            expect(instance.activeZoneInstance).toBe(newZoneInstance);
            expect(instance.eventListenerService.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_ZONE_INSTANCE_SELECTED, newZoneInstance);
            expect(instance.store.dispatch).toHaveBeenCalledWith(new SetSelectedComponentAction({
                component: newZoneInstance.instanceData,
                type: SelectedComponentType[ZoneInstanceType[newZoneInstance.type]]
            }));
            expect(instance.compositionGraphZoneUtils.startCyTagMode).toHaveBeenCalledWith(instance._cy);
            expect(instance.eventListenerService.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_CANVAS_TAG_START, ZoneInstanceType.POLICY);
        })
    })

    it('unset active zone instance', () => {
        instance.activeZoneInstance = createPolicyInstance();
        instance.unsetActiveZoneInstance();
        expect(instance.activeZoneInstance).toBeNull();
        expect(instance.zoneTagMode).toBeNull();
    })

    it('zone background clicked - we are not in tag mode and active zone instance exist', () => {
        instance.activeZoneInstance = createPolicyInstance();
        jest.spyOn(instance, 'unsetActiveZoneInstance');
        jest.spyOn(instance, 'selectTopologyTemplate');
        instance.zoneBackgroundClicked();
        expect(instance.unsetActiveZoneInstance).toHaveBeenCalled();
        expect(instance.selectTopologyTemplate).toHaveBeenCalled();
    })

    it('zone background clicked - we are not in tag mode and no active zone instance exist', () => {
        jest.spyOn(instance, 'unsetActiveZoneInstance');
        jest.spyOn(instance, 'selectTopologyTemplate');
        instance.zoneBackgroundClicked();
        expect(instance.unsetActiveZoneInstance).not.toHaveBeenCalled();
        expect(instance.selectTopologyTemplate).not.toHaveBeenCalled();
    })

    it('on zoom in', () => {
        jest.spyOn(instance, 'zoom');
        instance.zoom(true);
        expect(instance.generalGraphUtils.zoomGraphTo).toHaveBeenCalledWith(instance._cy, instance._cy.zoom() + .1);
    })

    it('on zoom out', () => {
        jest.spyOn(instance, 'zoom');
        instance.zoom(false);
        expect(instance.generalGraphUtils.zoomGraphTo).toHaveBeenCalledWith(instance._cy, instance._cy.zoom() - .1);
    })

    describe('cytoscape tap end event have been called', () => {

        it('canvas background was clicked while zone instance in tag mode, zone instance still selected in tag mode)', () => {
            let event = <Cy.EventObject>{cyTarget: instance._cy};
            instance.zoneTagMode = 'instance_in_tag'
            instance.onTapEnd(event);
            expect(instance.zoneTagMode).toBe('instance_in_tag');
        })

        it('canvas background was clicked and no zone instance selected, topology template is now selected', () => {
            let event = <Cy.EventObject>{cyTarget: instance._cy};
            jest.spyOn(instance, 'selectTopologyTemplate');
            instance.onTapEnd(event);
            expect(instance.selectTopologyTemplate).toHaveBeenCalled();
            expect(instance.eventListenerService.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED);
        })

        it('canvas background was clicked and zone instance was selected, topology template is now selected and zone instance is unselected', () => {
            let event = <Cy.EventObject>{cyTarget: instance._cy};
            instance.activeZoneInstance = createPolicyInstance();
            jest.spyOn(instance, 'selectTopologyTemplate');
            jest.spyOn(instance, 'unsetActiveZoneInstance');
            instance.onTapEnd(event);
            expect(instance.selectTopologyTemplate).toHaveBeenCalled();
            expect(instance.unsetActiveZoneInstance).toHaveBeenCalled();
        })


        it('canvas background was clicked and zone instance was selected, topology template is now selected and zone instance is unselected', () => {
            let event = <Cy.EventObject>{cyTarget: instance._cy};
            instance.activeZoneInstance = createPolicyInstance();
            jest.spyOn(instance, 'selectTopologyTemplate');
            jest.spyOn(instance, 'unsetActiveZoneInstance');
            instance.onTapEnd(event);
            expect(instance.selectTopologyTemplate).toHaveBeenCalled();
            expect(instance.unsetActiveZoneInstance).toHaveBeenCalled();
        })

        it('on simple edge clicked, open link menu and handle link click', () => {
            let event = <Cy.EventObject>{
                cyTarget: [{
                    isEdge: jest.fn().mockReturnValue(true),
                    data: jest.fn().mockReturnValue({type: 'simple'})
                }
            }];
            instance.openModifyLinkMenu = jest.fn();
            instance.onTapEnd(event);
            expect(instance.compositionGraphLinkUtils.handleLinkClick).toHaveBeenCalledWith(instance._cy, event);
            expect(instance.openModifyLinkMenu).toHaveBeenCalled();
        })

        it('on service path edge clicked, no menu is opened', () => {
            let event = <Cy.EventObject>{
                cyTarget: [{
                    isEdge: jest.fn().mockReturnValue(true),
                    data: jest.fn().mockReturnValue({type: 'service-path-link'})
                }]
            };
            instance.openModifyLinkMenu = jest.fn();
            instance.onTapEnd(event);
            expect(instance.compositionGraphLinkUtils.handleLinkClick).toHaveBeenCalledWith(instance._cy, event);
            expect(instance.openModifyLinkMenu).not.toHaveBeenCalled();
        })

        it('on drop after drag event (position has changed), call onNodesPositionChanged to update node position', () => {
            let event = <Cy.EventObject>{
                cyTarget: [{
                    isEdge: jest.fn().mockReturnValue(false),
                    position: jest.fn().mockReturnValue({x:2.11, y:2.44})
                }]
            };
            instance.currentlyClickedNodePosition = <Cy.Position>{x:2.33, y:2.44};
            instance.onTapEnd(event);
            let nodesMoved: Cy.CollectionNodes = instance._cy.$(':grabbed');
            expect(instance.nodesGraphUtils.onNodesPositionChanged).toHaveBeenCalledWith(instance._cy, instance.topologyTemplate, nodesMoved);

        })

        it('on node clicked (position not changed) while zone instance selected, unset active zone and call set selected instance', () => {
            let event = <Cy.EventObject>{
                cyTarget: [{
                    isEdge: jest.fn().mockReturnValue(false),
                    position: jest.fn().mockReturnValue({x:2.11, y:2.44}),
                    data: jest.fn().mockReturnValue({componentInstance: new ComponentInstance()})
                }],
            };
            instance.currentlyClickedNodePosition = <Cy.Position>{x:2.11, y:2.44};
            instance.activeZoneInstance = createPolicyInstance();
            jest.spyOn(instance, 'unsetActiveZoneInstance');
            jest.spyOn(instance, 'selectComponentInstance');
            instance.onTapEnd(event);
            expect(instance.unsetActiveZoneInstance).toHaveBeenCalled();
            expect(instance.selectComponentInstance).toHaveBeenCalledWith(event.cyTarget[0].data().componentInstance);
        })
    })

    it('initial view mode will turn off all cytoscape events', () => {
        jest.spyOn(instance, 'isViewOnly').mockReturnValue(true);
        jest.spyOn(instance._cy, 'off');
        instance.initViewMode();
        expect(instance._cy.off).toHaveBeenCalledWith('drag');
        expect(instance._cy.off).toHaveBeenCalledWith('handlemouseout');
        expect(instance._cy.off).toHaveBeenCalledWith('handlemouseover');
        expect(instance._cy.off).toHaveBeenCalledWith('canvasredraw');
        expect(instance._cy.off).toHaveBeenCalledWith('handletagclick');

    })
});
