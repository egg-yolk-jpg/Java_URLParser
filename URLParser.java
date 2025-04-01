/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.urlparser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * name: Yakimah Wiley 
 * assignment: M7 - URL Parser
 * date: 3/31/2025 
 * class: CMPSC222 - Secure Coding
 *
 */
public class URLParser {
    /**
     * URL Parser.
     * This program requests that a user provides a url and a key. The program
     * then scrapes through the website/url provided by the user and completes two
     * tasks:
     *      Task 1: Determines how many instances of the keyword occur on the 
     *          page that the url links to.
     *      Task 2: Looks for other hrefs provided on that page, then runs those
     *          secondary pages through the same keyword search completed in task 1
     * At the end of the program, the results of the search(es) are printed on the screen.
        * First: each successfully transversed URL will be displayed next to the number of times 
        *   the keyword has been located.
        * Second: The list of failed url scrapes will be displayed.
     **/
    
    /** Main function that calls all the relevant functions for the program to run
     * @param args
     * @throws MalformedURLException 
     */
    public static void main(String[] args) throws MalformedURLException{
        //Obtains the user-specified URL
        URL url = GetURL();
        
        //Obtains the user-specified keyword
        String keyword = GetKeyword();
        
        //Scraped through the webpage
        ParseURL(url, keyword);
    }
    
    /**
     * Retrieves user defined URL in the form of a string. 
     * The string is passed through an input validation process. If the string fails validation
     * the user is prompted to provide another domain. Otherwise, the user-defined
     * url is formatted then passed back to the main function
     * @return 
     */
    private static URL GetURL(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("What website will we be visiting today? (Must be in http(s) format)");
        String input = scanner.nextLine();
        try{ 
            //URLChecker is responsible for the input validation process
            if (URLChecker(input)) {
                URL url = new URL(input);
                return url;
            } else {
                System.out.println("Invalid URL. Try again.");
                return GetURL();
            }
        }catch(MalformedURLException ex){
            System.out.println("Invalid URL. Try again.");
            return GetURL();
        }               
    }
    
    /**
     * This function simply takes a strings and verifies that it matches against
     * the validation regex
     * @param url
     * @return 
     */
    private static Boolean URLChecker(String url){
        Pattern pattern = Pattern.compile("(http|https):\\/\\/.*");
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
        
    }
    
    /**
     * This function receives a user-defined keyword. 
     * As long as the keyword is less than 20 characters, the program will run as expected.
     * Otherwise, the user will be prompted to choose a different keyword
     * @return 
     */
    private static String GetKeyword(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select a keyword (less than 20 characters)");
        String input = scanner.nextLine();
        if(input.length() < 20){
            return input;
        }else{
           System.out.println("The provided value is too long. Try again.");
           return GetKeyword();
        }
    }
    
    /**
     * This function is in charge of initializing the wrappers and scrapping the user-defined
     * url and all hrefs discovered on the page.
     * @param unwrapped_url
     * @param keyword 
     */
    private static void ParseURL(URL unwrapped_url, String keyword){
        try{
            URLWrapper url = new URLWrapper(unwrapped_url);
            
            //Initializes the arraylist that will hold all urls that throw IOException
            ArrayList<String> exception_List = new ArrayList();
            
            //Uses a HashMap to organize urls and their respective keyword counts
            HashMap<URL, Integer> map = new HashMap<>();
            
            //The keyword search is ran for the user-define url
            url.findCount(keyword);
            map.put(url.getURL(), url.getCount());

            //InnerURLs are extracted from the user-defined URL and returned as an ArrayList
            ArrayList<URLWrapper> innerURLS = url.extractInnerURLS(keyword);
            /**
             * Each url within the innerURLS arraylist are ran through the process of 
             * determining the count of keyword appearance
             *      If no exception is thrown, the url and it's associated count are stored in the hashmap as
             *          a key:value pair. This is used to ensure that each url maintains knowledge of its
             *          correct keyword count after being sorted
             */
            for (URLWrapper item : innerURLS) {
                try{
                    item.findCount(keyword);
                    //The url and its associated keyword count is added to the hashmap
                    map.put(item.getURL(), item.getCount());
                }catch (IOException ex) {
                    //This specifies how the exception will be formatted
                    exception_List.add(String.format("""
                                                     IOException \t\t Domain: %s """, item.getURL()));
                }
            }
            
            //The map is passed to the function in charge of sorting and then returned to the original variable
            map = SortedMap(map);
            
            
            
            
            String filePath = "URLParser.txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                //The below specifies how successfully processed urls are to be printed on the console and prints them
                String temp = "\nKeyword: " + keyword.toUpperCase();
                writer.write(temp + "\n");
                System.out.println(temp);
                
                for (HashMap.Entry<URL, Integer> entry : map.entrySet()) {
                    URL key = entry.getKey();
                    Integer value = entry.getValue();
                    temp = "Keyword Count: " + value + " \t\t URL: " + key;
                    writer.write(temp + "\n");
                    System.out.println(temp);
                }

                //Errors are printed in this area
                temp = "\n===== \t ERRORS \t =====\n";
                writer.write(temp);
                System.out.printf(temp);
                for (String err : exception_List) {
                    temp = err;
                    writer.write(temp + "\n");
                    System.out.println(temp);
                }
                writer.close();
                
            } catch (IOException ex) {
                System.err.println("An error occurred while writing to the file: " + ex.getMessage());
            }
            
            
            
            
            
            
        }catch(IOException ex){
            System.out.println(ex);
        }
    }
    
    //This function is in charge of sorting the hashmap by keyword count
    private static HashMap SortedMap(HashMap map){
        //The hashmap is first made into a linkedList so that Collections.sort can be called
        List<HashMap.Entry<URL, Integer>> list = new LinkedList<>(map.entrySet());

        // Collections sorts the list in ascending order
        Collections.sort(list, (count1, count2) -> count1.getValue().compareTo(count2.getValue()));
        
        //Then we reverse the order
        Collections.reverse(list);

        // put data from sorted list to is placed back into hashmap format and returned to the calling method
        HashMap<URL, Integer> temp = new LinkedHashMap<>();
        for (HashMap.Entry<URL, Integer> item : list) {
            temp.put(item.getKey(), item.getValue());
        }
        
        return temp;
    }
}
