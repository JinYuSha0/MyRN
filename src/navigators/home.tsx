import React from 'react';
import Home from '@screens/home/home';
import { createStackNavigator } from '@react-navigation/stack';
import {
  HomeParamList,
  HomeNatigatorProps,
  HomeRouteName,
} from '@screens/home/types';

const Stack = createStackNavigator<HomeParamList>();

const HomeNavigator: React.FC<HomeNatigatorProps> = props => {
  const { routeName = HomeRouteName.Home, routeParams, screenOptions } = props;
  return (
    <Stack.Navigator initialRouteName={routeName} screenOptions={screenOptions}>
      <Stack.Screen
        name={HomeRouteName.Home}
        component={Home}
        initialParams={routeParams}
      />
    </Stack.Navigator>
  );
};

export default HomeNavigator;
