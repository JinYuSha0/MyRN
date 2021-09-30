import { NativeModules, NativeEventEmitter } from 'react-native';
import { Component } from '@src/types/bridge';
import { IsIOS } from './constant';

export const { RNBridge } = NativeModules;

export const EventEmitter = new NativeEventEmitter(RNBridge);

export function log(content: string) {
  RNBridge?.log(content);
  console.log('[MY_RN_LOG]', content);
}

export enum StatusBarMode {
  NORMAL = 0x0000, // 默认
  TRANSPARENT = 0x0001, // 沉浸式
  DARK = 0x0010, // 黑色字体
  LIGHT = 0x0100, // 白色字体
  TDARK = 0x0011, // 沉浸式加黑色字体
  TLIGHT = 0x0101, // 沉浸式加白色字体
}

/**
 * 打开指定模块
 * @param bundlePath
 * @param moduleName
 * @param statusBarMode
 */
export function openComponent(
  bundlePath: string,
  moduleName: string,
  statusBarMode: StatusBarMode = StatusBarMode.LIGHT,
) {
  RNBridge?.openComponent(bundlePath, moduleName, statusBarMode);
}

/**
 * 获取本机所有模块
 * @returns
 */
export function getAllComponent(): Promise<Component[]> {
  return RNBridge?.getAllComponent();
}

/**
 * ios注册事件
 * @param eventName 
 * @returns 
 */
export function registerEvent(eventName: string): Promise<boolean> {
  if (!IsIOS) return Promise.resolve(true)
  return RNBridge?.registerEvent(eventName)
}