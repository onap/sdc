import {InputFEModel} from "./input-fe-model";

describe('InputFEModel', () => {
  it('should return trimmed string', function () {
    const inputFeModel = new InputFEModel({} as any);
    inputFeModel.updateDefaultValueObj(' this is a test   ', true);
    expect(inputFeModel.getJSONDefaultValue()).toBe("this is a test");
  });
})
