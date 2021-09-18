const path = require('path');

module.exports = {
  entry: path.resolve(__dirname, '../index.js'),
  packagejson: require(path.resolve(__dirname, '../package.json')),
  whiteList: Array.from(new Set([])),
  blackList: [],
  out: path.resolve(__dirname, './bundles'),
};
