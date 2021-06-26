import { useEffect } from 'react';
import { NativeEventEmitter, NativeModules } from 'react-native';

export const useEventListener = (
  eventName: string,
  listener: (...args: any[]) => any
) => {
  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.SyrfClient);
    const eventListener = eventEmitter.addListener(eventName, listener);

    return () => {
      eventListener.remove();
    };
  }, [eventName, listener]);
};
