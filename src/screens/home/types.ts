import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { StackNavigationOptions } from '@react-navigation/stack';

export enum HomeRouteName {
  Home = 'Home',
}

type HomeRouteParams = {
  [HomeRouteName.Home]: {};
};

/** 路由参数列表 (所有) */
export type HomeParamList = Omit<
  Record<HomeRouteName, undefined>,
  keyof HomeRouteParams
> &
  HomeRouteParams;

/** Natigator */
export type HomeNatigatorProps = {
  routeName?: HomeRouteName;
  routeParams?: ValueOf<HomeRouteParams>;
  screenOptions?: StackNavigationOptions;
};

/** Navigation */
export type HomeNavigationProps<
  RouteName extends HomeRouteName = HomeRouteName,
> = StackNavigationProp<HomeParamList, RouteName>;

/** Route */
export type HomeRouteProp<RouteName extends HomeRouteName> = RouteProp<
  HomeParamList,
  RouteName
>;

/** ScreenProps */
export type HomeScreenProps<RouteName extends HomeRouteName> = {
  navigation: HomeNavigationProps<RouteName>;
  route: HomeRouteProp<RouteName>;
};
