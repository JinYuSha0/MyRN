module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    [
      require.resolve('babel-plugin-module-resolver'),
      {
        cwd: 'babelrc',
        extensions: [
          '.ios.ts',
          '.android.ts',
          '.ts',
          '.ios.tsx',
          '.android.tsx',
          '.tsx',
          '.jsx',
          '.js',
          '.json',
        ],
        alias: {
          '@src': './src',
          '@apis': './src/apis',
          '@assets': './src/assets',
          '@components': './src/components',
          '@hooks': './src/hooks',
          '@navigators': './src/navigators',
          '@registry': './src/registry',
          '@screens': './src/screens',
          '@types': './src/types',
          '@utils': './src/utils',
        },
      },
    ],
    'react-native-reanimated/plugin',
  ],
  env: {
    production: {
      plugins: ['transform-remove-console'],
    },
  },
};
