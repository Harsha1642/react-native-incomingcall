# react-native-incomingcall

## This library is in development, not ready for integration

## Installation

```sh
npm install @harsha1642/react-native-incomingcall

or

yarn add @harsha1642/react-native-incomingcall
```

### Android installation steps
In `AndroidManifest.xml`:
```java
// ...
        <service android:name="com.incomingcall.CallService" />

        <activity
            android:launchMode="singleInstance"
            android:exported="true"
            android:name="com.incomingcall.CallingActivity"
            android:showOnLockScreen="true"
            android:label="Intercom">
            <intent-filter>
                <action android:name="DESTROY_CALL" />
            </intent-filter>
        </activity>

        <activity
            android:supportsPictureInPicture="true"
            android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"
            android:launchMode="singleInstance"
            android:exported="true"
            android:name="com.incomingcall.AnswerCallActivity"
            android:showOnLockScreen="true"
            android:label="Intercom" />

        <receiver
            android:name="com.incomingcall.HungUpBroadcast"
            android:label="Intercom" />

     .....
      </application>
```

For RN < 0.60 :
react-native link @harsha1642/react-native-incomingcall

## Usage

```js
import { showIncomingCall } from '@harsha1642/react-native-incomingcall'

showIncomingCall({
    channelName: "channelName";
    channelId: "channelId";
    timeout: "timeout";
    component: "component";
    callerName: "callerName";
});

```

## Contributing

## License

---