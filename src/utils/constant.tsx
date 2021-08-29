import { Dimensions, Platform } from 'react-native';

const { height, width } = Dimensions.get('window');
export const ScreenWidth = width;
export const ScreenHeight = height;

export const IsAndroid = Platform.OS === 'android';
export const IsIOS = Platform.OS === 'ios';
