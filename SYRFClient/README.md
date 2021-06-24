
# react-native-syrf-client

## Getting started

`$ npm install react-native-syrf-client --save`

### Mostly automatic installation

`$ react-native link react-native-syrf-client`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-syrf-client` and add `RNSyrfClient.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNSyrfClient.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSyrfClientPackage;` to the imports at the top of the file
  - Add `new RNSyrfClientPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-syrf-client'
  	project(':react-native-syrf-client').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-syrf-client/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-syrf-client')
  	```


## Usage
```javascript
import RNSyrfClient from 'react-native-syrf-client';

// TODO: What to do with the module?
RNSyrfClient;
```
  