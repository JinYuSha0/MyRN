import { TNativeConstants } from '@src/types/nativeConstants';
import { Dimensions, Platform } from 'react-native';
import { RNBridge } from './rnBridge';

const { height, width } = Dimensions.get('window');
export const ScreenWidth = width;
export const ScreenHeight = height;

export const IsAndroid = Platform.OS === 'android';
export const IsIOS = Platform.OS === 'ios';

export const NativeConstants: TNativeConstants = RNBridge?.getConstants();

export const EventName = {
  CHECK_UPDATE_START: 'CHECK_UPDATE_START',
  CHECK_UPDATE_SUCCESS: 'CHECK_UPDATE_SUCCESS',
  CHECK_UPDATE_FAILURE: 'CHECK_UPDATE_FAILURE',
  CHECK_UPDATE_DOWNLOAD_NEWS_APPLY: 'CHECK_UPDATE_DOWNLOAD_NEWS_APPLY',
};
