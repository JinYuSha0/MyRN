import React from 'react';
import { View, Text } from 'react-native-ui-lib';
import { StyleSheet, Button } from 'react-native';
import { HomeRouteName, HomeScreenProps } from './types';
import { ScreenHeight, ScreenWidth } from '@src/utils/constant';
import { openFromAssets } from '@utils/rnBridge';

const Home: React.FC<HomeScreenProps<HomeRouteName.Home>> = props => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Hello World!</Text>
      <Button
        title={'jump to test'}
        onPress={() => openFromAssets('test.buz.android.bundle', 'Test')}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: ScreenWidth,
    height: ScreenHeight,
  },
  text: {
    color: '#000',
  },
});

export default Home;
