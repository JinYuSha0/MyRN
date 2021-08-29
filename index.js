import 'react-native-gesture-handler';
import React from 'react';
import Home from '@registry/home';
import Test from '@registry/test';
import { ComponentName } from '@registry/app';
import { AppRegistry, LogBox, Text } from 'react-native';
import { name as appName } from './app.json';

const ComponentMap = {
  [appName]: Home,
  [ComponentName.Home]: Home,
  [ComponentName.Test]: Test,
};

Object.keys(ComponentMap).forEach(name => {
  AppRegistry.registerComponent(name, () => ComponentMap[name]);
});

// 忽略警告
LogBox.ignoreAllLogs();

// 字体默认样式
(function settingFont(Text) {
  let _render = Text.render;
  Text.render = function (...args) {
    const originText = _render.apply(this, args);
    const { style, numberOfLines, children } = originText.props;
    return React.cloneElement(originText, {
      allowFontScaling: false, // 防止字体随系统的大小而改变
      style: [
        {
          fontSize: 14,
          color: '#000000',
        },
        style,
      ],
    });
  };
})(Text);
