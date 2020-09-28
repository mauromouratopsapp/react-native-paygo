
# react-native-paygo

## Getting started

`$ npm install react-native-paygo --save`

### Mostly automatic installation

`$ react-native link react-native-paygo`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import br.com.paygo.RNPaygoPackage;` to the imports at the top of the file
  - Add `new RNPaygoPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-paygo'
  	project(':react-native-paygo').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-paygo/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-paygo')
  	```

## Usage
```javascript
import RNPaygo from 'react-native-paygo';

// TODO: What to do with the module?
RNPaygo;
```
  