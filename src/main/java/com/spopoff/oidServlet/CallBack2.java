/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.oidServlet;

import com.spopoff.eidas.ProcessLogin;
import java.io.IOException;
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
public class CallBack2 extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(CallBack2.class);
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
        
        HttpSession session = request.getSession(false);
	String signAssertion = (String) session.getAttribute("state");
	String encryptAssertion = (String) session.getAttribute("nonce");
	String samlToken = (String) session.getAttribute("samlToken");


        String userInf = "{\"sub\":\"93ba924a2b8db8797183113028f2883ccc4579fd9e37fe50dcdc35e52d6a0245v1\",\"given_name\":\"Eric\",\"family_name\":\"Mercier\",\"gender\":\"male\",\"birthdate\":\"1981-04-21\",\"birthplace\":\"49007\",\"birthcountry\":\"99100\",\"email\":\"1234567891011\",\"address\":{\"formatted\":\"26 rue Desaix, 75015 Paris\",\"street_address\":\"26 rue Desaix\",\"locality\":\"Paris\",\"region\":\"Ile-de-France\",\"postal_code\":\"75015\",\"country\":\"France\"}}";
        //ICONNECTORTranslatorService beanTrans = ApplicationContextProvider.getApplicationContext().getBean("springManagedAUSERVICETranslator", ICONNECTORTranslatorService.class);
        //SpecificEidasNode nodeSpec = new SpecificEidasNode();
        ProcessLogin pl = new ProcessLogin();
        pl.setEncryptAssertion(encryptAssertion);
        pl.setSignAssertion(signAssertion);
        pl.setSamlToken(samlToken);
        pl.setEidasLoa("http://eidas.europa.eu/LoA/substantial");
        pl.setNodeSpec(null);
        pl.processAuthentication(request, response, userInf);
    }
    private void sendError(HttpServletResponse resp, String msg){
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try {
            resp.getWriter().print("<html><head><title>Error happened!</title></head>");
            resp.getWriter().print("<body>"+msg+"</body>");
            resp.getWriter().println("</html>");
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
