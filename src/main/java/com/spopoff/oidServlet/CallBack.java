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
        if(!once){ 
            LOG.debug("premier accès dans la servlet="+once);
        }
        HttpSession session = request.getSession(false);
	String signAssertion = (String) session.getAttribute("signAssertion");
	String encryptAssertion = (String) session.getAttribute("nonce");
	String samlToken = (String) session.getAttribute("samlToken");
        String sessId = session.getId();
        String nonce = java.util.UUID.randomUUID().toString();
        FcParamConfig frConf = new FcParamConfig(FrConn.getUrlToken(),FrConn.getUrlAuthz(),
            FrConn.getUrlRedir(),FrConn.getUrlUserI(), FrConn.getCle(), FrConn.getSecret(),
            FrConn.getScope(),sessId,
            nonce,"acr_values","eidas2", FrConn.getUrlLogout());
        FcConnection frConn = new FcConnection(frConf);
        String accesTok = "";
        String code = "";
        String state = request.getParameter("state");
        if(!sessId.equalsIgnoreCase(state)){
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
