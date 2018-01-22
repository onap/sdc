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
		extensions: ['.js', '.jsx']
	},
	devtool: 'eval-source-map',
	module: {
		rules: [
    		{test: /\.(js|jsx)$/, loader: 'eslint-loader', exclude: [/node_modules/], enforce: 'pre'},
            {test: /\.(js|jsx)$/, loader: 'babel-loader',
				exclude: /node_modules/,
				include: path.join(PATHS.SRC, 'lib')},
			{test: /\.(css)$/, use: [
                    {loader: 'style-loader'},
                    {loader: 'css-loader'}]},
			{test: /\.(png|woff|woff2|eot|ttf|otf)$/, loader: 'url-loader?limit=100000'},
            {
                test: /\.scss$/,
                include: path.join(PATHS.SRC, 'res'),
                loaders: ['style-loader', 'css-loader', 'sass-loader']
            },
			{
                test: /\.html$/,
                include: path.join(PATHS.SRC, 'lib'),
                loaders: ['raw-loader']
            },
			{
                test: /\.json$/,
                include: path.join(PATHS.SRC, 'lib'),
                loaders: ['json-loader']
            },
            {
                test: /\.svg$/,
                loader: 'svg-sprite-loader?' + JSON.stringify({
                    name: '[name]_[hash]',
                    prefixize: true
                })
            }
		]
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
                    failOnWarning: false,
                    failOnError: false,
                    configFile: 'eslintrc.json'
                },
                context: '/'
            }
        })
    ]
};

module.exports = config;
