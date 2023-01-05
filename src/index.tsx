import { NativeModules } from 'react-native';

const IncomingCall = NativeModules.IncomingCall;

export function showIncomingCall(options: {
  channelName: string;
  channelId: string;
  timeout: number;
  component: string;
  callerName: string;
}): any {
  IncomingCall.showIncomingCall(options);
}
