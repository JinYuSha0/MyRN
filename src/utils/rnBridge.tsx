import { NativeModules } from 'react-native';

const { RNBridge } = NativeModules;

export function log(content: string) {
  RNBridge?.log(content);
}

export function openFromAssets(bundlePath: string, moduleName: string) {
  RNBridge?.openFromAssets(bundlePath, moduleName);
}
