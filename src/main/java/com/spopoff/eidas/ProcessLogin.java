/*
Copyright Stéphane Georges Popoff, (Décembre 2010 - mars 2014)

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

package com.spopoff.eidas;

import com.spopoff.frconnect.FcTranslateAttr;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.IPersonalAttributeList;
import eu.eidas.auth.commons.EIDASErrors;
import eu.eidas.auth.commons.EIDASParameters;
import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.commons.PersonalAttribute;
import eu.eidas.auth.commons.PersonalAttributeList;
import eu.eidas.auth.commons.EIDASAuthnRequest;
import eu.eidas.auth.commons.EIDASAuthnResponse;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.engine.EIDASSAMLEngine;
import eu.eidas.auth.engine.core.SAMLEngineEncryptionI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.specific.SpecificEidasNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

public class ProcessLogin {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProcessLogin.class);
    private static final String SIGN_ASSERTION_PARAM="signAssertion";
    private String samlToken;
    private String username;
    private String callback;
    private String signAssertion;
    private String encryptAssertion;
    private String eidasLoa;
    private Properties idpProperties = EIDASUtil.loadConfigs(Constants.IDP_PROPERTIES);
    private SpecificEidasNode nodeSpec;

    private Properties loadConfigs(String path) {
        return EIDASUtil.loadConfigs(path);
    }

    public void setNodeSpec(SpecificEidasNode nodeSpec) {
        this.nodeSpec = nodeSpec;
    }

    public boolean processAuthentication(HttpServletRequest request, HttpServletResponse response,
            String jSon){

        EIDASUtil.createInstance(loadConfigs("eidasUtil.properties"));
        FcTranslateAttr transfo = new FcTranslateAttr();
        if(transfo.isIsErr()){
            LOG.error("erreur transformateur="+transfo.getMsgErr());
            return false;
        }
        EIDASAuthnRequest authnRequest = null;
        LOG.debug("Avant ProcessLogin validateRequest");
        try {
            authnRequest = validateRequest(samlToken);
        } catch (Exception e) {
            LOG.error( "erreur validateRequest", e);
            return false;
        }
        LOG.debug("Apres ProcessLogin validateRequest");
        if(authnRequest == null){
            LOG.debug("authnRequest null!!");
            try {
                response.sendError(500, "Erreur authnRequest null");
            } catch (IOException ex) {
                LOG.error("erreur sendError Erreur authnRequest null", ex);
            }
            return false;
        }
        this.callback = authnRequest.getAssertionConsumerServiceURL();
        LOG.debug("callback="+callback);
        //IPersonalAttributeList attrList2 = nodeSpec.normaliseAttributeNamesTo(attrList);
        IPersonalAttributeList attrList = null;
        try {
            attrList = extraitAttributJson(jSon, transfo);
        } catch (Exception ex) {
            sendErrorRedirect(authnRequest,request, ex.getMessage());
            return false;
        }
        LOG.debug("json attributeList="+attrList.toString());
        sendRedirect(authnRequest, (PersonalAttributeList) attrList, request);

        return true;
    }
	
    private EIDASAuthnRequest validateRequest(String samlToken){
        EIDASAuthnRequest authnRequest;
        EIDASSAMLEngine engine = null;
        try {
            engine = getSamlEngineInstance();
        } catch (EIDASSAMLEngineException e) {
            LOG.error("getSamlEngineInstance="+e);
        }
        if(engine == null){
            LOG.error("getSamlEngineInstance est null");
            return null;
        }
        LOG.debug("Avant validateEIDASAuthnRequest");
        try {
            authnRequest = engine.validateEIDASAuthnRequest(EIDASUtil.decodeSAMLToken(samlToken));
        } catch (Exception e) {
            LOG.error("validateRequest="+e);
            throw new InvalidParameterEIDASException(EIDASUtil
                    .getConfig(EIDASErrors.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EIDASUtil.getConfig(EIDASErrors.COLLEAGUE_REQ_INVALID_SAML
                            .errorMessage()));
        }
        LOG.debug("Fin validateRequest processLogin");
        return authnRequest;
    }
	
	private void sendRedirect(EIDASAuthnRequest authnRequest,
            PersonalAttributeList attrList, HttpServletRequest request) {
            try {
                String remoteAddress = request.getRemoteAddr();
                if (request.getHeader(EIDASParameters.HTTP_X_FORWARDED_FOR.toString()) != null)
                    remoteAddress = request
                                    .getHeader(EIDASParameters.HTTP_X_FORWARDED_FOR.toString());
                else {
                    if (request.getHeader(EIDASParameters.X_FORWARDED_FOR.toString()) != null)
                            remoteAddress = request
                                            .getHeader(EIDASParameters.X_FORWARDED_FOR.toString());
                }

                EIDASSAMLEngine engine = getSamlEngineInstance();
                EIDASAuthnResponse responseAuthReq = new EIDASAuthnResponse();
                for(PersonalAttribute pa:attrList){
                    if(pa.isEmptyValue() && pa.isRequired()){
                        pa.setStatus(EIDASStatusCode.STATUS_NOT_AVAILABLE.toString());
                    }
                }
                responseAuthReq.setPersonalAttributeList(attrList);
                responseAuthReq.setInResponseTo(authnRequest.getSamlId());
                if(callback==null){
                    authnRequest.setAssertionConsumerServiceURL(MetadataUtil.getAssertionUrlFromMetadata(new IdPMetadataProcessor(), engine, authnRequest));
                    callback=authnRequest.getAssertionConsumerServiceURL();
                }
                LOG.debug("sendRedirect callback="+callback);
                String metadataUrl=idpProperties==null?null:idpProperties.getProperty(Constants.IDP_METADATA_URL);
                if(metadataUrl!=null && !metadataUrl.isEmpty()) {
                    responseAuthReq.setIssuer(metadataUrl);
                }
                engine.setRequestIssuer(authnRequest.getIssuer());
                responseAuthReq.setAssuranceLevel(eidasLoa);
                EIDASAuthnResponse samlToken = engine.generateEIDASAuthnResponse(authnRequest, responseAuthReq, remoteAddress, false, Boolean.parseBoolean(request.getParameter(SIGN_ASSERTION_PARAM)));

                this.samlToken = EIDASUtil.encodeSAMLToken(samlToken.getTokenSaml());
            } catch (Exception e) {
                throw new InternalErrorEIDASException("0",
                                "Error generating SAMLToken");
            }
	}

	private EIDASSAMLEngine getSamlEngineInstance() throws EIDASSAMLEngineException{
		EIDASSAMLEngine engine = IDPUtil.createSAMLEngine(Constants.SAMLENGINE_NAME);
		Properties userProps = EIDASUtil.loadConfigs("samlengine.properties", false);
		if(userProps!=null && userProps.containsKey(SAMLEngineEncryptionI.DATA_ENCRYPTION_ALGORITHM)){
			engine.setEncrypterProperty(SAMLEngineEncryptionI.DATA_ENCRYPTION_ALGORITHM, userProps.getProperty(SAMLEngineEncryptionI.DATA_ENCRYPTION_ALGORITHM));
		}
		if(Boolean.parseBoolean(encryptAssertion)){
			engine.setEncrypterProperty(SAMLEngineEncryptionI.RESPONSE_ENCRYPTION_MANDATORY, encryptAssertion);
		}
		if(Boolean.parseBoolean(idpProperties.getProperty(IDPUtil.ACTIVE_METADATA_CHECK))) {
			engine.setMetadataProcessor(new IdPMetadataProcessor());
		}
		return engine;
	}

	public void sendErrorRedirect(EIDASAuthnRequest authnRequest, HttpServletRequest request, String msg){
		EIDASAuthnResponse samlTokenFail = new EIDASAuthnResponse();
		try {
			samlTokenFail.setStatusCode(EIDASStatusCode.RESPONDER_URI.toString());
			samlTokenFail.setSubStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
			samlTokenFail.setMessage(msg);
			EIDASSAMLEngine engine = getSamlEngineInstance();
			engine.setRequestIssuer(authnRequest.getIssuer());
			String metadataUrl=idpProperties==null?null:idpProperties.getProperty(Constants.IDP_METADATA_URL);
			if(metadataUrl!=null && !metadataUrl.isEmpty()) {
				samlTokenFail.setIssuer(metadataUrl);
			}
			if(callback==null){
				authnRequest.setAssertionConsumerServiceURL(MetadataUtil.getAssertionUrlFromMetadata(new IdPMetadataProcessor(), engine, authnRequest));
				callback=authnRequest.getAssertionConsumerServiceURL();
			}
			samlTokenFail.setAssuranceLevel(eidasLoa);
			samlTokenFail = engine.generateEIDASAuthnResponseFail(authnRequest, samlTokenFail, request.getRemoteAddr(), false);
		} catch (Exception e) {
			throw new InternalErrorEIDASException("0", "Error generating SAMLToken");
		}
		this.samlToken = EIDASUtil.encodeSAMLToken(samlTokenFail.getTokenSaml());
	}
        /**
         * change le jSon en une liste d'attribut
         * @param jSon
         * @return
         * @throws Exception 
         */
        private IPersonalAttributeList extraitAttributJson(String jSon, FcTranslateAttr transfo)throws Exception{
            JSONTokener jsTok = null;
            try {
                jsTok = new JSONTokener(jSon);
            } catch (Exception e) {
                throw new Exception("Erreur1 recup json=",e);
            }
            JSONObject jsObj = null;
            try {
                jsObj = new JSONObject(jsTok);
            } catch (JSONException ex) {
                throw new Exception("Erreur2 fabrique objet json=",ex);
            }
            if(jsObj == null){
                throw new Exception("Erreur3 objet json null");
            }
            if(jsObj.length()==0){
                throw new Exception("Erreur4 objet json vide");
            }
            LOG.debug("json objet nb="+jsObj.length());
            JSONArray jsNames = null;
            try {
                jsNames = jsObj.names();
            } catch (Exception e) {
                throw new Exception("Erreur5 objet json names ",e);
            }
            if(jsNames==null){
                throw new Exception("Erreur6 objet json names vide");
            }
            LOG.debug("json names nb="+jsNames.length());
            IPersonalAttributeList attrList = new PersonalAttributeList();
            for(int i=0; i<jsNames.length();i++){
                String key = (String)jsNames.get(i);
                LOG.debug("avant json key="+key);
                JSONObject jsObj2 = null;
                boolean isJsonObj = true;
                try {
                    jsObj2 = jsObj.getJSONObject(key);
                } catch (JSONException e) {
                    isJsonObj = false;
                }
                if(jsObj2==null){
                    isJsonObj = false;
                }
                if(key.equalsIgnoreCase("address")){
                    PersonalAttribute ad = uneAdresse(jsObj2, transfo);
                    attrList.add(ad);
                    continue;
                }
                LOG.debug("apres json key="+transfo.getTransfoName(key));
                PersonalAttribute pa = new PersonalAttribute();
                pa.setName(transfo.getTransfoName(key));
                String jsVal;
                ArrayList<String> tmp = new ArrayList<String>();
                //si pas un objet de json mais un simple champs
                if (!isJsonObj) {
                    jsVal = jsObj.getString(key);
                    tmp.add(jsVal);
                    LOG.info("[processLogin] Attribut1: " + key + "=>" + jsVal);
                    pa.setValue(tmp);
                    if(key.equalsIgnoreCase("email")){
                        username = jsVal;
                    }
                //sinon un objet a valeur multiple
                }else {
                    String jsVal2;
                    Map<String, String> cplx = new HashMap<String, String>();
                    JSONArray jsNames2 = null;
                    try {
                        jsNames2 = jsObj2.names();
                    } catch (Exception e) {
                        throw new Exception("Erreur7 objet json names ",e);
                    }
                    if(jsNames2==null){
                        throw new Exception("Erreur8 objet json names vide");
                    }
                    LOG.debug("json names2 nb="+jsNames2.length());
                    for(int j=0; j<jsNames2.length();j++){
                        String cle = (String)jsNames2.get(j);
                        jsVal2 = jsObj2.getString(cle);
                        cplx.put(cle, jsVal2);
                        LOG.info("[processLogin] Attribut2: " + cle + "=>" + jsVal2);
                    }
                    pa.setComplexValue(cplx);
                }

                pa.setStatus(EIDASStatusCode.STATUS_AVAILABLE.toString());
                attrList.add(pa);
            }
            //"nationalityCode"
            if(!attrList.containsKey("nationalityCode")){
                LOG.info("[processLogin] AttributX: nationalityCode=>CC");
                PersonalAttribute na = new PersonalAttribute();
                na.setName("nationalityCode");
                na.setStatus(EIDASStatusCode.STATUS_AVAILABLE.toString());
                ArrayList<String> nat = new ArrayList<String>();
                nat.add("CC");
                na.setValue(nat);
                attrList.add(na);
            }
            return attrList;
        }
        private PersonalAttribute uneAdresse(JSONObject jsObj2, FcTranslateAttr transfo) throws Exception{
            PersonalAttribute ret = new PersonalAttribute();
            ret.setName(transfo.getTransfoName("address"));
            String jsVal2;
            Map<String, String> cplx = new HashMap<String, String>();
            JSONArray jsNames2 = null;
            try {
                jsNames2 = jsObj2.names();
            } catch (Exception e) {
                throw new Exception("Erreur7 objet json names ",e);
            }
            if(jsNames2==null){
                throw new Exception("Erreur8 objet json names vide");
            }
            LOG.debug("json address nb="+jsNames2.length());
            for(int j=0; j<jsNames2.length();j++){
                String cle = (String)jsNames2.get(j);
                jsVal2 = jsObj2.getString(cle);
                cplx.put(transfo.getTransfoAddress(cle), jsVal2);
                LOG.info("[processLogin] Address avant: " + cle + "=>" + jsVal2);
            }
            ret.setValue(base64Xml(cplx));

            ret.setStatus(EIDASStatusCode.STATUS_AVAILABLE.toString());
            return ret;
        }
	private List<String> base64Xml(Map<String, String> attrs){
            List<String> ret = new ArrayList<String>();
            String xml = "<address>";
            for(Map.Entry<String, String> ent: attrs.entrySet()){
                xml+="<"+ent.getKey()+">"+ent.getValue()+"</"+ent.getKey()+">";
            }
            xml+="</address>";
            LOG.debug("une adresse="+xml);
            final byte[] oct = xml.getBytes(StandardCharsets.UTF_8);
            ret.add(Base64.getEncoder().encodeToString(oct));
            return ret;
        }
	/**
	 * @param samlToken the samlToken to set
	 */
	public void setSamlToken(String samlToken) {
		this.samlToken = samlToken;
	}

	/**
	 * @return the samlToken
	 */
	public String getSamlToken() {
		return samlToken;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(String callback) {
		this.callback = callback;
	}

	/**
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

    /**
     * @param signAssertion the signAssertion to set
     */
    public void setSignAssertion(String signAssertion) {
        this.signAssertion = signAssertion;
    }

    /**
     * @return the signAssertion value
     */
    public String getSignAssertion() {
        return signAssertion;
    }

    public String getEncryptAssertion() {
            return encryptAssertion;
    }

    public void setEncryptAssertion(String encryptAssertion) {
            this.encryptAssertion = encryptAssertion;
    }

    public void setEidasLoa(String eidasLoa) {
        this.eidasLoa = eidasLoa;
    }
}
