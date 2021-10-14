import React, { useMemo, useContext } from 'react';
export interface ScreenProps {
  routeName: string;
  bundleName: string;
  moduleName: string;
  statusBarMode: number;
  goBack?: boolean;
}

export interface AppContextProps extends Partial<ScreenProps> {}

const AppContext = React.createContext<AppContextProps>({});

export const AppProvider: React.FC<{ screenProps: ScreenProps }> = props => {
  const { children } = props;
  const value = useMemo(() => ({}), []);
  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export const useAppContext = () => {
  return useContext(AppContext);
};
