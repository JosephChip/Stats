package Stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 * The purpose of the Model class is to hold all of the data and perform all the
 * functionality involved in generating reports. The model is what does the
 * actual work below the GUI. The GUI is just a way for the user to interact
 * with the model. It is also worth noting that Model is a singleton class; that
 * is, it only ever has one instance which is shared by other classes.
 *
 * @author Christopher Buss
 * @version 1.0
 */
public class Model {

    // Instantiate the Model right away.
    private static Model model = new Model();
    // DefaultListModels are used to populate the JLists in the GUIs.
    private DefaultListModel<String> countiesAndMunicipalities;
    private DefaultListModel<String> reports;
    // The key is the name of the report and the DefaultListModel
    // holds the rules in the report as strings.
    private HashMap<String, DefaultListModel<String>> rules;
    // Holds all of the rules currently in effect. The key is the report name
    // and the value is the rules.
    private HashMap<String, ArrayList<ArrayList<String>>> rulesData;
    // Holds the currently selected report in PanelRule.
    private String selectedReport;
    // The file locations given by the user.
    private String csvFileLocation;
    private String ruleFileLocation;
    // Holds all of the entries in the given .csv file. The key is "dd/yyyy".
    // Entries are grouped together based on their date.
    private HashMap<String, ArrayList<String>> data;
    // Holds the possible "equals" values in a dropdown menu.
    private HashMap<Integer, DefaultComboBoxModel<String>> equalsDropdown = new HashMap<>();
    // Constants for possible rules. These are the "wheres" of the rules.
    // The "equals" are the user-entered strings that the "wheres" are
    // checked against.
    public static final int COUNTY = 0;
    public static final int MUNICIPALITY = 1;
    public static final int ZIP_CODE = 2;
    public static final int BODY_OF_WATER = 3;
    public static final int CONDO_NAME = 4;
    public static final int PROPERTY_TYPE = 5;
    public static final int NUM_OF_OPTIONS = 6;
    // Variables for reading or writing to config.properties file
    private static final String PROPERTIES_FILE = "config.properties";
    private File file = new File(PROPERTIES_FILE);
    private Properties prop;
    private OutputStream output;
    private InputStream input;
    // variables to store/load properties
    private static String companyProperty; 
    private static int agencyName;
    private static int propertyType;
    private static int daysOnMarket;
    private static int soldDate;
    private static int listPrice;
    private static int soldPrice;
    private static int municipality;
    private static int county;
    private static int zipCode;
    private static int sellingAgency;
    private static int bodyOfWater;
    private static int condominiumName;
    
    final int ARR_LISTING_COMPANY_NAME = 0;
    final int ARR_PROPERTY_TYPE = 1;
    final int ARR_DAYS_ON_MARKET = 2;
    final int ARR_SOLD_DATE = 3;
    final int ARR_LIST_PRICE = 4;
    final int ARR_SOLD_PRICE = 5; 
    final int ARR_MUNICIPALITY = 6;
    final int ARR_COUNTY = 7;
    final int ARR_ZIP_CODE = 8;
    final int ARR_SELLING_COMPANY_NAME = 9;
    final int ARR_BODY_OF_WATER = 10;
    final int ARR_CONDO_NAME = 11;
    
    /**
     * Adds to the arrays that will populate the "equals" JComboBox.
     *
     * @param where The integer representing the "where".
     * @param equals The "equals" that the where is to be checked against.
     */
    public void addToEqualsDropdown(int where, String equals) {
        DefaultComboBoxModel<String> arr;
        arr = equalsDropdown.get(where);
        if (arr.getIndexOf(equals) != -1) {
            equalsDropdown.put(where, arr);
        } else {
            arr.addElement(equals);
            equalsDropdown.put(where, arr);
        }
    }

    /**
     * Called to retrieve an array to fill the "equals" JComboBox.
     *
     * @param where The integer representing the "where".
     * @return An Object[] that is used to populate a JComboBox.
     */
    public DefaultComboBoxModel getEqualsDropdownArray(int where) {
        return equalsDropdown.get(where);
    }

    /**
     * Alphabetically sorts the contents of equalsDropdown.
     */
    private void sortEqualsDropdownArrays() {
        for (int i = 0; i < NUM_OF_OPTIONS; i++) {
            ArrayList<String> arrList = new ArrayList<>();
            DefaultComboBoxModel<String> combo = equalsDropdown.get(i);
            String[] arr = new String[combo.getSize()];
            for (int j = 0; j < arr.length; j++) {
                arr[j] = combo.getElementAt(j);
            }
            Arrays.sort(arr);
            combo.removeAllElements();
            for (Object str : arr) {
                combo.addElement((String) str);
            }
            equalsDropdown.put(i, combo);
        }
    }

