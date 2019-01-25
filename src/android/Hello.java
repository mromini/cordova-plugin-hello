package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class Hello extends CordovaPlugin {

    public static String md5(String input) {
    try {
      MessageDigest md =  MessageDigest.getInstance("MD5");

      StringBuilder hexString = new StringBuilder();
      for (byte digestByte : md.digest(input.getBytes()))
        hexString.append(String.format("%02X", digestByte));

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }
  public String TSID_API_REQ(final HashMap<String,String> params) throws IOException {
    String risultato = "KO";
    String AppCode = "EXTERNAL\\SALESAPP";
    String Email = params.get("EMAIL");
    String Password = params.get("PWD");
    long time= System.currentTimeMillis();
    String ReqDate = Long.toString(time);
    String AppPwd ="6bT4W6zxNUhuqXzZuErM88NUd8YM4jFw";
    String MAC = AppCode + Email + Password + ReqDate + AppPwd;
    String MAC_MD5 = md5(MAC);

    JSONObject jsonObj = new JSONObject();
    try {
      jsonObj.put("AppCode",AppCode);
      jsonObj.put("Email",Email);
      jsonObj.put("Password",Password);
      jsonObj.put("RequestDate",ReqDate);
      jsonObj.put("MAC",MAC_MD5);

    }catch (JSONException e) {

    }

    String json = jsonObj.toString();

    MCrypt mcrypt = new MCrypt();


    // Encrypting our dummy String
    String encryptedStr="KO";
    try {
      encryptedStr = MCrypt.byteArrayToB64String(mcrypt.encrypt(json));
    } catch (Exception e) {
      //Glb.DLog("Encryption error: " + e.getMessage());
    }

    return risultato = encryptedStr;
  }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else if (action.equals("encrypt")){
            String email = data.getString(0);
            String password = data.getString(1);
            HashMap<String, String> params=new HashMap<String, String>();
            params.put("EMAIL",email);
            params.put("PWD",password);

            String result = "KO";
            try {
              result = TSID_API_REQ(params);
            } catch (IOException err) {

            }

            if (result.equals("KO"))
                callbackContext.error("Unable to encrypt.");
            else
                callbackContext.success(result);

            return true;

        } else if (action.equals("decrypt")) {
            String encryptedString = data.getString(0);
            callbackContext.success(encryptedString);
            //String result = "KO";
            //MCrypt mcrypt = new MCrypt();
            //String err ="";
            //try {
            //    result = MCrypt.byteArrayToB64String(mcrypt.decrypt(encryptedString));
            //} catch (Exception e) {
            //    err = e.getMessage();
                //Glb.DLog("Encryption error: " + e.getMessage());
            //}

            //if (result.equals("KO"))
            //    callbackContext.error("Unable to decrypt.\n"+err);
            //else
            //    callbackContext.success(result);

            return true;

        } else {
            
            return false;

        }
    }
}
