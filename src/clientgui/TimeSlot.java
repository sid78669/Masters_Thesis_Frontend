/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Siddharth
 */
public class TimeSlot implements Serializable {

    static final long serialVersionUID = -223370167187439218L;

    public class DayTime implements Serializable {

        public int start = -1;
        public int end = -1;

        @Override
        public String toString() {
            return "(" + start + ":" + end + ")";
        }
    }

    private final DayTime[] week;
    private int id;
    private double credits;

    public TimeSlot(int TimeSlotID, double credits) {
        this.week = new DayTime[6];
        for (int i = 0; i < 6; i++) {
            week[i] = new DayTime();
        }
        this.id = TimeSlotID;
        this.credits = credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public double getCredits() {
        return this.credits;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int newID) {
        this.id = newID;
    }

    public boolean SetTime(int day, int start, int end) {
        if (day >= 0 && day <= 5) {
            week[day].start = start;
            week[day].end = end;
            return true;
        } else {
            return false;
        }
    }

    public String GetTimeOnDay(int day) {
        return week[day].toString();
    }

    public HashSet<String> GetUniqueTimes() {
        HashSet<String> uTimes = new HashSet<>();
        for (int i = 0; i < 7; i++) {
            if (week[i].start == -1) {
                continue;
            }
            if (!uTimes.contains(String.valueOf(week[i].start))) {
                uTimes.add(String.valueOf(week[i].start));
            }
            if (!uTimes.contains(String.valueOf(week[i].end))) {
                uTimes.add(String.valueOf(week[i].end));
            }
        }
        return uTimes;
    }

    @Override
    public String toString() {
        HashMap<String, String> byTime = new HashMap<>();
        char[] dayChar = {'M', 'T', 'W', 'R', 'F', 'S'};
        for (int i = 0; i < 6; i++) {
            String today = GetTimeOnDay(i);
            if (today.equals("(-1:-1)")) {
                continue;
            }
            if (byTime.containsKey(today)) {
                byTime.put(today, byTime.get(today) + dayChar[i]);
            } else {
                byTime.put(today, String.valueOf(dayChar[i]));
            }
        }

        String rtn = "";
        for (String s : byTime.keySet()) {
            rtn += " " + byTime.get(s) + " " + s;
        }

        return rtn.trim();
    }

    public String GetPrettyTime() {
        HashMap<String, String> byTime = new HashMap<>();
        char[] dayChar = {'G', 'H', 'I', 'J', 'K', 'L'};
        for (int i = 0; i < 6; i++) {
            String today = GetTimeOnDay(i);
            if (today.equals("(-1:-1)")) {
                continue;
            }else {
                today = today.replace("(", "").replace(")", "");
                String start = today.split(":")[0];
                String end = today.split(":")[1];
                today = "(" + ConvertToRegularTime(start) + "-" + ConvertToRegularTime(end) + ")";
            }
            if (byTime.containsKey(today)) {
                byTime.put(today, byTime.get(today) + dayChar[i]);
            } else {
                byTime.put(today, String.valueOf(dayChar[i]));
            }
        }

        StringBuilder output = new StringBuilder();
        for (String s : byTime.keySet()) {
            output.append(byTime.get(s));
            output.append(s);
            output.append(" ");
        }
        
        output.append("</html>");
        
        String rtnVal = output.toString().trim();
        rtnVal = rtnVal.replaceAll("G", "M");
        rtnVal = rtnVal.replaceAll("H", "T");
        rtnVal = rtnVal.replaceAll("I", "W");
        rtnVal = rtnVal.replaceAll("J", "R");
        rtnVal = rtnVal.replaceAll("K", "F");
        rtnVal = rtnVal.replaceAll("L", "S");
        rtnVal = rtnVal.replaceAll(" ", "<br /");
        return rtnVal;
    }

    private String ConvertToRegularTime(String input) {
        if(input.length() < 4){
            input = "0" + input;
        }
        int hour = Integer.parseInt(input.substring(0, 2));
        int minute = Integer.parseInt(input.substring(2));
        if (hour > 12) {
            return String.valueOf(hour - 12) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + "PM";
        } else if (hour < 12) {
            return String.valueOf(hour) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + "AM";
        } else {
            return String.valueOf(hour) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + "PM";
        }
    }

}
