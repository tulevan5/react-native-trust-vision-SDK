
package com.trustingsocial.reactnativetrustvision;

import android.os.Build;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.util.CollectionUtils;
import com.trustingsocial.tvcoresdk.external.Event;
import com.trustingsocial.tvcoresdk.external.EventListener;
import com.trustingsocial.tvcoresdk.external.FrameBatch;
import com.trustingsocial.tvcoresdk.external.SelfieImage;
import com.trustingsocial.tvcoresdk.external.TVCapturingCallBack;
import com.trustingsocial.tvcoresdk.external.TVDetectionError;
import com.trustingsocial.tvcoresdk.external.TVDetectionResult;
import com.trustingsocial.tvcoresdk.external.TVIDConfiguration;
import com.trustingsocial.tvcoresdk.external.TVImageClass;
import com.trustingsocial.tvcoresdk.external.TVSelfieConfiguration;
import com.trustingsocial.tvsdk.BuildConfig;
import com.trustingsocial.tvsdk.TrustVisionSDK;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RNTrustVisionRnsdkFrameworkModule extends ReactContextBaseJavaModule {
    private static String INTERNAL_ERROR = "internal_error";
    private static String SDK_CANCELED = "sdk_canceled";

    private final ReactApplicationContext reactContext;

    public RNTrustVisionRnsdkFrameworkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNTrustVisionRnsdkFramework";
    }

    @ReactMethod
    public void initialize(String jsonConfiguration, String languageCode, boolean enableEventTracking, Promise promise) {
        EventListener eventListener = null;
        if (enableEventTracking) {
            eventListener = new EventListener() {
                @Override
                public void onEvent(@NonNull Event event) {
                    WritableMap params = new WritableNativeMap();
                    params.putString("name", event.getName());
                    Map<String, String> eventParams = event.getCustomParams();
                    if (eventParams != null) {
                        params.putMap("params", RNTrustVisionUtils.convertToWritableMap(eventParams));
                    }

                    reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("TVSDKEvent", params);
                }
            };
        }
        TrustVisionSDK.init(jsonConfiguration, languageCode, eventListener);
        promise.resolve("initSuccess");
    }

    @ReactMethod
    public void startIdCapturing(ReadableMap config, final Promise promise) {
        try {
            TVIDConfiguration configuration = RNTrustVisionUtils.convertIdConfigFromMap(config);
            TrustVisionSDK.startIDCapturing(getCurrentActivity(), configuration, new TVCapturingCallBack() {

                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }
                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, "sdk is canceled by user");
                }

                @Override
                public void onNewFrameBatch(@NonNull FrameBatch frameBatch) {
                    onReceivedNewFrameBatch(frameBatch);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void startSelfieCapturing(ReadableMap config, final Promise promise) {
        try {
            TVSelfieConfiguration configuration = RNTrustVisionUtils.convertSelfieConfigFromMap(config);
            TrustVisionSDK.startSelfieCapturing(getCurrentActivity(), configuration, new TVCapturingCallBack() {

                @Override
                public void onError(TVDetectionError tvDetectionError) {
                    promise.reject(tvDetectionError.getDetailErrorCode(), RNTrustVisionUtils.convertErrorString(tvDetectionError));
                }

                @Override
                public void onSuccess(TVDetectionResult tvDetectionResult) {
                    try {
                        promise.resolve(convertResult(tvDetectionResult));
                    } catch (Exception e) {
                        promise.reject(INTERNAL_ERROR, "Parse result error");
                    }
                }

                @Override
                public void onCanceled() {
                    promise.reject(SDK_CANCELED, "sdk is canceled by user");
                }

                @Override
                public void onNewFrameBatch(@NonNull FrameBatch frameBatch) {
                    onReceivedNewFrameBatch(frameBatch);
                }
            });
        } catch (Exception ex) {
            promise.reject(INTERNAL_ERROR, ex.getMessage());
        }
    }

    @ReactMethod
    public void getInfo(Promise promise) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putString("X-TV-OS-Platform", "android");
        writableMap.putString("X-TV-OS-Version", Build.VERSION.RELEASE);
        writableMap.putString("X-TV-SDK-Version", BuildConfig.ADDITIONAL_VERSION_NAME);
        writableMap.putString("X-TV-Device-Model", getDeviceModel());

        promise.resolve(writableMap);
    }

    private void onReceivedNewFrameBatch(FrameBatch frameBatch) {
        WritableMap params = new WritableNativeMap();
        try {
            params.putString("batchId", frameBatch.getId());
            params.putArray("frames", RNTrustVisionUtils.toWritableArray(frameBatch.getFrames()));
            params.putMap("metadata", RNTrustVisionUtils.convertToWritableMap(frameBatch.getMetadata()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("TVSDKFrameBatch", params);
    }

    private String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return "";
        }
        char first = string.charAt(0);
        if (Character.isUpperCase(first)) {
            return string;
        } else {
            return Character.toUpperCase(first) + string.substring(1);
        }
    }

//    private WritableMap convertResult(Bitmap bitmap, Bitmap bitmap1) throws Exception {
//        WritableMap result = new WritableNativeMap();
//        if (bitmap != null) {
//            result.putString("id_front_image", RNTrustVisionUtils.convertBitmapToBase64(bitmap));
//        }
//        if (bitmap1 != null) {
//            result.putString("id_back_image", RNTrustVisionUtils.convertBitmapToBase64(bitmap1));
//        }
//        return result;
//    }
//
//    private WritableMap convertResult(List<Bitmap> bitmapList) throws Exception {
//        WritableMap result = new WritableNativeMap();
//        WritableArray bitmapArray = new WritableNativeArray();
//        for (Bitmap bitmap : bitmapList) {
//            if (bitmap != null) {
//                bitmapArray.pushString(RNTrustVisionUtils.convertBitmapToBase64(bitmap));
//            }
//        }
//        result.putArray("selfie_images", bitmapArray);
//
//        return result;
//    }

    private WritableMap convertResult(TVDetectionResult tvDetectionResult) throws Exception {
        WritableMap result = new WritableNativeMap();
        if (tvDetectionResult.getSelfieImages() != null && !tvDetectionResult.getSelfieImages().isEmpty()) {
            WritableArray selfieArray = new WritableNativeArray();
            for (SelfieImage selfieImage : tvDetectionResult.getSelfieImages()) {
                selfieArray.pushMap(convertSelfieImage(selfieImage));
            }
            result.putArray("selfieImages", selfieArray);
        }

        if (tvDetectionResult.getFrontCardImage() != null) {
            result.putMap("idFrontImage", convertTVImageClass(tvDetectionResult.getFrontCardImage()));
        }

        result.putMap("frontIdQr", convertQrResult(tvDetectionResult.isRequiredQrImage(), tvDetectionResult.getCardQrImage()));

        if (tvDetectionResult.getBackCardImage() != null) {
            result.putMap("idBackImage", convertTVImageClass(tvDetectionResult.getBackCardImage()));
        }

        if (!CollectionUtils.isEmpty(tvDetectionResult.getSelfieVideos())) {
            result.putArray("livenessVideos", convertVideoRecord(tvDetectionResult.getSelfieVideos()));
        }

        if (tvDetectionResult.getLivenessMetadata() != null) {
            result.putString("livenessMetadata", tvDetectionResult.getLivenessMetadata().toString());
        }

        if (tvDetectionResult.getSelfieFrameBatchIds() != null && !tvDetectionResult.getSelfieFrameBatchIds().isEmpty()) {
            result.putArray("livenessFrameBatchIds", convertFrameBatch(tvDetectionResult.getSelfieFrameBatchIds()));
        }

        if (tvDetectionResult.getFrontCardFrameBatchIds() != null && !tvDetectionResult.getFrontCardFrameBatchIds().isEmpty()) {
            result.putArray("frontCardFrameBatchIds", convertFrameBatch(tvDetectionResult.getFrontCardFrameBatchIds()));
        }

        if (tvDetectionResult.getBackCardFrameBatchIds() != null && !tvDetectionResult.getBackCardFrameBatchIds().isEmpty()) {
            result.putArray("backCardFrameBatchIds", convertFrameBatch(tvDetectionResult.getBackCardFrameBatchIds()));
        }

        return result;
    }

    private WritableArray convertFrameBatch(@NonNull LinkedHashSet<String> frameBatchIds) throws JSONException {
        return RNTrustVisionUtils.convertJsonToArray(new JSONArray(frameBatchIds.toArray()));
    }

    private WritableMap convertQrResult(boolean isRequire, TVImageClass qrImage) {
        WritableMap qrResult = new WritableNativeMap();
        qrResult.putBoolean("is_required", isRequire);
        if (qrImage != null) {
            WritableArray qrImageArray = new WritableNativeArray();
            qrImageArray.pushMap(convertTVImageClass(qrImage));
            qrResult.putArray("images", qrImageArray);
        }
        return qrResult;
    }

    private WritableArray convertVideoRecord(List<byte[]> videos) {
        WritableArray videoMap = new WritableNativeArray();
        for (byte[] video : videos) {
            videoMap.pushString(RNTrustVisionUtils.convertToBase64(video));
        }
        return videoMap;
    }

    private WritableMap convertSelfieImage(SelfieImage selfieImage) {
        WritableMap selfieMap = new WritableNativeMap();
        selfieMap.putString("gesture_type", selfieImage.getGestureType().toGestureType().toString().toLowerCase());

        TVImageClass frontalImage = selfieImage.getFrontalImage();
        if (frontalImage != null) {
            selfieMap.putMap("frontal_image", convertTVImageClass(frontalImage));
        }

        TVImageClass gestureImage = selfieImage.getGestureImage();
        if (gestureImage != null) {
            selfieMap.putMap("gesture_image", convertTVImageClass(gestureImage));
        }

        return selfieMap;
    }

    private WritableMap convertTVImageClass(TVImageClass tvImageClass) {
        WritableMap map = new WritableNativeMap();
        map.putString("raw_image_base64", RNTrustVisionUtils.convertBitmapToBase64(tvImageClass.getImage()));
        map.putString("image_id", tvImageClass.getImageId());
        return map;
    }
}