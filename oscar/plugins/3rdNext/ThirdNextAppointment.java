/*
 * This program is a standalone application to determine the 3rd next available
 * appointment based on data in an Oscar EMR database. It assumes a MySQL
 * database and the presence of a mysql-connector-java-*.jar file
 * in the classpath.
 * 
 * An example of how to compile and execute the program (after setting
 * appropriate dbUrl, dbUser and dbPassword strings in
 * ThirdNextAppointment.properties) is:
 * 
 * javac ThirdNextAppointment.java
 * java -cp ".:mysql-connector-java-5.1.28.jar" ThirdNextAppointment
 * 
 * The generateReport method is based on code from Oscar's 
 * src/main/java/oscar/oscarReport/reportByTemplate/ThirdApptTimeReporter.java
 * written by Randy Jonasz [mailto:rjonasz@gmail.com]
 * Some code is also borrowed from
 * http://www.tutorialspoint.com/jdbc/jdbc-sample-code.htm
 *
 * @author rrusk
 */

//package thirdnextappointment;

// Import required packages
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThirdNextAppointment {

    // Application properties file
    static final String CONFIG_FILE = "ThirdNextAppointment.properties";
    // JDBC driver name
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    
    static Connection getDBConnection(String dbUrl, String user, String pass) {
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException ex) {
            //Handle errors for Class.forName
            ex.printStackTrace(System.out);
        }
        // Open a connection
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, pass);
        } catch (SQLException se) {
            //Handle errors for JDBC
            System.err.println("ERROR connecting to database.  Check the database configuration.");
            System.err.println("  The configuration is specified in " + CONFIG_FILE);
            System.err.println("  It can be overridding with command-line parameters of the form");
            System.err.println("  parmconfig=value.");
            System.err.println("The error message was:");
            System.err.println(se.getMessage());
            //se.printStackTrace(System.out);
        }
        return conn;
    }
    
    static private String getProviderInfo(Connection conn, String provider) {
        String providerSQL = "select practitionerNo, ohip_no from provider where provider_no=" + provider;
        ResultSet rs;
        Statement stmt;
        String providerInfo = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(providerSQL);
            if (rs.next()) {
                String cpsId = rs.getString("practitionerNo");
                String mspNo = rs.getString("ohip_no");
                providerInfo = "\"cpsid\":\"" + cpsId + "\", \"msp\":\"" + mspNo + "\"";
            }
        } catch (SQLException se) {
            se.printStackTrace(System.out);
            System.err.println(providerSQL);
        }
        return providerInfo;
    }

    /**
     * Wrapper to method from
     * src/main/java/oscar/oscarReport/reportByTemplate/ThirdApptTimeReporter.java
     * Processes one provider at a time rather than an aggregate over all providers
     * @param conn
     * @param dateFrom
     * @param schedSymbolsStr
     * @param provider
     * @param apptLength
     * @return 
     */
    static String generateReport(Connection conn, String dateFrom, String schedSymbolsStr, String provider, int apptLength) {
        int numDays = -1;

        String[] schedSymbols = null;
        if (schedSymbolsStr != null) {
            schedSymbols = schedSymbolsStr.split(",");
        }
        
        if (dateFrom == null || provider == null || schedSymbols == null) {
            return "dateFrom and provider must be set and at least one schedule symbol must be set";
        }

        String scheduleSQL = "select scheduledate.provider_no, scheduletemplate.timecode, scheduledate.sdate from scheduletemplate, scheduledate where scheduletemplate.name=scheduledate.hour and scheduledate.sdate >= '" + dateFrom + "' and  scheduledate.provider_no in (" + provider + ") and scheduledate.status = 'A' and (scheduletemplate.provider_no=scheduledate.provider_no or scheduletemplate.provider_no='Public') order by scheduledate.sdate";
        String apptSQL = "";//select start_time, end_time from appointment where provider_no = '" + provider_no + "' and status not like '%C%' and appointment_date = '";
        String schedDate = "";
        ResultSet rs;
        ResultSet rs2;
        int unbooked;
        Statement stmt;
        Statement stmt2;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(scheduleSQL);
            int duration;
            String timecodes, code;
            String tmpApptSQL;
            String apptTime;

            int dayMins = 24 * 60;
            int iHours, iMins, apptHour_s, apptMin_s, apptHour_e, apptMin_e;
            int codePos;
            int latestApptHour, latestApptMin;
            int third = 3;
            int numAppts = 0;
            boolean codeMatch;
            while (rs.next() && numAppts < third) {
                timecodes = rs.getString("timecode");

                duration = dayMins / timecodes.length();

                schedDate = rs.getString("sdate");
                tmpApptSQL = "select start_time, end_time from appointment where provider_no = '" + rs.getString("provider_no") + "' and status not like '%C%' and appointment_date = '" + schedDate + "' order by start_time asc";

                stmt2 = conn.createStatement();
                rs2 = stmt2.executeQuery(tmpApptSQL);
                codePos = 0;
                latestApptHour = latestApptMin = 0;
                unbooked = 0;
                for (int iTotalMin = 0; iTotalMin < dayMins; iTotalMin += duration) {
                    code = timecodes.substring(codePos, codePos + 1);
                    ++codePos;
                    iHours = iTotalMin / 60;
                    iMins = iTotalMin % 60;
                    while (rs2.next()) {
                        apptTime = rs2.getString("start_time");
                        apptHour_s = Integer.parseInt(apptTime.substring(0, 2));
                        apptMin_s = Integer.parseInt(apptTime.substring(3, 5));

                        if (iHours == apptHour_s && iMins == apptMin_s) {
                            apptTime = rs2.getString("end_time");
                            apptHour_e = Integer.parseInt(apptTime.substring(0, 2));
                            apptMin_e = Integer.parseInt(apptTime.substring(3, 5));

                            if (apptHour_e > latestApptHour || (apptHour_e == latestApptHour && apptMin_e > latestApptMin)) {
                                latestApptHour = apptHour_e;
                                latestApptMin = apptMin_e;
                            }
                        } else {
                            rs2.previous();
                            break;
                        }

                    }

                    codeMatch = false;
                    for (int schedIdx = 0; schedIdx < schedSymbols.length; ++schedIdx) {

                        if (code.equals(schedSymbols[schedIdx])) {
                            codeMatch = true;
                            //System.out.println("codeMatched " + codeMatch);
                            if (iHours > latestApptHour || (iHours == latestApptHour && iMins > latestApptMin)) {
                                unbooked += duration;

                                if (unbooked >= apptLength) {
                                    unbooked = 0;
                                    ++numAppts;
                                    if (numAppts == third) {
                                        break;
                                    }
                                }
                            }

                        }
                    } //end for schedule symbols

                    if (numAppts == third) {
                        break;
                    }

                    if (!codeMatch) {
                        unbooked = 0;
                    }

                } //end for

            } //end while

            String calcDaysSQL = "select datediff('" + schedDate + "','" + dateFrom + "')";
            if (numAppts == third) {
                rs = stmt.executeQuery(calcDaysSQL);
                if (rs.next()) {
                    numDays = rs.getInt(1);
                }
            }

        } catch (SQLException se) {
            se.printStackTrace(System.out);
        } catch (NumberFormatException e) {
            e.printStackTrace(System.out);
        }

        // String sql = scheduleSQL + ";\n " + apptSQL;
        // System.out.println("sql: " + sql);
        String providerInfo = getProviderInfo(conn, provider);
        
        return makeClinicianJson(numDays, dateFrom, schedDate, provider, providerInfo);
    }

    static private String makeClinicianJson(int numDays, String requestDate, String thirdDate, String provider, String providerInfo) {
        return "{\"clinician\":\"" + provider + "\", " + providerInfo + ", " + "\"3rdnext\":" + numDays + "}";
    }
    
    static boolean isValidParameter(String s) {
        if (s.contains("=")) {
            String parts[] = s.split("=");
            if (((parts.length == 2) && (parts[0].length() > 0)) && (parts[1].length() > 0)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        
        Boolean errorOccurred = false;
        
        // Requires all commandline configuration parameters, if any, to be in
        //   the format parmname=value
        // Parameters on the commandline override parameters in the property
        //   configuration file
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (isValidParameter(arg)) {
                sb.append(arg).append("\n");
            } else {
                System.out.println("Parameter [" + arg +"] is not a valid property string");
            }
        }
        String commandlineProperties = sb.toString();
        
        // Get properties file with configuration parameters from class path
        Properties prop = new Properties();
        InputStream input;
        
        input = ThirdNextAppointment.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (input == null) {
            System.out.println("Unable to find configuration file " + CONFIG_FILE);
            return;
        }

        try {
            prop.load(input);
            if (!commandlineProperties.isEmpty()) {  // override with command line properties if any
                prop.load(new StringReader(commandlineProperties));
            }
        } catch (IOException ex) {
            Logger.getLogger(ThirdNextAppointment.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
        
        String dbUrl = prop.getProperty("dburl");
        if (dbUrl == null || dbUrl.isEmpty()) {
            System.err.println("The database url parameter 'dburl' must be set.");
            errorOccurred = true;
        }
        String dbUser = prop.getProperty("dbuser");
        if (dbUser == null || dbUser.isEmpty()) {
            System.err.println("The database user parameter 'dbuser' must be set.");
            errorOccurred = true;
        }
        String dbPassword = prop.getProperty("dbpassword");
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("The database user password parameter 'dbpassword' must be set.");
            errorOccurred = true;
        }
        String schedSymbols = prop.getProperty("schedsymbols");
        if (schedSymbols == null || schedSymbols.isEmpty()) {
            System.err.println("The schedule symbols parameter 'schedsymbols' must be set.");
            System.err.println("  It should look something like 'schedsymbols=1,2,3'");
            errorOccurred = true;
        }
        String providerNums = prop.getProperty("providernums");
        if (providerNums == null || providerNums.isEmpty()) {
            System.err.println("The provider number parameter of the service providers must be set.");
            System.err.println("  It should look something like 'providernums=101,110'");
            errorOccurred = true;
        }
        Integer apptLength = null;
        String apptLengthStr = prop.getProperty("apptlength");
        if (apptLengthStr == null || apptLengthStr.isEmpty()) {
            System.err.println("The appointment length parameter apptlength must be set.");
            System.err.println("  It should look something like 'apptlength=15'");
            errorOccurred = true;
        } else {
            try {
                apptLength = Integer.parseInt(prop.getProperty("apptlength"));
            } catch (NumberFormatException nf) {
                System.err.println("The apptlength parameter is invalid.");
                errorOccurred = true;
            }
        }
//        String dateFrom = prop.getProperty("datefrom");
//        if (dateFrom == null || dateFrom.isEmpty()) {
//            System.err.println("The date from parameter must be set.");
//            System.err.println("  It should look something like 'datefrom=2015-06-09'");
//            errorOccurred = true;
//        }
        String clinicName = prop.getProperty("clinic");
        if (clinicName == null || clinicName.isEmpty()) {
            System.err.println("The clinic name parameter must be set.");
            errorOccurred = true;
        }
        
        String earliestDate = prop.getProperty("earliest");
        if (earliestDate == null || earliestDate.isEmpty()) {
            System.err.println("The earliestDate parameter must be set.");
            errorOccurred = true;
        }
        
        String weekDay = prop.getProperty("weekday");
        if (weekDay == null || weekDay.isEmpty()) {
            System.err.println("The weekday paramater must be set.");
            errorOccurred = true;
        }
            
        if (errorOccurred) {
            System.err.println("Set unconfigured parameters in '" + CONFIG_FILE + "'");
            System.err.println("  or pass them in on the command-line in the format 'parm=value'");
            return;
        }
        
        Connection conn = null;        
        try {            
            //System.out.println("Connecting to database...");
            conn = getDBConnection(dbUrl, dbUser, dbPassword);

            
            String[] providers = null;
            if (providerNums != null) {
                providers = providerNums.split(",");
            }

            ArrayList<String> weekDays = ReportDate.getDates(earliestDate, weekDay);
            
            String filepath = null;
            
            if (conn != null && providers != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                for (String dateFrom: weekDays) {
                    filepath = "./reports/thirdnextappt_" + clinicName + "_" + dateFrom + ".txt";
                    File f = new File(filepath);
                    if (!f.exists()) {
                        PrintWriter out = new PrintWriter(filepath);
                        long timestamp = df.parse(dateFrom).getTime() / 1000; // time in seconds rather than milliseconds
                        out.println("{\"clinic\":\"" + clinicName + "\", \"date\":" + timestamp + ", \"clinicians\":[");
                        int index = 0;
                        for (String provider : providers) {
                            out.print(generateReport(conn, dateFrom, schedSymbols, provider, apptLength));
                            index++;
                            if (index < providers.length) {
                                out.println(", ");
                            } else {
                                out.println("");
                            }
                        }
                        out.println("]}");
                        out.close();
                    }
                }
            }            

            //STEP 6: Clean-up environment
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace(System.out);
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace(System.out);
        } finally {
            //finally block used to close resources
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace(System.out);
            }//end finally try
        }//end try
        //System.out.println("Goodbye!");
    }//end main
}//end ThirdNextAppointment
