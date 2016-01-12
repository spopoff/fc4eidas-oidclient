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
