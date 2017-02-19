var webpack = require('webpack');
var path = require('path');

var PATHS = {
	SRC: path.resolve(__dirname, 'src/main/webapp'),
	TARGET: path.resolve(__dirname, 'dist')
};

var devmode = (process.env.npm_lifecycle_event === 'start');

var entry = [];
if (devmode) {
	entry.push('babel-polyfill');
	entry.push(path.resolve(PATHS.SRC, 'lib/main.jsx'));
} else {
	entry.push(path.resolve(PATHS.SRC, 'lib/ecomp/asdc/sequencer/Sequencer.jsx'));
}

var config = {
	entry: entry,
	output: {
		path: PATHS.TARGET,
		filename: 'index.js',
		libraryTarget: 'umd'
	},
	resolve: {
		extensions: ['', '.js', '.jsx']
	},
	eslint: {
		failOnWarning: false,
		failOnError: true,
		configFile: 'eslintrc.json'
	},
	devtool: 'eval-source-map',
	module: {
		preLoaders: [{
			test: /\.(js|jsx)?$/,
			loader: 'eslint-loader',
			exclude: /node_modules/
		}],
		loaders: [{
			test: /\.(js|jsx)$/,
			include: path.join(PATHS.SRC, 'lib'),
			loader: 'babel-loader',
			exclude: /node_modules/,
			query: {
				presets: ['es2015', 'react']
			}
		}, {
			test: /\.css$/,
			loaders: ['style', 'css']
		}, {
			test: /\.(png|woff|woff2|eot|ttf|otf)$/,
			loader: 'url-loader?limit=100000'
		}, {
			test: /\.scss$/,
			include: path.join(PATHS.SRC, 'res'),
			loaders: ['style', 'css', 'sass']
		}, {
			test: /\.html$/,
			include: path.join(PATHS.SRC, 'lib'),
			loaders: ['raw']
		}, {
			test: /\.json$/,
			include: path.join(PATHS.SRC, 'lib'),
			loaders: ['json']
		}, {
			test: /\.svg$/,
			loader: 'svg-sprite?' + JSON.stringify({
				name: '[name]_[hash]',
				prefixize: true
			})
		}]
	},
	externals: (devmode ? {} : {
		'd3': 'd3',
		'lodash': 'lodash',
		'react': 'react',
		'react-dnd': 'react-dnd',
		'react-dnd-html5-backend': 'react-dnd-html5-backend',
		'react-dom': 'react-dom',
		'react-redux': 'react-redux',
		'react-select': 'react-select',
		'redux': 'redux'
	}),
	devServer: {
		port: 4096,
		quiet: false,
		contentBase: 'src/main/webapp',
		proxy: {
			'/services/*': {
				target: 'http://localhost:38080/asdc-sequencer',
				secure: false
			}
		}
	}
};

module.exports = config;
