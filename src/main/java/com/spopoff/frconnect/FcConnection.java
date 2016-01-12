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

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.slf4j.LoggerFactory;

public class FcConnection {
	
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FcConnection.class.getName());
    FcParamConfig configuration;
    private final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

    public FcConnection(FcParamConfig configuration) {
        super();
        this.configuration = configuration;
    }

    public URI getRedirectUri() throws FcConnectException{

        try {
            OAuthClientRequest request = OAuthClientRequest
               .authorizationLocation(configuration.getAuthorizationUri())
               .setClientId(configuration.getClientId())
               .setRedirectURI(configuration.getRedirectUri())
               .setResponseType(ResponseType.CODE.toString())
               .setScope(configuration.getScope())
               .setState(configuration.getState())
               .setParameter("nonce", configuration.getNonce())
               .setParameter(configuration.getVerifParameterId(), configuration.getVerifParameterValue())
               .buildQueryMessage();

            LOG.debug(request.getLocationUri());

        return new URI(request.getLocationUri());

        } catch (OAuthSystemException e) {
                throw new FcConnectException(e);
        } catch (URISyntaxException e) {
                throw new FcConnectException("The uri is not well formed", e);
        }

    }

    public String getAccessToken(String code) throws FcConnectException{

        LOG.debug( "autorization code="+code);
			
        try {
            //rÃ©cupÃ©ration de l'access token
            OAuthClientRequest authClientRequest = OAuthClientRequest
                .tokenLocation(configuration.getTokenUri())
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(configuration.getClientId())
                .setClientSecret(configuration.getClientSecret())
                .setRedirectURI(configuration.getRedirectUri())
                .setCode(code)
                .buildBodyMessage();
            authClientRequest.setLocationUri(configuration.getTokenUri());
            LOG.debug( "authClientRequest.body="+authClientRequest.getBody());
            LOG.debug( "authClientRequest.getLocationUri="+authClientRequest.getLocationUri());
            OAuthJSONAccessTokenResponse tokResp = oAuthClient.accessToken(authClientRequest);
            LOG.debug( "OAuthJSONAccessTokenResponse="+tokResp.toString());
            String ret = tokResp.getAccessToken();
            return ret;

        } catch (OAuthSystemException e) {
                throw new FcConnectException("Error during request for accessToken : ", e);
        } catch (OAuthProblemException e) {
                throw new FcConnectException("Error during accessToken retrieving : ", e);
        }
    }

    public String getUserInfo(String accessToken) throws FcConnectException{
		

        //rÃ©cupÃ©ration du profil client
        OAuthClientRequest bearerClientRequest;
        try {
            bearerClientRequest = new OAuthBearerClientRequest(configuration.getUserInfoUri())
                  .setAccessToken(accessToken)
                  .buildHeaderMessage();

        } catch (OAuthSystemException e) {
            throw new FcConnectException("Error during bearerClientRequest: ", e);
        }
        LOG.debug( "bearerClientRequest.getLocationUri()="+bearerClientRequest.getLocationUri());
        OAuthResourceResponse resourceResponse = null;
        try {
            resourceResponse = oAuthClient.resource(bearerClientRequest,
                    OAuth.HttpMethod.GET,
                    OAuthResourceResponse.class);

        } catch (OAuthSystemException e) {
            throw new FcConnectException("Error during userInfo request building : ", e);
        } catch (OAuthProblemException e) {
            throw new FcConnectException("Error during userInfo retrieving : ", e);
        }
        if(resourceResponse==null){
            LOG.error("erreur sur recup resourceResponse null!!");
            throw new FcConnectException("erreur sur recup resourceResponse null!!");
        }
        String retour = null;
        LOG.debug( "etat retour="+resourceResponse.getResponseCode());
        retour = resourceResponse.getBody();
        return retour;
    }
    
    public String getUrlLogout(String idToken, String state, String backUri){
        String ret = "";
        ret += configuration.getLogoutUri()+"?id_token="+idToken+"&state="+state+"&post_logout_redirect_uri="+backUri;
        return ret;
    }
    /**
     * termine le client OAuth
     */
    public void closeClient(){
        try {
            oAuthClient.shutdown();
        } catch (Exception e) {
            LOG.error("Erreur fermeture clientHttpOAuth "+e);
        }
    }
}
