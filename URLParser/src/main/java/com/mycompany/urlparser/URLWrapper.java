/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.urlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Apache_PHP
 */
public class URLWrapper {
    //url contains the link associated with this instance
    private URL url;
    //count contains the number of times a specified keyword has been found on the instance's link
    private int count = 0;
    
    URLWrapper(URL url){ // Constructor
        this.url = url;
    }
    
    /**
     * Find Count - Reads through the provided page in search of a user specified keyword.
     * As the method finds the provided word, it increases the counter by 1.
     * The number of times the word was found is stored in the count variable above.
     * 
     * @param keyword
     * @throws IOException 
     */
    protected void findCount(String keyword) throws IOException{
        //getReader provides an accessible BufferedReader without cluttering the code
        try (BufferedReader br = getReader(url)) {
            String line = br.readLine();
            while (line != null) {
                line = line.toLowerCase();
                
                //Uses the Pattern-Matcher classes to search for any instance of the provided keyword
                Pattern pattern = Pattern.compile(keyword.toLowerCase());
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    count++;
                }
                line = br.readLine();
            }
        }
    }
    
    /**
     * Calls the Buffered Reader.
     * This function is solely in charge of returning the buffered reader to
     * any method that requires it. It is here to reduce clutter in the code.
     * @param url
     * @return
     * @throws IOException 
     */
    private BufferedReader getReader(URL url) throws IOException{
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setConnectTimeout(5000);   //set 5 sec to wait while connecting
        return new BufferedReader(new InputStreamReader(uc.getInputStream()));
    }
    
    /**
     * Transverses through the url associated with an instance and returns every
     *  href found on the page.
     * Each url in the list is passed through URLWrapper and then stored in an arraylist
     *  that is returned to the calling function.
     * @param key
     * @return
     * @throws IOException 
     */
    protected ArrayList<URLWrapper> extractInnerURLS(String key) throws IOException{
        //getReader provides an accessible BufferedReader without cluttering the code
        ArrayList<URLWrapper> innerURLS;
        try (BufferedReader br = getReader(url)) {
            innerURLS = new ArrayList();
            String line = br.readLine();
            while (line != null) {
                line = line.toLowerCase();
                
                //Uses the Pattern-Matcher classes to search for any instance of the provided keyword
                Pattern pattern = Pattern.compile("href=\\\"((http|https):\\/\\/.*)\\\"");
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    //The below statment isolates the full url string, turns it into a Java URL, and then creates an 
                    //instance of URLWrapper based on the provided URL
                    innerURLS.add(new URLWrapper(new URL(matcher.group().substring(6, matcher.group().length()-1))));
                }
                line = br.readLine();
            }
        }
        return innerURLS;
    }
    
    //  GETTERS //
    protected URL getURL(){
        return url;
    }
    
    protected int getCount(){
        return count;
    }
}
