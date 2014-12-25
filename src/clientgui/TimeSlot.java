/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Siddharth
 */
public class TimeSlot implements Serializable {

    static final long serialVersionUID = -223370167187439218L;
    
    public class DayTime implements Serializable{
        public int start = -1;
        public int end = -1;
        
        @Override
        public String toString(){
            return "(" + start + ":" + end + ")";
        }
    }

    private final DayTime[] week;
    private int id;
    private double credits;

    public TimeSlot(int TimeSlotID, double credits) {
        this.week = new DayTime[6];
        for(int i = 0; i < 6; i++){
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
    
    public void setID(int newID){
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

    @Override
    public String toString() {
        HashMap<String, String> byTime = new HashMap<>();
        char[] dayChar = {'M', 'T', 'W', 'R', 'F', 'S'};
        for (int i = 0; i < 6; i++) {
            String today = GetTimeOnDay(i);
            if(today.equals("(-1:-1)"))
                continue;
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
}
