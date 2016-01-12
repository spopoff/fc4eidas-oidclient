/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.frconnect;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SPOPOFF
 */
public class FcTranslateAttr {
    private final String trans = "{\"sub\":\"PersonIdentifier\",\"given_name\":\"CurrentGivenName\",\"family_name\":\"CurrentFamilyName\",\"gender\":\"gender\",\"birthdate\":\"DateOfBirth\",\"birthplace\":\"countryCodeOfBirth\",\"birthcountry\":\"nationalityCode\",\"email\":\"eMail\",\"address\":\"residenceAddress\"}";
    //"address":{"formatted":"26 rue Desaix, 75015 Paris","street_address":"26 rue Desaix","locality":"Paris","region":"Ile-de-France","postal_code":"75015","country":"France"}
    //<eidas:LocatorDesignator>22</eidas:LocatorDesignator>
    //<eidas:Thoroughfare>Arcacia Avenue</eidas:Thoroughfare>
    //<eidas:PostName>London</eidas:PostName>
    //<eidas:PostCode>SW1A 1AA</eidas:Postcode>
    private final String transAddress = "{\"formatted\":\"LocatorDesignator\",\"street_address\":\"Thoroughfare\",\"locality\":\"PostName\",\"region\":\"AdminunitSecondLine\",\"postal_code\":\"PostCode\",\"country\":\"AdminunitFirstLine\"}";
    private boolean isErr = false;
    private String msgErr = "";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FcTranslateAttr.class);
    private final Map<String, String> transfo = new HashMap<String, String>();
    private final Map<String, String> transfoAddress = new HashMap<String, String>();
    public FcTranslateAttr(){
        translateAttr(trans, transfo);
        translateAttr(transAddress, transfoAddress);
        
    }
    private void translateAttr(String trans, Map<String,String>transfo){
        JSONTokener jsTok = null;
        try {
            jsTok = new JSONTokener(trans);
        } catch (Exception e) {
            isErr = true;
            msgErr = e.getMessage();
            return;
        }
        JSONObject jsObj = null;
        try {
            jsObj = new JSONObject(jsTok);
        } catch (JSONException ex) {
            isErr = true;
            msgErr = ex.getMessage();
            return;
        }
	if(jsObj == null){
            msgErr = "json vide";
            isErr = true;
            return;
        }
        if(jsObj.length()==0){
            isErr = true;
            return;
        }
        JSONArray jsNames = null;
        try {
            jsNames = jsObj.names();
        } catch (Exception e) {
            isErr = true;
            msgErr = e.getMessage();
            return;
        }
        if(jsNames==null){
            msgErr = "jsNames vide";
            isErr = true;
            return;
        }
        LOG.debug("json names nb="+jsNames.length());
        for(int i=0; i<jsNames.length();i++){
            String key = (String)jsNames.get(i);
            String val = jsObj.getString(key);
            transfo.put(key, val);
        }
    }

    public boolean isIsErr() {
        return isErr;
    }

    public String getMsgErr() {
        return msgErr;
    }
    public String getTransfoName(String avant){
        return transfo.get(avant);
    }
    public String getTransfoAddress(String avant){
        return transfoAddress.get(avant);
    }
}
