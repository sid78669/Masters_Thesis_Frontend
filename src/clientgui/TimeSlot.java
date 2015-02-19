/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public class TimeOfDay implements Serializable {

        public Calendar start = null;
        public Calendar end = null;

        public TimeOfDay() {
        }

        ;

        @Override
        public String toString() {
            StringBuilder rtnVal = new StringBuilder("(");
            rtnVal.append(start.get(Calendar.HOUR));
            rtnVal.append(":");
            if (start.get(Calendar.MINUTE) < 10) {
                rtnVal.append("0");
            }
            rtnVal.append(start.get(Calendar.MINUTE));
            if (start.get(Calendar.AM) == 1) {
                rtnVal.append(" AM - ");
            } else {
                rtnVal.append(" PM - ");
            }
            rtnVal.append(end.get(Calendar.HOUR));
            rtnVal.append(":");
            if (end.get(Calendar.MINUTE) < 10) {
                rtnVal.append("0");
            }
            rtnVal.append(end.get(Calendar.MINUTE));
            if (end.get(Calendar.AM) == 1) {
                rtnVal.append(" AM");
            } else {
                rtnVal.append(" PM");
            }

            return rtnVal.toString();
        }
    }

    private final DayTime[] week;
    private final TimeOfDay[] times;
    private int id;
    private double credits;

    public TimeSlot(int TimeSlotID, double credits) {
        this.week = new DayTime[6];
        this.times = new TimeOfDay[6];
        for (int i = 0; i < 6; i++) {
            week[i] = new DayTime();
            times[i] = new TimeOfDay();
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

    public boolean isConflict(TimeSlot other) {
        for (int i = 0; i < 6; i++) {
            if (week[i].start == -1 || other.week[i].start == -1) {
                continue;
            }
            if (week[i].start >= other.week[i].start && week[i].start < other.week[i].end) {
                return true;
            } else if (week[i].start <= other.week[i].start && week[i].end >= other.week[i].start) {
                return true;
            } else if (week[i].end >= other.week[i].start && week[i].end <= other.week[i].end) {
                return true;
            } else if (other.week[i].end >= week[i].start && other.week[i].end <= week[i].start) {
                return true;
            }
        }
        return false;
    }

    public boolean isConsecutive(TimeSlot other) {
        for (int i = 0; i < 6; i++) {
            if (week[i].start == -1) {
                continue;
            }
            Date currentStart = new Date(0, 0, 0, week[i].start / 100, week[i].start % 100, 0);
            Date currentEnd = new Date(0, 0, 0, week[i].end / 100, week[i].end % 100, 0);
            Date otherStart = new Date(0, 0, 0, other.week[i].start / 100, other.week[i].start % 100, 0);
            Date otherEnd = new Date(0, 0, 0, other.week[i].end / 100, other.week[i].end % 100, 0);

            long minutesSE = Math.abs(currentStart.getTime() - otherEnd.getTime()) / 60000;
            long minutesES = Math.abs(currentEnd.getTime() - otherStart.getTime()) / 60000;

            if (minutesSE <= 16 || minutesES <= 16) {
                return true;
            }
        }

        return false;
    }

    public boolean isSpreadOut(TimeSlot other) {
        for (int i = 0; i < 6; i++) {
            if (week[i].start != -1 && other.week[i].start != -1) {
                if ((isMorning() && other.isEvening()) || (isEvening() && other.isMorning())) {
                    return true;
                }
            }
        }
        return false;
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

    public boolean SetTime(int day, int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        if (day >= 0 && day <= 5) {
            times[day].start = new GregorianCalendar(0, 0, 0, hourStart, minuteStart);
            times[day].end = new GregorianCalendar(0, 0, 0, hourStart, minuteEnd);
            return true;
        } else {
            return false;
        }
    }

    public boolean isMorning() {
        return week[0].end < 1200 && week[1].end < 1200 && week[2].end < 1200 && week[3].end < 1200 && week[4].end < 1200;
    }

    public boolean isAfternoon() {
        return ((week[0].start != -1 && week[0].start >= 1200 && week[0].end < 1800) || week[0].start == -1) && ((week[1].start != -1 && week[1].start >= 1200 && week[1].end < 1800) || week[1].start == -1) && ((week[2].start != -1 && week[2].start >= 1200 && week[2].end < 1800) || week[2].start == -1)
                && ((week[3].start != -1 && week[3].start >= 1200 && week[3].end < 1800) || week[3].start == -1) && ((week[4].start != -1 && week[4].start >= 1200 && week[4].end < 1800) || week[4].start == -1);
    }

    public boolean isEvening() {
        return !(isMorning() || isAfternoon());
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
            } else {
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
        if (input.length() < 4) {
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
