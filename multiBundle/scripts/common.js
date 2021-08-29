const fs = require('fs');
const path = require('path');
const Server = require('metro/src/Server');
const output = require('metro/src/shared/output/bundle');
const loadConfig =
  require('@react-native-community/cli/build/tools/config/index').default;
const loadMetroConfig =
  require('@react-native-community/cli/build/tools/loadMetroConfig').default;
const saveAssets =
  require('@react-native-community/cli/build/commands/bundle/saveAssets').default;
const bundleSplitConfig = require('../bundleSplit.conf');
const genRequiredCode = require('../utils/genRequiredCode');
const genFileHash = require('../utils/genFileHash');
const genPathFactory = require('../utils/genPathFactory');
const getModuleId = require('../utils/getModuleId')(false, 0);
const deffered = require('../utils/deffered');
const { delDir, createDirIfNotExists } = require('../utils/fsUtils');
const {
  getBundleOutputPath,
  getAssetsOutputPath,
} = require('../utils/getOutputpath');
const analysisRegisterComponent = require('../utils/analysisRegisterComponent');
const bundleBuz = require('./bussines');

const args = require('../utils/args')();

const ctx = loadConfig();
const rootPath = ctx.root;
const genPath = genPathFactory(rootPath);

const commonDep = (function (dependencies, whiteList, blackList) {
  let list = [];
  const result = Object.create(null);

  // 白名单低优先级
  whiteList.forEach(regOrName => {
    if (typeof regOrName === 'string') {
      list = list.concat(
        Object.keys(dependencies)
          .filter(name => name === regOrName)
          .map(name => ({ [name]: dependencies[name] })),
      );
    } else if (regOrName instanceof RegExp) {
      list = list.concat(
        Object.keys(dependencies)
          .filter(name => regOrName.test(name))
          .map(name => ({ [name]: dependencies[name] })),
      );
    }
  });

  // 黑名单高优先级
  blackList.forEach(regOrName => {
    list.forEach((obj, index) => {
      const name = Object.entries(obj)[0][0];
      if (typeof regOrName === 'string') {
        regOrName === name && list.splice(index, 1);
      } else if (regOrName instanceof RegExp) {
        regOrName.test(name) && list.splice(index, 1);
      }
    });
  });

  list.forEach((obj, index) => {
    const [name, value] = Object.entries(obj)[0];
    result[name] = value;
  });

  return result;
})(
  bundleSplitConfig.packagejson.dependencies,
  bundleSplitConfig.whiteList,
  bundleSplitConfig.blackList,
);

const code = genRequiredCode(commonDep);
const codeDirPath = path.resolve(__dirname, '../temp');
delDir(codeDirPath);
const codeFilePath = path.resolve(
  createDirIfNotExists(codeDirPath),
  `.${Math.random().toString(36).split('.')[1]}.js`,
);
fs.writeFileSync(codeFilePath, code);
const sourceMapPath = path.resolve(__dirname, '../sourceMap');

const moduleIdMap = Object.create(null);
const platform = args['platform'] || 'android';
const bundleOutputPath = createDirIfNotExists(getBundleOutputPath(platform));
const assetsOutPuthPath = createDirIfNotExists(getAssetsOutputPath(platform));
const [p, resolve] = deffered();
const bundle = async platform => {
  const config = await loadMetroConfig(ctx);
  config.serializer.createModuleIdFactory = function () {
    return function (path) {
      const id = getModuleId(genPath(path));
      moduleIdMap[genPath(path)] = {
        id,
        hash: genFileHash(path),
      };
      return id;
    };
  };
  const server = new Server(config);
  try {
    const commonRequestOpts = {
      entryFile: codeFilePath,
      dev: false,
      minify: true,
      platform,
    };
    const bundle = await output.build(server, commonRequestOpts);
    output.save(
      bundle,
      {
        bundleOutput: path.resolve(
          createDirIfNotExists(bundleOutputPath),
          'common.android.bundle',
        ),
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
    fs.writeFileSync(
      path.resolve(
        createDirIfNotExists(sourceMapPath),
        `moduleIdMap-${+new Date()}.json`,
      ),
      JSON.stringify(moduleIdMap, null, 2),
    );
  } finally {
    fs.unlinkSync(codeFilePath);
    server.end();
    resolve();
  }
};

if (!args.buz) {
  bundle(platform);
} else {
  resolve();
}

p.then(() => {
  const pAll = [];
  analysisRegisterComponent().then(res => {
    let startId = Object.keys(moduleIdMap).length;
    for (let component of res.keys()) {
      const entryFilePath = path.resolve(
        createDirIfNotExists(codeDirPath),
        `${component}.${Math.random().toString(36).split('.')[1]}.js`,
      );
      fs.writeFileSync(entryFilePath, res.get(component));
      pAll.push(bundleBuz(platform, component, entryFilePath, startId));
    }
    Promise.all(pAll).then(() => {
      console.log('end');
    });
  });
});
