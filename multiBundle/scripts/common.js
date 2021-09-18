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
const getNewestSourceMap = require('../utils/getNewestSourceMap');

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
      list.push({ [String(regOrName)]: true });
    } else if (regOrName instanceof RegExp) {
      list = list.concat(
        Object.keys(dependencies)
          .filter(name => regOrName.test(name))
          .map(name => ({ [name]: true })),
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
    if (!name.includes('/')) {
      result[name] = value;
    } else {
      const split = name.split('/');
      split.reduce((a, b, index) => {
        const level = Object.create(null);
        a[b] = index === split.length - 1 ? true : level;
        return level;
      }, result);
    }
  });

  return result;
})(
  bundleSplitConfig.packagejson.dependencies,
  bundleSplitConfig.whiteList,
  bundleSplitConfig.blackList,
);

const sourceMapPath = path.resolve(__dirname, '../sourceMap');
const nodeModulePath = path.join(process.cwd(), 'node_modules');
const codeDirPath = path.resolve(__dirname, '../temp');

let moduleIdMap = Object.create(null);
const platform = args['platform'] || 'android';
const bundleOutputPath = createDirIfNotExists(getBundleOutputPath(platform));
const assetsOutPuthPath = createDirIfNotExists(getAssetsOutputPath(platform));
const outputBundleFileName = `common.${platform}.bundle`;
const bundleOutputFilePath = path.resolve(
  createDirIfNotExists(bundleOutputPath),
  outputBundleFileName,
);
const [p, resolve] = deffered();
const sep = require('path').sep;
const detectFilter = path => {
  let filter = false;
  if (path.includes(nodeModulePath)) {
    // 外部依赖
    return true;
  }
  return filter;
};
const bundle = async platform => {
  const config = await loadMetroConfig(ctx);
  config.serializer.processModuleFilter = function (module) {
    const { path } = module;
    return detectFilter(path);
  };
  config.serializer.createModuleIdFactory = function () {
    return function (path) {
      if (detectFilter(path)) {
        const id = getModuleId(genPath(path));
        moduleIdMap[genPath(path)] = {
          id,
          hash: genFileHash(path),
        };
        return id;
      }
      return null;
    };
  };
  const server = new Server(config);
  try {
    const commonRequestOpts = {
      entryFile: path.join(process.cwd(), 'index.js'),
      dev: false,
      minify: true,
      platform,
    };
    const bundle = await output.build(server, commonRequestOpts);
    bundle.code =
      `var __BUNDLE_START_TIME__=this.nativePerformanceNow?nativePerformanceNow():Date.now(),__DEV__=false,process=this.process||{},__METRO_GLOBAL_PREFIX__='';process.env=process.env||{};process.env.NODE_ENV=process.env.NODE_ENV||"production";\r` +
      bundle.code;
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
    fs.writeFileSync(
      path.resolve(
        createDirIfNotExists(sourceMapPath),
        `moduleIdMap-${+new Date()}.json`,
      ),
      JSON.stringify(
        Object.assign({
          [bundleOutputFilePath]: {
            id: -1,
            hash: genFileHash(bundleOutputFilePath),
          },
          ...moduleIdMap,
        }),
        null,
        2,
      ),
    );
  } finally {
    server.end();
    resolve();
  }
};

if (!args.buz) {
  bundle(platform);
} else {
  resolve(true);
}

p.then(isBuz => {
  const pAll = [];
  let startId = Object.keys(moduleIdMap).length;
  if (isBuz) {
    startId = Object.keys(require(getNewestSourceMap())).length;
  }
  delDir(codeDirPath);
  analysisRegisterComponent().then(res => {
    for (let i = 0; i < Array.from(res.keys()).length; i++) {
      const component = Array.from(res.keys())[i];
      const entryFilePath = path.resolve(
        createDirIfNotExists(codeDirPath),
        `${component}.${Math.random().toString(36).split('.')[1]}.js`,
      );
      fs.writeFileSync(entryFilePath, res.get(component));
      pAll.push(
        bundleBuz(
          platform,
          component,
          entryFilePath,
          startId + i * 100000,
          isBuz,
        ),
      );
    }
    Promise.all(pAll)
      .then(childComponents => {
        if (!isBuz) {
          const components = {
            [outputBundleFileName]: {
              hash: genFileHash(bundleOutputFilePath),
            },
          };
          childComponents.forEach(componentHash => {
            Object.assign(components, componentHash);
          });
          fs.writeFileSync(
            path.resolve(bundleOutputPath, 'appSetting.json'),
            JSON.stringify(
              { components, timestamp: +new Date() },
              undefined,
              2,
            ),
          );
        }
      })
      .then(() => {
        console.log('end');
      });
  });
});
