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
package com.spopoff.eidas;

import eu.eidas.auth.engine.EIDASSAMLEngine;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;

import org.apache.log4j.Logger;

public class IDPUtil {
    /**
     * name of the property which switches idp metadata on and off
     */
    public static final String ACTIVE_METADATA_CHECK="idp.metadata.check";
    private static final Logger logger = Logger.getLogger(IDPUtil.class.getName());

    private static final String SAML_ENGINE_LOCATION_VAR="IDP_CONF_LOCATION";
    static CertificateConfigurationManager idpSamlEngineConfig=null;
    public static EIDASSAMLEngine createSAMLEngine(String samlEngineName) throws EIDASSAMLEngineException {
        if(idpSamlEngineConfig==null && System.getenv(SAML_ENGINE_LOCATION_VAR)!=null){
            idpSamlEngineConfig = ApplicationContextProvider.getApplicationContext().getBean(CertificateConfigurationManager.class);
            idpSamlEngineConfig.setLocation(getLocation(System.getenv(SAML_ENGINE_LOCATION_VAR)));
            logger.info("retrieving config from "+System.getenv(SAML_ENGINE_LOCATION_VAR));
        }
        if(idpSamlEngineConfig != null && idpSamlEngineConfig.isActive() && idpSamlEngineConfig.getConfiguration() != null && !idpSamlEngineConfig.getConfiguration().isEmpty()){
            return EIDASSAMLEngine.createSAMLEngine(samlEngineName,idpSamlEngineConfig);
        }
        else {
            return EIDASSAMLEngine.createSAMLEngine(samlEngineName);
        }

    }

    private static final String[] PATH_PREFIXES={"file://", "file:/","file:" };
    private static String getLocation(String location){
        if (location!=null){
            for(String prefix:PATH_PREFIXES){
                if(location.startsWith(prefix)){
                    return location.substring(prefix.length());
                }
            }
        }
        return location;
    }
}
