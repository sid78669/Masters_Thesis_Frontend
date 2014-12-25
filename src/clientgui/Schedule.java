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

        public String course;
        public String prof;
        public String time;

        public String toString() {
            return course + "," + prof + "," + time;
        }
    }
