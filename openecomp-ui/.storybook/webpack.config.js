var ASDCConfig = require('../webpack.common.js');

module.exports = function(baseConfig, configType) {
    baseConfig.module.loaders =  baseConfig.module.loaders.concat([
        {test: /\.(css|scss)$/, loaders: ['style', 'css?sourceMap', 'sass?sourceMap']},

        // required for font icons
        {test: /\.(woff|woff2)(\?.*)?$/, loader: 'url-loader?limit=16384&mimetype=application/font-woff'},
        {test: /\.(ttf|eot|otf)(\?.*)?$/, loader: 'file-loader'},
        {test: /\.(png|jpg|svg)(\?.*)?$/, loader: 'url-loader?limit=16384'},

        {test: /\.json$/, loaders: ['json']},
        {test: /\.html$/, loaders: ['html']}
    ]);
    baseConfig.resolve = { root: ASDCConfig.resolve.modules, alias:  ASDCConfig.resolve.alias };
    return baseConfig;
}
