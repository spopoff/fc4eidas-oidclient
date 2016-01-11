/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff;

import com.spopoff.frconnect.FcTranslateAttr;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SPOPOFF
 */
public class transfoTest {
    
    public transfoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void hello() {
        FcTranslateAttr transfo = new FcTranslateAttr();
        if(transfo.isIsErr()){
            Assert.assertFalse(transfo.isIsErr());
        }
        Assert.assertEquals("egal", transfo.getTransfoName("sub"), "PersonIdentifier");
     
     }
}
