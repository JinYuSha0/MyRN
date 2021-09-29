import React from 'react';
import Test from '@screens/test/test';
import { createStackNavigator } from '@react-navigation/stack';
import {
  HomeParamList,
  HomeNatigatorProps,
  HomeRouteName,
} from '@screens/home/types';

const Stack = createStackNavigator<HomeParamList>();

const TestNavigator: React.FC<HomeNatigatorProps> = props => {
  const { routeName = HomeRouteName.Home, routeParams, screenOptions } = props;
  return (
    <Stack.Navigator initialRouteName={routeName} screenOptions={screenOptions}>
      <Stack.Screen
        name={HomeRouteName.Home}
        component={Test}
        initialParams={routeParams}
        options={{ title: 'TEST' }}
      />
    </Stack.Navigator>
  );
};

export default TestNavigator;
