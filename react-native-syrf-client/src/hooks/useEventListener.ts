import { useEffect } from 'react';
import { NativeEventEmitter, NativeModules } from 'react-native';

export const useEventListener = (
  eventName: string,
  listener: (...args: any[]) => any,
) => {
  useEffect(() => {
    let eventEmitter: NativeEventEmitter;
    if (eventName) {
      eventEmitter = new NativeEventEmitter(NativeModules.SyrfClient);
      eventEmitter.addListener(eventName, listener);
    }
    return () => {
      if (eventName) {
        eventEmitter.removeAllListeners(eventName);
      }
    };
  }, [eventName, listener]);
};
