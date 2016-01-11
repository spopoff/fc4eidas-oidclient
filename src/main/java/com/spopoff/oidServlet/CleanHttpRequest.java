/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.oidServlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author SPOPOFF
 */
public class CleanHttpRequest extends HttpServletRequestWrapper{
    private final Map<String, String[]> params = new HashMap<String, String[]>();
    public CleanHttpRequest(HttpServletRequest request) {
        super(request);
    }
    @Override
    public String getParameter(String name) {
        // if we added one, return that one
        if ( params.get( name ) != null ) {
              return params.get( name )[0];
        }
        // otherwise return what's in the original request
        HttpServletRequest req = (HttpServletRequest) super.getRequest();
        return req.getParameter( name );
    }

    public void addParameter( String name, String value ) {
        String[] val = new String[1];
        val[0] = value;
        params.put( name, val );
    }

    /**
     * surcharge du Map de paramètre
     * @return
     */
    @Override
    public Map<String, String[]> getParameterMap(){
        return Collections.unmodifiableMap(params);
    }
    @Override
    public Enumeration<String> getParameterNames()
    {
        return Collections.enumeration(params.keySet());
    }    
    
}
