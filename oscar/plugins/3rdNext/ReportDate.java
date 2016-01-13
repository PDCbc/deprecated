/*
 * Determines date for specific weekday, example Tuesday, on preceeding weeks
 * back to a specified earliest date.
 * @author rrusk
 */
//package thirdnextappointment;

import java.io.File;
//import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rrusk
 */
public class ReportDate {
            
    static final ClassLoader loader = ReportDate.class.getClassLoader();
    
    public ClassLoader getLoader() {
        return loader;
    }
    
    static private int setWeekDay(String weekDay) {
        String matchDay = weekDay.toUpperCase();
        int result;
        if (matchDay.startsWith("MON")) {
            result = Calendar.MONDAY;
        } else if (matchDay.startsWith("TUE")) {
            result = Calendar.TUESDAY;
        } else if (matchDay.startsWith("WED")) {
            result = Calendar.WEDNESDAY;
        } else if (matchDay.startsWith("THU")) {
            result = Calendar.THURSDAY;
        } else if (matchDay.startsWith("FRI")) {
            result = Calendar.FRIDAY;
        } else if (matchDay.startsWith("SAT")) {
            result = Calendar.SATURDAY;
        } else { // must be Sunday
            result = Calendar.SUNDAY;
        }
        return result;
    }
    
 /**
  * 
  * @param earliestDate in format "yyyy-MM-dd"
  * @param weekDay with String value, for example "TUESDAY".
  * @return array of strings containing dates of specified weekday
  */
    static public ArrayList<String> getDates(String earliestDate, String weekDay) {
        ArrayList<String> weekdayArrayList = new ArrayList<String>();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date earliestReportDate;
        try {
            earliestReportDate = dateFormat.parse(earliestDate);
        } catch (ParseException ex) {
            Logger.getLogger(ReportDate.class.getName()).log(Level.SEVERE, null, ex);
            return weekdayArrayList;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(earliestReportDate);
        
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        
        Date reportDate = calendar.getTime();
        
        int cutoffYear = cal.get(Calendar.YEAR);
        int cutoffMonth = cal.get(Calendar.MONTH);
        int cutoffDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        GregorianCalendar gCal =
                new GregorianCalendar(cutoffYear, cutoffMonth, cutoffDayOfMonth);
        Date cutoffDate = gCal.getTime();
        
        int theWeekday = setWeekDay(weekDay);
        
        boolean includeToday = true;
        while (day != theWeekday) {
            includeToday = false;
            calendar.add(Calendar.DATE, 1);
            day = calendar.get(Calendar.DAY_OF_WEEK);
        }
        
        if (includeToday) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            reportDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
            weekdayArrayList.add(dateFormat.format(reportDate));
        }

        while (reportDate.after(cutoffDate)) {
            calendar.add(Calendar.DATE, -7);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            reportDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
            if (reportDate.after(cutoffDate)  || reportDate.equals(cutoffDate)) {
                weekdayArrayList.add(dateFormat.format(reportDate));
            }
        };
        return weekdayArrayList;
    }
    
    public static void main(String[] args) {
        
/*        File fd = new File("."); // current directory
        File[] files = fd.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.print("directory:");
            } else {
                System.out.print("     file:");
            }
            try {
                System.out.println(file.getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(ReportDate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
*/
        ReportDate rd = new ReportDate();
        ArrayList<String> tuesdays = rd.getDates("2014-04-01", "TUESDAY");

        String filepath = null;
        for (int i = 0; i < tuesdays.size(); i++) {
            filepath = "./reports/thirdnextappt_" + tuesdays.get(i) + ".txt";

            File f = new File(filepath);
            if (!f.exists()) {
                System.out.println("file '" + filepath + "' doesn't exist");
            }
        }
    }
}
