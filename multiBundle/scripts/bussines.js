const fs = require('fs');
const path = require('path');
const compressing = require('compressing');
const Server = require('metro/src/Server');
const output = require('metro/src/shared/output/bundle');
const loadConfig =
  require('@react-native-community/cli/build/tools/config/index').default;
const loadMetroConfig =
  require('@react-native-community/cli/build/tools/loadMetroConfig').default;
const saveAssets =
  require('@react-native-community/cli/build/commands/bundle/saveAssets').default;
const { createDirIfNotExists, delDir } = require('../utils/fsUtils');
const {
  getBundleOutputPath,
  getAssetsOutputPath,
  genBuzBundleOutputPath,
  genBuzAssetsOutputPath,
} = require('../utils/getOutputpath');
const getNewestSourceMap = require('../utils/getNewestSourceMap');
const genPathFactory = require('../utils/genPathFactory');
const genFileHash = require('../utils/genFileHash');

const ctx = loadConfig();
const rootPath = ctx.root;
const genPath = genPathFactory(rootPath);

const bunele = async (platform, component, entryFile, startId, isBuz) => {
  const getModuleId = require('../utils/getModuleId')(true, startId);
  const bundlePath = isBuz
    ? genBuzBundleOutputPath(platform, component)
    : getBundleOutputPath(platform);
  const assetsPath = isBuz
    ? genBuzAssetsOutputPath(platform, component)
    : getAssetsOutputPath(platform);
  if (isBuz) {
    delDir(bundlePath);
    delDir(assetsPath);
  }
  const bundleOutputPath = createDirIfNotExists(bundlePath);
  const assetsOutPuthPath = createDirIfNotExists(assetsPath);
  const fileName = `${String(component).toLocaleLowerCase()}.buz.${String(
    platform,
  ).toLocaleLowerCase()}.bundle`;
  const bundleOutputFilePath = path.resolve(
    createDirIfNotExists(bundleOutputPath),
    fileName,
  );
  const config = await loadMetroConfig(ctx);
  const moduleIdMap = require(getNewestSourceMap());
  const commonHash = Object.keys(moduleIdMap)
    .map(key => moduleIdMap[key])
    .find(o => o.id === -1).hash;
  config.serializer.processModuleFilter = function (module) {
    const { path } = module;
    if (
      path.indexOf('polyfills') >= 0 ||
      path.indexOf('__prelude__') >= 0 ||
      path.indexOf('source-map') >= 0
    ) {
      return false;
    }
    const filePath = genPath(path);
    const moduleInfo = moduleIdMap[filePath];
    if (moduleInfo && moduleInfo.hash === genFileHash(path)) {
      return false;
    }
    return true;
  };
  config.serializer.createModuleIdFactory = function () {
    return path => {
      path = genPath(path);
      const commonModule = moduleIdMap[path];
      if (commonModule) {
        return commonModule.id;
      }
      const id = getModuleId(path, true);
      return id;
    };
  };
  const commonRequestOpts = {
    entryFile,
    dev: false,
    minify: true,
    platform,
  };
  const server = new Server(config);
  try {
    const bundle = await output.build(server, commonRequestOpts);
    output.save(
      bundle,
      {
        bundleOutput: bundleOutputFilePath,
        encoding: 'utf-8',
      },
      console.log,
    );
    const outputAssets = await server.getAssets({
      ...Server.DEFAULT_BUNDLE_OPTIONS,
      ...commonRequestOpts,
      bundleType: 'todo',
    });
    await saveAssets(
      outputAssets,
      platform,
      createDirIfNotExists(assetsOutPuthPath),
    );
    const hash = genFileHash(bundleOutputFilePath);
    if (isBuz) {
      fs.writeFileSync(
        path.join(bundleOutputPath, 'setting.json'),
        JSON.stringify(
          {
            hash,
            commonHash,
            bundleName: fileName,
            componentName: component,
            timestamp: +new Date(),
          },
          undefined,
          2,
        ),
      );
      console.log(
        bundleOutputPath,
        path.join(bundleOutputPath, '../', `${component}-${hash}.zip`),
      );
      await compressing.zip.compressDir(
        bundleOutputPath,
        path.join(bundleOutputPath, '../', `${component}-${hash}.zip`),
        {
          relativePath: hash,
          ignoreBase: true,
        },
      );
    }
    return {
      [fileName]: {
        hash,
        componentName: component,
      },
    };
  } finally {
    server.end();
  }
};

module.exports = bunele;
