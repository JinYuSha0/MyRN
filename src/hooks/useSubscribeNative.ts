import { useEffect, useRef } from 'react';
import { EmitterSubscription } from 'react-native';
import { EventEmitter } from '@utils/rnBridge';
import { NativeConstants } from '@utils/constant';

export default function useSubscribeNative(
  eventNames: string[],
  listener: (eventName: string, event: any) => void,
) {
  const eventListeners = useRef<EmitterSubscription[]>([]);

  useEffect(() => {
    eventNames.forEach(eventName => {
      eventListeners.current.push(
        EventEmitter.addListener(
          NativeConstants.prefix + eventName,
          listener.bind(null, eventName),
        ),
      );
    });

    return () => {
      eventListeners.current.forEach(eventListener => {
        EventEmitter.removeSubscription(eventListener);
      });
    };
  }, []);
}
