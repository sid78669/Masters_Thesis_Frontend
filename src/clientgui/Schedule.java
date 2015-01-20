/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.io.Serializable;

/**
 *
 * @author Siddharth
 */
public class Schedule implements Serializable {

    static final long serialVersionUID = 7214654737023782937L;
    public String course;
    public String prof;
    public String time;

    @Override
    public String toString() {
        return course + "," + prof + "," + time;
    }

    public boolean equals(Schedule other) {
        return (this.course.equals(other.course) && this.prof.equals(other.prof) && this.time.equals(other.time));
    }
}
