/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.frconnect;

/**
 *
 * @author SPOPOFF
 */
public final class FrConn {
    
        private static final String cle = "c287dabadcce74c01e9d701df71e2aa3d6a080dac43cf3a3f6d622d7230de78d";
        private static final String secret = "0945430219a6f253400214f2ffc4695ed2174e2406affc8826bb5446b0057ec0";
        private static final String urlAuthz = "https://fcp.integ01.dev-franceconnect.fr/api/v1/authorize";
        private static final String urlToken = "https://fcp.integ01.dev-franceconnect.fr/api/v1/token";
        private static final String urlUserI = "https://fcp.integ01.dev-franceconnect.fr/api/v1/userinfo";
        private static final String urlRedir = "http://countryc.spopoff.com:8080/oidclient/callback";
        private static final String scope = "profile email address phone openid preferred_username birth";
        private static final String urlLogout = "http://fcp.integ01.dev-franceconnect.fr/api/v1/logout";

    /**
     * Get the value of cle
     *
     * @return the value of cle
     */
    public static String getCle() {
        return cle;
    }

    public static String getSecret() {
        return secret;
    }

    public static String getUrlAuthz() {
        return urlAuthz;
    }

    public static String getUrlToken() {
        return urlToken;
    }

    public static String getUrlUserI() {
        return urlUserI;
    }

    public static String getUrlRedir() {
        return urlRedir;
    }

    public static String getScope() {
        return scope;
    }
    public static String getUrlLogout() {
        return urlLogout;
    }
}
