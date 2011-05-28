/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.forgerock.openidm.testselenium;

/**
 *
 * @author matthiastristl
 */
public class Constants {
    public static String hostName = "localhost";
    public static Integer port = 4445;
    public static String userAgent = "*safari";
    public static String baseURL ="http://localhost:8080/idm";
    public static String loginUser = "administrator";
    public static String password = "secret";
    
// used in cleanAll
    public static Boolean removeAll = true;
    public static Boolean removeUsers = true;
    public static Boolean removeResource = true;
// used in addResource
    public static Boolean removeResourceAtEnd = false;
// used in addUsers
    public static Boolean removeUsersAtEnd = false;
    public static Integer nrUsers = 2;

}
