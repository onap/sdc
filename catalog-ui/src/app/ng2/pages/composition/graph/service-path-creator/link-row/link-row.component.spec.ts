import {async, ComponentFixture} from "@angular/core/testing";
import {CacheService} from "../../../../../services/cache.service";
import {ConfigureFn, configureTests} from "../../../../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {LinkRowComponent} from "./link-row.component";
import {DropdownValue} from "../../../../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {MapItemData, ServicePathMapItem} from "../../../../../../models/graph/nodes-and-links-map";

describe('artifact form component', () => {

    let fixture: ComponentFixture<LinkRowComponent>;
    let cacheServiceMock: Partial<CacheService>;

    beforeEach(
        async(() => {


            cacheServiceMock = {
                contains: jest.fn(),
                remove: jest.fn(),
                set: jest.fn(),
                get: jest.fn()
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [LinkRowComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: []
                    ,
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(LinkRowComponent);
            });
        })
    );


    it('should match current snapshot of artifact form component', () => {
        expect(fixture).toMatchSnapshot();
    });


    it('ngOnChanges() -> in case data exist -> call to parseInitialData()' ,() => {
        // init values / mock functions
        let data = 'something';
        fixture.componentInstance.parseInitialData = jest.fn();
        fixture.componentInstance.data = data;

        // call to the tested function
        fixture.componentInstance.ngOnChanges();

        // expect that
        expect(fixture.componentInstance.parseInitialData).toHaveBeenCalledWith(data);
    });

    it('onSourceSelected() -> in case id -> srcCP, link.fromCP, link.toNode, link.toCP, target, targetCP should be updated accordingly' ,() => {
        // init values / mock functions
        let id = 'id';
        let data = 'data';
        let link = {
            fromCP:'testVal',
            toNode:'testVal',
            toCP:'testVal'
        }
        let target = ['val1', 'val2'];
        let targetCP = ['val1', 'val2'];

        fixture.componentInstance.findOptions = jest.fn();
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => 'dummyConvertedVal');
        fixture.componentInstance.data = data;
        fixture.componentInstance.link = link;
        fixture.componentInstance.target = target;
        fixture.componentInstance.targetCP = targetCP;

        // call to the tested function
        fixture.componentInstance.onSourceSelected(id);

        // expect that
        expect(fixture.componentInstance.findOptions).toHaveBeenCalledWith(data, id);
        expect(fixture.componentInstance.srcCP).toBe('dummyConvertedVal');
        expect(fixture.componentInstance.link.fromCP).toBe('');
        expect(fixture.componentInstance.link.toNode).toBe('');
        expect(fixture.componentInstance.link.toCP).toBe('');
        expect(fixture.componentInstance.target.length).toBe(0);
        expect(fixture.componentInstance.targetCP.length).toBe(0);
    });

    it('onSourceSelected() -> in case id undefined -> No Change to srcCP, link.fromCP, link.toNode, link.toCP, target, targetCP' ,() => {
        // init values / mock functions
        let id;
        let data = 'data';
        let link = {
            fromCP:'testVal',
            toNode:'testVal',
            toCP:'testVal'
        }
        let target = ['val1', 'val2'];
        let targetCP = ['val1', 'val2'];

        fixture.componentInstance.findOptions = jest.fn();
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => 'dummyConvertedVal');
        fixture.componentInstance.data = data;
        fixture.componentInstance.link = link;
        fixture.componentInstance.target = target;
        fixture.componentInstance.targetCP = targetCP;

        // call to the tested function
        fixture.componentInstance.onSourceSelected(id);

        // expect that
        expect(fixture.componentInstance.link.fromCP).toBe(link.fromCP);
        expect(fixture.componentInstance.link.toNode).toBe(link.toNode);
        expect(fixture.componentInstance.link.toCP).toBe(link.toCP);
        expect(fixture.componentInstance.target.length).toBe(2);
        expect(fixture.componentInstance.target[0]).toBe('val1')
        expect(fixture.componentInstance.targetCP.length).toBe(2);
        expect(fixture.componentInstance.targetCP[1]).toBe('val2');
    });

    it('onSrcCPSelected() -> in case id  -> Verify target, link.fromCPOriginId, link.toNode, link.toCP, targetCP.length' ,() => {
        // init values / mock functions
        let id = 'id';
        let link = {
            fromNode:'testVal',
            toCPOriginId: 'initValue_ShouldBeChanged'
        };
        let option1 = {
            id: 'something'
        };
        let option2 = {
            id: 'id',
            data: {"ownerId":1}
        };

        fixture.componentInstance.link = link;
        fixture.componentInstance.findOptions = jest.fn(() => [option1, option2]);
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => 'dummyConvertedVal');

        // call to the tested function
        fixture.componentInstance.onSrcCPSelected(id);

        // expect that
        expect(fixture.componentInstance.target).toBe('dummyConvertedVal');
        expect(fixture.componentInstance.link.fromCPOriginId).toBe(option2.data.ownerId);
        expect(fixture.componentInstance.link.toNode).toBe('');
        expect(fixture.componentInstance.link.toCP).toBe('');
        expect(fixture.componentInstance.targetCP.length).toBe(0);

    });

    it('onSrcCPSelected() -> in case id undefined -> Verify target, link.fromCPOriginId, link.toNode, link.toCP, targetCP.length' ,() => {
        // init values / mock functions
        let id;

        let targetInput:Array<DropdownValue> = [{value:'Value', label:'Label', hidden:true, selected:true}];

        let linkInput = {
            fromCPOriginId:'expectedLinkFromCPOriginId',
            toNode:'expectedLinkToNode',
            toCP:'expectedLinkToCP',
            // Link Object
            canEdit:true,
            canRemove:true,
            isFirst:true,
            // ForwardingPathLink Object
            ownerId:'',
            fromNode:'',
            fromCP:'',
            toCPOriginId:''
        }

        fixture.componentInstance.target = targetInput;
        fixture.componentInstance.link = linkInput;
        fixture.componentInstance.targetCP = targetInput;


        // call to the tested function
        fixture.componentInstance.onSrcCPSelected(id);

        // expect that
        expect(fixture.componentInstance.target).toBe(targetInput);
        expect(fixture.componentInstance.link.fromCPOriginId).toBe('expectedLinkFromCPOriginId');
        expect(fixture.componentInstance.link.toNode).toBe('expectedLinkToNode');
        expect(fixture.componentInstance.link.toCP).toBe('expectedLinkToCP');
        expect(fixture.componentInstance.targetCP.length).toBe(1);
    });

    it('onTargetSelected() -> in case id  -> Verify targetCP & link.toCP' ,() => {
        // init values / mock functions
        let id = 'id';
        let link = {
            toCP:'testVal'
        }
        let targetCP = ['val1', 'val2'];

        fixture.componentInstance.findOptions = jest.fn();
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => 'dummyConvertedVal');
        fixture.componentInstance.link = link;
        fixture.componentInstance.targetCP = targetCP;

        // call to the tested function
        fixture.componentInstance.onTargetSelected(id);

        // expect that
        expect(fixture.componentInstance.targetCP).toBe('dummyConvertedVal');
        expect(fixture.componentInstance.link.toCP).toBe('');

    });

    it('onTargetSelected() -> in case id undefined -> Verify targetCP & link.toCP' ,() => {
        // init values / mock functions
        let id;
        let link = {
            toCP:'toCP_testVal'
        }
        let targetCP = ['val1', 'val2'];

        fixture.componentInstance.findOptions = jest.fn();
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => 'dummyConvertedVal');
        fixture.componentInstance.link = link;
        fixture.componentInstance.targetCP = targetCP;

        // call to the tested function
        fixture.componentInstance.onTargetSelected(id);

        // expect that
        expect(fixture.componentInstance.targetCP.length).toBe(2);
        expect(fixture.componentInstance.targetCP).toEqual(['val1', 'val2']);
        expect(fixture.componentInstance.link.toCP).toBe('toCP_testVal');
    });

    it('onTargetCPSelected() -> in case id  -> Validate toCPOriginId' ,() => {
        // init values / mock functions
        let id = 'id';
        let link = {
            toNode:'testVal',
            toCPOriginId: 'initValue_ShouldBeChanged'
        };
        let option1 = {
             id: 'something'
        };
        let option2 = {
             id: 'id',
             data: {"ownerId":1}
        };
        fixture.componentInstance.link = link;
        fixture.componentInstance.findOptions = jest.fn(() => [option1, option2]);

        // call to the tested function
        fixture.componentInstance.onTargetCPSelected(id);

        // expect that
        expect(fixture.componentInstance.link.toCPOriginId).toBe(option2.data.ownerId);
    });

    it('onTargetCPSelected() -> in case id undefined -> Validate toCPOriginId' ,() => {
        // init values / mock functions
        let id;
        let link = {
            toNode:'testVal',
            toCPOriginId: 'initValue_ShouldRemain'
        };
        let option1 = {
            id: 'something'
        };
        let option2 = {
            id: 'id',
            data: {"ownerId":1}
        };
        fixture.componentInstance.link = link;
        fixture.componentInstance.findOptions = jest.fn(() => [option1, option2]);

        // call to the tested function
        fixture.componentInstance.onTargetCPSelected(id);

        // expect that
        expect(fixture.componentInstance.link.toCPOriginId).toBe('initValue_ShouldRemain');
    });


    it('findOptions() -> in case item.data.options -> Validate return item.data.options' ,() => {
        // init values / mock functions
        const innerMapItemData1: MapItemData = { id: 'innerMapItemData1_id', name: 'innerMapItemData1_name', options: []};
        const innerServicePathItem: ServicePathMapItem = { id: 'innerServicePathItem_id', data: innerMapItemData1 };
        const mapItemData1: MapItemData = { id: 'mapItemData1_id', name: 'mapItemData1_name', options: [innerServicePathItem]};

        const servicePathItem: ServicePathMapItem = { id: 'servicePathItem_id', data: mapItemData1 };
        const arrServicePathItems: ServicePathMapItem[] = [servicePathItem];

        let nodeOrCPId: string = servicePathItem.id;

        // call to the tested function
        let res = fixture.componentInstance.findOptions(arrServicePathItems, nodeOrCPId);

        // expect that
        expect(res).toEqual([innerServicePathItem]);
    });

    it('findOptions() -> in case NOT item || item.data || item.data.options -> Validate return null' ,() => {
        // init values / mock functions
        let item = [{
            // data: {
                data:{
                    name:'data_name',
                    id: 'data_id'
                },
                name:'name',
                id: 'id'
            // }
        }];
        let items: Array<ServicePathMapItem> = item;
        let nodeOrCPId: string = 'someString';

        // call to the tested function
        let res = fixture.componentInstance.findOptions(items, nodeOrCPId);

        // expect that
        expect(res).toBe(null);
    });

    it('convertValuesToDropDownOptions() -> Verify that the result is sorted' ,() => {
        // init values / mock functions
        const mapItemData1: MapItemData = { id: 'Z_ID', name: 'Z_NAME'};
        const servicePathItem1: ServicePathMapItem = { id: 'Z_servicePathItem_id', data: mapItemData1 };

        const mapItemData2: MapItemData = { id: 'A_ID', name: 'A_NAME'};
        const servicePathItem2: ServicePathMapItem = { id: 'A_servicePathItem_id', data: mapItemData2 };

        const mapItemData3: MapItemData = { id: 'M_ID', name: 'M_NAME'};
        const servicePathItem3: ServicePathMapItem = { id: 'M_servicePathItem_id', data: mapItemData3 };

        const arrServicePathItems: ServicePathMapItem[] = [servicePathItem1, servicePathItem2, servicePathItem3];

        // call to the tested function
        let res = fixture.componentInstance.convertValuesToDropDownOptions(arrServicePathItems);

        // expect that
        expect(res.length).toBe(3);
        expect(res[0].value).toBe("A_servicePathItem_id");
        expect(res[0].label).toBe("A_NAME");
        expect(res[1].value).toBe("M_servicePathItem_id");
        expect(res[1].label).toBe("M_NAME");
        expect(res[2].value).toBe("Z_servicePathItem_id");
        expect(res[2].label).toBe("Z_NAME");

    });

    it('parseInitialData() -> link.fromNode Exist => Verify srcCP' ,() => {
        // init values / mock functions

        //Simulate Array<ServicePathMapItem to pass to the function
        const mapItemData1: MapItemData = { id: 'mapItemID', name: 'mapItemName'};
        const servicePathItem1: ServicePathMapItem = { id: 'servicePathItemId', data: mapItemData1 };
        const arrServicePathItems: ServicePathMapItem[] = [servicePathItem1];

        //Simulate link
        let link = {
            fromNode:'testVal'
        };
        fixture.componentInstance.link = link;

        //Simulate the response from convertValuesToDropDownOptions()
        const value = "expected_id_fromNode";
        const label = "expected_label_fromNode"
        let result:Array<DropdownValue> = [];
        result[0] =  new DropdownValue(value, label);
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => result);

        //Simulate the response from findOptions()
        const innerMapItemData1: MapItemData = { id: 'innerMapItemData1_id', name: 'innerMapItemData1_name', options: []};
        const options: ServicePathMapItem = { id: 'innerServicePathItem_id', data: innerMapItemData1 };
        fixture.componentInstance.findOptions = jest.fn(() => options);


        // call to the tested function
        fixture.componentInstance.parseInitialData(arrServicePathItems);

        // expect that
        expect(fixture.componentInstance.srcCP.length).toBe(1);
        expect(fixture.componentInstance.srcCP[0]).toEqual({
            "value": value,
            "label": label,
            "hidden": false,
            "selected": false
        });
    });

    it('parseInitialData() -> link.fromNode & link.fromCP Exist => Verify srcCP' ,() => {
        // init values / mock functions

        //Simulate Array<ServicePathMapItem to pass to the function
        const mapItemData1: MapItemData = { id: 'mapItemID', name: 'mapItemName'};
        const servicePathItem1: ServicePathMapItem = { id: 'servicePathItemId', data: mapItemData1 };
        const arrServicePathItems: ServicePathMapItem[] = [servicePathItem1];

        //Simulate link
        let link = {
            fromNode:'testVal',
            fromCP: 'testVal'
        };
        fixture.componentInstance.link = link;

        //Simulate the response from convertValuesToDropDownOptions()
        const value = "expected_id_fromNode_and_fromCP";
        const label = "expected_label_fromNode_and_fromCP"
        let result:Array<DropdownValue> = [];
        result[0] =  new DropdownValue(value, label);
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => result);

        //Simulate the response from findOptions()
        const innerMapItemData1: MapItemData = { id: 'innerMapItemData1_id', name: 'innerMapItemData1_name', options: []};
        const options: ServicePathMapItem = { id: 'innerServicePathItem_id', data: innerMapItemData1 };
        fixture.componentInstance.findOptions = jest.fn(() => options);


        // call to the tested function
        fixture.componentInstance.parseInitialData(arrServicePathItems);

        // expect that
        expect(fixture.componentInstance.srcCP.length).toBe(1);
        expect(fixture.componentInstance.srcCP[0]).toEqual({
            "value": value,
            "label": label,
            "hidden": false,
            "selected": false
        });
    });


    it('parseInitialData() -> link.fromNode & link.fromCP & link.toNode Exist => Verify srcCP' ,() => {
        // init values / mock functions

        //Simulate Array<ServicePathMapItem to pass to the function
        const mapItemData1: MapItemData = { id: 'mapItemID', name: 'mapItemName'};
        const servicePathItem1: ServicePathMapItem = { id: 'servicePathItemId', data: mapItemData1 };
        const arrServicePathItems: ServicePathMapItem[] = [servicePathItem1];

        //Simulate link
        let link = {
            fromNode:'testVal',
            fromCP: 'testVal',
            toNode: 'testVal'
        };
        fixture.componentInstance.link = link;

        //Simulate the response from convertValuesToDropDownOptions()
        const value = "expected_id_fromNode_and_fromCP_and_toNode";
        const label = "expected_label_fromNode_and_fromCP_and_toNode"
        let result:Array<DropdownValue> = [];
        result[0] =  new DropdownValue(value, label);
        fixture.componentInstance.convertValuesToDropDownOptions = jest.fn(() => result);

        //Simulate the response from findOptions()
        const innerMapItemData1: MapItemData = { id: 'innerMapItemData1_id', name: 'innerMapItemData1_name', options: []};
        const options: ServicePathMapItem = { id: 'innerServicePathItem_id', data: innerMapItemData1 };
        fixture.componentInstance.findOptions = jest.fn(() => options);


        // call to the tested function
        fixture.componentInstance.parseInitialData(arrServicePathItems);

        // expect that
        expect(fixture.componentInstance.srcCP.length).toBe(1);
        expect(fixture.componentInstance.srcCP[0]).toEqual({
            "value": value,
            "label": label,
            "hidden": false,
            "selected": false
        });
    });



});