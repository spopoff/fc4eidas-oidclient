package com.spopoff.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import java.util.Map;
import org.apache.struts2.interceptor.ParameterAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginAction extends ActionSupport implements ParameterAware {

    private static final long serialVersionUID = -7243683543548722148L;
    private static final Logger LOG = LoggerFactory.getLogger(LoginAction.class);
    private String samlToken;
    private String encryptAssertion;
    private String username;
    private String callback = "...";
    private Map<String, String[]> maps;


    @Override
    public String execute(){
        LOG.debug("Welcome in action LOGIN, back to="+callback);
        if(maps==null){
            LOG.error("erreur param null");
            return Action.ERROR;
        }
        if(maps.isEmpty()){
            LOG.error("erreur param vide");
            return Action.ERROR;
        }
        try {
            this.samlToken = maps.get("samlToken")[0];
            this.username = maps.get("username")[0];
            this.callback = maps.get("callback")[0];
        } catch (Exception e) {
            LOG.error("erreur recup param="+e);
            return Action.ERROR;
        }
        LOG.debug("Bye, back to="+callback);
        return Action.SUCCESS;
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

    public String getEncryptAssertion() {
            return encryptAssertion;
    }

    public void setEncryptAssertion(String encryptAssertion) {
            this.encryptAssertion = encryptAssertion;
    }

    public void setParameters(Map<String, String[]> maps) {
        this.maps = maps;
    }
}
