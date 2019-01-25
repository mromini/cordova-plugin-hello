package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

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
    String encryptedStr="";
    try {
      encryptedStr = MCrypt.byteArrayToB64String(mcrypt.encrypt(json));
    } catch (Exception e) {
      Glb.DLog("Encryption error: " + e.getMessage());
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
            meMap.put("EMAIL",email);
            meMap.put("PWD",password);

            String result = TSID_API_REQ(params);

            callbackContext.success(result);

            return true;

        } else {
            
            return false;

        }
    }
}
