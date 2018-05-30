module.exports = function () {
	let localDevConfig = {};
	try {
		localDevConfig = require('../devConfig');
	} catch (e) {}
	const devConfig = Object.assign({}, require('../devConfig.defaults'), localDevConfig);
	let devPort = process.env.PORT || devConfig.port;
	devConfig.port = devPort;
	return devConfig;
}();
