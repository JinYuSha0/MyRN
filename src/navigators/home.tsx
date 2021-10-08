import React from 'react';
import Home from '@screens/home/home';
import { createStackNavigator } from '@react-navigation/stack';
import {
  HomeParamList,
  HomeNatigatorProps,
  HomeRouteName,
} from '@screens/home/types';
import { TouchableOpacity } from 'react-native-gesture-handler';
import { View } from 'react-native-ui-lib';
import { checkUpdate } from '@src/utils/rnBridge';
import RefreshSVG from '@assets/images/refresh.svg';

const Stack = createStackNavigator<HomeParamList>();

const HomeNavigator: React.FC<HomeNatigatorProps> = props => {
  const { routeName = HomeRouteName.Home, routeParams, screenOptions } = props;
  return (
    <Stack.Navigator initialRouteName={routeName} screenOptions={screenOptions}>
      <Stack.Screen
        name={HomeRouteName.Home}
        component={Home}
        initialParams={routeParams}
        options={{
          headerRight: props => (
            <View paddingR-8>
              <TouchableOpacity activeOpacity={0.7} onPress={checkUpdate}>
                <RefreshSVG width={22} fill="#fff" />
              </TouchableOpacity>
            </View>
          ),
        }}
      />
    </Stack.Navigator>
  );
};

export default HomeNavigator;
