/*
Copyright Stéphane Georges Popoff, (Décembre 2010 - mars 2016)

spopoff@rocketmail.com

Ce logiciel est un programme informatique servant à gérer des habilitations.

Ce logiciel est régi par la licence [CeCILL|CeCILL-B|CeCILL-C] soumise au droit français et
respectant les principes de diffusion des logiciels libres. Vous pouvez
utiliser, modifier et/ou redistribuer ce programme sous les conditions
de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA
sur le site "http://www.cecill.info".

En contrepartie de l'accessibilité au code source et des droits de copie,
de modification et de redistribution accordés par cette licence, il n'est
offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
seule une responsabilité restreinte pèse sur l'auteur du programme,  le
titulaire des droits patrimoniaux et les concédants successifs.

A cet égard  l'attention de l'utilisateur est attirée sur les risques
associés au chargement,  à l'utilisation,  à la modification et/ou au
développement et à la reproduction du logiciel par l'utilisateur étant
donné sa spécificité de logiciel libre, qui peut le rendre complexe à
manipuler et qui le réserve donc à des développeurs et des professionnels
avertis possédant  des  connaissances  informatiques approfondies.  Les
utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
logiciel à leurs besoins dans des conditions permettant d'assurer la
sécurité de leurs systèmes et ou de leurs données et, plus généralement,
à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.

Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
pris connaissance de la licence [CeCILL|CeCILL-B|CeCILL-C], et que vous en avez accepté les
termes.
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
