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
