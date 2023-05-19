/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

 import React from 'react';
 import {
   SafeAreaView,
   StyleSheet,
   ScrollView,
   View,
   Text,
   StatusBar,
   Button,
   NativeEventEmitter,
 } from 'react-native';
 
 import {
   Header,
   LearnMoreLinks,
   Colors,
   DebugInstructions,
   ReloadInstructions,
 } from 'react-native/Libraries/NewAppScreen';
 
 import RNTrustVisionRnsdkFramework, {
   TVConst,
   TVErrorCode,
 } from 'react-native-trust-vision-SDK';
 
 const App: () => React$Node = () => {
   const onSessionConnect = (event) => {
     console.log(event);  
   };
   
   // DeviceEventEmitter.addListener('TVSDKEvent', onSessionConnect);
 
   const onPress = async () => {
     try {
       console.log("TVSDK - init")
       const clientSettingJsonString = "{\"settings\":{\"sdk_settings\":{\"active_liveness_settings\":{\"face_tracking_setting\":{\"android_terminate_threshold\":0.002847,\"android_warning_threshold\":0.001474,\"enable\":true,\"ios_terminate_threshold\":0.003393,\"ios_warning_threshold\":0.002176,\"limit_for\":\"all_flow\",\"max_warning_time\":5},\"flow_interval_time_ms\":1500,\"limit_time_liveness_check\":{\"enable\":true,\"limit_time_second\":15},\"record_video\":{\"enable\":false},\"save_encoded_frames\":{\"enable\":true,\"frames_interval_ms\":120},\"terminate_if_no_face\":{\"enable\":true,\"max_invalid_frame\":5,\"max_time_ms\":1000}},\"id_detection_settings\":{\"scan_qr_settings\":{\"enable\":false,\"limit_time_second\":10}, \"save_frame_settings\":{\"enable\":true,\"frames_interval_ms\":190}, \"limit_time_settings\":{\"enable\":true,\"limit_time_second\":20}, \"flow_interval_time_ms\": 2000, \"auto_capture\":{\"enable\":false,\"show_capture_button\":true},\"blur_check\":{\"enable\":true,\"threshold\":0.29},\"disable_capture_button_if_alert\":true,\"glare_check\":{\"enable\":true,\"threshold\":0.001},\"id_detection\":{\"enable\":true}},\"liveness_settings\":{\"vertical_check\":{\"enable\":true,\"threshold\":40}}}}}"
       await RNTrustVisionRnsdkFramework.initialize(
         clientSettingJsonString,
         'vi',
         true
       );
 
       console.log("TVSDK - events listener")
       const tvsdkEmitter = new NativeEventEmitter(RNTrustVisionRnsdkFramework);
       const subscription = tvsdkEmitter.addListener('TVSDKFrameBatch',(event) => {
           console.log("TVSDKFrameBatch - ", event)
       });
 
       // ======== Id capturing ========
       const cardType = {
         id: 'card_id',
         name: 'card_name',
         orientation: TVConst.Orientation.LANDSCAPE,
         hasBackSide: true,
       };

       console.log("TVSDK - start id capturing")
       const idFrontConfig = {
         cardType: cardType,
         cardSide: TVConst.CardSide.FRONT,
         isEnableSound: true,
         isEnableSanityCheck: true,
         isReadBothSide: false,
         skipConfirmScreen: false,
         isEnableDetectingIdCardTampering: true,
       };
       const idFrontResult = await RNTrustVisionRnsdkFramework.startIdCapturing(idFrontConfig);
       console.log("idFrontResult", idFrontResult);

       const idBackConfig = {
         cardType: cardType,
         cardSide: TVConst.CardSide.BACK,
         isEnableSound: true,
         isEnableSanityCheck: true,
         isReadBothSide: false,
         skipConfirmScreen: false,
         isEnableDetectingIdCardTampering: true,
       };
       const idBackResult = await RNTrustVisionRnsdkFramework.startIdCapturing(idBackConfig);
       console.log("idBackResult", idBackResult);

       // ======== Selfie Capturing ========
       console.log("TVSDK - start selfie capturing")
       // selfie capturing
       const selfieConfig = {
         cameraOption: TVConst.SelfieCameraMode.FRONT,
         livenessMode: TVConst.LivenessMode.ACTIVE,
         isEnableSound: true,
         skipConfirmScreen: true
       };

       console.log('Selfie Config', selfieConfig);
       const selfieResult = await RNTrustVisionRnsdkFramework.startSelfieCapturing(
         selfieConfig
       );
       console.log('Selfie Result', selfieResult);
     } catch (e) {
       console.log('Error: ', e.code, ' - ', e.message);
     }
   };
 
   return (
     <>
       <StatusBar barStyle="dark-content" />
       <SafeAreaView>
         <Button title={'Press Me'} onPress={onPress} />
         <ScrollView
           contentInsetAdjustmentBehavior="automatic"
           style={styles.scrollView}>
           <Header />
           {global.HermesInternal == null ? null : (
             <View style={styles.engine}>
               <Text style={styles.footer}>Engine: Hermes</Text>
             </View>
           )}
           <View style={styles.body}>
             <View style={styles.sectionContainer}>
               <Text style={styles.sectionTitle}>Step One</Text>
               <Text style={styles.sectionDescription}>
                 Edit <Text style={styles.highlight}>App.js</Text> to change this
                 screen and then come back to see your edits.
               </Text>
             </View>
             <View style={styles.sectionContainer}>
               <Text style={styles.sectionTitle}>See Your Changes</Text>
               <Text style={styles.sectionDescription}>
                 <ReloadInstructions />
               </Text>
             </View>
             <View style={styles.sectionContainer}>
               <Text style={styles.sectionTitle}>Debug</Text>
               <Text style={styles.sectionDescription}>
                 <DebugInstructions />
               </Text>
             </View>
             <View style={styles.sectionContainer}>
               <Text style={styles.sectionTitle}>Learn More</Text>
               <Text style={styles.sectionDescription}>
                 Read the docs to discover what to do next:
               </Text>
             </View>
             <LearnMoreLinks />
           </View>
         </ScrollView>
       </SafeAreaView>
     </>
   );
 };
 
 const styles = StyleSheet.create({
   scrollView: {
     backgroundColor: Colors.lighter,
   },
   engine: {
     position: 'absolute',
     right: 0,
   },
   body: {
     backgroundColor: Colors.white,
   },
   sectionContainer: {
     marginTop: 32,
     paddingHorizontal: 24,
   },
   sectionTitle: {
     fontSize: 24,
     fontWeight: '600',
     color: Colors.black,
   },
   sectionDescription: {
     marginTop: 8,
     fontSize: 18,
     fontWeight: '400',
     color: Colors.dark,
   },
   highlight: {
     fontWeight: '700',
   },
   footer: {
     color: Colors.dark,
     fontSize: 12,
     fontWeight: '600',
     padding: 4,
     paddingRight: 12,
     textAlign: 'right',
   },
 });
 
 export default App;
 