import React from 'react';
import { NavigationContainer, DarkTheme } from '@react-navigation/native';
import { AppProvider, ScreenProps } from '../app.context';

export enum ComponentName {
  Home = 'Home',
  Test = 'Test',
}

const App: React.FC<ScreenProps> = props => {
  const { children, ...rest } = props;
  const content = (
    <NavigationContainer theme={DarkTheme}>{children}</NavigationContainer>
  );
  return <AppProvider screenProps={rest}>{content}</AppProvider>;
};

export default App;
