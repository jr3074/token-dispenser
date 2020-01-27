package com.github.yeriomin.tokendispenser;

import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.PropertiesDeviceInfoProvider;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.lang.NullPointerException;

import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class TokenAc2dmResource {

    public String handle(Request request, Response response) {
        String email = request.params("email");
        String aasToken = Server.passwords.get(email);
        if (null == aasToken || aasToken.isEmpty()) {
            halt(404, "No password for this email");
        }
        int code = 500;
        String message;
        try {
            return getToken(email, aasToken);
        } catch (GooglePlayException e) {
            if (e.getCode() >= 400) {
                code = e.getCode();
            }
            message = e.getMessage();
            Server.LOG.warn(e.getClass().getName() + ": " + message + (e.getRawResponse() != null ? (" body: " + new String(e.getRawResponse())) : ""));
            halt(code, message);
        } catch (IOException e) {
            message = e.getMessage();
            Server.LOG.error(e.getClass().getName() + ": " + message);
            halt(code, message);
        }
        return "";
    }

    GooglePlayAPI getApi() {
	return getApi("bacon");
    }

    GooglePlayAPI getApi(String device) {
	String device_file = "device-" + device + ".properties";
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getSystemResourceAsStream(device_file));
        } catch (IOException | NullPointerException e) {
            halt(500, device_file + " not found");
        }

        PropertiesDeviceInfoProvider deviceInfoProvider = new PropertiesDeviceInfoProvider();
        deviceInfoProvider.setProperties(properties);
        deviceInfoProvider.setLocaleString(Locale.ENGLISH.toString());

        GooglePlayAPI api = new GooglePlayAPI();
        api.setClient(new OkHttpClientAdapter());
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setLocale(Locale.US);
        return api;
    }

    protected String getToken(String email, String aasToken) throws IOException {
        return getApi(device).generateToken(email, aasToken);
    }

}
