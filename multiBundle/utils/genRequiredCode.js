const generate = require('@babel/generator').default;
const types = require('@babel/types');

function genBuildImportDeclaration(t, source) {
  return t.importDeclaration([], t.stringLiteral(`${source}`));
}

/**
 * 生成common包import代码文件
 * @param {*} dependencies
 * @returns
 */
module.exports = function (dependencies) {
  const ast = {
    type: 'Program',
    body: [],
  };

  Object.keys(dependencies).map(dependency => {
    ast.body.push(genBuildImportDeclaration(types, dependency));
  });

  const { code } = generate(ast);

  return Buffer.from(code);
};
