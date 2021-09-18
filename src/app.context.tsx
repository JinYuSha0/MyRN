import React, { useMemo, useContext } from 'react';
import useSubscribeNative from '@hooks/useSubscribeNative';
import { EventName } from './utils/constant';

export interface ScreenProps {
  routeName: string;
  bundlePath: string;
  bundleType: 'assets' | 'file' | 'network';
  moduleName: string;
  statusBarMode: number;
}

export interface AppContextProps extends Partial<ScreenProps> {}

const AppContext = React.createContext<AppContextProps>({});

export const AppProvider: React.FC<{ screenProps: ScreenProps }> = props => {
  const { children } = props;
  const value = useMemo(() => ({}), []);
  useSubscribeNative(
    [
      EventName.CHECK_UPDATE_START,
      EventName.CHECK_UPDATE_SUCCESS,
      EventName.CHECK_UPDATE_FAILURE,
    ],
    (eventName: string, eventData: any) => {
      console.log(1111, eventName, eventData);
    },
  );
  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export const useAppContext = () => {
  return useContext(AppContext);
};
