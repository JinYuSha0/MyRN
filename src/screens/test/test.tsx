import React from 'react';
import { View, Text } from 'react-native-ui-lib';
import { StyleSheet, Image } from 'react-native';
import { ScreenHeight, ScreenWidth } from '@src/utils/constant';

const Test: React.FC<{}> = props => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>This is assets test!</Text>
      {/* <Image style={styles.image} source={require('@assets/images/bg2.jpeg')} /> */}
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
  image: {
    width: ScreenWidth,
    height: 200,
  },
});

export default Test;
