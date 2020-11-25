import {InputFEModel} from "./input-fe-model";

describe('InputFEModel', () => {
  it('should return trimmed string', function () {
    const inputFeModel = new InputFEModel({} as any);
    inputFeModel.updateDefaultValueObj(' this is a test   ', true);
    expect(inputFeModel.getJSONDefaultValue()).toBe("this is a test");
  });
  it('should return null when input is null', () => {
    const inputFeModel = new InputFEModel({} as any);
    inputFeModel.updateDefaultValueObj(null, true);
    expect(inputFeModel.getJSONDefaultValue()).toBe(null);
  });
})
