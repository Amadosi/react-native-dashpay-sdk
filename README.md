
# react-native-dashpay-sdk

## Getting started

`$ npm install react-native-dashpay-sdk --save`

### Mostly automatic installation

`$ react-native link react-native-dashpay-sdk`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-dashpay-sdk` and add `RNDashpaySdk.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNDashpaySdk.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.dashpay.rndashpay.RNDashpaySdkPackage;` to the imports at the top of the file
  - Add `new RNDashpaySdkPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-dashpay-sdk'
  	project(':react-native-dashpay-sdk').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-dashpay-sdk/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-dashpay-sdk')
  	```


## Usage
```javascript
import RNDashpaySdk from 'react-native-dashpay-sdk';

const paymentRef = '12345';
const amount = '200';

// Simply call the pay function by passing the payment reference and amount to be paid
// This returns a promise
RNDashpaySdk.pay(paymentRef, amount)
.then(data=>console.log(data))
.catch(err=>console.log(err.message));
```
