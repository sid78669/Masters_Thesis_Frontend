/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;
import java.util.HashSet;

/**
 *
 * @author Siddharth
 */
public class Professor implements Serializable {
    static final long serialVersionUID = -1762495948864341651L;
    private final HashSet<String> courseTaught;
    private String profName;
    private int profID;
    private int[] preferences;
    private double creditsAssigned;

    public Professor(int profID, String profName, double creditsAssigned) {
        this.profID = profID;
        this.profName = profName;
        this.creditsAssigned = creditsAssigned;
        courseTaught = new HashSet<>();
        preferences = new int[3];
    }

    public int getProfID(){
        return profID;
    }
    
    public void setProfID(int newID){
        profID = newID;
    }
    
    public void setProfName(String newName) {
        if (!newName.isEmpty()) {
            this.profName = newName;
        }
    }

    public String getProfName() {
        return profName;
    }

    public void setCredits(double newCreds) {
        if (newCreds >= 0) {
            this.creditsAssigned = newCreds;
        }
    }
    
    public double getCredits(){
        return this.creditsAssigned;
    }
    
    public void addCourseTaught(String course){
        if(!course.isEmpty() && !courseTaught.contains(course)){
            courseTaught.add(course);
        }
    }
    
    public boolean removeCourseTaught(String course){
        if(course.isEmpty())
            return false;
        else
            return courseTaught.remove(course);
    }
    
    public String getPreferenceString(){
        String rtnVal = "(";
        for (int i = 0; i < 3; i++) {
            rtnVal += preferences[i];
            if (i < 2) {
                rtnVal += "-";
            }
        }
        return rtnVal + ")";
    }
    
    public int[] getPreference(){
        return preferences;
    }
    
    public void setPreference(int [] prefs){
        if(prefs.length == 3){
            preferences[0] = prefs[0];
            preferences[1] = prefs[1];
            preferences[2] = prefs[2];
        }
    }

    Object[] getCoursesTaught() {
        return courseTaught.toArray();
    }

    boolean hasCourse(String selectedCourse) {
        return courseTaught.contains(selectedCourse);
    }
}
