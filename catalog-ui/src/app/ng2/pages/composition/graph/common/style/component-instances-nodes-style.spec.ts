import {async} from "@angular/core/testing";
import {ComponentInstanceNodesStyle} from "./component-instances-nodes-style";


describe('component instance nodes style component', () => {

    beforeEach(
        async(() => {
            const createElement = document.createElement.bind(document);
            document.createElement = (tagName) => {
                if (tagName === 'canvas') {
                    return {
                        getContext: () => ({
                            font: "",
                            measureText: (x) => ({width: x.length})
                        }),
                    };
                }
                return createElement(tagName);
            };
        })
    );

    it('verify getGraphDisplayName for String.length smaller than 67 chars', () => {
        let inputString = 'SomeText';
        let expectedRes = inputString;
        let res = ComponentInstanceNodesStyle.getGraphDisplayName(inputString);
        expect(res).toBe(expectedRes);
    });

    it('verify getGraphDisplayName for String.length greater than 67 chars', () => {
        let inputString = 'AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDDEEEEEEEEEEFFFFFFFFFFGGGGGGGGGG12345678';
        let expectedRes = 'AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDDEEEEEEEEEEFFFFFFFFF...';
        let res = ComponentInstanceNodesStyle.getGraphDisplayName(inputString);
        expect(res).toBe(expectedRes);
    });
}