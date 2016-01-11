/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        if(samlToken == null || signAssertion == null || encryptAssertion == null){
            signAssertion = java.util.UUID.randomUUID().toString();
            encryptAssertion = java.util.UUID.randomUUID().toString();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("state", signAssertion);
        session.setAttribute("nonce", encryptAssertion);
        session.setAttribute("samlToken", samlToken);
        /*
        String tokenUri, 
        String authorizationUri, 
        String redirectUri,
        String userInfoUri, 
        String clientId, 
        String clientSecret,
        String scope,
        String state, String verifParameterId, String verifParameterValue
        */
        FcParamConfig frConf = new FcParamConfig(FrConn.getUrlToken(),FrConn.getUrlAuthz(),
            FrConn.getUrlRedir(), FrConn.getUrlUserI(),FrConn.getCle(), FrConn.getSecret(),
            FrConn.getScope(),signAssertion,
            encryptAssertion,"acr_values","eidas2", FrConn.getUrlLogout());
        FcConnection frConn = new FcConnection(frConf);
        LOG.debug("Avant la redirection pour demande accessToken");
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
