/**
 * 获取运行参数
 * @returns
 */
module.exports = function () {
  const arr = process.argv.slice(2);
  const result = {};
  arr.forEach((value, index) => {
    if (index % 2 === 1) {
      result[arr[index - 1].replace(/-/g, '')] = value;
    }
  });
  return result;
};
