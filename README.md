
# react-native-react-native-gertec-pinpad

## Getting started

`$ npm install react-native-react-native-gertec-pinpad --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-gertec-pinpad`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-react-native-gertec-pinpad` and add `RNReactNativeGertecPinpad.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNReactNativeGertecPinpad.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeGertecPinpadPackage;` to the imports at the top of the file
  - Add `new RNReactNativeGertecPinpadPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-gertec-pinpad'
  	project(':react-native-react-native-gertec-pinpad').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-gertec-pinpad/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-gertec-pinpad')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNReactNativeGertecPinpad.sln` in `node_modules/react-native-react-native-gertec-pinpad/windows/RNReactNativeGertecPinpad.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using React.Native.Gertec.Pinpad.RNReactNativeGertecPinpad;` to the usings at the top of the file
  - Add `new RNReactNativeGertecPinpadPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNReactNativeGertecPinpad from 'react-native-react-native-gertec-pinpad';

// TODO: What to do with the module?
RNReactNativeGertecPinpad;
```
  