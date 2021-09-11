import {
  StackNavigationOptions,
  CardStyleInterpolators,
} from '@react-navigation/stack';
import { IsAndroid } from '@src/utils/constant';

export const CommonScreenProps: StackNavigationOptions = {
  headerStatusBarHeight: IsAndroid ? 0 : undefined,
  headerStyle: {
    backgroundColor: '#000',
    elevation: 0, // android 隐藏导航栏底部横线
    shadowColor: '#000',
    shadowOffset: { height: 0, width: 0 }, // ios 隐藏导航栏底部横线
  },
  headerTitleAlign: 'center',
  headerBackTitleVisible: false, // 隐藏返回按钮后面的标题
  headerTintColor: '#FFF',
  headerTitleStyle: {
    fontWeight: 'bold',
    color: '#FFF',
  },
  cardStyle: {
    backgroundColor: '#F2F2F2',
  },
  headerLeftContainerStyle: {
    paddingLeft: 8,
  },
  cardStyleInterpolator: CardStyleInterpolators.forHorizontalIOS,
};
