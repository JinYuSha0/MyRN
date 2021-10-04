#!/usr/bin/env bash

echo "Cleaning all the things";
watchman watch-del-all;
rm -rf node_modules;
rm -rf $TMPDIR/react-*
rm -rf $TMPDIR/yarn-*
cd ios
rm -rf Pods;
rm -rf Podfile.lock;
rm -rf build;
yarn cache clean --force
pod cache clean --all
cd ../android
rm -rf build
cd ..
rm -rf ~/Library/Developer/Xcode/DerivedData

echo "Installing things again";
yarn install;
cd ios
pod install
cd ..;

yarn start -- --reset-cache;