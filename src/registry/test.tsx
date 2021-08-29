import React from 'react';
import { View, Text } from 'react-native-ui-lib';
import { StyleSheet } from 'react-native';
import { ScreenHeight, ScreenWidth } from '@src/utils/constant';

const Test: React.FC<{}> = props => {
  return (
    <View style={styles.container}>
      <Text style={styles.text}>This is test!</Text>
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

export default Test;