    /**
     * Constructor is private to prevent outside instantiation.
     */
    private Model() {
        countiesAndMunicipalities = new DefaultListModel<>();
        reports = new DefaultListModel<>();
        rules = new HashMap<>();
        rulesData = new HashMap<>();
        csvFileLocation = "";
        ruleFileLocation = "";
        data = new HashMap();
        for (int i = 0; i < NUM_OF_OPTIONS; i++) {
            equalsDropdown.put(i, new DefaultComboBoxModel<String>());
        }
        
        prop = new Properties();
        
        try {
         
            // Create the default config.properties file if the config.properties
            // file does not exists.
            if (!file.exists()) {
                createDefaultPropertiesFile();
            }
            
            loadPropertiesFile();

        } catch(IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
        
    /**
     * Creates the config.properties file and loads it with default values
     */
    private void createDefaultPropertiesFile() throws IOException {
        
        if (output == null) {
            output = new FileOutputStream(PROPERTIES_FILE);
        }
        
        // Set default properties    
        prop.setProperty("company", "MyCompany");
        prop.setProperty("Agency Name", "1");
        prop.setProperty("Property Type", "6");
        prop.setProperty("Days on Market", "134");
        prop.setProperty("Sold Date", "14");
        prop.setProperty("List Price", "25");
        prop.setProperty("Sold Price", "42");
        prop.setProperty("Municipality", "41");
        prop.setProperty("County", "43");
        prop.setProperty("Zip Code", "46");
        prop.setProperty("Selling Agency", "9");
        prop.setProperty("Body of Water", "66");
        prop.setProperty("Condominium Name", "58");
        
        // Save the data
        prop.store(output, null);
    }

    /**
     * Loads the config.properties file's values into memory
     */
    private void loadPropertiesFile() throws IOException {
        
        if (input == null) {
            input = new FileInputStream(PROPERTIES_FILE);
        }
        
        // Load the properties file.
        prop.load(input);
        
        // Load the values in
        companyProperty = prop.getProperty("company");
        agencyName = Integer.parseInt(prop.getProperty("Agency Name"));
        propertyType = Integer.parseInt(prop.getProperty("Property Type"));
        daysOnMarket = Integer.parseInt(prop.getProperty("Days on Market"));
        soldDate = Integer.parseInt(prop.getProperty("Sold Date"));
        listPrice = Integer.parseInt(prop.getProperty("List Price"));
        soldPrice = Integer.parseInt(prop.getProperty("Sold Price"));
        municipality = Integer.parseInt(prop.getProperty("Municipality"));
        county = Integer.parseInt(prop.getProperty("County"));
        zipCode = Integer.parseInt(prop.getProperty("Zip Code"));
        sellingAgency = Integer.parseInt(prop.getProperty("Selling Agency"));
        bodyOfWater = Integer.parseInt(prop.getProperty("Body of Water"));
        condominiumName = Integer.parseInt(prop.getProperty("Condominium Name"));
    }
    
    /**
     * Instead of instantiating, outside classes grab the existing instance.
     *
     * @return The singleton Model.
     */
    public static Model getInstance() {
        return model;
    }

    /**
     * Imports entries from the CSV file given by csvFileLocation. The entries
     * are imported into memory for quicker access; also, the counties and
     * municipalities of each entry are read into countiesAndMunicipalities in
     * order to populate the JList on PanelMain.
     *
     * @throws IOException
     */
    public void importCSVDataFromFile() throws IOException {

        // BufferedReader to read file.
        BufferedReader br = new BufferedReader(new FileReader(csvFileLocation));

        // Used for reading and spliting the entries.
        String line;
        String[] splitLine;
        ArrayList<String> arr;

        // Skip header of file.
        br.readLine();

        // Keep reading lines until there is nothing to read.
        while ((line = br.readLine()) != null) {

            // Whenever a comma is found, read forward to make sure that there
            // is either an even amount of quotes or no quotes at all. This
            // ensures that commas inside of quotes are not split.
            splitLine = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            // Pass the municipality and county.
            addMunicipalityAndCounty(
                    splitLine[municipality].replaceAll("\"", ""),
                    splitLine[county].replaceAll("\"", ""));

            String date = cleanDate(
                    splitLine[soldDate].replaceAll("\"", ""));

            // Use mm/yyyy as key.
            if (data.containsKey(date)) {
                arr = data.get(date);
            } else {
                arr = new ArrayList<>();
            }
                    
            // Reassemble only the relevant data for the entry back into a 
            // string and add it to the ArrayList whose key is the date of
            // the entry.            
            arr.add(splitLine[agencyName] + "," +                         // Listing Company Name- 0
                    splitLine[propertyType].replaceAll("\"", "") + "," +  // Property Type       - 1
                    splitLine[daysOnMarket].replaceAll("\"", "") + "," +  // DOM                 - 2
                    date + "," +                                          // Sold Date           - 3
                    splitLine[listPrice].replaceAll("\"", "") + "," +     // List Price          - 4
                    splitLine[soldPrice].replaceAll("\"", "") + "," +     // Sold Price          - 5
                    splitLine[municipality].replaceAll("\"", "") + "," +  // Municipality        - 6
                    splitLine[county].replaceAll("\"", "") + "," +        // County              - 7
                    splitLine[zipCode].replaceAll("\"", "") + "," +       // Zip Code            - 8
                    splitLine[sellingAgency] + "," +                      // Selling Company Name- 9
                    splitLine[bodyOfWater].replaceAll("\"", "") + "," +   // Body of Water       - 10
                    splitLine[condominiumName].replaceAll("\"", ""));     // Condo Name          - 11
            data.put(date, arr);
            
            // Add them to the "equals" dropbox for later use.
            addToEqualsDropdown(COUNTY, splitLine[county].replaceAll("\"", ""));
            addToEqualsDropdown(MUNICIPALITY, splitLine[municipality].replaceAll("\"", ""));
            addToEqualsDropdown(ZIP_CODE, splitLine[zipCode].replaceAll("\"", ""));
            addToEqualsDropdown(BODY_OF_WATER, splitLine[bodyOfWater].replaceAll("\"", ""));
            addToEqualsDropdown(CONDO_NAME, splitLine[condominiumName].replaceAll("\"", ""));
            addToEqualsDropdown(PROPERTY_TYPE, splitLine[propertyType].replaceAll("\"", ""));
        }

        // Sort after all of the data has been entered.
        sortAlphabetically(countiesAndMunicipalities);
        sortEqualsDropdownArrays();
    }

    /**
     * Takes a date in the format mm/dd/yyyy or yyyy-mm-dd and converts it to
     * the format mm/yyyy.
     *
     * @param date The date to be cleaned.
     * @return The singleton Model.
     */
    public String cleanDate(String date) {
        if (date.contains("-")) {
            return date.substring(5, 7) + "/" + date.substring(0, 4);
        } else {
            date = date.replaceAll("/.*/", "/");

            // Prepend a 0 onto the date if the month is less than 10.
            // Example: turn 1/2012 into 01/2012.
            if (date.length() < 7) {
                return "0" + date;
            } else {
                return date;
            }
        }
    }

    /**
     * Import rules from file given by ruleFileLocation. Rules are indexed by
     * line (where). The first line of the file is counties, the second line is
     * municipalities, etc. Each line contains strings separated by commas. Each
     * string is an equal. So if a file looked like this:
     *
     * Kenosha
     *
     * 53090
     *
     * There would be two rules in effect: Where county = "Kenosha" Where Zip
     * Code = "53090"
     *
     * @throws IOException
     */
    public void importRuleDataFromFile() throws IOException {

        // Clear the current rules in effect.
        rulesData = new HashMap<>();
        rules.clear();

        // BufferedReader to read file.
        BufferedReader br = new BufferedReader(new FileReader(ruleFileLocation));

        // Used for reading and spliting the entries.
        String line;
        String[] splitLine;
        ArrayList<String> arr;

        int lineNumber = 0;

        // Keep reading lines until there is nothing to read.
        while ((line = br.readLine()) != null) {

            // First line is the report name.
            addReport(line);
            selectedReport = line;

            // Next lines are rules.
            for (int i = 0; i < NUM_OF_OPTIONS; i++) {
                line = br.readLine();

                // Split the strings and add the rules.
                splitLine = line.split(",");
                for (int j = 0; j < splitLine.length; j++) {
                    if (splitLine[j].trim().length() > 0) {
                        addRule(i, splitLine[j]);
                    }
                }
                lineNumber++;
            }
        }
    }

    /**
     * Add the given municipality/county combination to the DefaultListModel.
     * This is purely aesthetic: there is not functional reason for displaying
     * the municipalities and counties.
     *
     * @param municipality Municipality to add to JList.
     * @param county County to add to JList.
     */
    public void addMunicipalityAndCounty(String municipality, String county) {

        // Clean input.
        municipality = cleanInput(municipality);
        county = cleanInput(county);

        // Format as "Municipality (County)".
        String entry = municipality + " (" + county + ")";

        // If the entry was not already added, add it.
        if (countiesAndMunicipalities.indexOf(entry) == -1) {
            countiesAndMunicipalities.addElement(entry);
        }
    }

    /**
     * Cleans the given string and spits it back out.
     *
     * @param str String to clean.
     * @return The cleaned string.
     */
    public String cleanInput(String str) {

        // Change first letter to uppercase and the rest to lowercase.
        str = str.toLowerCase();
        str = str.substring(0, 1).toUpperCase()
                + str.substring(1);

        // If there are two words, then do the same for the second word.
        int indexOfSpace = str.indexOf(' ');
        if (indexOfSpace != -1) {
            str = str.substring(0, indexOfSpace)
                    + str.substring(indexOfSpace, indexOfSpace + 2).toUpperCase()
                    + str.substring(indexOfSpace + 2);
        }

        return str;
    }

    /**
     * Alphabetically sorts the given DefaultListModel.
     *
     * @param list The list to sort.
     */
    private void sortAlphabetically(DefaultListModel list) {

        // Grab the array from list.
        Object[] arr = list.toArray();

        // Sort the array, remove all elements from list, and then add the 
        // sorted array back into the list.
        Arrays.sort(arr);
        list.removeAllElements();
        for (Object str : arr) {
            list.addElement((String) str);
        }
    }

    /**
     * Setter for csvFileLocation.
     *
     * @param location The absolute path for the CSV file.
     */
    public void setcsvFileLocation(String location) {
        csvFileLocation = location;
    }

    /**
     * Add a report.
     *
     * @param report
     */
    public void addReport(String report) {
        if (reports.contains(report)) {
            return;
        } else {
            reports.addElement(report);
            rules.put(report, new DefaultListModel<String>());
            ArrayList<ArrayList<String>> arr = new ArrayList<>();
            for (int i = 0; i < NUM_OF_OPTIONS; i++) {
                arr.add(new ArrayList<String>());
            }
            rulesData.put(report, arr);
        }
    }

    /**
     * Delete a report.
     *
     * @param report
     */
    public void deleteReport(String report) {

        // Remove the report from the DefaultListModel.
        reports.removeElement(report);

        // Remove the rules from the DefaultListMadel.
        rules.remove(report).clear();
        
        // Remove the rules from memory. 
        rulesData.remove(report);
    }

    public DefaultListModel<String> getReportsList() {
        return reports;
    }

    /**
     * Add a rule to the DefaultListModel (rule) and to the HashMap (rulesData).
     *
     * @param where The chosen rule's index.
     * @param equals What the rule should equal.
     */
    public void addRule(int where, String equals) {

        // Form the first part of the string for display in JList.
        String entry = "";
        switch (where) {
            case COUNTY:
                entry = "Where County = ";
                break;
            case MUNICIPALITY:
                entry = "Where Municipality = ";
                break;
            case ZIP_CODE:
                entry = "Where Zip Code = ";
                break;
            case BODY_OF_WATER:
                entry = "Where Body of Water = ";
                break;
            case CONDO_NAME:
                entry = "Where Condo Name = ";
                break;
            case PROPERTY_TYPE:
                entry = "Where Category = ";
                break;
        }

        // Add the equals part.
        entry += equals.trim();

        // Add to DefaultListModel for JList given the currently selected report.
        DefaultListModel<String> mod = rules.remove(selectedReport);
        mod.addElement(entry);
        sortAlphabetically(mod);
        rules.put(selectedReport, mod);

        // Add the rule to the HashMap using "where" as key.
        ArrayList<ArrayList<String>> arrArr = rulesData.remove(selectedReport);
        ArrayList<String> arrStr = arrArr.remove(where);
        arrStr.add(equals);
        arrArr.add(where, arrStr);
        rulesData.put(selectedReport, arrArr);
    }

    /**
     * Sets the currently selected report in PanelRule's reportList.
     *
     * @param report The currently selected report in PanelRule.
     */
    public void setSelectedReport(String report) {
        selectedReport = report;
    }

    /**
     * Returns the currently selected report in PanelRule's reportList.
     *
     * @return The currently selected report in PanelRule's reportList.
     */
    public String getSelectedReport() {
        return selectedReport;
    }

    /**
     * Setter for ruleFileLocation.
     *
     * @param location The absolute path for the rule file.
     */
    public void setRuleFileLocation(String location) {
        ruleFileLocation = location;
    }

    /**
     * Save the rule file in memory to disk.
     *
     * @throws IOException
     */
    public void saveRuleFile() throws IOException {
        String file = "";

        // Append .rule to file name if it is not already appended.
        if (ruleFileLocation.substring(ruleFileLocation.length() - 5,
                ruleFileLocation.length()).equals(".rule")) {
            file = ruleFileLocation;
        } else {
            file = ruleFileLocation + ".rule";
        }

        // FileWriter to write rules to file.
        FileWriter fw = new FileWriter(new File(file));

        // Cycle through reports.
        for (String report : rulesData.keySet()) {
            fw.write(report + "\n");
            ArrayList<ArrayList<String>> arrArr = rulesData.get(report);

            // Cycle through the possible "wheres". Each line represents a 
            // "where".
            ArrayList<String> arrStr;
            for (int i = 0; i < NUM_OF_OPTIONS; i++) {
                arrStr = arrArr.get(i);

                // Write all of the "equals" associated with the "where".
                for (String str : arrStr) {
                    fw.write(str + ",");
                }
                fw.write("\n");
            }
        }

        fw.close();
    }

    /**
     * Removes the rules selected by the user.
     *
     * @param selectedIndices An array on ints; the selected indices.
     */
    public void deleteRules(int[] selectedIndices) {
        Arrays.sort(selectedIndices);
        ArrayList<ArrayList<String>> arrArr = rulesData.remove(selectedReport);
        DefaultListModel<String> mod = rules.remove(selectedReport);
        ArrayList<String> arrStr;
        String entry;

        // Use the removed list item to find the corresponding item to remove
        // in rulesData.
        for (int i = selectedIndices.length - 1; i >= 0; --i) {
            entry = mod.remove(selectedIndices[i]);
            rules.put(selectedReport, mod);
            if (entry.contains("County")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(COUNTY);
                arrStr.remove(entry);
                arrArr.add(COUNTY, arrStr);
                rulesData.put(selectedReport, arrArr);
            } else if (entry.contains("Municipality")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(MUNICIPALITY);
                arrStr.remove(entry);
                arrArr.add(MUNICIPALITY, arrStr);
                rulesData.put(selectedReport, arrArr);
            } else if (entry.contains("Zip Code")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(ZIP_CODE);
                arrStr.remove(entry);
                arrArr.add(ZIP_CODE, arrStr);
                rulesData.put(selectedReport, arrArr);
            } else if (entry.contains("Body of Water")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(BODY_OF_WATER);
                arrStr.remove(entry);
                arrArr.add(BODY_OF_WATER, arrStr);
                rulesData.put(selectedReport, arrArr);
            } else if (entry.contains("Condo Name")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(CONDO_NAME);
                arrStr.remove(entry);
                arrArr.add(CONDO_NAME, arrStr);
                rulesData.put(selectedReport, arrArr);
            } else if (entry.contains("Category")) {
                entry = entry.substring(entry.indexOf("=") + 2, entry.length());
                arrStr = arrArr.remove(PROPERTY_TYPE);
                arrStr.remove(entry);
                arrArr.add(PROPERTY_TYPE, arrStr);
                rulesData.put(selectedReport, arrArr);
            }
        }
    }

    /**
     * Getter for countiesAndMunicipalities.
     *
     * @return
     */
    public DefaultListModel getCountiesAndMunicipalities() {
        return countiesAndMunicipalities;
    }

    /**
     * Getter for rules.
     *
     * @return
     */
    public DefaultListModel<String> getRulesList(String report) {
        return rules.get(report);
    }

    /**
     * Calls all of the methods for generating reports based on rules.
     *
     * @param quarter The selected quarter.
     */
    public void generateReports(int quarter, int baseYear) throws IOException {
        for (String report : rulesData.keySet()) {
            generateReport(quarter, report, baseYear);
        }
    }

    /**
     * Generate the given report.
     *
     * @param quarter The quarter to generate the report for.
     * @param report The report to generate.
     * @throws IOException
     */
    private void generateReport(int quarter, String report, int baseYear) throws IOException {

        // Grab the rules to generate the report.
        ArrayList<ArrayList<String>> arrArr = rulesData.get(report);

        // arrRules holds the "equals" for "where".
        ArrayList<String> arrRules;

        ArrayList<String> arrData; // All the properties sold within a mm/yyyy.
        String[] splitDataEntry; // The individual elements of an entry.

        // date will be the key for the data HashMap.
        String date = "";

        // Get the current year and the previous year.
        int currentYear = baseYear;
        int previousYear = currentYear - 1;

        // Hold the count for sold properties within a given range within a
        // given month. Rows represent the months and rows represent the price
        // range,
        int[][] currentYearStats = new int[13][14];
        int[][] previousYearStats = new int[13][14];

        String equals = ""; // Holds the "equals" string.
        String fileName = ""; // file name of the generated report.

        // Adjust the starting month and ending month based on the selected
        // quarter. If quater = 5, then startMonth and endMonth are not changed.
        int startMonth = 1;
        int endMonth = 12;
        switch (quarter) {
            case 1:
                startMonth = 1;
                endMonth = 3;
                break;
            case 2:
                startMonth = 4;
                endMonth = 6;
                break;
            case 3:
                startMonth = 7;
                endMonth = 9;
                break;
            case 4:
                startMonth = 10;
                endMonth = 12;
                break;
        }


        // Iterate through months looking for the given "equals".
        for (int j = startMonth; j <= endMonth; j++) {

            /*
             * CURRENT YEAR
             */

            // Build the key (mm/yyyy) to find all the entries within the
            // given month in the current year.
            if (j < 10) {
                date = "0" + j + "/" + currentYear;
            } else {
                date = j + "/" + currentYear;
            }

            // Grab all entries (houses sold) within the given mm/yyyy.
            arrData = data.get(date);

            // Skip this month/year if no properties are found.
            if (arrData != null) {

                // Iterate through all of the entries (house sold) in the given
                // mm/yyy.
                for (String entry : arrData) {

                    // Whenever a comma is found, read forward to make sure that there
                    // is either an even amount of quotes or no quotes at all. This
                    // ensures that commas inside of quotes are not split.
                    splitDataEntry = entry.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    // If the sold property is in the desired "where", then 
                    // check its sold price. An entry must match only one
                    // "equals" for any "where". If there are no "equals"
                    // for a given "where", then that particular "where"
                    // does not factor into the statement.
                    if ((arrArr.get(COUNTY).isEmpty() || arrArr.get(COUNTY).contains(splitDataEntry[ARR_COUNTY])) && 
                            (arrArr.get(MUNICIPALITY).isEmpty() || arrArr.get(MUNICIPALITY).contains(splitDataEntry[ARR_MUNICIPALITY])) && 
                            (arrArr.get(ZIP_CODE).isEmpty() || arrArr.get(ZIP_CODE).contains(splitDataEntry[ARR_ZIP_CODE])) && 
                            (arrArr.get(BODY_OF_WATER).isEmpty() || arrArr.get(BODY_OF_WATER).contains(splitDataEntry[ARR_BODY_OF_WATER])) &&
                            (arrArr.get(CONDO_NAME).isEmpty() || arrArr.get(CONDO_NAME).contains(splitDataEntry[ARR_CONDO_NAME])) && 
                            (arrArr.get(PROPERTY_TYPE).isEmpty() || arrArr.get(PROPERTY_TYPE).contains(splitDataEntry[ARR_PROPERTY_TYPE]))) {

                        double soldPrice = Double.parseDouble(splitDataEntry[ARR_SOLD_PRICE]);

                        // Grab sold price and increment one of the ranges.
                        if (soldPrice <= 59999) {
                            currentYearStats[j][0] += 1;
                        } else if (soldPrice <= 99999) {
                            currentYearStats[j][1] += 1;
                        } else if (soldPrice <= 149999) {
                            currentYearStats[j][2] += 1;
                        } else if (soldPrice <= 199999) {
                            currentYearStats[j][3] += 1;
                        } else if (soldPrice <= 249999) {
                            currentYearStats[j][4] += 1;
                        } else if (soldPrice <= 299999) {
                            currentYearStats[j][5] += 1;
                        } else if (soldPrice <= 399999) {
                            currentYearStats[j][6] += 1;
                        } else if (soldPrice <= 499999) {
                            currentYearStats[j][7] += 1;
                        } else if (soldPrice <= 749999) {
                            currentYearStats[j][8] += 1;
                        } else if (soldPrice <= 999999) {
                            currentYearStats[j][9] += 1;
                        } else if (1000000 <= soldPrice) {
                            currentYearStats[j][10] += 1;
                        }

                        // [11] is reserved for the sum of properties sold by 
                        // COMPANY +the sum of properties listed by COMPANY.
                        if(splitDataEntry[ARR_LISTING_COMPANY_NAME].contains(companyProperty)) {
                            currentYearStats[j][11] += 1;
                        }
                        if(splitDataEntry[ARR_SELLING_COMPANY_NAME].contains(companyProperty)) {
                            currentYearStats[j][11] += 1;
                        }
                        
                        // [12] is reserved for the total amount of sold
                        // properties.
                        currentYearStats[j][12] += 1;

                        // [13] is reserved for the total amount of cash
                        // made in the month.
                        currentYearStats[j][13] += soldPrice;
                    }
                }
            }

            /*
             * PREVIOUS YEAR
             */

            // Build the key (mm/yyyy) to find all the entries within the
            // given month in the previous year.
            if (j < 10) {
                date = "0" + j + "/" + previousYear;
            } else {
                date = j + "/" + previousYear;
            }

            // Grab all entries (houses sold) within the given mm/yyyy.
            arrData = data.get(date);

            // Skip this month if no properties are found.
            if (arrData != null) {

                // Iterate through all of the entries (house sold) in the given
                // mm/yyy.
                for (String entry : arrData) {

                    // Whenever a comma is found, read forward to make sure that there
                    // is either an even amount of quotes or no quotes at all. This
                    // ensures that commas inside of quotes are not split.
                    splitDataEntry = entry.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    // If the sold property is in the desired "where", then 
                    // check its sold price. An entry must match only one
                    // "equals" for any "where". If there are no "equals"
                    // for a given "where", then that particular "where"
                    // does not factor into the statement.
                    if ((arrArr.get(COUNTY).isEmpty()
                            || arrArr.get(COUNTY).contains(splitDataEntry[ARR_COUNTY]))
                            && (arrArr.get(MUNICIPALITY).isEmpty()
                            || arrArr.get(MUNICIPALITY).contains(splitDataEntry[ARR_MUNICIPALITY]))
                            && (arrArr.get(ZIP_CODE).isEmpty()
                            || arrArr.get(ZIP_CODE).contains(splitDataEntry[ARR_ZIP_CODE]))
                            && (arrArr.get(BODY_OF_WATER).isEmpty()
                            || arrArr.get(BODY_OF_WATER).contains(splitDataEntry[ARR_BODY_OF_WATER]))
                            && (arrArr.get(CONDO_NAME).isEmpty()
                            || arrArr.get(CONDO_NAME).contains(splitDataEntry[ARR_CONDO_NAME]))
                            && (arrArr.get(PROPERTY_TYPE).isEmpty()
                            || arrArr.get(PROPERTY_TYPE).contains(splitDataEntry[ARR_PROPERTY_TYPE]))) {

                        // Grab sold price and increment one of the ranges.
                        double soldPrice = Double.parseDouble(splitDataEntry[ARR_SOLD_PRICE]);

                        if (soldPrice <= 59999) {
                            previousYearStats[j][0] += 1;
                        } else if (soldPrice <= 99999) {
                            previousYearStats[j][1] += 1;
                        } else if (soldPrice <= 149999) {
                            previousYearStats[j][2] += 1;
                        } else if (soldPrice <= 199999) {
                            previousYearStats[j][3] += 1;
                        } else if (soldPrice <= 249999) {
                            previousYearStats[j][4] += 1;
                        } else if (soldPrice <= 299999) {
                            previousYearStats[j][5] += 1;
                        } else if (soldPrice <= 399999) {
                            previousYearStats[j][6] += 1;
                        } else if (soldPrice <= 499999) {
                            previousYearStats[j][7] += 1;
                        } else if (soldPrice <= 749999) {
                            previousYearStats[j][8] += 1;
                        } else if (soldPrice <= 999999) {
                            previousYearStats[j][9] += 1;
                        } else if (1000000 <= soldPrice) {
                            previousYearStats[j][10] += 1;
                        }

                        // [11] is reserved for the sum of properties sold by 
                        // COMPANY +the sum of properties listed by COMPANY.
                        if(splitDataEntry[ARR_LISTING_COMPANY_NAME].contains(companyProperty)) {
                            previousYearStats[j][11] += 1;
                        }
                        if(splitDataEntry[ARR_SELLING_COMPANY_NAME].contains(companyProperty)) {
                            previousYearStats[j][11] += 1;
                        }
                        
                        // [12] is reserved for the total amount of sold
                        // properties.
                        previousYearStats[j][12] += 1;

                        // [13] is reserved for the total amount of cash
                        // made in the month.
                        previousYearStats[j][13] += soldPrice;
                    }
                }
            }
        }

        // Add quarter to file name if full year is not selected.
        if (quarter < 5) {
            fileName = report + "Q" + quarter + ".csv";
        } else {
            fileName = report + currentYear + ".csv";
        }

        /**
         * PRINT THE FILE
         */
        
        /**
         * BEGIN MAIN REPORT.
         */
        
        // Create the report file.
        File reportFile = new File(fileName);

        // FileWriter to write data to report.
        FileWriter fw = new FileWriter(reportFile);

        // Write rules used to create this file.
        fw.write("Rules: \n");
        
        ArrayList<ArrayList <String>> arr = rulesData.get(report);
        
        ArrayList<String> arrRule = arr.get(COUNTY); 
        fw.write("Where County =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        arrRule = arr.get(MUNICIPALITY); 
        fw.write("Where Municipality =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        arrRule = arr.get(ZIP_CODE); 
        fw.write("Where Zip Code =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        arrRule = arr.get(BODY_OF_WATER); 
        fw.write("Where Body of Water =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        arrRule = arr.get(CONDO_NAME); 
        fw.write("Where Condo Name =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        arrRule = arr.get(PROPERTY_TYPE); 
        fw.write("Where Category =,");
        if(arrRule.size() == 0) 
            fw.write("ANY\n");
        for(int i = 0; i < arrRule.size(); i++) {
            if(i == arrRule.size() - 1)
                fw.write(arrRule.get(i) + "\n");
            else
                fw.write(arrRule.get(i) + ", ");
        }
        
        fw.write("\n");
        
        // Write first comma
        fw.write(",");

        // Print the header.
        for (int j = startMonth; j <= endMonth; j++) {

            // Find month.
            String month = "";
            switch (j) {
                case 1:
                    month = "January";
                    break;
                case 2:
                    month = "February";
                    break;
                case 3:
                    month = "March";
                    break;
                case 4:
                    month = "April";
                    break;
                case 5:
                    month = "May";
                    break;
                case 6:
                    month = "June";
                    break;
                case 7:
                    month = "July";
                    break;
                case 8:
                    month = "August";
                    break;
                case 9:
                    month = "September";
                    break;
                case 10:
                    month = "October";
                    break;
                case 11:
                    month = "November";
                    break;
                case 12:
                    month = "December";
                    break;
            }

            // Write header for month.
            if (j == endMonth) {
                fw.write(month + previousYear + ":,"
                        + month + currentYear + ":");
            } else {
                fw.write(month + previousYear + ":,"
                        + month + currentYear + ":,,,");
            }
        }

        fw.write("\n");

        // Print stats for 0-59999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("0-59999," + previousYearStats[j][0] + ","
                        + currentYearStats[j][0]);
            } else {
                fw.write("0-59999," + previousYearStats[j][0] + ","
                        + currentYearStats[j][0] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 60000-99999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("60000-99999," + previousYearStats[j][1] + ","
                        + currentYearStats[j][1]);
            } else {
                fw.write("60000-99999," + previousYearStats[j][1] + ","
                        + currentYearStats[j][1] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 100000-149999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("100000-149999," + previousYearStats[j][2] + ","
                        + currentYearStats[j][2]);
            } else {
                fw.write("100000-149999," + previousYearStats[j][2] + ","
                        + currentYearStats[j][2] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 150000-199999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("150000-199999," + previousYearStats[j][3] + ","
                        + currentYearStats[j][3]);
            } else {
                fw.write("150000-199999," + previousYearStats[j][3] + ","
                        + currentYearStats[j][3] + ",,");
            }

        }
        fw.write("\n");

        // Print stats 200000-249999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("200000-249999," + previousYearStats[j][4] + ","
                        + currentYearStats[j][4]);
            } else {
                fw.write("200000-249999," + previousYearStats[j][4] + ","
                        + currentYearStats[j][4] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 250000-299999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("250000-299999," + previousYearStats[j][5] + ","
                        + currentYearStats[j][5]);
            } else {
                fw.write("250000-299999," + previousYearStats[j][5] + ","
                        + currentYearStats[j][5] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 300000-399999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("300000-399999," + previousYearStats[j][6] + ","
                        + currentYearStats[j][6]);
            } else {
                fw.write("300000-399999," + previousYearStats[j][6] + ","
                        + currentYearStats[j][6] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 400000-499999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("400000-499999," + previousYearStats[j][7] + ","
                        + currentYearStats[j][7]);
            } else {
                fw.write("400000-499999," + previousYearStats[j][7] + ","
                        + currentYearStats[j][7] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 500000-749999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("500000-749999," + previousYearStats[j][8] + ","
                        + currentYearStats[j][8]);
            } else {
                fw.write("500000-749999," + previousYearStats[j][8] + ","
                        + currentYearStats[j][8] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 750000-999999.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("750000-999999," + previousYearStats[j][9] + ","
                        + currentYearStats[j][9]);
            } else {
                fw.write("750000-999999," + previousYearStats[j][9] + ","
                        + currentYearStats[j][9] + ",,");
            }
        }

        fw.write("\n");

        // Print stats 1000000.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("1000000+," + previousYearStats[j][10] + ","
                        + currentYearStats[j][10]);
            } else {
                fw.write("1000000+," + previousYearStats[j][10] + ","
                        + currentYearStats[j][10] + ",,");
            }
        }

        fw.write("\n");

        // Print stats total sales.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("Total Sales," + previousYearStats[j][12] + ","
                        + currentYearStats[j][12]);
            } else {
                fw.write("Total Sales," + previousYearStats[j][12] + ","
                        + currentYearStats[j][12] + ",,");
            }
        }

        fw.write("\n");

        // Print stats total money.
        for (int j = startMonth; j <= endMonth; j++) {
            String current = NumberFormat.getInstance().format(
                    currentYearStats[j][13]);
            String previous = NumberFormat.getInstance().format(
                    previousYearStats[j][13]);
            if (j == endMonth) {
                fw.write("Total Money,\"$" + previous + " \",\"$"
                        + current + " \"");
            } else {
                fw.write("Total Money,\"$" + previous + " \",\"$"
                        + current + " \",,");
            }
        }

        fw.write("\n");

        // Print stats sides for COMPANY.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("Side (" + companyProperty + ")," + previousYearStats[j][11] + ","
                        + currentYearStats[j][11]);
            } else {
                fw.write("Side (" + companyProperty + ")," + previousYearStats[j][11] + ","
                        + currentYearStats[j][11] + ",,");
            }
        }
        
        fw.write("\n");

        // Print stats total sides.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write("Side (Total)," + (previousYearStats[j][12] * 2) + ","
                        + (currentYearStats[j][12] * 2));
            } else {
                fw.write("Side (Total)," + (previousYearStats[j][12] * 2) + ","
                        + (currentYearStats[j][12] * 2) + ",,");
            }
        }
        
        fw.write("\n");

        // Print stats sides %COMPANY.
        for (int j = startMonth; j <= endMonth; j++) {

            if (j == endMonth) {
                fw.write(companyProperty + "," + 
                        (previousYearStats[j][11] / ((double) previousYearStats[j][12] * 2)) * 100 
                        + "%," + currentYearStats[j][11] / ((double) currentYearStats[j][12] * 2) * 100
                        + "%");
            } else {
                fw.write(companyProperty + "," + 
                        (previousYearStats[j][11] / ((double) previousYearStats[j][12] * 2)) * 100 
                        + "%," + currentYearStats[j][11] / ((double) currentYearStats[j][12] * 2) * 100
                        + "%,,");
            }
        }
        
        fw.write("\n\n\n");
        
        /**
         * BEGIN TOTAL SALES PER YEAR.
         */
        
        int sumCurrentYear = 0;
        int sumPreviousYear = 0;
        
        // Print header. 
        fw.write("," + previousYear + "," + currentYear + "\n");
        
        // Write totals of sales for 0-59999.
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][0];
            sumPreviousYear += previousYearStats[j][0];
        }
        fw.write("0-59999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 60000-99999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][1];
            sumPreviousYear += previousYearStats[j][1];
        }
        fw.write("60000-99999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 100000-149999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][2];
            sumPreviousYear += previousYearStats[j][2];
        }
        fw.write("100000-149999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 150000-199999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][3];
            sumPreviousYear += previousYearStats[j][3];
        }
        fw.write("150000-199999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 200000-249999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][4];
            sumPreviousYear += previousYearStats[j][4];
        }
        fw.write("200000-249999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 250000-299999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][5];
            sumPreviousYear += previousYearStats[j][5];
        }
        fw.write("250000-299999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 300000-399999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][6];
            sumPreviousYear += previousYearStats[j][6];
        }
        fw.write("300000-399999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 400000-499999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][7];
            sumPreviousYear += previousYearStats[j][7];
        }
        fw.write("400000-499999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 500000-749999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][8];
            sumPreviousYear += previousYearStats[j][8];
        }
        fw.write("500000-749999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for 750000-999999. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][9];
            sumPreviousYear += previousYearStats[j][9];
        }
        fw.write("750000-999999," + sumPreviousYear + "," + sumCurrentYear);
        sumCurrentYear = 0;
        sumPreviousYear = 0;
        fw.write("\n");
        
        // Write totals of sales for '1000000+. 
        for (int j = startMonth; j <= endMonth; j++) {
            sumCurrentYear += currentYearStats[j][10];
            sumPreviousYear += previousYearStats[j][10];
        }
        fw.write("'1000000+," + sumPreviousYear + "," + sumCurrentYear);
        
        fw.write("\n\n\n");
        
        /**
         * BEGIN TOTAL CASH PER YEAR.
         */
        
        // Print header.
        fw.write("," + previousYear + "," + currentYear + "\n");
        
        // Print cash totals for each month included in the report.
        for (int j = startMonth; j <= endMonth; j++) {
            
            // Find month.
            String month = "";
            switch (j) {
                case 1:
                    month = "January";
                    break;
                case 2:
                    month = "February";
                    break;
                case 3:
                    month = "March";
                    break;
                case 4:
                    month = "April";
                    break;
                case 5:
                    month = "May";
                    break;
                case 6:
                    month = "June";
                    break;
                case 7:
                    month = "July";
                    break;
                case 8:
                    month = "August";
                    break;
                case 9:
                    month = "September";
                    break;
                case 10:
                    month = "October";
                    break;
                case 11:
                    month = "November";
                    break;
                case 12:
                    month = "December";
                    break;
            }
            
            // Format and print.
            String current = NumberFormat.getInstance().format(
                    currentYearStats[j][13]);
            String previous = NumberFormat.getInstance().format(
                    previousYearStats[j][13]);   
            fw.write(month + ",\"$" + previous + " \",\"$" 
                    +  current + " \"");
            
            fw.write("\n");
        }
        
        // Close the FileWriter flushing the output.
        fw.close();
    }
}
