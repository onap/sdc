import { TestBed } from '@angular/core/testing';
import { SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/Rx';
import CollectionNodes = Cy.CollectionNodes;
import { Mock } from 'ts-mockery';
import { ComponentInstance } from '../../../../../models';
import { ComponentMetadata } from '../../../../../models/component-metadata';
import { Resource } from '../../../../../models/components/resource';
import { CompositionCiNodeCp } from '../../../../../models/graph/nodes/composition-graph-nodes/composition-ci-node-cp';
import { CompositionCiNodeVl } from '../../../../../models/graph/nodes/composition-graph-nodes/composition-ci-node-vl';
import { EventListenerService } from '../../../../../services';
import CollectionEdges = Cy.CollectionEdges;
import { GRAPH_EVENTS } from '../../../../../utils/constants';
import { ServiceServiceNg2 } from '../../../../services/component-services/service.service';
import { TopologyTemplateService } from '../../../../services/component-services/topology-template.service';
import { ComponentGenericResponse } from '../../../../services/responses/component-generic-response';
import { QueueServiceUtils } from '../../../../utils/queue-service-utils';
import { WorkspaceService } from '../../../workspace/workspace.service';
import { CompositionService } from '../../composition.service';
import { CommonGraphUtils } from '../common/common-graph-utils';
import { CompositionGraphGeneralUtils } from './composition-graph-general-utils';
import { CompositionGraphNodesUtils } from './composition-graph-nodes-utils';

describe('composition graph nodes utils', () => {

    const CP_TO_DELETE_ID = 'cp1';
    const VL_TO_DELETE_ID = 'vl';
    const CP2_ID = 'cp2';

    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let service: CompositionGraphNodesUtils;
    let topologyServiceMock: TopologyTemplateService;
    let queueServiceMock: QueueServiceUtils;
    let workspaceServiceMock: WorkspaceService;
    let compositionServiceMock: CompositionService;
    let eventListenerServiceMock: EventListenerService;
    const cpInstanceMock: ComponentInstance = Mock.of<ComponentInstance>({
        uniqueId: CP_TO_DELETE_ID,
        isVl: () => false
    });
    const vlInstanceMock: ComponentInstance = Mock.of<ComponentInstance>({
        uniqueId: VL_TO_DELETE_ID,
        isVl: () => true
    });
    const cp2InstanceMock: ComponentInstance = Mock.of<ComponentInstance>({
        uniqueId: CP2_ID,
        isVl: () => false
    });

    const cyMock = Mock.of<Cy.Instance>({
        remove: jest.fn(),
        collection: jest.fn()
    });

    const serviceServiceMock = Mock.of<ServiceServiceNg2>({
        getComponentCompositionData : () => Observable.of(Mock.of<ComponentGenericResponse>())
    });

    // Instances on the graph cp, vl, cp2
    const cp = Mock.from<CompositionCiNodeCp>({ id: CP_TO_DELETE_ID, componentInstance: cpInstanceMock });
    const vl = Mock.from<CompositionCiNodeVl>({ id: VL_TO_DELETE_ID, componentInstance: vlInstanceMock });
    const cp2 = Mock.from<CompositionCiNodeCp>({ id: CP2_ID, componentInstance: cp2InstanceMock });

    beforeEach(() => {

        loaderServiceMock = {
            activate: jest.fn(),
            deactivate: jest.fn()
        };

        topologyServiceMock = Mock.of<TopologyTemplateService>({
            deleteComponentInstance : () => Observable.of(cpInstanceMock)
        });

        queueServiceMock = Mock.of<QueueServiceUtils>({
            addBlockingUIAction : ( (f) => f() )
        });

        workspaceServiceMock = Mock.of<WorkspaceService>({
            metadata: Mock.of<ComponentMetadata>( { uniqueId: 'topologyTemplateUniqueId' } )
        });

        compositionServiceMock = Mock.of<CompositionService>({
            deleteComponentInstance : jest.fn()
        });

        eventListenerServiceMock = Mock.of<EventListenerService>({
            notifyObservers : jest.fn()
        });

        TestBed.configureTestingModule({
            imports: [],
            providers: [
                CompositionGraphNodesUtils,
                {provide: WorkspaceService, useValue: workspaceServiceMock},
                {provide: TopologyTemplateService, useValue: topologyServiceMock},
                {provide: CompositionService, useValue: compositionServiceMock},
                {provide: CompositionGraphGeneralUtils, useValue: {}},
                {provide: CommonGraphUtils, useValue: {}},
                {provide: EventListenerService, useValue: eventListenerServiceMock},
                {provide: QueueServiceUtils, useValue: queueServiceMock},
                {provide: ServiceServiceNg2, useValue: serviceServiceMock},
                {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock}
            ]
        });
        service = TestBed.get(CompositionGraphNodesUtils);
    });

    it('When a CP is deleted which is connected to a VL that has another leg to another CP, the VL is deleted as well', () => {
        // Prepare a VL that is connected to both CP and CP2
        const vlToDelete = Mock.of<CollectionNodes>({
            data:  () => vl,
            connectedEdges: () => Mock.of<CollectionEdges>({
                length: 2,
                connectedNodes: () => [cp, cp2] as CollectionNodes
            })
        });

        // Prepare a CP which is connected to a VL
        const cpToDelete = Mock.of<CollectionNodes>({
            data:  () => cp,
            connectedEdges: () => Mock.of<CollectionEdges>({
                length: 1,
                connectedNodes: () => [vlToDelete] as CollectionNodes
            })
        });
        service.deleteNode(cyMock, Mock.of<Resource>(), cpToDelete);
        expect(compositionServiceMock.deleteComponentInstance).toHaveBeenCalledWith(CP_TO_DELETE_ID);
        expect(eventListenerServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE, VL_TO_DELETE_ID);
        expect(eventListenerServiceMock.notifyObservers).toHaveBeenLastCalledWith(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE_SUCCESS, CP_TO_DELETE_ID);
        expect(cyMock.remove).toHaveBeenCalled();
    });

    it('When a CP is deleted which is solely connected to another VL the VL is not deleted', () => {
        // Prepare a VL that is connected only to 1 CP
        const vlToDelete = Mock.of<CollectionNodes>({
            data:  () => vl,
            connectedEdges: () => Mock.of<CollectionEdges>({
                length: 1,
                connectedNodes: () => [cp] as CollectionNodes
            })
        });

        // Prepare a CP which is connected to a VL
        const cpToDelete = Mock.of<CollectionNodes>({
            data:  () => cp,
            connectedEdges: () => Mock.of<CollectionEdges>({
                length: 1,
                connectedNodes: () => [vlToDelete] as CollectionNodes
            })
        });
        service.deleteNode(cyMock, Mock.of<Resource>(), cpToDelete);
        expect(compositionServiceMock.deleteComponentInstance).toHaveBeenCalledWith(CP_TO_DELETE_ID);
        expect(eventListenerServiceMock.notifyObservers).toHaveBeenLastCalledWith(GRAPH_EVENTS.ON_DELETE_COMPONENT_INSTANCE_SUCCESS, CP_TO_DELETE_ID);
        expect(eventListenerServiceMock.notifyObservers).toHaveBeenCalledTimes(1);
        expect(cyMock.remove).toHaveBeenCalled();
    });
});
