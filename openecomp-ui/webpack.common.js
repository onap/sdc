'use strict';

let path = require('path');

let localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch (e) {
	console.log('Could not find local dev config.');
}
let devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);

module.exports = {
	entry: devConfig.bundles,
	resolve: {
		modules: [path.resolve('.'), path.join(__dirname, 'node_modules')],
		alias: {
			i18nJson: 'nfvo-utils/i18n/en.json', // only for default build, not through gulp
			'nfvo-utils': 'src/nfvo-utils',
			'nfvo-components': 'src/nfvo-components',
			'sdc-app': 'src/sdc-app',
			'react-select/dist/' : 'node_modules/react-select/dist/',
			'jquery' : 'node_modules/restful-js/node_modules/jquery'
		}
	},
	module: {
		rules: [
			{test: /\.(js|jsx)$/, loader: 'source-map-loader', exclude: [/node_modules/, path.resolve(__dirname, '../dox-sequence-diagram/')], enforce: 'pre'},
			{test: /\.(js|jsx)$/, use: [
				{loader : 'react-hot-loader'},
				{loader : 'babel-loader'},
				{loader : 'eslint-loader'}], exclude: [/node_modules/, path.resolve(__dirname, '../dox-sequence-diagram/')]},
			{test: /\.(css|scss)$/, use: [
				{loader: 'style-loader'},
				{loader: 'css-loader?sourceMap'},
				{loader: 'sass-loader?sourceMap', options: { output: { path: path.join(__dirname, 'dist') } }}]},

			// required for font icons
			{test: /\.(woff|woff2)(\?.*)?$/, loader: 'url-loader?limit=16384&mimetype=application/font-woff'},
			{test: /\.(ttf|eot|otf)(\?.*)?$/, loader: 'file-loader'},
			{test: /\.(png|jpg|svg)(\?.*)?$/, loader: 'url-loader?limit=16384', exclude: path.join(__dirname, 'resources/images/svg') },
			{test: /\.html$/, use: [ {loader: 'html-loader'}]}
			]
	},
	plugins: []
};
