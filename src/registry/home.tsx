import React, { memo } from 'react';
import App from './app';
import HomeNavigator from '@src/navigators/home';
import { ScreenProps } from '@src/app.context';

const Home: React.FC<ScreenProps> = props => {
  return (
    <App {...props}>
      <HomeNavigator />
    </App>
  );
};

export default memo(Home);
