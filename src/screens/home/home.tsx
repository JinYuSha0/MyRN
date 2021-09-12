import React, { useEffect, useState } from 'react';
import { View, Text, Button, Card } from 'react-native-ui-lib';
import { StyleSheet, ScrollView } from 'react-native';
import { HomeRouteName, HomeScreenProps } from './types';
import { ScreenHeight, ScreenWidth } from '@src/utils/constant';
import { openFromAssets, getAllComponent } from '@utils/rnBridge';
import { Component } from '@src/types/bridge';

const Home: React.FC<HomeScreenProps<HomeRouteName.Home>> = props => {
  const [components, setComponents] = useState<Component[]>([]);
  useEffect(() => {
    getAllComponent().then(components => {
      setComponents(components);
      console.log(components);
    });
  }, []);
  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={{ paddingHorizontal: 20 }}>
      {components.map(component => {
        return (
          <Card key={component.BundleName} style={styles.componentWrapper}>
            <View row style={{ justifyContent: 'space-between' }}>
              <Text>{component.ComponentName || 'Common'}</Text>
              <Text>{component.Version}</Text>
            </View>
            <Text marginT-6>{component.FilePath}</Text>
            <Text marginT-6>{component.Hash}</Text>
            <Button
              marginT-20
              disabled={!component.ComponentName}
              onPress={() =>
                openFromAssets(component.BundleName, component.ComponentName)
              }
              label={'JUMP'}
            />
          </Card>
        );
      })}
    </ScrollView>
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
  componentWrapper: {
    padding: 20,
    backgroundColor: '#f8f8f8',
    marginTop: 20,
  },
});

export default Home;
