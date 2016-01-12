/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.oidServlet;

import com.spopoff.eidas.ProcessLogin;
import com.spopoff.frconnect.FcConnectException;
import com.spopoff.frconnect.FcConnection;
import com.spopoff.frconnect.FcParamConfig;
import com.spopoff.frconnect.FrConn;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SPOPOFF
 */
public class CallBack extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(CallBack.class);
    private boolean once = true;
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
        if(!once) return;
        HttpSession session = request.getSession(false);
	String signAssertion = (String) session.getAttribute("state");
	String encryptAssertion = (String) session.getAttribute("nonce");
	String samlToken = (String) session.getAttribute("samlToken");


        FcParamConfig frConf = new FcParamConfig(FrConn.getUrlToken(),FrConn.getUrlAuthz(),
            FrConn.getUrlRedir(),FrConn.getUrlUserI(), FrConn.getCle(), FrConn.getSecret(),
            FrConn.getScope(),signAssertion,
            encryptAssertion,"acr_values","eidas2", FrConn.getUrlLogout());
        FcConnection frConn = new FcConnection(frConf);
        String accesTok = "";
        String code = "";
        String state = request.getParameter("state");
        if(!signAssertion.equalsIgnoreCase(state)){
            LOG.error("state different!");
            sendError(response, "state different!");
            return;
        }else{
            code = request.getParameter("code");
            LOG.debug("response code="+code);
        }
        try {
            accesTok = frConn.getAccessToken(code);
        } catch (FcConnectException ex) {
            LOG.error("getAccessToken=", ex);
        }
        String userInf = "";
        if(accesTok.isEmpty()){
            accesTok = "Rien en retour";
            userInf = "Rien de rien";
            sendError(response, "AccessToken vide "+accesTok);
            return;
        }else{
            LOG.debug("accessToken="+accesTok);
            try {
                userInf = frConn.getUserInfo(accesTok);
            } catch (FcConnectException ex) {
                LOG.error("getUserInfo=", ex);
                userInf = ex.getMessage();
                sendError(response, "getUserInfo="+userInf);
                return;
            }
        }
        //on ferme la session OAuth
        frConn.closeClient();
        LOG.debug("commence ProcessLogin");
        ProcessLogin pl = new ProcessLogin();
        pl.setEncryptAssertion(encryptAssertion);
        pl.setSignAssertion(signAssertion);
        pl.setSamlToken(samlToken);
        pl.setEidasLoa("http://eidas.europa.eu/LoA/substantial");
        pl.setNodeSpec(null);
        boolean fin = false;
        LOG.debug("Avant ProcessLogin.processAuthentication");
        try {
            fin = pl.processAuthentication(request, response, userInf);
        } catch (Exception e) {
            LOG.error("processAuthentication=", e);
            sendError(response, "processAuthentication="+e);
        }
        LOG.debug("Apres ProcessLogin.processAuthentication fin="+fin);
        if(fin){
//            request.setAttribute("samlToken", pl.getSamlToken());
//            request.setAttribute("username", pl.getUsername());
//            request.setAttribute("callback", pl.getCallback());
            CleanHttpRequest newReq = new CleanHttpRequest(request);
            newReq.addParameter("samlToken", pl.getSamlToken());
            newReq.addParameter("username", pl.getUsername());
            newReq.addParameter("callback", pl.getCallback());
            LOG.debug("avant redirect=/metadata/Login");
            RequestDispatcher dispatcher=request.getRequestDispatcher("/metadata/Login");
            dispatcher.forward(newReq, response);
            //response.sendRedirect("/metadata/Login");
        }else{
            response.sendError(500, "Echec authentification");
        }
       once = false;
    }
    private void sendError(HttpServletResponse resp, String msg){
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try {
            resp.sendError(500, msg);
        } catch (IOException iOException) {
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
