import { useEffect, useRef } from 'react';
import { EmitterSubscription } from 'react-native';
import { EventEmitter, registerEvent } from '@utils/rnBridge';
import { IsIOS, NativeConstants } from '@utils/constant';

export default function useSubscribeNative(
  eventNames: string[],
  listener: (eventName: string, event: any) => void,
) {
  const eventListeners = useRef<EmitterSubscription[]>([]);

  useEffect(() => {
    eventNames.forEach(async eventName => {
      if (IsIOS) {
        await registerEvent(NativeConstants.prefix + eventName)
      }
      eventListeners.current.push(
        EventEmitter.addListener(
          NativeConstants.prefix + eventName,
          listener.bind(null, eventName),
        ),
      );
    });

    return () => {
      eventListeners.current.forEach(eventListener => {
        if (eventListener && typeof eventListener.remove === 'function') {
          eventListener.remove()
        } else {
          EventEmitter.removeSubscription(eventListener);
        }
      });
    };
  }, []);
}
