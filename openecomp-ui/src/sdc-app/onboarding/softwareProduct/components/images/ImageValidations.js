
import Validator from 'nfvo-utils/Validator.js';

export  const imageCustomValidations = {
	'version': value => Validator.validate('version', value, [{type: 'required', data: true}])
};
