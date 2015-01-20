/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Vector;

/**
 *
 * @author Siddharth
 */
public class Course implements Serializable {

    static final long serialVersionUID = -7810198861837087621L;
    private final HashSet<String> incompCourses;
    private String courseID;
    private String courseTitle;
    private int courseIDGenerated;
    private double creditValue;
    private int sectionCount;
    private int[] preferences;

    public Course(String courseID, String courseTitle, int courseIDGenerated, double creditValue, int sectionCount) {
        this.courseID = courseID.toLowerCase();
        this.courseTitle = courseTitle;
        this.courseIDGenerated = courseIDGenerated;
        this.creditValue = creditValue;
        this.sectionCount = sectionCount;
        this.incompCourses = new HashSet<>();
        this.preferences = new int[3];
    }

    @Override
    public String toString() {
        StringBuilder sbr = new StringBuilder();
        sbr.append(courseIDGenerated);
        sbr.append(": ").append(courseTitle).append("(").append(courseID).append(") ");
        sbr.append("Sections: ").append(sectionCount).append(" Credits: ").append(creditValue);
        sbr.append(" Incomps:");
        incompCourses.stream().forEach((c) -> {
            sbr.append(c).append(", ");
        });

        return sbr.toString();
    }

    public void addIncompatibleCourse(String incomp) {
        if (!incomp.isEmpty() && !incompCourses.contains(incomp)) {
            incompCourses.add(incomp);
        }
    }

    @SuppressWarnings("unchecked")
    public Vector<String> getIncompCourses() {
        Vector<String> v = new Vector(incompCourses);
        return v;
    }

    public boolean hasIncomp(String incomp) {
        return incompCourses.contains(incomp);
    }

    public boolean removeIncompatibleCourse(String incomp) {
        if (incomp.isEmpty()) {
            return false;
        } else {
            return incompCourses.remove(incomp);
        }
    }

    public String getTitle() {
        return courseTitle;
    }

    public void setTitle(String newTitle) {
        if (!newTitle.isEmpty()) {
            this.courseTitle = newTitle;
        }
    }

    public String getID() {
        return courseID;
    }

    public void setID(String newID) {
        if (!newID.isEmpty()) {
            this.courseID = newID.toLowerCase();
        }
    }

    public int getGeneratedID() {
        return this.courseIDGenerated;
    }

    public void setGeneratedID(int newID) {
        this.courseIDGenerated = newID;
    }

    public double getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(double newCredits) {
        if (newCredits > 0) {
            this.creditValue = newCredits;
        }
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(int newSectionCount) {
        if (sectionCount >= 0) {
            this.sectionCount = newSectionCount;
        }
    }

    public String getPreferenceString() {
        String rtnVal = "(";
        for (int i = 0; i < 3; i++) {
            rtnVal += preferences[i];
            if (i < 2) {
                rtnVal += "-";
            }
        }
        return rtnVal + ")";
    }

    public int[] getPreferences() {
        return preferences;
    }

    public void setPreferences(int[] prefs) {
        if (prefs.length == 3) {
            preferences[0] = prefs[0];
            preferences[1] = prefs[1];
            preferences[2] = prefs[2];
        }
    }
}
