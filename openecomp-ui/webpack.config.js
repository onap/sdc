'use strict';

const path = require('path');
const webpack = require('webpack');
const proxyServer = require('./proxy-server');

let localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch (e) {}
let devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);
let devPort = process.env.PORT || devConfig.port;

let webpackCommon = require('./webpack.common');

function getEntrySources(sources) {
	for (let i in sources) {
		if (sources.hasOwnProperty(i)) {
			sources[i].push('webpack-dev-server/client?http://localhost:' + devPort);
			sources[i].push('webpack/hot/only-dev-server');
		}
	}
	return sources;
}

let webpackDevConfig = Object.assign({}, webpackCommon, {
	entry: getEntrySources(devConfig.bundles),
	devtool: 'eval-source-map',
	output: {
		path: path.join(__dirname, 'dist'),
		publicPath: `http://localhost:${devPort}/onboarding/`,
		filename: '[name].js'
	},
	devServer: {
		port: devPort,
		historyApiFallback: true,
		publicPath: `http://localhost:${devPort}/onboarding/`,
		contentBase: path.join(__dirname, 'dist'),
		hot: true,
		inline: true,
		stats: {
			colors: true,
			exclude: ['node_modules']
		},
		setup: proxyServer
	},
	plugins: [
		new webpack.DefinePlugin({
			DEV: true,
			DEBUG: true
		}),
		new webpack.HotModuleReplacementPlugin(),
		new webpack.LoaderOptionsPlugin({
			options: {
				eslint: {
					configFile: './.eslintrc',
					emitError: true,
					emitWarning: true
				},
				context: '/'
			}
		})
	]
});

module.exports = webpackDevConfig;
