const path = require('path')

module.exports = {
    entry: path.resolve(__dirname, '../index.js'),
    packagejson: require(path.resolve(__dirname, '../package.json')),
    whiteList: [
        /@react-native-community\/.*/,
        /@react-navigation\/.*/,
        /react-native-?.*/,
        'react',
        'react-i18next',
        'dayjs',
        'decimal.js-light',
    ],
    blackList: [],
    out: path.resolve(__dirname, './bundles')
}