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
package com.spopoff.oidServlet;

import com.spopoff.frconnect.FcConnectException;
import com.spopoff.frconnect.FcConnection;
import com.spopoff.frconnect.FcParamConfig;
import com.spopoff.frconnect.FrConn;
import eu.eidas.auth.commons.EIDASParameters;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SPOPOFF
 */
public class ClientAuthz extends HttpServlet {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ClientAuthz.class);
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
	String samlToken = "";
        String signAssertion = "";
        String encryptAssertion = "";
        try {
            samlToken = request.getParameter(EIDASParameters.SAML_REQUEST.toString());
            signAssertion = request.getParameter("signAssertion");
            encryptAssertion = request.getParameter("encryptAssertion");
        } catch (Exception e) {
            LOG.warn( "en direct", e);
        }
        String nonce = java.util.UUID.randomUUID().toString();
        LOG.debug("encryptAssertion="+encryptAssertion);
        HttpSession session = request.getSession(true);
        session.setAttribute("signAssertion", signAssertion);
        session.setAttribute("nonce", encryptAssertion);
        session.setAttribute("samlToken", samlToken);
        String sessId = session.getId();
        /*
        String tokenUri, 
        String authorizationUri, 
        String redirectUri,
        String userInfoUri, 
        String clientId, 
        String clientSecret,
        String scope,
        String state,
        String nonce,
        String verifParameterId,
        String verifParameterValue
        */
        FcParamConfig frConf = new FcParamConfig(FrConn.getUrlToken(),FrConn.getUrlAuthz(),
            FrConn.getUrlRedir(), FrConn.getUrlUserI(),FrConn.getCle(), FrConn.getSecret(),
            FrConn.getScope(),sessId,
            nonce,"acr_values","eidas2", FrConn.getUrlLogout());
        FcConnection frConn = new FcConnection(frConf);
        LOG.debug("Avant la redirection pour demande accessToken state="+frConf.getState()+" nonce="+nonce);
        try {
            response.sendRedirect(frConn.getRedirectUri().toString());
        } catch (FcConnectException ex) {
            LOG.error( "erreur dedans FcConnection ", ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
