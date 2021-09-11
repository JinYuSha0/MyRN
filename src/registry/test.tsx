import React, { memo } from 'react';
import App from './app';
import TestNavigator from '@src/navigators/test';
import { ScreenProps } from '@src/app.context';

const Test: React.FC<ScreenProps> = props => {
  return (
    <App {...props}>
      <TestNavigator />
    </App>
  );
};

export default memo(Test);
