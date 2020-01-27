package com.github.yeriomin.tokendispenser;

import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.PropertiesDeviceInfoProvider;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class TokenAc2dmGsfIdResource extends TokenAc2dmResource {

    @Override
    public String handle(Request request, Response response) {
	String device = request.params("device");
	if (device == null){
		device = "bacon";
	}
        String email = Server.passwords.getRandomEmail();
        String aasToken = Server.passwords.get(email);
        int code = 500;
        String message;
        try {
            String token = getApi(device).generateToken(email, aasToken);
            return token;
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
}
