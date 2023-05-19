package com.trustingsocial.reactnativetrustvision;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.trustingsocial.apisdk.data.TVApiError;
import com.trustingsocial.apisdk.utils.GsonUtils;
import com.trustingsocial.tvcoresdk.external.TVCardType;
import com.trustingsocial.tvcoresdk.external.TVDetectionError;
import com.trustingsocial.tvcoresdk.external.TVGraphicsUtils;
import com.trustingsocial.tvcoresdk.external.TVIDConfiguration;
import com.trustingsocial.tvcoresdk.external.TVSDKConfiguration;
import com.trustingsocial.tvcoresdk.external.TVSelfieConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class RNTrustVisionUtils {

    static <T> WritableArray toWritableArray(List<T> objects) throws JSONException {
        WritableArray array = new WritableNativeArray();
        for (T element : objects) {
            WritableMap map = convertJsonToMap(new JSONObject(GsonUtils.toJson(element)));
            array.pushMap(map);
        }
        return array;
    }
//
//    static <T> WritableArray toWritableArrayObject(List<T> objects) {
//        WritableArray array = new WritableNativeArray();
//        for (T element : objects) {
//            array.pushString(element.toString());
//        }
//        return array;
//    }

    static WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = new WritableNativeMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }
        return map;
    }

    static WritableMap objectToMap(Object object) throws JSONException {
        return convertJsonToMap(new JSONObject(GsonUtils.toJson(object)));
    }

    static WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(convertJsonToMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String) {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }
        return array;
    }

    static TVIDConfiguration convertIdConfigFromMap(ReadableMap map) {
        TVIDConfiguration.Builder configuration = new TVIDConfiguration.Builder();

        if (map.hasKey("isEnableSound")) {
            configuration.setEnableSound(map.getBoolean("isEnableSound"));
        }

        if (map.hasKey("cardType")) {
            ReadableMap cardMap = map.getMap("cardType");
            configuration.setCardType(readCardType(cardMap));
        }

        if (map.hasKey("isReadBothSide")) {
            configuration.setReadBothSide(map.getBoolean("isReadBothSide"));
        }

        if (map.hasKey("cardSide")) {
            String cardSideString = map.getString("cardSide").toUpperCase();
            TVSDKConfiguration.TVCardSide cardSide = TVSDKConfiguration.TVCardSide.valueOf(cardSideString);
            configuration.setCardSide(cardSide);
        }

        if (map.hasKey("skipConfirmScreen")) {
            configuration.setSkipConfirmScreen(map.getBoolean("skipConfirmScreen"));
        } else {
            configuration.setSkipConfirmScreen(false);
        }

        if (map.hasKey("isEnableScanQr")) {
            configuration.setEnableScanQr(map.getBoolean("isEnableScanQr"));
        }

        return configuration.build();
    }

    static Integer getColorInt(String colorHexString) {
        try {
            return Color.parseColor(colorHexString);
        } catch (Exception ex) {
            return null;
        }
    }

    private static TVCardType readCardType(ReadableMap readableMap) {
        String cardId = readableMap.getString("id");
        String cardName = readableMap.getString("name");
        String orientation = readableMap.getString("orientation");
        boolean isRequireBackSide = readableMap.getBoolean("hasBackSide");
        return new TVCardType(cardId, cardName, isRequireBackSide, TVCardType.TVCardOrientation.valueOf(orientation.toUpperCase()));

    }

    static TVSelfieConfiguration convertSelfieConfigFromMap(ReadableMap map) {
        TVSelfieConfiguration.Builder configuration = new TVSelfieConfiguration.Builder();

        if (map.hasKey("isEnableSound")) {
            configuration.setEnableSound(map.getBoolean("isEnableSound"));
        }

        if (map.hasKey("livenessMode")) {
            TVSDKConfiguration.TVLivenessMode livenessMode = TVSDKConfiguration.TVLivenessMode.valueOf(map.getString("livenessMode").toUpperCase());
            configuration.setLivenessMode(livenessMode);
        }

        if (map.hasKey("cameraOption")) {
            configuration.setCameraOption(TVSDKConfiguration.TVCameraOption.valueOf(map.getString("cameraOption").toUpperCase()));
        }

        if (map.hasKey("skipConfirmScreen")) {
            configuration.setSkipConfirmScreen(map.getBoolean("skipConfirmScreen"));
        } else {
            configuration.setSkipConfirmScreen(true);
        }


        return configuration.build();
    }

//    static String loadBase64Image(String imageUrl) {
//        if (imageUrl == null) return null;
//        try {
//            Bitmap bitmap = TV.loadImageFromStorage(imageUrl);
//            byte[] byteArray = TVSDKUtil.toByteArray(bitmap);
//            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
//        } catch (Exception ex) {
//            return null;
//        }
//    }

    static String convertBitmapToBase64(Bitmap bitmap) {
        byte[] byteArray = TVGraphicsUtils.toByteArray(bitmap, 100);
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    static String convertToBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    static WritableMap convertToWritableMap(Map<String, String> map) {
        WritableMap writableMap = new WritableNativeMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writableMap.putString(entry.getKey(), entry.getValue());
        }

        return writableMap;
    }

    static String convertErrorString(TVDetectionError resultError) {
        String errorCode;
        switch (resultError.getErrorCode()) {
            case TVDetectionError.DETECTION_ERROR_AUTHENTICATION_MISSING:
                errorCode = "authentication_missing_error";
                break;
            case TVDetectionError.DETECTION_ERROR_CAMERA_ERROR:
                errorCode = "camera_error";
                break;
            case TVDetectionError.DETECTION_ERROR_PERMISSION_MISSING:
                errorCode = "permission_missing_error";
                break;
            default:
                errorCode = resultError.getDetailErrorCode();
                break;

        }
        return GsonUtils.toJson(new TVApiError(errorCode, resultError.getErrorDescription()));
    }
}
