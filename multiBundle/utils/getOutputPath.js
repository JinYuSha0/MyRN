const path = require('path');

/**
 * 生成bundle包输出目录
 * @param {*} platform
 * @returns
 */
function getBundleOutputPath(platform) {
  if (platform === 'android') {
    return path.resolve(__dirname, `../../android/app/src/main/assets/`);
  } else if (platform === 'ios') {
    // todo
  }
}

/**
 * 输出资源文件输出目录
 * @param {*} platform
 * @returns
 */
function getAssetsOutputPath(platform) {
  if (platform === 'android') {
    return path.resolve(__dirname, `../../android/app/src/main/res/`);
  } else if (platform === 'ios') {
    // todo
  }
}

module.exports = {
  getBundleOutputPath,
  getAssetsOutputPath,
};
