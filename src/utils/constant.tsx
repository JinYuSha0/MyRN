import { TNativeConstants } from '@src/types/nativeConstants';
import { Dimensions, Platform, StatusBar } from 'react-native';
import { RNBridge } from './rnBridge';

const { height, width } = Dimensions.get('window');
export const ScreenWidth = width;
export const ScreenHeight = height;

export const IsAndroid = Platform.OS === 'android';
export const IsIOS = Platform.OS === 'ios';

export const NativeConstants: TNativeConstants = RNBridge?.getConstants();

export const StatusBarHeight = (() => {
  if (IsAndroid) {
    return StatusBar.currentHeight;
  } else {
    return 0;
  }
})();

export const EventName = {
  CHECK_UPDATE_START: 'CHECK_UPDATE_START',
  CHECK_UPDATE_SUCCESS: 'CHECK_UPDATE_SUCCESS', // type NetWorkRsult
  CHECK_UPDATE_FAILURE: 'CHECK_UPDATE_FAILURE', // errorMsg: string
  CHECK_UPDATE_DOWNLOAD_NEWS: 'CHECK_UPDATE_DOWNLOAD_NEWS', // type Component
  CHECK_UPDATE_DOWNLOAD_PROGRESS: 'CHECK_UPDATE_DOWNLOAD_PROGRESS', // { componentName: string, progress: number }
  CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS: 'CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS', // type Component
  CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE: 'CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE', // errorMsg: string
  CHECK_UPDATE_DOWNLOAD_NEWS_APPLY: 'CHECK_UPDATE_DOWNLOAD_NEWS_APPLY', // componentName: string
};
