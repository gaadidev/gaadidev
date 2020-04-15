package com.customise.gaadi.camera.util;

public class CommonUtil {
    public static boolean isStringContainsData(String data) {
        boolean isStringContainsData = false;
        if (data != null && !data.equals("")) {
            isStringContainsData = true;
        }
        return isStringContainsData;
    }
}
