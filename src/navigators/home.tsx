import React from 'react';
import Home from '@screens/home/home';
import { CommonScreenProps } from './index';
import { createStackNavigator } from '@react-navigation/stack';
import {
  HomeParamList,
  HomeNatigatorProps,
  HomeRouteName,
} from '@screens/home/types';

const Stack = createStackNavigator<HomeParamList>();

const HomeNavigator: React.FC<HomeNatigatorProps> = props => {
  const { routeName = HomeRouteName.Home, routeParams } = props;
  return (
    <Stack.Navigator
      initialRouteName={routeName}
      screenOptions={CommonScreenProps}>
      <Stack.Screen
        name={HomeRouteName.Home}
        component={Home}
        initialParams={routeParams}
      />
    </Stack.Navigator>
  );
};

export default HomeNavigator;
