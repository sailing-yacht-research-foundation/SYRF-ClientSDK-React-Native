import { useEffect } from 'react';
import { EmitterSubscription, NativeEventEmitter, NativeModules } from 'react-native';

export const useEventListener = (
  eventName: string,
  listener: (...args: any[]) => any,
) => {
  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.SyrfClient);
    let eventListener: EmitterSubscription;
    if (eventName) {
      eventListener = eventEmitter.addListener(eventName, listener);
    }
    return () => {
      eventListener.remove();
      if (eventName) {
        eventEmitter.removeAllListeners(eventName);
      }
    };
  }, [eventName, listener]);
};
