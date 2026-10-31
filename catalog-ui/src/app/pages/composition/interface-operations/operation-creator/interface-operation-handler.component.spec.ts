
import {of as observableOf, Observable} from 'rxjs';
import {InterfaceOperationHandlerComponent} from './interface-operation-handler.component';

describe('InterfaceOperationHandlerComponent interface types', () => {

    const interfaceTypes = {
        'tosca.interfaces.Root': [],
        'tosca.interfaces.node.lifecycle.Standard': ['start', 'create']
    };

    function loadInterfaceTypes(currentInterfaceType: string) {
        const componentService: any = {getInterfaceTypesByModel: jest.fn(() => observableOf(interfaceTypes))};
        const comp: any = new InterfaceOperationHandlerComponent(null, componentService, null);
        comp.interfaceType = currentInterfaceType;
        comp.operationToUpdate = {};
        comp.interfaceOperationDropDown = {};
        comp.loadInterfaceType();
        return comp;
    }

    it('does not offer an interface type that defines no operations', () => {
        const comp = loadInterfaceTypes(undefined);
        expect(comp.interfaceTypeOptions.map(option => option.value))
            .toEqual(['tosca.interfaces.node.lifecycle.Standard']);
    });

    it('keeps the current interface type even when it defines no operations', () => {
        const comp = loadInterfaceTypes('tosca.interfaces.Root');
        expect(comp.interfaceTypeOptions.map(option => option.value))
            .toEqual(['tosca.interfaces.Root', 'tosca.interfaces.node.lifecycle.Standard']);
        expect(comp.selectedInterfaceType.value).toBe('tosca.interfaces.Root');
    });
});
