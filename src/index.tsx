import { NativeModules } from 'react-native';

const IncomingCall = NativeModules.IncomingCall;

export function onDisplayIncomingCall(): any {
  IncomingCall.onDisplayIncomingCall();
}
