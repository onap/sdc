import {InputFEModel} from "./input-fe-model";

describe('InputFEModel', () => {
  [
    { inputValue: undefined, expectedValue: null },
    { inputValue: null, expectedValue: null },
    { inputValue: ' this is a test   ', expectedValue: 'this is a test' }
  ].forEach(({inputValue, expectedValue}) => {
      describe(`input is ${inputValue}`, () => {
        it(`should return ${expectedValue}`, () => {
          const inputFeModel = new InputFEModel({} as any);
          inputFeModel.updateDefaultValueObj(inputValue, true);
          expect(inputFeModel.getJSONDefaultValue()).toBe(expectedValue);
        });
      });
  });
});
