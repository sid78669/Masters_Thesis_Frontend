/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Siddharth
 */
public class GUIForm extends javax.swing.JFrame {

    private int CurrentStep = 0;
    private HashMap<String, Course> courseList;
    private HashSet<Integer> courseIDs;
    private HashSet<Integer> profIDs;
    private HashSet<Integer> timeslotIDs;
    private HashMap<String, Professor> profList;
    private HashMap<String, TimeSlot> timeslotList;
    private HashMap<String, Schedule> scheduledCoursesList;
    private HashMap<String, Schedule> resultListBySections;
    private HashMap<String, ArrayList<Schedule>> resultListByProfessor;
    private HashMap<String, ArrayList<Schedule>> resultListByCourses;
    private HashMap<Integer, String> timeslotLookup;
    private HashMap<Integer, String> profLookup;
    private HashMap<Integer, String> sectionLookup;
    private LinkedList<ScheduleReplace> undoList;
    protected Vector<String> courseListData;
    protected Vector<String> courseSectionListData;
    protected Vector<String> unscheduledCourses;
    protected Vector<String> profListData;
    protected Vector<String> timeslotListData;
    private Course currentCourse;
    private Professor currentProfessor;
    private TimeSlot currentTimeslot;
    private Schedule currentSchedule;
    private String outputFileName;
    private int generations;
    private int populationSize;
    private int replacementWait;
    private int mutationProbability;
    private TimeSlotVerifier tsVerify;
    private DefaultTableModel dtm;
    private int dtmSelectedRow;
    private JPanel[][] timeblocks;
    private String resultKey;
    private boolean resultCourse;
    private ArrayList<ArrayList<Integer>> incompatibleSectionList;

    /**
     * Creates new form GUIForm
     */
    @SuppressWarnings("unchecked")
    public GUIForm() {
        this.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent evt) {
                        String[] options = {"Yes", "No", "Browse"};
                        String message;
                        if (txtSetupFileName.getText().isEmpty()) {
                            message = "<html>Would you like to save the current setup?</html>";
                        } else {
                            message = "<html>Would you like to save the current setup to<br />" + txtSetupFileName.getText() + "?</html>";
                        }
                        int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Save Setup To File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                        if (choice == 0) {
                            btnSaveSetupActionPerformed(null);
                        } else if (choice == 2) {
                            btnBrowseSetupFileNameActionPerformed(null);
                            btnSaveSetupActionPerformed(null);
                        }

                        System.exit(0);
                    }
                }
        );

        undoList = new LinkedList<>();
        courseIDs = new HashSet<>();
        profIDs = new HashSet<>();
        timeslotIDs = new HashSet<>();
        courseList = new HashMap<>();
        profList = new HashMap<>();
        timeslotList = new HashMap<>();
        scheduledCoursesList = new HashMap<>();
        resultListBySections = new HashMap<>();
        resultListByProfessor = new HashMap<>();
        resultListByCourses = new HashMap<>();
        courseListData = new Vector();
        profListData = new Vector();
        timeslotListData = new Vector();
        courseSectionListData = new Vector();
        unscheduledCourses = new Vector();
        incompatibleSectionList = new ArrayList<>();
        initComponents();
        timeslotLookup = new HashMap<>();
        profLookup = new HashMap<>();
        sectionLookup = new HashMap<>();
        tabbedPanels.setEnabledAt(5, false);
        timeblocks = new JPanel[8][56];

        pnlTimeColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlMondayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlTuesdayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlWednesdayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlThursdayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlFridayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlSaturdayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        pnlSundayColumn.setLayout(new GridLayout(56, 1, 0, 2));
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int day = 0; day < 8; day++) {
            int hour = 8;
            Color today = Color.WHITE;
            for (int time = 0; time < 56; time++) {
                timeblocks[day][time] = new JPanel();
                if (time % 4 == 0 && time > 0) {
                    hour++;
                }
                if (day == 0 && time % 4 == 0) {
                    timeblocks[day][time].setPreferredSize(new Dimension(pnlTimeColumn.getWidth() - 2, 25));
                    timeblocks[day][time].add(new JLabel(ConvertToRegularTime(hour, 0)));
                    timeblocks[day][time].setToolTipText(days[day] + " " + ConvertToRegularTime(hour, 0));
                } else {
                    timeblocks[day][time].setPreferredSize(new Dimension(pnlMondayColumn.getWidth() - 2, 25));
                    timeblocks[day][time].setToolTipText(days[day] + " " + ConvertToRegularTime(hour, 15 * (time % 4)));
                }
                timeblocks[day][time].setBackground(today);
                switch (day) {
                    case 0:
                        pnlTimeColumn.add(timeblocks[day][time]);
                        break;
                    case 1:
                        pnlMondayColumn.add(timeblocks[day][time]);
                        break;
                    case 2:
                        pnlTuesdayColumn.add(timeblocks[day][time]);
                        break;
                    case 3:
                        pnlWednesdayColumn.add(timeblocks[day][time]);
                        break;
                    case 4:
                        pnlThursdayColumn.add(timeblocks[day][time]);
                        break;
                    case 5:
                        pnlFridayColumn.add(timeblocks[day][time]);
                        break;
                    case 6:
                        pnlSaturdayColumn.add(timeblocks[day][time]);
                        break;
                    case 7:
                        pnlSundayColumn.add(timeblocks[day][time]);
                        break;
                }
            }
        }
        txtCourseCreditValue.setInputVerifier(new DoubleValueInputVerifier());
        txtTimeSlotCreditValue.setInputVerifier(new DoubleValueInputVerifier());
        tsVerify = new TimeSlotVerifier();
        txtMondayStart.setInputVerifier(tsVerify);
        txtMondayEnd.setInputVerifier(tsVerify);
        txtTuesdayStart.setInputVerifier(tsVerify);
        txtTuesdayEnd.setInputVerifier(tsVerify);
        txtWednesdayStart.setInputVerifier(tsVerify);
        txtWednesdayEnd.setInputVerifier(tsVerify);
        txtThursdayStart.setInputVerifier(tsVerify);
        txtThursdayEnd.setInputVerifier(tsVerify);
        txtFridayStart.setInputVerifier(tsVerify);
        txtFridayEnd.setInputVerifier(tsVerify);
        txtSaturdayStart.setInputVerifier(tsVerify);
        txtSaturdayEnd.setInputVerifier(tsVerify);
        dtm = new DefaultTableModel(0, 3);
        dtm.setColumnIdentifiers(new String[]{"Course", "Professor", "Timeslot"});
        tableSchedule.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int rawIndex = tableSchedule.getSelectedRow();
                if (rawIndex >= 0 && rawIndex < dtm.getRowCount()) {
                    dtmSelectedRow = tableSchedule.convertRowIndexToModel(rawIndex);
                    String course = dtm.getValueAt(dtmSelectedRow, 0).toString();
                    String prof = dtm.getValueAt(dtmSelectedRow, 1).toString();
                    String time = dtm.getValueAt(dtmSelectedRow, 2).toString();
                    cbScheduleCourse.setSelectedItem(course);
                    cbScheduleProfessor.setSelectedItem(prof);
                    cbScheduleTimeslot.setSelectedItem(time);
                    currentSchedule = scheduledCoursesList.get(course);
                }
            }
        });
        tableSchedule.setAutoCreateRowSorter(true);

        tableSchedule.setModel(dtm);
        if (scheduledCoursesList == null) {
            scheduledCoursesList = new HashMap<>();
        }
        String[] options = {"Yes", "No"};
        int choice = JOptionPane.showOptionDialog(null, "Would you like to load data from a file?", "Load Setup From File", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
        if (choice == 0) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileFilter(new FileNameExtensionFilter("Configuration File", new String[]{"conf"}));

            int returnVal = fc.showOpenDialog(pnlContainer);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try (
                        InputStream inFile = new FileInputStream(fc.getSelectedFile());
                        InputStream buffer = new BufferedInputStream(inFile);
                        ObjectInput input = new ObjectInputStream(buffer);) {
                    try {
                        outputFileName = fc.getSelectedFile().getAbsolutePath();
                        txtSetupFileName.setText(outputFileName);
                        courseList = (HashMap<String, Course>) input.readObject();
                        if (courseList != null && courseList.size() > 0) {
                            for (String key : courseList.keySet()) {
                                if (courseList.get(key).getSectionCount() > 0) {
                                    for (int i = 1; i <= courseList.get(key).getSectionCount(); i++) {
                                        courseSectionListData.addElement(key + "(" + String.valueOf(i) + ")");
                                    }
                                }
                            }
                            Collections.sort(courseSectionListData);
                            unscheduledCourses = new Vector(courseSectionListData);
                            Collections.sort(unscheduledCourses);
                        }

                        try {
                            profList = (HashMap<String, Professor>) input.readObject();
                        } catch (IncompatibleClassChangeError c) {
                            System.err.println(c.getMessage());
                        }
                        timeslotList = (HashMap<String, TimeSlot>) input.readObject();
                        scheduledCoursesList = (HashMap<String, Schedule>) input.readObject();
                        if (scheduledCoursesList == null) {
                            scheduledCoursesList = new HashMap<>();
                        } else {
                            for (String s : scheduledCoursesList.keySet()) {
                                unscheduledCourses.removeElement(s);
                                Schedule t = scheduledCoursesList.get(s);
                                dtm.addRow(new String[]{t.course, t.prof, t.time});
                            }
                        }
                        generations = (int) input.readObject();
                        spinnerGenerations.setValue(generations);

                        populationSize = (int) input.readObject();
                        spinnerPopulationSize.setValue(populationSize);

                        replacementWait = (int) input.readObject();
                        spinnerReplacementWait.setValue(replacementWait);

                        mutationProbability = (int) input.readObject();
                        spinnerMutationProbabilty.setValue(mutationProbability);

                        courseListData = (Vector) input.readObject();

                        if (courseListData == null) {
                            courseListData = new Vector();
                        } else if (courseList.size() != courseListData.size()) {
                            courseListData.clear();
                            for (String s : courseList.keySet()) {
                                courseListData.addElement(s);
                            }
                            Collections.sort(courseListData);
                        }
                        profListData = (Vector) input.readObject();
                        if (profListData == null) {
                            profListData = new Vector();
                        } else if (profListData.size() != profList.size()) {
                            profListData.clear();
                            for (String s : profList.keySet()) {
                                profListData.addElement(s);
                            }
                            Collections.sort(profListData);
                        }
                        timeslotListData = (Vector) input.readObject();
                        if (timeslotListData == null) {
                            timeslotListData = new Vector();
                        } else if (timeslotList.size() != timeslotListData.size()) {
                            timeslotListData.clear();
                            for (String key : timeslotList.keySet()) {
                                timeslotListData.addElement(timeslotList.get(key).toString());
                            }
                            Collections.sort(timeslotListData);
                        }

                        courseIDs = (HashSet<Integer>) input.readObject();
                        profIDs = (HashSet<Integer>) input.readObject();
                        String generatedFile = (String) input.readObject();
                        if (!generatedFile.isEmpty()) {
                            txtGeneratedFileName.setText(generatedFile);
                        }
                    } catch (ClassNotFoundException ex) {
                        System.err.println(ex.getMessage());
                    }

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
                tabbedPanels.setEnabledAt(5, true);
                setTitle("CS:POp - " + fc.getSelectedFile().getName());
            }
        } else {
            setTitle("CS:POp");
            generations = 5000;
            populationSize = 50;
            replacementWait = 100;
            mutationProbability = 2;
            spinnerGenerations.setValue(generations);
            spinnerPopulationSize.setValue(populationSize);
            spinnerReplacementWait.setValue(replacementWait);
            spinnerMutationProbabilty.setValue(mutationProbability);
        }
        txtCourseGeneratedID.setText(String.valueOf(courseList.size()));
        txtProfGeneratedID.setText(String.valueOf(profList.size()));
        txtTSGeneratedID.setText(String.valueOf(timeslotList.size()));
        if (courseListData != null && !courseListData.isEmpty()) {
            listCourses.setListData(courseListData);
            spCourseList.revalidate();
            spCourseList.repaint();
        }
        if (profListData != null && !profListData.isEmpty()) {
            listProfs.setListData(profListData);
            spProfList.revalidate();
            spProfList.repaint();
        }
        if (timeslotListData != null && !timeslotListData.isEmpty()) {
            listTimeslots.setListData(timeslotListData);
            spTimeslotList.revalidate();
            spTimeslotList.repaint();
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableSchedule.getModel());
        sorter.setSortsOnUpdates(true);
        tableSchedule.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        int columnIndexToSort = 0;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        //resultSelectedSection = null;
    }

    private void fixIDs() {
        //Fix all the professor IDs
        updateProfessorIDs();
        //Fix all the timeslot IDs
        updateTimeSlotIDs();
    }

    private void UpdateResultsView(String key, boolean isCourse) {
        resultCourse = isCourse;
        resultKey = key;
        for (int i = 1; i < 8; i++) {
            for (int j = 0; j < 56; j++) {
                timeblocks[i][j].setLayout(new GridLayout(0, 1, 0, 2));
                timeblocks[i][j].removeAll();
                timeblocks[i][j].setBackground(Color.white);
                timeblocks[i][j].revalidate();
                timeblocks[i][j].repaint();
            }
        }
        /*
         1. Get all schedules for the course's sections.
         2. Iterate over schedules 
         3. add their color coded label to the start cell
         4. color their remaining cells with the same color.
         5. update the tooltip on all related cells
         */
        ArrayList<Schedule> schedulesToDisplay;
        if (isCourse) {
            schedulesToDisplay = resultListByCourses.get(key);
        } else {
            schedulesToDisplay = resultListByProfessor.get(key);
        }

        for (Schedule s : schedulesToDisplay) {
            //Pick color for schedule.
            Color thisSchedule = new Color(Color.HSBtoRGB((float) Math.random(), (float) Math.random(), 0.5F + ((float) Math.random()) / 2F));
            ArrayList<Integer[]> toMark = getCellsToColor(timeslotList.get(s.time));
            for (int i = 0; i < toMark.size(); i++) {
                if (timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].getBackground().equals(Color.white)) {
                    timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].setBackground(thisSchedule);
                }
            }
            toMark = getCellsToLabel(timeslotList.get(s.time));

            for (int i = 0; i < toMark.size(); i++) {
                JLabel course = new JLabel(CleanCourse(s.course));
                course.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        //Update the combo box with the details of this course.
                        JLabel source = (JLabel) e.getSource();
                        cbScheduleSection.setSelectedItem(source.getClientProperty("courseID").toString());
                        cbProfessorSelection.setSelectedItem(s.prof);
                        cbTimeSelection.setSelectedItem(s.time);

                    }

                    @Override
                    public void mousePressed(MouseEvent e) {

                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {

                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                    }
                });
                course.setToolTipText("<html><u>" + CleanCourse(s.course) + "</u><br/>" + timeslotList.get(s.time).GetPrettyTime());
                course.putClientProperty("courseID", s.course);
                timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].add(course);
                //}
                if (timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].getComponentCount() > 1) {
                    //Label already exists
                    Component[] allComps = timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].getComponents();
                    timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].setLayout(new GridLayout(allComps.length, 1, 0, 2));
                    int totalHeight = 0;
                    for (Component c : allComps) {
                        totalHeight += c.getPreferredSize().height + 2;
                    }
                    if (timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].getHeight() < totalHeight) {
                        Dimension newDimension = new Dimension();
                        newDimension.width = timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].getWidth();
                        newDimension.height = totalHeight;
                        for (int col = 0; col < 8; col++) {
                            timeblocks[col][toMark.get(i)[1]].setPreferredSize(newDimension);
                        }
                    }
                }

                timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].revalidate();
                timeblocks[toMark.get(i)[0]][toMark.get(i)[1]].repaint();
            }
        }
    }

    private ArrayList<Integer[]> getCellsToColor(TimeSlot t) {
        ArrayList<Integer[]> rtnVal = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int startTime = Integer.parseInt(t.GetTimeOnDay(i).replace("(", "").replace(")", "").split(":")[0]);
            if (startTime == -1) {
                continue;
            }
            int hour = startTime / 100 - 8;
            int minute = startTime % 100;
            if (minute >= 0 && minute < 15) {
                minute = 0;
            } else if (minute >= 15 && minute < 30) {
                minute = 1;
            } else if (minute >= 30 && minute < 45) {
                minute = 2;
            } else {
                minute = 3;
            }
            int startCell = hour * 4 + minute;
            rtnVal.add(new Integer[]{i + 1, hour * 4 + minute});

            int time = Integer.parseInt(t.GetTimeOnDay(i).replace("(", "").replace(")", "").split(":")[1]);
            hour = time / 100 - 8;
            minute = time % 100;
            if (minute >= 0 && minute < 15) {
                minute = 0;
            } else if (minute >= 15 && minute < 30) {
                minute = 1;
            } else if (minute >= 30 && minute < 45) {
                minute = 2;
            } else {
                minute = 3;
            }
            int endCell = hour * 4 + minute;
            if (minute > 0) {
                endCell++;
            }
            while (startCell < endCell) {
                rtnVal.add(new Integer[]{i + 1, startCell++});
            }

        }
        return rtnVal;
    }

    private ArrayList<Integer[]> getCellsToLabel(TimeSlot t) {
        ArrayList<Integer[]> rtnVal = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int time = Integer.parseInt(t.GetTimeOnDay(i).replace("(", "").replace(")", "").split(":")[0]);
            if (time == -1) {
                continue;
            }
            int hour = time / 100 - 8;
            int minute = time % 100;
            if (minute >= 0 && minute < 15) {
                minute = 0;
            } else if (minute >= 15 && minute < 30) {
                minute = 1;
            } else if (minute >= 30 && minute < 45) {
                minute = 2;
            } else {
                minute = 3;
            }
            rtnVal.add(new Integer[]{i + 1, hour * 4 + minute});
        }
        return rtnVal;
    }

    private String CleanCourse(String course) {
        course = course.toUpperCase();
        for (int i = 0; i < course.length(); i++) {
            if (!Character.isAlphabetic(course.charAt(i))) {
                course = course.substring(0, i) + " " + course.substring(i);
                break;
            }
        }
        course = course.replace("(", ".").replace(")", "");
        return course;
    }

    private ArrayList<ArrayList<Integer>> GenerateIncompatibleSectionArray() {
        ArrayList<ArrayList<Integer>> rtnVal = new ArrayList<>();
        //Use courseSectionListData(Vector<String>), it is already a sorted section list.
        for (int i = 0; i < courseSectionListData.size(); i++) {
            rtnVal.add(new ArrayList<>());
        }
        //For each course, get a list of incompatible courses.
        for (String c : courseList.keySet()) {
            Course co = courseList.get(c);
            int sectionsToAddIncompTo = (co.getSectionCount() > 1 ? co.getSectionCount() / 2 : 1);
            ArrayList<Integer> sections = new ArrayList<>();
            for (int z = 0; z < sectionsToAddIncompTo; z++) {
                String coSectionID = co.getID() + "(" + (z + 1) + ")";
                sections.add(courseSectionListData.indexOf(coSectionID));
            }
            for (String incompatible : co.getIncompCourses()) {
                //Get ID of half of it's sections
                Course incomp = courseList.get(incompatible);
                int incompSectionsToAdd = (incomp.getSectionCount() > 1 ? incomp.getSectionCount() / 2 : 1);
                for (int i = 0; i < incompSectionsToAdd; i++) {
                    String incompSectionID = incomp.getID() + "(" + (i + 1) + ")";
                    //Get index of this section.
                    int incompSectionIndex = courseSectionListData.indexOf(incompSectionID);
                    for (int a = 0; a < sections.size(); a++) {
                        //rtnVal.set(i, sections)
                        if (!rtnVal.get(sections.get(a)).contains(incompSectionIndex)) {
                            rtnVal.get(sections.get(a)).add(incompSectionIndex);
                        }
                        if (!rtnVal.get(incompSectionIndex).contains(sections.get(a))) {
                            rtnVal.get(incompSectionIndex).add(sections.get(a));
                        }
                    }
                }
            }
        }
        return rtnVal;
    }

    private ArrayList<Double> GenerateSectionCreditArray() {
        ArrayList<Double> rtnVal = new ArrayList<>();
        for (int i = 0; i < courseSectionListData.size(); i++) {
            String course = courseSectionListData.get(i);
            course = course.substring(0, course.indexOf("("));
            rtnVal.add(i, courseList.get(course).getCreditValue());
        }

        return rtnVal;
    }

    private ArrayList<ArrayList<Integer>> GenerateSectionProfArray(ArrayList<ArrayList<Integer>> profSectionList) {
        ArrayList<ArrayList<Integer>> rtnVal = new ArrayList<>();
        for (int i = 0; i < courseSectionListData.size(); i++) {
            rtnVal.add(new ArrayList<>());
        }
        for (int i = 0; i < profSectionList.size(); i++) {
            ArrayList<Integer> sections = profSectionList.get(i);
            for (Integer sec : sections) {
                if (!rtnVal.get(sec).contains(i)) {
                    rtnVal.get(sec).add(i);
                }
            }
        }

        return rtnVal;
    }

    private ArrayList<ArrayList<Integer>> GenerateProfessorSectionArray() {
        ArrayList<ArrayList<Integer>> rtnVal = new ArrayList<>();
        for (int i = 0; i < profListData.size(); i++) {
            rtnVal.add(new ArrayList<>());
        }
        for (Professor p : profList.values()) {
            Object[] ct = p.getCoursesTaught();
            for (Object course : ct) {
                Course c = courseList.get(course.toString());
                int sectionCount = c.getSectionCount();
                for (int i = 0; i < sectionCount; i++) {
                    String sectionID = c.getID() + "(" + (i + 1) + ")";
                    if (!rtnVal.get(p.getProfID()).contains(courseSectionListData.indexOf(sectionID))) {
                        rtnVal.get(p.getProfID()).add(courseSectionListData.indexOf(sectionID));
                    }
                }
            }
        }

        return rtnVal;
    }

    private HashMap<Double, ArrayList<Integer>> GenerateCreditTimeslotArray() {
        HashMap<Double, ArrayList<Integer>> rtnVal = new HashMap<>();
        for (TimeSlot t : timeslotList.values()) {
            if (!rtnVal.containsKey(t.getCredits())) {
                rtnVal.put(t.getCredits(), new ArrayList<>());
            }
            rtnVal.get(t.getCredits()).add(t.getID());
        }

        return rtnVal;
    }

    private ArrayList<String> GenerateIncompatibleSectionBitString() {
        ArrayList<String> rtnVal = new ArrayList<>();

        for (int i = 0; i < incompatibleSectionList.size(); i++) {
            char[] bitstring = new char[sectionLookup.size()];
            ArrayList<Integer> incomps = incompatibleSectionList.get(i);
            for (int x = 0; x < incompatibleSectionList.size(); x++) {
                if (incomps.contains(x)) {
                    bitstring[x] = '1';
                } else {
                    bitstring[x] = '0';
                }
            }
            rtnVal.add(i, new String(bitstring));
        }

        return rtnVal;
    }

    private ArrayList<ArrayList<ArrayList<Integer>>> GenerateAssociatedProfArray(ArrayList<ArrayList<Integer>> sectionProf, ArrayList<ArrayList<Integer>> profSection) {
        ArrayList<ArrayList<ArrayList<Integer>>> rtnVal = new ArrayList<>();
        ArrayList<Integer> currentProf = new ArrayList<>();
        for (Professor p : profList.values()) {
            for (int section : profSection.get(p.getProfID())) {
                currentProf.addAll(sectionProf.get(section));
            }
            if (currentProf.contains(p.getProfID())) {
                currentProf.remove(currentProf.indexOf(p.getProfID()));
            }

            ArrayList<ArrayList<Integer>> sharedCourses = new ArrayList<>();
            currentProf = new ArrayList<>(new LinkedHashSet<Integer>(currentProf));

            for (Integer ap : currentProf) {
                if (ap == p.getProfID()) {
                    continue;
                }
                ArrayList<Integer> sectionsShared = GetIntersection(profSection.get(p.getProfID()), profSection.get(ap));
                int shareSize = sectionsShared.size();
                sectionsShared.add(0, ap);
                sectionsShared.add(1, shareSize);
                sharedCourses.add(sectionsShared);
            }
            rtnVal.add(p.getProfID(), sharedCourses);
            currentProf.clear();
        }

        return rtnVal;
    }

    private ArrayList<Integer> GetIntersection(ArrayList<Integer> first, ArrayList<Integer> second) {
        Set<Integer> canAdd = new HashSet<>(first);
        ArrayList<Integer> result = new ArrayList<>();
        for (int n : second) {
            if (canAdd.contains(n)) {
                result.add(n);
                canAdd.remove(n);
            }
        }

        return result;
    }

    private int FindRowInSchedule(String elementID) {
        int rowID = -1;
        int col = tableSchedule.convertColumnIndexToView(0);
        for (int row = tableSchedule.getRowCount(); --row >= 0 && rowID == -1;) {
            if (elementID.equals(tableSchedule.getValueAt(row, col))) {
                rowID = tableSchedule.convertRowIndexToModel(row);
            }
        }

        return rowID;
    }

    private void RemoveSection(String elementID) {
        for (int s : sectionLookup.keySet()) {
            if (sectionLookup.get(s).equals(elementID)) {
                sectionLookup.remove(s);
                return;
            }
        }
    }

    private static class ScheduleReplace {

        Schedule original;
        Schedule updated;

        public ScheduleReplace() {
        }
    }

    public class DoubleValueInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            String text = ((JTextField) input).getText();
            if (text.isEmpty()) {
                return true;
            }
            try {
                Double value = new Double(text);
                return (value > 0 && value < 20);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public class TimeSlotVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            String text = ((JTextField) input).getText();
            if (text.isEmpty()) {
                return true;
            }

            text = text.replaceAll(":", "");
            try {
                Integer value = new Integer(text);
                int minute = value % 100;
                int hour = value / 100;
                return ((minute >= 0 && minute <= 59) && (hour >= 0 && hour <= 23));
            } catch (NumberFormatException n) {
                return false;
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewByGroup = new javax.swing.ButtonGroup();
        pnlContainer = new javax.swing.JPanel();
        tabbedPanels = new javax.swing.JTabbedPane();
        pnlCourses = new javax.swing.JPanel();
        courseData = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        txtCourseTitle = new javax.swing.JTextField();
        lblCourseID = new javax.swing.JLabel();
        txtCourseID = new javax.swing.JTextField();
        lblCourseCreditValue = new javax.swing.JLabel();
        txtCourseCreditValue = new javax.swing.JTextField();
        btnSaveCourse = new javax.swing.JButton();
        pnlIncompatibleCourses = new javax.swing.JPanel();
        spCourseIncomp = new javax.swing.JScrollPane();
        listIncompCourses = new JList(new Vector());
        dropIncompCourses = new javax.swing.JComboBox<String>();
        btnModifyIncomp = new javax.swing.JButton();
        lblCourseSectionCount = new javax.swing.JLabel();
        spinnerSections = new javax.swing.JSpinner();
        btnDeleteCourse = new javax.swing.JButton();
        lblCourseGeneratedID = new javax.swing.JLabel();
        txtCourseGeneratedID = new javax.swing.JTextField();
        pnlCourseTimePreference = new javax.swing.JPanel();
        lblCourseTimeHighest = new javax.swing.JLabel();
        lblCourseTimeNormal = new javax.swing.JLabel();
        lblCourseTimeLeast = new javax.swing.JLabel();
        lblCourseTimeNA = new javax.swing.JLabel();
        lblCourseTimeMorning = new javax.swing.JLabel();
        lblCourseTimeAfternoon = new javax.swing.JLabel();
        lblCourseTimeEvening = new javax.swing.JLabel();
        cbCourseHighestMorning = new javax.swing.JCheckBox();
        cbCourseHighestAfternoon = new javax.swing.JCheckBox();
        cbCourseHighestEvening = new javax.swing.JCheckBox();
        cbCourseNormalEvening = new javax.swing.JCheckBox();
        cbCourseNormalAfternoon = new javax.swing.JCheckBox();
        cbCourseNormalMorning = new javax.swing.JCheckBox();
        cbCourseLeastEvening = new javax.swing.JCheckBox();
        cbCourseLeastAfternoon = new javax.swing.JCheckBox();
        cbCourseLeastMorning = new javax.swing.JCheckBox();
        cbCourseNA_Morning = new javax.swing.JCheckBox();
        cbCourseNA_Afternoon = new javax.swing.JCheckBox();
        cbCourseNA_Evening = new javax.swing.JCheckBox();
        chkCourseNoPreference = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        spCourseList = new javax.swing.JScrollPane();
        listCourses = new JList(courseListData);
        btnNewCourse = new javax.swing.JButton();
        pnlProfessor = new javax.swing.JPanel();
        spProfList = new javax.swing.JScrollPane();
        listProfs = new javax.swing.JList<String>();
        pnlProfData = new javax.swing.JPanel();
        lblGeneratedID = new javax.swing.JLabel();
        lblProfName = new javax.swing.JLabel();
        txtProfGeneratedID = new javax.swing.JTextField();
        txtProfName = new javax.swing.JTextField();
        lblProfCredits = new javax.swing.JLabel();
        spinnerCreditsAssigned = new javax.swing.JSpinner();
        pnlProfCourseTaught = new javax.swing.JPanel();
        btnAddCourseTaught = new javax.swing.JButton();
        cbProfCourseTaught = new javax.swing.JComboBox<String>();
        spCourseTaughtList = new javax.swing.JScrollPane();
        listCourseTaught = new javax.swing.JList<Object>();
        btnDeleteProf = new javax.swing.JButton();
        btnSaveProf = new javax.swing.JButton();
        chkProfNoPreference = new javax.swing.JCheckBox();
        pnlProfTimePreference = new javax.swing.JPanel();
        lblProfTimeHighest = new javax.swing.JLabel();
        lblProfTimeNormal = new javax.swing.JLabel();
        lblProfTimeLeast = new javax.swing.JLabel();
        lblProfTimeNA = new javax.swing.JLabel();
        lblProfTimeMorning = new javax.swing.JLabel();
        lblProfTimeAfternoon = new javax.swing.JLabel();
        lblProfTimeEvening = new javax.swing.JLabel();
        cbProfHighestMorning = new javax.swing.JCheckBox();
        cbProfHighestAfternoon = new javax.swing.JCheckBox();
        cbProfHighestEvening = new javax.swing.JCheckBox();
        cbProfNormalEvening = new javax.swing.JCheckBox();
        cbProfNormalAfternoon = new javax.swing.JCheckBox();
        cbProfNormalMorning = new javax.swing.JCheckBox();
        cbProfLeastEvening = new javax.swing.JCheckBox();
        cbProfLeastAfternoon = new javax.swing.JCheckBox();
        cbProfLeastMorning = new javax.swing.JCheckBox();
        cbProfNA_Morning = new javax.swing.JCheckBox();
        cbProfNA_Afternoon = new javax.swing.JCheckBox();
        cbProfNA_Evening = new javax.swing.JCheckBox();
        btnNewProf = new javax.swing.JButton();
        pnlTimeSlots = new javax.swing.JPanel();
        btnNewTimeslot = new javax.swing.JButton();
        spTimeslotList = new javax.swing.JScrollPane();
        listTimeslots = new javax.swing.JList<String>();
        lblGeneratedTSID = new javax.swing.JLabel();
        txtTSGeneratedID = new javax.swing.JTextField();
        lblTimeSlotCreditValue = new javax.swing.JLabel();
        txtTimeSlotCreditValue = new javax.swing.JTextField();
        pnlScheduleDays = new javax.swing.JPanel();
        pnlMonday = new javax.swing.JPanel();
        lblMondayStart = new javax.swing.JLabel();
        txtMondayStart = new javax.swing.JTextField();
        lblMondayEnd = new javax.swing.JLabel();
        txtMondayEnd = new javax.swing.JTextField();
        pnlTuesday = new javax.swing.JPanel();
        lblTuesdayStart = new javax.swing.JLabel();
        txtTuesdayStart = new javax.swing.JTextField();
        lblTuesdayEnd = new javax.swing.JLabel();
        txtTuesdayEnd = new javax.swing.JTextField();
        pnlWednesday = new javax.swing.JPanel();
        lblWednesdayStart = new javax.swing.JLabel();
        txtWednesdayStart = new javax.swing.JTextField();
        lblWednesdayEnd = new javax.swing.JLabel();
        txtWednesdayEnd = new javax.swing.JTextField();
        pnlThursday = new javax.swing.JPanel();
        lbThursdayStart = new javax.swing.JLabel();
        txtThursdayStart = new javax.swing.JTextField();
        lblThursdayEnd = new javax.swing.JLabel();
        txtThursdayEnd = new javax.swing.JTextField();
        pnlFriday = new javax.swing.JPanel();
        lbFridayStart = new javax.swing.JLabel();
        txtFridayStart = new javax.swing.JTextField();
        lblFridayEnd = new javax.swing.JLabel();
        txtFridayEnd = new javax.swing.JTextField();
        pnlSaturday = new javax.swing.JPanel();
        lbSaturdayStart = new javax.swing.JLabel();
        txtSaturdayStart = new javax.swing.JTextField();
        lblSaturdayEnd = new javax.swing.JLabel();
        txtSaturdayEnd = new javax.swing.JTextField();
        btnSaveTimeSlot = new javax.swing.JButton();
        btnDeleteTimeSlot = new javax.swing.JButton();
        pnlInitSchedule = new javax.swing.JPanel();
        spTableSchedule = new javax.swing.JScrollPane();
        tableSchedule = new javax.swing.JTable();
        lblScheduleCourse = new javax.swing.JLabel();
        cbScheduleCourse = new javax.swing.JComboBox<String>();
        lblScheduleProfessor = new javax.swing.JLabel();
        cbScheduleProfessor = new javax.swing.JComboBox<String>();
        lblScheduleTimeslot = new javax.swing.JLabel();
        cbScheduleTimeslot = new javax.swing.JComboBox<String>();
        btnSaveSchedule = new javax.swing.JButton();
        btnDeleteSchedule = new javax.swing.JButton();
        spUnscheduledCourses = new javax.swing.JScrollPane();
        listUnscheduledCourses = new javax.swing.JList<String>();
        lblUnscheduledCourses = new javax.swing.JLabel();
        lblScheduledCourses = new javax.swing.JLabel();
        btnValidateInitialSchedule = new javax.swing.JButton();
        pnlConfiguration = new javax.swing.JPanel();
        pnlAdvancedConfig = new javax.swing.JPanel();
        lblGenerations = new javax.swing.JLabel();
        lblPopulationSize = new javax.swing.JLabel();
        lblReplacementWait = new javax.swing.JLabel();
        lblMutationProbabilty = new javax.swing.JLabel();
        spinnerGenerations = new javax.swing.JSpinner();
        spinnerPopulationSize = new javax.swing.JSpinner();
        spinnerReplacementWait = new javax.swing.JSpinner();
        spinnerMutationProbabilty = new javax.swing.JSpinner();
        lbSetupFileName = new javax.swing.JLabel();
        txtSetupFileName = new javax.swing.JTextField();
        btnBrowseSetupFileName = new javax.swing.JButton();
        btnGenerateInputFile = new javax.swing.JButton();
        btnSaveSetup = new javax.swing.JButton();
        lblGeneratedFileName = new javax.swing.JLabel();
        btnBrowseGeneratedFileName = new javax.swing.JButton();
        txtGeneratedFileName = new javax.swing.JTextField();
        pnlResults = new javax.swing.JPanel();
        pnlResultContainer = new javax.swing.JPanel();
        lblResultFile = new javax.swing.JLabel();
        btnBrowseResult = new javax.swing.JButton();
        txtResultPath = new javax.swing.JTextField();
        pnlViewByControls = new javax.swing.JPanel();
        rdoCourse = new javax.swing.JRadioButton();
        rdoProfessor = new javax.swing.JRadioButton();
        spViewBySelection = new javax.swing.JScrollPane();
        listViewBySelection = new javax.swing.JList<String>();
        btnResultChangeUndo = new javax.swing.JButton();
        btnResultChangeUpdate = new javax.swing.JButton();
        lblChangeProfessorTo = new javax.swing.JLabel();
        lblChangeTimeTo = new javax.swing.JLabel();
        cbProfessorSelection = new javax.swing.JComboBox<String>();
        cbTimeSelection = new javax.swing.JComboBox<String>();
        lblScheduleSectionLabel = new javax.swing.JLabel();
        cbScheduleSection = new javax.swing.JComboBox<String>();
        btnStatistics = new javax.swing.JButton();
        btnGenerateResult = new javax.swing.JButton();
        scrollSchedule = new javax.swing.JScrollPane();
        pnlSchedule = new javax.swing.JPanel();
        pnlTimeColumn = new javax.swing.JPanel();
        pnlMondayColumn = new javax.swing.JPanel();
        pnlTuesdayColumn = new javax.swing.JPanel();
        pnlWednesdayColumn = new javax.swing.JPanel();
        pnlThursdayColumn = new javax.swing.JPanel();
        pnlFridayColumn = new javax.swing.JPanel();
        pnlSaturdayColumn = new javax.swing.JPanel();
        pnlSundayColumn = new javax.swing.JPanel();
        pnlResultScheduleLabels = new javax.swing.JPanel();
        pnlResultScheduleLabel_Time = new javax.swing.JPanel();
        lblTimeColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Sunday = new javax.swing.JPanel();
        lblSundayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Saturday = new javax.swing.JPanel();
        lblSaturdayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Friday = new javax.swing.JPanel();
        lblFridayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Thursday = new javax.swing.JPanel();
        lblThursdayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Wednesday = new javax.swing.JPanel();
        lblWednesdayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Tuesday = new javax.swing.JPanel();
        lblTuesdayColumn = new javax.swing.JLabel();
        pnlResultScheduleLabel_Monday = new javax.swing.JPanel();
        lblMondayColumn = new javax.swing.JLabel();
        txtResultStatus = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        miOpenConfig = new javax.swing.JMenuItem();
        miSaveConfig = new javax.swing.JMenuItem();
        menuAbout = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 800));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        tabbedPanels.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPanelsStateChanged(evt);
            }
        });

        courseData.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblTitle.setLabelFor(txtCourseTitle);
        lblTitle.setText("Course Title");

        txtCourseTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCourseTitleActionPerformed(evt);
            }
        });

        lblCourseID.setLabelFor(txtCourseID);
        lblCourseID.setText("Course ID");

        lblCourseCreditValue.setLabelFor(txtCourseCreditValue);
        lblCourseCreditValue.setText("Credit Value");

        txtCourseCreditValue.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCourseCreditValueKeyTyped(evt);
            }
        });

        btnSaveCourse.setBackground(new java.awt.Color(204, 204, 255));
        btnSaveCourse.setText("Save Course");
        btnSaveCourse.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSaveCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCourseActionPerformed(evt);
            }
        });

        pnlIncompatibleCourses.setBorder(javax.swing.BorderFactory.createTitledBorder("Incompatible Courses"));

        listIncompCourses.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listIncompCourses.setEnabled(false);
        spCourseIncomp.setViewportView(listIncompCourses);

        dropIncompCourses.setEnabled(false);
        dropIncompCourses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropIncompCoursesActionPerformed(evt);
            }
        });

        btnModifyIncomp.setText("Add");
        btnModifyIncomp.setEnabled(false);
        btnModifyIncomp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModifyIncompActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlIncompatibleCoursesLayout = new javax.swing.GroupLayout(pnlIncompatibleCourses);
        pnlIncompatibleCourses.setLayout(pnlIncompatibleCoursesLayout);
        pnlIncompatibleCoursesLayout.setHorizontalGroup(
            pnlIncompatibleCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlIncompatibleCoursesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlIncompatibleCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spCourseIncomp)
                    .addGroup(pnlIncompatibleCoursesLayout.createSequentialGroup()
                        .addComponent(dropIncompCourses, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnModifyIncomp, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlIncompatibleCoursesLayout.setVerticalGroup(
            pnlIncompatibleCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlIncompatibleCoursesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlIncompatibleCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dropIncompCourses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModifyIncomp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spCourseIncomp, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblCourseSectionCount.setLabelFor(spinnerSections);
        lblCourseSectionCount.setText("Sections");

        btnDeleteCourse.setBackground(new java.awt.Color(204, 204, 255));
        btnDeleteCourse.setText("Delete Course");
        btnDeleteCourse.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDeleteCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCourseActionPerformed(evt);
            }
        });

        lblCourseGeneratedID.setLabelFor(txtCourseGeneratedID);
        lblCourseGeneratedID.setText("Generated ID");

        txtCourseGeneratedID.setEditable(false);

        pnlCourseTimePreference.setBorder(javax.swing.BorderFactory.createTitledBorder("Time Preference"));

        lblCourseTimeHighest.setText("Highest");

        lblCourseTimeNormal.setText("Normal");

        lblCourseTimeLeast.setText("Least");

        lblCourseTimeNA.setText("<html><center>Not<br/>Available");

        lblCourseTimeMorning.setText("Morning");

        lblCourseTimeAfternoon.setText("Afternoon");

        lblCourseTimeEvening.setText("Evening");

        cbCourseHighestMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseHighestMorningActionPerformed(evt);
            }
        });

        cbCourseHighestAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseHighestAfternoonActionPerformed(evt);
            }
        });

        cbCourseHighestEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseHighestEveningActionPerformed(evt);
            }
        });

        cbCourseNormalEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNormalEveningActionPerformed(evt);
            }
        });

        cbCourseNormalAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNormalAfternoonActionPerformed(evt);
            }
        });

        cbCourseNormalMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNormalMorningActionPerformed(evt);
            }
        });

        cbCourseLeastEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseLeastEveningActionPerformed(evt);
            }
        });

        cbCourseLeastAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseLeastAfternoonActionPerformed(evt);
            }
        });

        cbCourseLeastMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseLeastMorningActionPerformed(evt);
            }
        });

        cbCourseNA_Morning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNA_MorningActionPerformed(evt);
            }
        });

        cbCourseNA_Afternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNA_AfternoonActionPerformed(evt);
            }
        });

        cbCourseNA_Evening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCourseNA_EveningActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCourseTimePreferenceLayout = new javax.swing.GroupLayout(pnlCourseTimePreference);
        pnlCourseTimePreference.setLayout(pnlCourseTimePreferenceLayout);
        pnlCourseTimePreferenceLayout.setHorizontalGroup(
            pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCourseTimePreferenceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblCourseTimeEvening)
                    .addComponent(lblCourseTimeMorning)
                    .addComponent(lblCourseTimeAfternoon))
                .addGap(18, 18, 18)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbCourseHighestEvening)
                    .addComponent(cbCourseHighestAfternoon)
                    .addComponent(cbCourseHighestMorning)
                    .addComponent(lblCourseTimeHighest))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeNormal)
                    .addComponent(cbCourseNormalMorning)
                    .addComponent(cbCourseNormalAfternoon)
                    .addComponent(cbCourseNormalEvening))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeLeast)
                    .addComponent(cbCourseLeastMorning)
                    .addComponent(cbCourseLeastAfternoon)
                    .addComponent(cbCourseLeastEvening))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeNA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbCourseNA_Morning)
                    .addComponent(cbCourseNA_Afternoon)
                    .addComponent(cbCourseNA_Evening))
                .addContainerGap(386, Short.MAX_VALUE))
        );
        pnlCourseTimePreferenceLayout.setVerticalGroup(
            pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCourseTimePreferenceLayout.createSequentialGroup()
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeHighest)
                    .addComponent(lblCourseTimeNormal)
                    .addComponent(lblCourseTimeLeast)
                    .addComponent(lblCourseTimeNA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbCourseNA_Morning)
                    .addComponent(cbCourseLeastMorning)
                    .addComponent(cbCourseNormalMorning)
                    .addComponent(cbCourseHighestMorning)
                    .addComponent(lblCourseTimeMorning))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeAfternoon)
                    .addComponent(cbCourseHighestAfternoon)
                    .addComponent(cbCourseNormalAfternoon)
                    .addComponent(cbCourseLeastAfternoon)
                    .addComponent(cbCourseNA_Afternoon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlCourseTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCourseTimeEvening)
                    .addComponent(cbCourseHighestEvening)
                    .addComponent(cbCourseNormalEvening)
                    .addComponent(cbCourseLeastEvening)
                    .addComponent(cbCourseNA_Evening)))
        );

        chkCourseNoPreference.setText("No Time Preference");
        chkCourseNoPreference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCourseNoPreferenceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout courseDataLayout = new javax.swing.GroupLayout(courseData);
        courseData.setLayout(courseDataLayout);
        courseDataLayout.setHorizontalGroup(
            courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(courseDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(courseDataLayout.createSequentialGroup()
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCourseCreditValue)
                            .addComponent(lblCourseSectionCount, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerSections, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtCourseCreditValue)))
                    .addComponent(btnDeleteCourse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSaveCourse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(courseDataLayout.createSequentialGroup()
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTitle)
                            .addComponent(lblCourseID)
                            .addComponent(lblCourseGeneratedID))
                        .addGap(18, 18, 18)
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCourseGeneratedID)
                            .addComponent(txtCourseTitle)
                            .addComponent(txtCourseID)))
                    .addGroup(courseDataLayout.createSequentialGroup()
                        .addComponent(chkCourseNoPreference)
                        .addGap(18, 18, 18)
                        .addComponent(pnlCourseTimePreference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(courseDataLayout.createSequentialGroup()
                        .addComponent(pnlIncompatibleCourses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(289, 289, 289)))
                .addContainerGap())
        );
        courseDataLayout.setVerticalGroup(
            courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(courseDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourseGeneratedID)
                    .addComponent(txtCourseGeneratedID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(txtCourseTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourseID)
                    .addComponent(txtCourseID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourseCreditValue)
                    .addComponent(txtCourseCreditValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourseSectionCount)
                    .addComponent(spinnerSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkCourseNoPreference)
                    .addComponent(pnlCourseTimePreference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlIncompatibleCourses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(courseDataLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        listCourses.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listCourses.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listCoursesValueChanged(evt);
            }
        });
        spCourseList.setViewportView(listCourses);

        btnNewCourse.setText("New Course");
        btnNewCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewCourseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlCoursesLayout = new javax.swing.GroupLayout(pnlCourses);
        pnlCourses.setLayout(pnlCoursesLayout);
        pnlCoursesLayout.setHorizontalGroup(
            pnlCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCoursesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spCourseList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnNewCourse, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(courseData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlCoursesLayout.setVerticalGroup(
            pnlCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCoursesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCoursesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(courseData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlCoursesLayout.createSequentialGroup()
                        .addComponent(btnNewCourse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spCourseList)))
                .addContainerGap())
        );

        tabbedPanels.addTab("Courses", pnlCourses);

        listProfs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listProfs.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listProfsValueChanged(evt);
            }
        });
        spProfList.setViewportView(listProfs);

        pnlProfData.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblGeneratedID.setLabelFor(txtProfGeneratedID);
        lblGeneratedID.setText("Generated ID");

        lblProfName.setLabelFor(txtProfName);
        lblProfName.setText("Name");

        txtProfGeneratedID.setEditable(false);

        lblProfCredits.setLabelFor(spinnerCreditsAssigned);
        lblProfCredits.setText("Credit Assigned");

        spinnerCreditsAssigned.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.0d)));

        pnlProfCourseTaught.setBorder(javax.swing.BorderFactory.createTitledBorder("Courses Taught"));

        btnAddCourseTaught.setText("Add");
        btnAddCourseTaught.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCourseTaughtActionPerformed(evt);
            }
        });

        cbProfCourseTaught.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfCourseTaughtActionPerformed(evt);
            }
        });

        listCourseTaught.setEnabled(false);
        spCourseTaughtList.setViewportView(listCourseTaught);

        javax.swing.GroupLayout pnlProfCourseTaughtLayout = new javax.swing.GroupLayout(pnlProfCourseTaught);
        pnlProfCourseTaught.setLayout(pnlProfCourseTaughtLayout);
        pnlProfCourseTaughtLayout.setHorizontalGroup(
            pnlProfCourseTaughtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfCourseTaughtLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfCourseTaughtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spCourseTaughtList, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                    .addGroup(pnlProfCourseTaughtLayout.createSequentialGroup()
                        .addComponent(cbProfCourseTaught, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAddCourseTaught)))
                .addContainerGap())
        );
        pnlProfCourseTaughtLayout.setVerticalGroup(
            pnlProfCourseTaughtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfCourseTaughtLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfCourseTaughtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCourseTaught)
                    .addComponent(cbProfCourseTaught, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(spCourseTaughtList, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnDeleteProf.setText("Delete Professor");
        btnDeleteProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteProfActionPerformed(evt);
            }
        });

        btnSaveProf.setText("Save Professor");
        btnSaveProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveProfActionPerformed(evt);
            }
        });

        chkProfNoPreference.setText("No Time Preference");
        chkProfNoPreference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkProfNoPreferenceActionPerformed(evt);
            }
        });

        pnlProfTimePreference.setBorder(javax.swing.BorderFactory.createTitledBorder("Time Preference"));

        lblProfTimeHighest.setText("Highest");

        lblProfTimeNormal.setText("Normal");

        lblProfTimeLeast.setText("Least");

        lblProfTimeNA.setText("<html><center>Not<br/>Available");

        lblProfTimeMorning.setText("Morning");

        lblProfTimeAfternoon.setText("Afternoon");

        lblProfTimeEvening.setText("Evening");

        cbProfHighestMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfHighestMorningActionPerformed(evt);
            }
        });

        cbProfHighestAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfHighestAfternoonActionPerformed(evt);
            }
        });

        cbProfHighestEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfHighestEveningActionPerformed(evt);
            }
        });

        cbProfNormalEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNormalEveningActionPerformed(evt);
            }
        });

        cbProfNormalAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNormalAfternoonActionPerformed(evt);
            }
        });

        cbProfNormalMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNormalMorningActionPerformed(evt);
            }
        });

        cbProfLeastEvening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfLeastEveningActionPerformed(evt);
            }
        });

        cbProfLeastAfternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfLeastAfternoonActionPerformed(evt);
            }
        });

        cbProfLeastMorning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfLeastMorningActionPerformed(evt);
            }
        });

        cbProfNA_Morning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNA_MorningActionPerformed(evt);
            }
        });

        cbProfNA_Afternoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNA_AfternoonActionPerformed(evt);
            }
        });

        cbProfNA_Evening.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbProfNA_EveningActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProfTimePreferenceLayout = new javax.swing.GroupLayout(pnlProfTimePreference);
        pnlProfTimePreference.setLayout(pnlProfTimePreferenceLayout);
        pnlProfTimePreferenceLayout.setHorizontalGroup(
            pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfTimePreferenceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblProfTimeEvening)
                    .addComponent(lblProfTimeMorning)
                    .addComponent(lblProfTimeAfternoon))
                .addGap(18, 18, 18)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbProfHighestEvening)
                    .addComponent(cbProfHighestAfternoon)
                    .addComponent(cbProfHighestMorning)
                    .addComponent(lblProfTimeHighest))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeNormal)
                    .addComponent(cbProfNormalMorning)
                    .addComponent(cbProfNormalAfternoon)
                    .addComponent(cbProfNormalEvening))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeLeast)
                    .addComponent(cbProfLeastMorning)
                    .addComponent(cbProfLeastAfternoon)
                    .addComponent(cbProfLeastEvening))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeNA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbProfNA_Morning)
                    .addComponent(cbProfNA_Afternoon)
                    .addComponent(cbProfNA_Evening))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlProfTimePreferenceLayout.setVerticalGroup(
            pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfTimePreferenceLayout.createSequentialGroup()
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeHighest)
                    .addComponent(lblProfTimeNormal)
                    .addComponent(lblProfTimeLeast)
                    .addComponent(lblProfTimeNA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbProfNA_Morning)
                    .addComponent(cbProfLeastMorning)
                    .addComponent(cbProfNormalMorning)
                    .addComponent(cbProfHighestMorning)
                    .addComponent(lblProfTimeMorning))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeAfternoon)
                    .addComponent(cbProfHighestAfternoon)
                    .addComponent(cbProfNormalAfternoon)
                    .addComponent(cbProfLeastAfternoon)
                    .addComponent(cbProfNA_Afternoon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProfTimePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblProfTimeEvening)
                    .addComponent(cbProfHighestEvening)
                    .addComponent(cbProfNormalEvening)
                    .addComponent(cbProfLeastEvening)
                    .addComponent(cbProfNA_Evening)))
        );

        javax.swing.GroupLayout pnlProfDataLayout = new javax.swing.GroupLayout(pnlProfData);
        pnlProfData.setLayout(pnlProfDataLayout);
        pnlProfDataLayout.setHorizontalGroup(
            pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDeleteProf, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProfCourseTaught, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlProfDataLayout.createSequentialGroup()
                        .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblGeneratedID)
                            .addComponent(lblProfName)
                            .addComponent(lblProfCredits))
                        .addGap(18, 18, 18)
                        .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerCreditsAssigned)
                            .addComponent(txtProfGeneratedID)
                            .addComponent(txtProfName)))
                    .addComponent(btnSaveProf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlProfDataLayout.createSequentialGroup()
                        .addComponent(chkProfNoPreference)
                        .addGap(18, 18, 18)
                        .addComponent(pnlProfTimePreference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlProfDataLayout.setVerticalGroup(
            pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGeneratedID)
                    .addComponent(txtProfGeneratedID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfName)
                    .addComponent(txtProfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfCredits)
                    .addComponent(spinnerCreditsAssigned, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkProfNoPreference)
                    .addComponent(pnlProfTimePreference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProfCourseTaught, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveProf, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDeleteProf, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnNewProf.setText("New Professor");
        btnNewProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewProfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProfessorLayout = new javax.swing.GroupLayout(pnlProfessor);
        pnlProfessor.setLayout(pnlProfessorLayout);
        pnlProfessorLayout.setHorizontalGroup(
            pnlProfessorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfessorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfessorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spProfList, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btnNewProf, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProfData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlProfessorLayout.setVerticalGroup(
            pnlProfessorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProfessorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfessorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlProfData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlProfessorLayout.createSequentialGroup()
                        .addComponent(btnNewProf)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spProfList)))
                .addContainerGap())
        );

        tabbedPanels.addTab("Professors", pnlProfessor);

        btnNewTimeslot.setText("New Timeslot");
        btnNewTimeslot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewTimeslotActionPerformed(evt);
            }
        });

        listTimeslots.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listTimeslots.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTimeslotsValueChanged(evt);
            }
        });
        spTimeslotList.setViewportView(listTimeslots);

        lblGeneratedTSID.setLabelFor(txtTSGeneratedID);
        lblGeneratedTSID.setText("Generated ID");

        txtTSGeneratedID.setEditable(false);
        txtTSGeneratedID.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        lblTimeSlotCreditValue.setLabelFor(txtTimeSlotCreditValue);
        lblTimeSlotCreditValue.setText("Credit Value");

        pnlScheduleDays.setBorder(javax.swing.BorderFactory.createTitledBorder("Weekly Schedule"));

        pnlMonday.setBorder(javax.swing.BorderFactory.createTitledBorder("Monday"));

        lblMondayStart.setLabelFor(txtMondayStart);
        lblMondayStart.setText("Start Time");

        lblMondayEnd.setLabelFor(txtMondayEnd);
        lblMondayEnd.setText("End Time");

        javax.swing.GroupLayout pnlMondayLayout = new javax.swing.GroupLayout(pnlMonday);
        pnlMonday.setLayout(pnlMondayLayout);
        pnlMondayLayout.setHorizontalGroup(
            pnlMondayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMondayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMondayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtMondayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblMondayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtMondayEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMondayLayout.setVerticalGroup(
            pnlMondayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMondayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblMondayStart)
                .addComponent(txtMondayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblMondayEnd)
                .addComponent(txtMondayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlTuesday.setBorder(javax.swing.BorderFactory.createTitledBorder("Tuesday"));

        lblTuesdayStart.setLabelFor(txtTuesdayStart);
        lblTuesdayStart.setText("Start Time");

        lblTuesdayEnd.setLabelFor(txtTuesdayEnd);
        lblTuesdayEnd.setText("End Time");

        javax.swing.GroupLayout pnlTuesdayLayout = new javax.swing.GroupLayout(pnlTuesday);
        pnlTuesday.setLayout(pnlTuesdayLayout);
        pnlTuesdayLayout.setHorizontalGroup(
            pnlTuesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTuesdayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTuesdayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTuesdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTuesdayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTuesdayEnd)
                .addContainerGap())
        );
        pnlTuesdayLayout.setVerticalGroup(
            pnlTuesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTuesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblTuesdayStart)
                .addComponent(txtTuesdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblTuesdayEnd)
                .addComponent(txtTuesdayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlWednesday.setBorder(javax.swing.BorderFactory.createTitledBorder("Wednesday"));

        lblWednesdayStart.setLabelFor(txtWednesdayStart);
        lblWednesdayStart.setText("Start Time");

        lblWednesdayEnd.setLabelFor(txtWednesdayEnd);
        lblWednesdayEnd.setText("End Time");

        javax.swing.GroupLayout pnlWednesdayLayout = new javax.swing.GroupLayout(pnlWednesday);
        pnlWednesday.setLayout(pnlWednesdayLayout);
        pnlWednesdayLayout.setHorizontalGroup(
            pnlWednesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWednesdayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWednesdayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtWednesdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblWednesdayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtWednesdayEnd)
                .addContainerGap())
        );
        pnlWednesdayLayout.setVerticalGroup(
            pnlWednesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWednesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblWednesdayStart)
                .addComponent(txtWednesdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblWednesdayEnd)
                .addComponent(txtWednesdayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlThursday.setBorder(javax.swing.BorderFactory.createTitledBorder("Thursday"));

        lbThursdayStart.setLabelFor(txtThursdayStart);
        lbThursdayStart.setText("Start Time");

        lblThursdayEnd.setLabelFor(txtThursdayEnd);
        lblThursdayEnd.setText("End Time");

        javax.swing.GroupLayout pnlThursdayLayout = new javax.swing.GroupLayout(pnlThursday);
        pnlThursday.setLayout(pnlThursdayLayout);
        pnlThursdayLayout.setHorizontalGroup(
            pnlThursdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThursdayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbThursdayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtThursdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblThursdayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtThursdayEnd)
                .addContainerGap())
        );
        pnlThursdayLayout.setVerticalGroup(
            pnlThursdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThursdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbThursdayStart)
                .addComponent(txtThursdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblThursdayEnd)
                .addComponent(txtThursdayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlFriday.setBorder(javax.swing.BorderFactory.createTitledBorder("Friday"));

        lbFridayStart.setLabelFor(txtFridayStart);
        lbFridayStart.setText("Start Time");

        lblFridayEnd.setLabelFor(txtFridayEnd);
        lblFridayEnd.setText("End Time");

        javax.swing.GroupLayout pnlFridayLayout = new javax.swing.GroupLayout(pnlFriday);
        pnlFriday.setLayout(pnlFridayLayout);
        pnlFridayLayout.setHorizontalGroup(
            pnlFridayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFridayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbFridayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtFridayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFridayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtFridayEnd)
                .addContainerGap())
        );
        pnlFridayLayout.setVerticalGroup(
            pnlFridayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFridayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbFridayStart)
                .addComponent(txtFridayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblFridayEnd)
                .addComponent(txtFridayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlSaturday.setBorder(javax.swing.BorderFactory.createTitledBorder("Saturday"));

        lbSaturdayStart.setLabelFor(txtSaturdayStart);
        lbSaturdayStart.setText("Start Time");

        lblSaturdayEnd.setLabelFor(txtSaturdayEnd);
        lblSaturdayEnd.setText("End Time");

        javax.swing.GroupLayout pnlSaturdayLayout = new javax.swing.GroupLayout(pnlSaturday);
        pnlSaturday.setLayout(pnlSaturdayLayout);
        pnlSaturdayLayout.setHorizontalGroup(
            pnlSaturdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaturdayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbSaturdayStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSaturdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSaturdayEnd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSaturdayEnd)
                .addContainerGap())
        );
        pnlSaturdayLayout.setVerticalGroup(
            pnlSaturdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSaturdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbSaturdayStart)
                .addComponent(txtSaturdayStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblSaturdayEnd)
                .addComponent(txtSaturdayEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout pnlScheduleDaysLayout = new javax.swing.GroupLayout(pnlScheduleDays);
        pnlScheduleDays.setLayout(pnlScheduleDaysLayout);
        pnlScheduleDaysLayout.setHorizontalGroup(
            pnlScheduleDaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScheduleDaysLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlScheduleDaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlMonday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTuesday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlWednesday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlThursday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlFriday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSaturday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlScheduleDaysLayout.setVerticalGroup(
            pnlScheduleDaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScheduleDaysLayout.createSequentialGroup()
                .addComponent(pnlMonday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTuesday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlWednesday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlThursday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFriday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSaturday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSaveTimeSlot.setText("Save Timeslot");
        btnSaveTimeSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveTimeSlotActionPerformed(evt);
            }
        });

        btnDeleteTimeSlot.setText("Delete Timeslot");
        btnDeleteTimeSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTimeSlotActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTimeSlotsLayout = new javax.swing.GroupLayout(pnlTimeSlots);
        pnlTimeSlots.setLayout(pnlTimeSlotsLayout);
        pnlTimeSlotsLayout.setHorizontalGroup(
            pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTimeSlotsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spTimeslotList, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(btnNewTimeslot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSaveTimeSlot, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteTimeSlot, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlScheduleDays, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlTimeSlotsLayout.createSequentialGroup()
                        .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblTimeSlotCreditValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblGeneratedTSID, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTSGeneratedID)
                            .addComponent(txtTimeSlotCreditValue))))
                .addContainerGap())
        );
        pnlTimeSlotsLayout.setVerticalGroup(
            pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTimeSlotsLayout.createSequentialGroup()
                .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTimeSlotsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnNewTimeslot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spTimeslotList))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTimeSlotsLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTSGeneratedID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblGeneratedTSID))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlTimeSlotsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTimeSlotCreditValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTimeSlotCreditValue))
                        .addGap(18, 18, 18)
                        .addComponent(pnlScheduleDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(btnSaveTimeSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnDeleteTimeSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(104, 104, 104)))
                .addContainerGap())
        );

        tabbedPanels.addTab("Time Slots", pnlTimeSlots);

        tableSchedule.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        spTableSchedule.setViewportView(tableSchedule);

        lblScheduleCourse.setText("Course");

        cbScheduleCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbScheduleCourseActionPerformed(evt);
            }
        });

        lblScheduleProfessor.setText("Professor");

        lblScheduleTimeslot.setText("Time Slot");

        btnSaveSchedule.setText("Add/Update Section");
        btnSaveSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveScheduleActionPerformed(evt);
            }
        });

        btnDeleteSchedule.setText("Delete Section");
        btnDeleteSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteScheduleActionPerformed(evt);
            }
        });

        listUnscheduledCourses.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listUnscheduledCourses.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listUnscheduledCoursesValueChanged(evt);
            }
        });
        spUnscheduledCourses.setViewportView(listUnscheduledCourses);

        lblUnscheduledCourses.setText("Unscheduled Courses");

        lblScheduledCourses.setText("Scheduled Courses");

        btnValidateInitialSchedule.setText("Validate Schedule");
        btnValidateInitialSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidateInitialScheduleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlInitScheduleLayout = new javax.swing.GroupLayout(pnlInitSchedule);
        pnlInitSchedule.setLayout(pnlInitScheduleLayout);
        pnlInitScheduleLayout.setHorizontalGroup(
            pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUnscheduledCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spUnscheduledCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                                .addComponent(lblScheduledCourses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(2, 2, 2))
                            .addComponent(spTableSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)))
                    .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                        .addComponent(btnSaveSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDeleteSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnValidateInitialSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblScheduleProfessor)
                            .addComponent(lblScheduleTimeslot)
                            .addComponent(lblScheduleCourse))
                        .addGap(18, 18, 18)
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbScheduleCourse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbScheduleTimeslot, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbScheduleProfessor, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlInitScheduleLayout.setVerticalGroup(
            pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInitScheduleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblScheduleCourse)
                    .addComponent(cbScheduleCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbScheduleProfessor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScheduleProfessor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblScheduleTimeslot)
                    .addComponent(cbScheduleTimeslot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSaveSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDeleteSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnValidateInitialSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUnscheduledCourses)
                    .addComponent(lblScheduledCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spUnscheduledCourses)
                    .addComponent(spTableSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabbedPanels.addTab("Initial Schedule", pnlInitSchedule);

        pnlAdvancedConfig.setBorder(javax.swing.BorderFactory.createTitledBorder("Advanced Configuration"));

        lblGenerations.setLabelFor(spinnerGenerations);
        lblGenerations.setText("Evolution Generations");

        lblPopulationSize.setLabelFor(spinnerPopulationSize);
        lblPopulationSize.setText("Population Size");

        lblReplacementWait.setLabelFor(spinnerReplacementWait);
        lblReplacementWait.setText("Replacement Wait");

        lblMutationProbabilty.setLabelFor(spinnerMutationProbabilty);
        lblMutationProbabilty.setText("Mutation Probability (XX%)");

        spinnerGenerations.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        spinnerPopulationSize.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        spinnerReplacementWait.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        spinnerMutationProbabilty.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));

        javax.swing.GroupLayout pnlAdvancedConfigLayout = new javax.swing.GroupLayout(pnlAdvancedConfig);
        pnlAdvancedConfig.setLayout(pnlAdvancedConfigLayout);
        pnlAdvancedConfigLayout.setHorizontalGroup(
            pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvancedConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGenerations)
                    .addComponent(lblPopulationSize)
                    .addComponent(lblMutationProbabilty)
                    .addComponent(lblReplacementWait))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 255, Short.MAX_VALUE)
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(spinnerGenerations, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .addComponent(spinnerPopulationSize)
                    .addComponent(spinnerReplacementWait, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spinnerMutationProbabilty))
                .addContainerGap())
        );
        pnlAdvancedConfigLayout.setVerticalGroup(
            pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvancedConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGenerations)
                    .addComponent(spinnerGenerations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPopulationSize)
                    .addComponent(spinnerPopulationSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReplacementWait)
                    .addComponent(spinnerReplacementWait, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAdvancedConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMutationProbabilty)
                    .addComponent(spinnerMutationProbabilty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lbSetupFileName.setLabelFor(txtSetupFileName);
        lbSetupFileName.setText("Setup File Name");

        btnBrowseSetupFileName.setText("Browse");
        btnBrowseSetupFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseSetupFileNameActionPerformed(evt);
            }
        });

        btnGenerateInputFile.setText("Generate Input File");
        btnGenerateInputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateInputFileActionPerformed(evt);
            }
        });

        btnSaveSetup.setText("Save Setup");
        btnSaveSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSetupActionPerformed(evt);
            }
        });

        lblGeneratedFileName.setText("Generated File Name");

        btnBrowseGeneratedFileName.setText("Browse");
        btnBrowseGeneratedFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseGeneratedFileNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlConfigurationLayout = new javax.swing.GroupLayout(pnlConfiguration);
        pnlConfiguration.setLayout(pnlConfigurationLayout);
        pnlConfigurationLayout.setHorizontalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSaveSetup, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGenerateInputFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAdvancedConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlConfigurationLayout.createSequentialGroup()
                        .addComponent(lbSetupFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSetupFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBrowseSetupFileName))
                    .addGroup(pnlConfigurationLayout.createSequentialGroup()
                        .addComponent(lblGeneratedFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtGeneratedFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnBrowseGeneratedFileName)))
                .addContainerGap())
        );
        pnlConfigurationLayout.setVerticalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConfigurationLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGeneratedFileName)
                    .addComponent(btnBrowseGeneratedFileName)
                    .addComponent(txtGeneratedFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlAdvancedConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGenerateInputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbSetupFileName)
                    .addComponent(btnBrowseSetupFileName)
                    .addComponent(txtSetupFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveSetup, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(335, Short.MAX_VALUE))
        );

        tabbedPanels.addTab("Generate Input", pnlConfiguration);

        lblResultFile.setLabelFor(txtResultPath);
        lblResultFile.setText("Result File");

        btnBrowseResult.setText("Browse");
        btnBrowseResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseResultActionPerformed(evt);
            }
        });

        txtResultPath.setEditable(false);
        txtResultPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtResultPathActionPerformed(evt);
            }
        });

        pnlViewByControls.setBorder(javax.swing.BorderFactory.createTitledBorder("View By"));

        viewByGroup.add(rdoCourse);
        rdoCourse.setSelected(true);
        rdoCourse.setText("Course");
        rdoCourse.setEnabled(false);

        viewByGroup.add(rdoProfessor);
        rdoProfessor.setText("Professor");
        rdoProfessor.setEnabled(false);
        rdoProfessor.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rdoProfessorItemStateChanged(evt);
            }
        });

        listViewBySelection.setEnabled(false);
        listViewBySelection.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listViewBySelectionValueChanged(evt);
            }
        });
        spViewBySelection.setViewportView(listViewBySelection);

        btnResultChangeUndo.setText("Undo");
        btnResultChangeUndo.setEnabled(false);
        btnResultChangeUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResultChangeUndoActionPerformed(evt);
            }
        });

        btnResultChangeUpdate.setText("<html><center>Validate<br />And<br />Update</center></html>");
        btnResultChangeUpdate.setEnabled(false);
        btnResultChangeUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResultChangeUpdateActionPerformed(evt);
            }
        });

        lblChangeProfessorTo.setLabelFor(cbProfessorSelection);
        lblChangeProfessorTo.setText("Professor");

        lblChangeTimeTo.setLabelFor(cbTimeSelection);
        lblChangeTimeTo.setText("Time Slot");

        cbProfessorSelection.setEnabled(false);

        cbTimeSelection.setEnabled(false);

        lblScheduleSectionLabel.setText("Section");

        cbScheduleSection.setEnabled(false);
        cbScheduleSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbScheduleSectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlViewByControlsLayout = new javax.swing.GroupLayout(pnlViewByControls);
        pnlViewByControls.setLayout(pnlViewByControlsLayout);
        pnlViewByControlsLayout.setHorizontalGroup(
            pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewByControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoCourse)
                    .addComponent(rdoProfessor))
                .addGap(18, 18, 18)
                .addComponent(spViewBySelection, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblChangeProfessorTo)
                    .addComponent(lblChangeTimeTo)
                    .addComponent(lblScheduleSectionLabel))
                .addGap(18, 18, 18)
                .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbProfessorSelection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbScheduleSection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbTimeSelection, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnResultChangeUndo)
                    .addComponent(btnResultChangeUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlViewByControlsLayout.setVerticalGroup(
            pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlViewByControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoCourse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdoProfessor)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlViewByControlsLayout.createSequentialGroup()
                .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(spViewBySelection, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlViewByControlsLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(btnResultChangeUndo))
                        .addGroup(pnlViewByControlsLayout.createSequentialGroup()
                            .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblScheduleSectionLabel)
                                .addComponent(cbScheduleSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblChangeProfessorTo)
                                .addComponent(cbProfessorSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(pnlViewByControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblChangeTimeTo)
                                .addComponent(cbTimeSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(btnResultChangeUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnStatistics.setText("Statistics");
        btnStatistics.setEnabled(false);

        btnGenerateResult.setText("Generate Output File");
        btnGenerateResult.setEnabled(false);

        scrollSchedule.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollSchedule.setEnabled(false);

        pnlSchedule.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pnlScheduleComponentResized(evt);
            }
        });

        pnlTimeColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlTimeColumn.setMinimumSize(new java.awt.Dimension(152, 20));
        pnlTimeColumn.setPreferredSize(new java.awt.Dimension(152, 498));

        javax.swing.GroupLayout pnlTimeColumnLayout = new javax.swing.GroupLayout(pnlTimeColumn);
        pnlTimeColumn.setLayout(pnlTimeColumnLayout);
        pnlTimeColumnLayout.setHorizontalGroup(
            pnlTimeColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        pnlTimeColumnLayout.setVerticalGroup(
            pnlTimeColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlMondayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlMondayColumn.setMinimumSize(new java.awt.Dimension(112, 100));
        pnlMondayColumn.setPreferredSize(new java.awt.Dimension(112, 16));

        javax.swing.GroupLayout pnlMondayColumnLayout = new javax.swing.GroupLayout(pnlMondayColumn);
        pnlMondayColumn.setLayout(pnlMondayColumnLayout);
        pnlMondayColumnLayout.setHorizontalGroup(
            pnlMondayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlMondayColumnLayout.setVerticalGroup(
            pnlMondayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlTuesdayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlTuesdayColumn.setMinimumSize(new java.awt.Dimension(112, 100));

        javax.swing.GroupLayout pnlTuesdayColumnLayout = new javax.swing.GroupLayout(pnlTuesdayColumn);
        pnlTuesdayColumn.setLayout(pnlTuesdayColumnLayout);
        pnlTuesdayColumnLayout.setHorizontalGroup(
            pnlTuesdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlTuesdayColumnLayout.setVerticalGroup(
            pnlTuesdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlWednesdayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlWednesdayColumn.setMinimumSize(new java.awt.Dimension(112, 100));

        javax.swing.GroupLayout pnlWednesdayColumnLayout = new javax.swing.GroupLayout(pnlWednesdayColumn);
        pnlWednesdayColumn.setLayout(pnlWednesdayColumnLayout);
        pnlWednesdayColumnLayout.setHorizontalGroup(
            pnlWednesdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlWednesdayColumnLayout.setVerticalGroup(
            pnlWednesdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlThursdayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlThursdayColumn.setMinimumSize(new java.awt.Dimension(112, 100));

        javax.swing.GroupLayout pnlThursdayColumnLayout = new javax.swing.GroupLayout(pnlThursdayColumn);
        pnlThursdayColumn.setLayout(pnlThursdayColumnLayout);
        pnlThursdayColumnLayout.setHorizontalGroup(
            pnlThursdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlThursdayColumnLayout.setVerticalGroup(
            pnlThursdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlFridayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlFridayColumn.setMinimumSize(new java.awt.Dimension(112, 100));
        pnlFridayColumn.setPreferredSize(new java.awt.Dimension(112, 16));

        javax.swing.GroupLayout pnlFridayColumnLayout = new javax.swing.GroupLayout(pnlFridayColumn);
        pnlFridayColumn.setLayout(pnlFridayColumnLayout);
        pnlFridayColumnLayout.setHorizontalGroup(
            pnlFridayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlFridayColumnLayout.setVerticalGroup(
            pnlFridayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlSaturdayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlSaturdayColumn.setMinimumSize(new java.awt.Dimension(112, 100));
        pnlSaturdayColumn.setPreferredSize(new java.awt.Dimension(112, 16));

        javax.swing.GroupLayout pnlSaturdayColumnLayout = new javax.swing.GroupLayout(pnlSaturdayColumn);
        pnlSaturdayColumn.setLayout(pnlSaturdayColumnLayout);
        pnlSaturdayColumnLayout.setHorizontalGroup(
            pnlSaturdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );
        pnlSaturdayColumnLayout.setVerticalGroup(
            pnlSaturdayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        pnlSundayColumn.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 1, new java.awt.Color(0, 0, 0)));
        pnlSundayColumn.setMinimumSize(new java.awt.Dimension(112, 100));

        javax.swing.GroupLayout pnlSundayColumnLayout = new javax.swing.GroupLayout(pnlSundayColumn);
        pnlSundayColumn.setLayout(pnlSundayColumnLayout);
        pnlSundayColumnLayout.setHorizontalGroup(
            pnlSundayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 112, Short.MAX_VALUE)
        );
        pnlSundayColumnLayout.setVerticalGroup(
            pnlSundayColumnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 536, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlScheduleLayout = new javax.swing.GroupLayout(pnlSchedule);
        pnlSchedule.setLayout(pnlScheduleLayout);
        pnlScheduleLayout.setHorizontalGroup(
            pnlScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScheduleLayout.createSequentialGroup()
                .addComponent(pnlTimeColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMondayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTuesdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlWednesdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlThursdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFridayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSaturdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSundayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlScheduleLayout.setVerticalGroup(
            pnlScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTimeColumn, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
            .addComponent(pnlMondayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
            .addComponent(pnlTuesdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlWednesdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlThursdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlFridayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
            .addComponent(pnlSaturdayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
            .addComponent(pnlSundayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        scrollSchedule.setViewportView(pnlSchedule);

        pnlResultScheduleLabels.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(0, 0, 0)));

        pnlResultScheduleLabel_Time.setPreferredSize(new java.awt.Dimension(104, 100));

        lblTimeColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTimeColumn.setText("Time");

        javax.swing.GroupLayout pnlResultScheduleLabel_TimeLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Time);
        pnlResultScheduleLabel_Time.setLayout(pnlResultScheduleLabel_TimeLayout);
        pnlResultScheduleLabel_TimeLayout.setHorizontalGroup(
            pnlResultScheduleLabel_TimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTimeColumn, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_TimeLayout.setVerticalGroup(
            pnlResultScheduleLabel_TimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTimeColumn)
        );

        pnlResultScheduleLabel_Sunday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Sunday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblSundayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSundayColumn.setText("Sunday");

        javax.swing.GroupLayout pnlResultScheduleLabel_SundayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Sunday);
        pnlResultScheduleLabel_Sunday.setLayout(pnlResultScheduleLabel_SundayLayout);
        pnlResultScheduleLabel_SundayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_SundayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSundayColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_SundayLayout.setVerticalGroup(
            pnlResultScheduleLabel_SundayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSundayColumn)
        );

        pnlResultScheduleLabel_Saturday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Saturday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblSaturdayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSaturdayColumn.setText("Saturday");

        javax.swing.GroupLayout pnlResultScheduleLabel_SaturdayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Saturday);
        pnlResultScheduleLabel_Saturday.setLayout(pnlResultScheduleLabel_SaturdayLayout);
        pnlResultScheduleLabel_SaturdayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_SaturdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSaturdayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_SaturdayLayout.setVerticalGroup(
            pnlResultScheduleLabel_SaturdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSaturdayColumn)
        );

        pnlResultScheduleLabel_Friday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Friday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblFridayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFridayColumn.setText("Friday");

        javax.swing.GroupLayout pnlResultScheduleLabel_FridayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Friday);
        pnlResultScheduleLabel_Friday.setLayout(pnlResultScheduleLabel_FridayLayout);
        pnlResultScheduleLabel_FridayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_FridayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFridayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_FridayLayout.setVerticalGroup(
            pnlResultScheduleLabel_FridayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblFridayColumn)
        );

        pnlResultScheduleLabel_Thursday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Thursday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblThursdayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblThursdayColumn.setText("Thursday");

        javax.swing.GroupLayout pnlResultScheduleLabel_ThursdayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Thursday);
        pnlResultScheduleLabel_Thursday.setLayout(pnlResultScheduleLabel_ThursdayLayout);
        pnlResultScheduleLabel_ThursdayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_ThursdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblThursdayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_ThursdayLayout.setVerticalGroup(
            pnlResultScheduleLabel_ThursdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblThursdayColumn)
        );

        pnlResultScheduleLabel_Wednesday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Wednesday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblWednesdayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWednesdayColumn.setText("Wednesday");

        javax.swing.GroupLayout pnlResultScheduleLabel_WednesdayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Wednesday);
        pnlResultScheduleLabel_Wednesday.setLayout(pnlResultScheduleLabel_WednesdayLayout);
        pnlResultScheduleLabel_WednesdayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_WednesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblWednesdayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_WednesdayLayout.setVerticalGroup(
            pnlResultScheduleLabel_WednesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblWednesdayColumn)
        );

        pnlResultScheduleLabel_Tuesday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Tuesday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblTuesdayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTuesdayColumn.setText("Tuesday");

        javax.swing.GroupLayout pnlResultScheduleLabel_TuesdayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Tuesday);
        pnlResultScheduleLabel_Tuesday.setLayout(pnlResultScheduleLabel_TuesdayLayout);
        pnlResultScheduleLabel_TuesdayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_TuesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTuesdayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_TuesdayLayout.setVerticalGroup(
            pnlResultScheduleLabel_TuesdayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTuesdayColumn)
        );

        pnlResultScheduleLabel_Monday.setMinimumSize(new java.awt.Dimension(104, 0));
        pnlResultScheduleLabel_Monday.setPreferredSize(new java.awt.Dimension(104, 100));

        lblMondayColumn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMondayColumn.setText("Monday");

        javax.swing.GroupLayout pnlResultScheduleLabel_MondayLayout = new javax.swing.GroupLayout(pnlResultScheduleLabel_Monday);
        pnlResultScheduleLabel_Monday.setLayout(pnlResultScheduleLabel_MondayLayout);
        pnlResultScheduleLabel_MondayLayout.setHorizontalGroup(
            pnlResultScheduleLabel_MondayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMondayColumn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultScheduleLabel_MondayLayout.setVerticalGroup(
            pnlResultScheduleLabel_MondayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblMondayColumn)
        );

        javax.swing.GroupLayout pnlResultScheduleLabelsLayout = new javax.swing.GroupLayout(pnlResultScheduleLabels);
        pnlResultScheduleLabels.setLayout(pnlResultScheduleLabelsLayout);
        pnlResultScheduleLabelsLayout.setHorizontalGroup(
            pnlResultScheduleLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultScheduleLabelsLayout.createSequentialGroup()
                .addComponent(pnlResultScheduleLabel_Time, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Monday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Tuesday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Wednesday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Thursday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Friday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Saturday, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabel_Sunday, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
        );
        pnlResultScheduleLabelsLayout.setVerticalGroup(
            pnlResultScheduleLabelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlResultScheduleLabel_Saturday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Friday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Thursday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Wednesday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Tuesday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Monday, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Time, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
            .addComponent(pnlResultScheduleLabel_Sunday, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
        );

        txtResultStatus.setEditable(false);
        txtResultStatus.setText("Schedule: ");

        javax.swing.GroupLayout pnlResultContainerLayout = new javax.swing.GroupLayout(pnlResultContainer);
        pnlResultContainer.setLayout(pnlResultContainerLayout);
        pnlResultContainerLayout.setHorizontalGroup(
            pnlResultContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResultContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlViewByControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlResultContainerLayout.createSequentialGroup()
                        .addComponent(lblResultFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtResultPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowseResult))
                    .addComponent(scrollSchedule)
                    .addComponent(pnlResultScheduleLabels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlResultContainerLayout.createSequentialGroup()
                        .addComponent(btnStatistics, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtResultStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGenerateResult, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlResultContainerLayout.setVerticalGroup(
            pnlResultContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResultContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblResultFile)
                    .addComponent(btnBrowseResult)
                    .addComponent(txtResultPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlViewByControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultScheduleLabels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResultContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStatistics)
                    .addComponent(btnGenerateResult)
                    .addComponent(txtResultStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlResultsLayout = new javax.swing.GroupLayout(pnlResults);
        pnlResults.setLayout(pnlResultsLayout);
        pnlResultsLayout.setHorizontalGroup(
            pnlResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlResultContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlResultsLayout.setVerticalGroup(
            pnlResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlResultContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tabbedPanels.addTab("View Result", pnlResults);

        javax.swing.GroupLayout pnlContainerLayout = new javax.swing.GroupLayout(pnlContainer);
        pnlContainer.setLayout(pnlContainerLayout);
        pnlContainerLayout.setHorizontalGroup(
            pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanels)
        );
        pnlContainerLayout.setVerticalGroup(
            pnlContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanels)
        );

        menuFile.setText("File");

        miOpenConfig.setText("Open Config");
        miOpenConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenConfigActionPerformed(evt);
            }
        });
        menuFile.add(miOpenConfig);

        miSaveConfig.setText("Save Config");
        miSaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveConfigActionPerformed(evt);
            }
        });
        menuFile.add(miSaveConfig);

        menuBar.add(menuFile);

        menuAbout.setText("Edit");
        menuBar.add(menuAbout);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tabbedPanelsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPanelsStateChanged
        if (tabbedPanels.getSelectedIndex() == 3) {
            cbScheduleCourse.removeAllItems();
            for (Object s : courseSectionListData) {
                cbScheduleCourse.addItem(s.toString());
            }
            listUnscheduledCourses.setListData(unscheduledCourses);
            spUnscheduledCourses.revalidate();
            spUnscheduledCourses.repaint();
            incompatibleSectionList.clear();
            incompatibleSectionList = GenerateIncompatibleSectionArray();
        } else if (tabbedPanels.getSelectedIndex() == 5) {
            listViewBySelection.setListData(courseListData);
            if (sectionLookup.isEmpty()) {
                TreeSet<String> courses = new TreeSet<>();
                for (String c : courseList.keySet()) {
                    courses.add(c + ", " + String.valueOf(courseList.get(c).getSectionCount()) + "\n");
                }
                for (String s : courses) {
                    String[] parts = s.split(",");
                    for (int i = 0; i < courseList.get(parts[0]).getSectionCount(); i++) {
                        sectionLookup.put(sectionLookup.size(), parts[0] + "(" + (i + 1) + ")");
                    }
                }

                for (String pr : profList.keySet()) {
                    profLookup.put(profList.get(pr).getProfID(), pr);
                }

                for (String s : timeslotList.keySet()) {
                    timeslotLookup.put(timeslotList.get(s).getID(), s);
                }
            }
        }
    }//GEN-LAST:event_tabbedPanelsStateChanged

    private void btnBrowseGeneratedFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseGeneratedFileNameActionPerformed
        JFileChooser fc = new JFileChooser();
        if (!txtGeneratedFileName.getText().isEmpty()) {
            fc = new JFileChooser(new File(txtGeneratedFileName.getText()));
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Data File", new String[]{"dat"}));
        int returnVal = fc.showSaveDialog(pnlContainer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = fc.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".dat")) {
                fileName += ".dat";
            }
            txtGeneratedFileName.setText(fileName);
        }
    }//GEN-LAST:event_btnBrowseGeneratedFileNameActionPerformed

    private void btnSaveSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSetupActionPerformed
        while (txtSetupFileName.getText().isEmpty()) {
            btnBrowseSetupFileNameActionPerformed(evt);
        }
        try (
                OutputStream oFile = new FileOutputStream(txtSetupFileName.getText());
                OutputStream buffer = new BufferedOutputStream(oFile);
                ObjectOutput output = new ObjectOutputStream(buffer);) {

            output.writeObject(courseList);
            output.writeObject(profList);
            output.writeObject(timeslotList);
            output.writeObject(scheduledCoursesList);
            generations = Integer.parseInt(spinnerGenerations.getValue().toString());
            output.writeObject(generations);
            populationSize = Integer.parseInt(spinnerPopulationSize.getValue().toString());
            output.writeObject(populationSize);
            replacementWait = Integer.parseInt(spinnerReplacementWait.getValue().toString());
            output.writeObject(replacementWait);
            output.writeObject(mutationProbability);
            output.writeObject(courseListData);
            output.writeObject(profListData);
            output.writeObject(timeslotListData);
            output.writeObject(courseIDs);
            output.writeObject(profIDs);
            output.writeObject(txtGeneratedFileName.getText());
            setTitle("CS:POp - " + (new File(txtSetupFileName.getText())).getName());
            JOptionPane.showMessageDialog(pnlContainer, "<html><p>Setup Saved to<br />" + txtSetupFileName.getText() + "</p></html>", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(pnlContainer, "<html>Following error occured while saving: <br /><p>" + e.getMessage() + "</p></html>", "Save Failed", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException n) {
            n.printStackTrace();
        }
    }//GEN-LAST:event_btnSaveSetupActionPerformed
    
    private HashMap<Double, ArrayList<Integer>> GenerateCreditTimeslotMap() {
        HashMap<Double, ArrayList<Integer>> rtnVal = new HashMap<>();
        for (TimeSlot t : timeslotList.values()) {
            if (!rtnVal.containsKey(t.getCredits())) {
                rtnVal.put(t.getCredits(), new ArrayList<>());
            }
            rtnVal.get(t.getCredits()).add(t.getID());
        }

        return rtnVal;
    }
    
    private void btnGenerateInputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateInputFileActionPerformed
        while (txtGeneratedFileName.getText().isEmpty()) {
            btnBrowseGeneratedFileNameActionPerformed(null);
        }
        double courseCredits = 0, profCredits = 0;
        for (Course c : courseList.values()) {
            courseCredits += (c.getCreditValue() * c.getSectionCount());
        }

        for (Professor p : profList.values()) {
            profCredits += p.getCredits();
        }
        DecimalFormat df = new DecimalFormat("#.#");
        if (profCredits < courseCredits) {
            String[] options = {"Yes", "No"};
            String message = "<html>You have scheduled " + df.format(courseCredits) + "worth of classes,<br/>while only " + df.format(profCredits) + " worth of professor credits is available.<br />Would you like to generate an input? (Schedule might contain severely unbalanced professors.).</html>";
            int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Invalid Schedule", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "No");
            if (choice == 1) {
                return;
            }
        }
        String validation = validateSchedule(false);
        if (!validation.isEmpty()) {
            String[] options = {"Yes", "No"};
            String message = "<html>The initial schedule is not valid due to:<br />" + validation + "<br />Continue?</html>";
            int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Invalid Schedule", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "No");
            if (choice == 1) {
                return;
            }
        }

        BufferedWriter writer = null;
        try {
            fixIDs();
            TreeSet<String> courses = new TreeSet<>();
            for (String c : courseList.keySet()) {
                courses.add(c + ", " + String.valueOf(courseList.get(c).getSectionCount()) + "\n");
            }
            if(sectionLookup.isEmpty()){
                for (String s : courses) {
                    String[] parts = s.split(",");
                    for (int i = 0; i < courseList.get(parts[0]).getSectionCount(); i++) {
                        sectionLookup.put(sectionLookup.size(), parts[0] + "(" + (i + 1) + ")");
                    }
                }
            }
            btnGenerateInputFile.setEnabled(false);
            incompatibleSectionList.clear();
            incompatibleSectionList = GenerateIncompatibleSectionArray();
            ArrayList<String> incompatibleBitString = GenerateIncompatibleSectionBitString();
            ArrayList<Double> sectionCredit = GenerateSectionCreditArray();

            ArrayList<ArrayList<Integer>> profSection = GenerateProfessorSectionArray();
            ArrayList<ArrayList<Integer>> sectionProf = GenerateSectionProfArray(profSection);
            ArrayList<ArrayList<ArrayList<Integer>>> associatedProf = GenerateAssociatedProfArray(sectionProf, profSection);
            HashMap<Double, ArrayList<Integer>> creditTimeslot = GenerateCreditTimeslotArray();

            String inputFile = txtGeneratedFileName.getText();
            File outFile = new File(inputFile);
            writer = new BufferedWriter(new FileWriter(outFile));

            //Write Parameters
            writer.write("//START*PARAMETERS*\n");
            writer.write(spinnerGenerations.getValue().toString() + "\n");//generation_count
            writer.write(spinnerPopulationSize.getValue().toString() + "\n"); //population_size
            writer.write(spinnerReplacementWait.getValue().toString() + "\n"); //replacement_wait
            writer.write(spinnerMutationProbabilty.getValue().toString() + "\n"); //mutation_probability
            writer.write(courseSectionListData.size() + "\n"); //section_count
            writer.write(profList.size() + "\n"); //professor_count
            writer.write(timeslotList.size() + "\n"); //timeslot_count
            writer.write(creditTimeslot.size() + "\n"); //credit_count
            writer.write("*END*PARAMETERS\n");
            //Write arrays literally as they need to look like
            //sectionID, incompSize, incompSections
            writer.write("//*START*SECTION*\n");
            writer.write("//sectionID, incompSize[, incompSections]\n");
            for (int i = 0; i < incompatibleSectionList.size(); i++) {
                writer.write(i + "," + incompatibleBitString.get(i) + " ");
                writer.write(("\n"));
            }
            writer.write("*END*SECTION*\n");

            writer.write("//*START*SECTIONCREDIT*\n");
            for (int i = 0; i < sectionCredit.size(); i++) {
                writer.write(i + "," + df.format(sectionCredit.get(i)) + "\n");
            }
            writer.write("*END*SECTIONCREDIT*\n");

            writer.write("//*START*PROFCREDIT*\n");
            for (Professor p : profList.values()) {
                writer.write(p.getProfID() + "," + df.format(p.getCredits()) + "\n");
            }
            writer.write("*END*PROFCREDIT*\n");

            writer.write("//*START*SECTION*PROF*\n");
            writer.write("//sectionID, profSize, profs\n");
            for (int i = 0; i < sectionProf.size(); i++) {
                writer.write(i + "," + sectionProf.get(i).size());
                for (int z = 0; z < sectionProf.get(i).size(); z++) {
                    writer.write("," + sectionProf.get(i).get(z));
                }
                writer.write(("\n"));
            }
            writer.write("*END*SECTION*PROF*\n");
            writer.write("//*START*SECTION*TIMESLOT*\n");
            HashMap<Double, ArrayList<Integer>> creditTimeslotMap = GenerateCreditTimeslotMap();
            for (int i = 0; i < sectionLookup.size(); i++) {
                writer.write(i + ",");
                String course = sectionLookup.get(i);
                course = course.substring(0, course.indexOf("("));
                double credit = courseList.get(course).getCreditValue();
                writer.write(String.valueOf(creditTimeslotMap.get(credit).size()));
                for (Integer time : creditTimeslotMap.get(credit)) {
                    writer.write("," + time);
                }
                writer.write("\n");
            }
            writer.write("*END*SECTION*TIMESLOT*\n");
            writer.write("//*START*PROF*SECTION*\n");
            writer.write("//profID, sectionSize, sections\n");
            for (int i = 0; i < profSection.size(); i++) {
                writer.write(i + "," + profSection.get(i).size());
                Collections.sort(profSection.get(i));
                for (int z = 0; z < profSection.get(i).size(); z++) {
                    writer.write("," + profSection.get(i).get(z));
                }
                writer.write(("\n"));
            }
            writer.write("*END*PROF*SECTION*\n");
            writer.write("//*START*PROF*ASSOCIATE*\n");
            writer.write("//profID, assocaiteSize, [associatesID, sectionsSize, sectionList]\n");
            for (int i = 0; i < associatedProf.size(); i++) {
                writer.write(i + "," + associatedProf.get(i).size());
                for (int z = 0; z < associatedProf.get(i).size(); z++) {
                    for (int y = 0; y < associatedProf.get(i).get(z).size(); y++) {
                        writer.write("," + associatedProf.get(i).get(z).get(y));
                    }
                }
                writer.write(("\n"));
            }
            writer.write("*END*PROF*ASSOCIATE*\n");

            writer.write("//*START*COURSEPREF*\n");
            writer.write("//sectionID, m, a, e\n");
            for (int i = 0; i < courseSectionListData.size(); i++) {
                writer.write(i + ",");
                String course = sectionLookup.get(i);
                course = course.substring(0, course.indexOf("("));
                Course c = courseList.get(course);
                writer.write(c.getPreferences()[0] + "," + c.getPreferences()[1] + "," + c.getPreferences()[2] + "\n");
            }
            writer.write("*END*COURSEPREF*\n");

            writer.write("//START*PROFPREF*\n");
            writer.write("//profID, pref(m-a-e)\n");
            for (Professor p : profList.values()) {
                writer.write(p.getProfID() + "," + p.getPreference()[0] + "," + p.getPreference()[1] + "," + p.getPreference()[2] + "\n");
            }
            writer.write("*END*PROFPREF*\n");

            writer.write("//*START*TIME*CREDIT*LEGEND*\n");
            StringBuilder cts = new StringBuilder();
            int creditCounter = 0;
            for (Double d : creditTimeslot.keySet()) {
                writer.write(creditCounter + "," + df.format(d) + "\n");
                cts.append(creditCounter++).append(",").append(creditTimeslot.get(d).size());
                for (int i = 0; i < creditTimeslot.get(d).size(); i++) {
                    cts.append(",").append(creditTimeslot.get(d).get(i));
                }
                cts.append("\n");
            }
            writer.write("*END*TIME*CREDIT*LEGEND*\n");

            writer.write("//*START*CREDIT*TIMESLOT*\n");
            writer.write(cts.toString());
            writer.write("*END*CREDIT*TIMESLOT*\n");

            writer.write("//*START*TIMESLOT*\n");
            writer.write("//timeslot_id	, credit rating, isMorning, isAfternoon, isEvening, overlap, consecutive, spreadout\n");
            for (TimeSlot t : timeslotList.values()) {
                writer.write(t.getID() + "," + df.format(t.getCredits()) + ",");
                if (t.isMorning()) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                writer.write(",");
                if (t.isAfternoon()) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }

                writer.write(",");
                if (t.isEvening()) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                writer.write(",");
                char[] overlap = new char[timeslotList.size()];
                char[] consecutive = new char[timeslotList.size()];
                char[] spreadOut = new char[timeslotList.size()];

                for (TimeSlot ot : timeslotList.values()) {
                    if (t.isConflict(ot)) {
                        overlap[ot.getID()] = '1';
                    } else {
                        overlap[ot.getID()] = '0';
                    }

                    if (t.isConsecutive(ot)) {
                        consecutive[ot.getID()] = '1';
                    } else {
                        consecutive[ot.getID()] = '0';
                    }

                    if (t.isSpreadOut(ot)) {
                        spreadOut[ot.getID()] = '1';
                    } else {
                        spreadOut[ot.getID()] = '0';
                    }
                }
                writer.write(new String(overlap) + "," + new String(consecutive) + "," + new String(spreadOut));
                writer.write("\n");
            }
            writer.write("*END*TIMESLOT*\n");

            writer.write("//*START*INITIAL*\n");
            for (Schedule s : scheduledCoursesList.values()) {
                writer.write(courseSectionListData.indexOf(s.course) + ",");
                writer.write(profList.get(s.prof).getProfID() + ",");
                writer.write(timeslotList.get(s.time).getID() + "\n");
            }

            writer.write("*END*INITIAL*\n");
            //OUTPUT KEY
            writer.write("*START*KEY*\n");
            //Output section: sxx%SectionID
            for (int s : sectionLookup.keySet()) {
                writer.write("s" + s + "%" + sectionLookup.get(s) + "\n");
            }

            if(profLookup.isEmpty()){
                for(String prof : profList.keySet()){
                    profLookup.put(profList.get(prof).getProfID(), prof);
                }
            }
            for (int p : profLookup.keySet()) {
                writer.write("p" + p + "%" + profLookup.get(p) + "\n");
            }

            if(timeslotLookup.isEmpty()){
                for(String ti : timeslotList.keySet()){
                    timeslotLookup.put(timeslotList.get(ti).getID(), ti);
                }                
            }            
            for (int t : timeslotLookup.keySet()) {
                writer.write("t" + t + "%" + timeslotLookup.get(t) + "\n");
            }

            writer.write("*END*KEY*\n");
            JOptionPane.showMessageDialog(pnlContainer, "<html><p>Input file generated to<br />" + txtGeneratedFileName.getText() + ".</p></html>", "Input File Generated Successfully", JOptionPane.INFORMATION_MESSAGE);
            tabbedPanels.setEnabledAt(5, true);
            btnGenerateInputFile.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {

            }
        }
    }//GEN-LAST:event_btnGenerateInputFileActionPerformed

    private void btnBrowseSetupFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseSetupFileNameActionPerformed
        JFileChooser fc = new JFileChooser();
        if (!txtSetupFileName.getText().isEmpty()) {
            fc = new JFileChooser(new File(txtSetupFileName.getText()));
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Configuration File", new String[]{"conf"}));
        int returnVal = fc.showSaveDialog(pnlContainer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = fc.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".conf")) {
                fileName += ".conf";
            }
            txtSetupFileName.setText(fileName);
        }
    }//GEN-LAST:event_btnBrowseSetupFileNameActionPerformed

    private void listUnscheduledCoursesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listUnscheduledCoursesValueChanged
        if (listUnscheduledCourses.getSelectedIndex() >= 0) {
            cbScheduleCourse.setSelectedItem(listUnscheduledCourses.getSelectedValue());
        }
    }//GEN-LAST:event_listUnscheduledCoursesValueChanged

    private void btnDeleteScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteScheduleActionPerformed
        if (currentSchedule != null) {
            unscheduledCourses.add(currentSchedule.course);
            dtm.removeRow(dtmSelectedRow);
            listUnscheduledCourses.setListData(unscheduledCourses);
            spUnscheduledCourses.revalidate();
            spUnscheduledCourses.repaint();
        }
    }//GEN-LAST:event_btnDeleteScheduleActionPerformed

    private void btnSaveScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveScheduleActionPerformed
        boolean newRow = false;
        if (currentSchedule == null) {
            //New Course
            newRow = true;
            currentSchedule = new Schedule();
        }

        tableSchedule.clearSelection();
        currentSchedule.course = cbScheduleCourse.getSelectedItem().toString();
        currentSchedule.prof = cbScheduleProfessor.getSelectedItem().toString();
        currentSchedule.time = cbScheduleTimeslot.getSelectedItem().toString();

        if (newRow) {
            dtm.addRow(new String[]{currentSchedule.course, currentSchedule.prof, currentSchedule.time});
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableSchedule.getModel());
            sorter.setSortsOnUpdates(true);
            tableSchedule.setRowSorter(sorter);

            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            int columnIndexToSort = 0;
            sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
            sorter.setSortKeys(sortKeys);
            sorter.sort();
        } else {
            int rowIndex = 0;
            while (rowIndex < dtm.getRowCount() && !currentSchedule.course.equals(dtm.getValueAt(rowIndex, 0))) {
                rowIndex++;
            }

            if (dtm.getRowCount() == rowIndex) {
                return;
            }

            dtm.removeRow(rowIndex);
            dtm.insertRow(rowIndex, new String[]{currentSchedule.course, currentSchedule.prof, currentSchedule.time});
        }
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableSchedule.getModel());
        sorter.setSortsOnUpdates(true);
        tableSchedule.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        int columnIndexToSort = 0;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        scheduledCoursesList.put(currentSchedule.course, currentSchedule);
        unscheduledCourses.removeElement(currentSchedule.course);
        listUnscheduledCourses.setListData(unscheduledCourses);
        spUnscheduledCourses.revalidate();
        spUnscheduledCourses.repaint();
    }//GEN-LAST:event_btnSaveScheduleActionPerformed

    private void cbScheduleCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbScheduleCourseActionPerformed
        if (cbScheduleCourse.getSelectedIndex() >= 0) {
            String course = cbScheduleCourse.getSelectedItem().toString();
            if (scheduledCoursesList.containsKey(cbScheduleCourse.getSelectedItem().toString())) {
                currentSchedule = scheduledCoursesList.get(cbScheduleCourse.getSelectedItem().toString());
            } else {
                currentSchedule = null;
            }

            course = course.substring(0, course.indexOf('('));

            if (courseList.get(course) != null) {
                cbScheduleProfessor.removeAllItems();
                for (Object s : profListData) {
                    if (profList.get(s.toString()).hasCourse(course)) {
                        cbScheduleProfessor.addItem(s.toString());
                    }
                }
                if (currentSchedule != null) {
                    cbScheduleProfessor.setSelectedItem(currentSchedule.prof);
                }

                double credvalue = courseList.get(course).getCreditValue();
                cbScheduleTimeslot.removeAllItems();
                for (Iterator it = timeslotListData.iterator(); it.hasNext();) {
                    Object s = it.next();
                    if (timeslotList.get(s.toString()).getCredits() == credvalue) {
                        cbScheduleTimeslot.addItem(s.toString());
                    }
                }

                if (currentSchedule != null) {
                    cbScheduleTimeslot.setSelectedItem(currentSchedule.time);
                }
            }
        }
    }//GEN-LAST:event_cbScheduleCourseActionPerformed

    private void btnDeleteTimeSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTimeSlotActionPerformed
        if (currentTimeslot != null) {
            int id = currentTimeslot.getID();
            String listing = currentTimeslot.toString();
            timeslotList.remove(currentTimeslot.getID());
            timeslotListData.removeElement(listing);
            listTimeslots.setListData(timeslotListData);
            spTimeslotList.revalidate();
            spTimeslotList.repaint();
        }
    }//GEN-LAST:event_btnDeleteTimeSlotActionPerformed

    private void btnSaveTimeSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveTimeSlotActionPerformed
        if (txtTimeSlotCreditValue.getText().isEmpty()) {
            txtTimeSlotCreditValue.setBorder(new LineBorder(Color.red));
            JOptionPane.showMessageDialog(pnlContainer, "A credit value is required for the time slot.", "Credits Value not provided.", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            txtTimeSlotCreditValue.setBorder(new LineBorder(Color.lightGray));
        }
        if (txtMondayStart.getText().isEmpty() && txtMondayEnd.getText().isEmpty()
                && txtTuesdayStart.getText().isEmpty() && txtTuesdayStart.getText().isEmpty()
                && txtWednesdayStart.getText().isEmpty() && txtWednesdayEnd.getText().isEmpty()
                && txtThursdayStart.getText().isEmpty() && txtThursdayEnd.getText().isEmpty()
                && txtFridayStart.getText().isEmpty() && txtFridayEnd.getText().isEmpty()
                && txtSaturdayStart.getText().isEmpty() && txtSaturdayEnd.getText().isEmpty()) {
            JOptionPane.showMessageDialog(pnlContainer, "At least one time is required for the timeslot.", "Timeslot empty", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((!txtMondayStart.getText().isEmpty() && txtMondayEnd.getText().isEmpty()) || (txtMondayStart.getText().isEmpty() && !txtMondayEnd.getText().isEmpty())) {
            pnlMonday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Monday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlMonday.setBackground(null);
        }

        if ((!txtTuesdayStart.getText().isEmpty() && txtTuesdayEnd.getText().isEmpty()) || (txtTuesdayStart.getText().isEmpty() && !txtTuesdayEnd.getText().isEmpty())) {
            pnlTuesday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Tuesday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlTuesday.setBackground(null);
        }

        if ((!txtWednesdayStart.getText().isEmpty() && txtWednesdayEnd.getText().isEmpty()) || (txtWednesdayStart.getText().isEmpty() && !txtWednesdayEnd.getText().isEmpty())) {
            pnlWednesday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Wednesday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlWednesday.setBackground(null);
        }

        if ((!txtThursdayStart.getText().isEmpty() && txtThursdayEnd.getText().isEmpty()) || (txtThursdayStart.getText().isEmpty() && !txtThursdayEnd.getText().isEmpty())) {
            pnlThursday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Thursday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlThursday.setBackground(null);
        }

        if ((!txtFridayStart.getText().isEmpty() && txtFridayEnd.getText().isEmpty()) || (txtFridayStart.getText().isEmpty() && !txtFridayEnd.getText().isEmpty())) {
            pnlFriday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Friday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlFriday.setBackground(null);
        }

        if ((!txtSaturdayStart.getText().isEmpty() && txtSaturdayEnd.getText().isEmpty()) || (txtSaturdayStart.getText().isEmpty() && !txtSaturdayEnd.getText().isEmpty())) {
            pnlSaturday.setBackground(Color.red);
            JOptionPane.showMessageDialog(pnlContainer, "Saturday time slot not complete.", "Incomplete Timeslot", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            pnlSaturday.setBackground(null);
        }

        boolean fresh = false;
        int id = -1;
        double creds = -1;
        try {
            id = Integer.parseInt(txtTSGeneratedID.getText());
            creds = Double.parseDouble(txtTimeSlotCreditValue.getText());
        } catch (NumberFormatException n) {
            System.err.println(n.getMessage());
            return;
        }
        if (currentTimeslot == null) {
            currentTimeslot = new TimeSlot(id, creds);
            fresh = true;
        }
        currentTimeslot.setCredits(creds);
        String currentListing = "";
        if (!fresh) {
            currentListing = currentTimeslot.toString();
        }
        if (!txtMondayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtMondayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtMondayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(0, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (!txtTuesdayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtTuesdayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtTuesdayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(1, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (!txtWednesdayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtWednesdayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtWednesdayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(2, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (!txtThursdayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtThursdayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtThursdayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(3, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (!txtFridayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtFridayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtFridayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(4, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (!txtSaturdayStart.getText().isEmpty()) {
            try {
                int start = Integer.parseInt(txtSaturdayStart.getText().replaceAll(":", ""));
                int end = Integer.parseInt(txtSaturdayEnd.getText().replaceAll(":", ""));
                currentTimeslot.SetTime(5, start, end);
            } catch (NumberFormatException n) {
                System.err.println(n.getMessage());
            }
        }

        if (fresh || !currentListing.equals(currentTimeslot.toString())) {
            timeslotListData.removeElement(currentListing);
            if (!timeslotListData.contains(currentListing)) {
                timeslotListData.addElement(currentTimeslot.toString());
            }
            Collections.sort(timeslotListData);
            listTimeslots.setListData(timeslotListData);
            spTimeslotList.revalidate();
            spTimeslotList.repaint();
        }
        if (timeslotList.containsKey(currentTimeslot.toString())) {
            timeslotList.remove(currentTimeslot.toString());
        }
        timeslotList.put(currentTimeslot.toString(), currentTimeslot);
        updateTimeSlotIDs();
        btnSaveTimeSlot.setEnabled(false);
    }//GEN-LAST:event_btnSaveTimeSlotActionPerformed

    private void listTimeslotsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTimeslotsValueChanged
        if (listTimeslots.getSelectedIndex() >= 0) {
            currentTimeslot = timeslotList.get(listTimeslots.getSelectedValue());
            if (currentTimeslot != null) {
                txtTSGeneratedID.setText(String.valueOf(currentTimeslot.getID()));
                txtTimeSlotCreditValue.setText(String.valueOf(currentTimeslot.getCredits()));
                for (int i = 0; i < 6; i++) {
                    String currentTime = currentTimeslot.GetTimeOnDay(i);
                    if (!currentTime.equals("(-1:-1)")) {
                        switch (i) {
                            case 0:
                                txtMondayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtMondayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                            case 1:
                                txtTuesdayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtTuesdayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                            case 2:
                                txtWednesdayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtWednesdayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                            case 3:
                                txtThursdayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtThursdayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                            case 4:
                                txtFridayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtFridayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                            case 5:
                                txtSaturdayStart.setText(currentTime.substring(1, currentTime.indexOf(':')));
                                txtSaturdayEnd.setText(currentTime.substring(currentTime.indexOf(':') + 1, currentTime.length() - 1));
                                break;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                txtMondayStart.setText("");
                                txtMondayEnd.setText("");
                                break;
                            case 1:
                                txtTuesdayStart.setText("");
                                txtTuesdayEnd.setText("");
                                break;
                            case 2:
                                txtWednesdayStart.setText("");
                                txtWednesdayEnd.setText("");
                                break;
                            case 3:
                                txtThursdayStart.setText("");
                                txtThursdayEnd.setText("");
                                break;
                            case 4:
                                txtFridayStart.setText("");
                                txtFridayEnd.setText("");
                                break;
                            case 5:
                                txtSaturdayStart.setText("");
                                txtSaturdayEnd.setText("");
                                break;
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_listTimeslotsValueChanged

    private void btnNewTimeslotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewTimeslotActionPerformed
        int newID = timeslotList.size();

        txtTSGeneratedID.setText(String.valueOf(newID));
        String empty = "";
        txtTimeSlotCreditValue.setText(empty);
        txtMondayEnd.setText(empty);
        txtMondayStart.setText(empty);
        txtTuesdayEnd.setText(empty);
        txtTuesdayStart.setText(empty);
        txtWednesdayEnd.setText(empty);
        txtWednesdayStart.setText(empty);
        txtThursdayEnd.setText(empty);
        txtThursdayStart.setText(empty);
        txtFridayEnd.setText(empty);
        txtFridayStart.setText(empty);
        txtSaturdayEnd.setText(empty);
        txtSaturdayStart.setText(empty);
        currentTimeslot = null;
        btnSaveTimeSlot.setEnabled(true);
    }//GEN-LAST:event_btnNewTimeslotActionPerformed

    private void btnNewProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewProfActionPerformed
        int newID = profList.size();
        boolean switchSides = false;
        while (profIDs.contains(newID)) {
            if (!switchSides) {
                newID--;
            } else {
                newID++;
            }
            if (newID == -1) {
                switchSides = true;
                newID = profList.size() + 1;
            }
        }
        txtProfGeneratedID.setText(String.valueOf(newID));
        txtProfName.setText("");
        spinnerCreditsAssigned.setValue(0);
        Iterator it = courseList.keySet().iterator();
        while (it.hasNext()) {
            cbProfCourseTaught.addItem(it.next().toString());
        }
        listCourseTaught.setListData(new Vector<>());
        cbProfHighestMorning.setEnabled(false);
        cbProfHighestAfternoon.setEnabled(false);
        cbProfHighestEvening.setEnabled(false);
        cbProfNormalMorning.setEnabled(false);
        cbProfNormalAfternoon.setEnabled(false);
        cbProfNormalEvening.setEnabled(false);
        cbProfLeastMorning.setEnabled(false);
        cbProfLeastAfternoon.setEnabled(false);
        cbProfLeastEvening.setEnabled(false);
        cbProfNA_Morning.setEnabled(false);
        cbProfNA_Afternoon.setEnabled(false);
        cbProfNA_Evening.setEnabled(false);
    }//GEN-LAST:event_btnNewProfActionPerformed

    private void btnSaveProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfActionPerformed
        if (!chkProfNoPreference.isSelected()) {
            if (!(cbProfHighestMorning.isSelected() || cbProfNormalMorning.isSelected() || cbProfLeastMorning.isSelected() || cbProfNA_Morning.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for morning.");
                return;
            }

            if (!(cbProfHighestAfternoon.isSelected() || cbProfNormalAfternoon.isSelected() || cbProfLeastAfternoon.isSelected() || cbProfNA_Afternoon.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for morning.");
                return;
            }

            if (!(cbProfHighestEvening.isSelected() || cbProfNormalEvening.isSelected() || cbProfLeastEvening.isSelected() || cbProfNA_Evening.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for morning.");
                return;
            }
        }

        if (txtProfName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(pnlContainer, "Please enter a professor name.");
            txtProfName.setBorder(new LineBorder(Color.red));
            return;
        } else {
            txtProfName.setBorder(new LineBorder(Color.lightGray));
        }
        int id = -1;
        double creds = -1;
        try {
            id = Integer.parseInt(txtProfGeneratedID.getText());
            creds = Double.parseDouble(spinnerCreditsAssigned.getValue().toString());
        } catch (NumberFormatException n) {
            System.err.println(n.getMessage());
        }
        Object[] taught = currentProfessor.getCoursesTaught();
        if (profList.containsKey(txtProfName.getText())) {
            currentProfessor = profList.get(txtProfName.getText());
        } else {
            currentProfessor = new Professor(id, txtProfName.getText(), creds);
        }
        for (Object o : taught) {
            if (!currentProfessor.hasCourse(o.toString())) {
                currentProfessor.addCourseTaught(o.toString());
            }
        }
        if (currentProfessor.getCredits() != creds) {
            currentProfessor.setCredits(creds);
        }
        int[] prefsSelected = new int[3];
        if (chkProfNoPreference.isSelected()) {
            prefsSelected[0] = -1;
            prefsSelected[1] = -1;
            prefsSelected[2] = -1;
        } else {
            if (cbProfHighestMorning.isSelected()) {
                prefsSelected[0] = 0;
            } else if (cbProfNormalMorning.isSelected()) {
                prefsSelected[0] = 1;
            } else if (cbProfLeastMorning.isSelected()) {
                prefsSelected[0] = 2;
            } else if (cbProfNA_Morning.isSelected()) {
                prefsSelected[0] = Integer.MAX_VALUE;
            }

            if (cbProfHighestAfternoon.isSelected()) {
                prefsSelected[1] = 0;
            } else if (cbProfNormalAfternoon.isSelected()) {
                prefsSelected[1] = 1;
            } else if (cbProfLeastAfternoon.isSelected()) {
                prefsSelected[1] = 2;
            } else if (cbProfNA_Afternoon.isSelected()) {
                prefsSelected[1] = Integer.MAX_VALUE;
            }

            if (cbProfHighestEvening.isSelected()) {
                prefsSelected[0] = 0;
            } else if (cbProfNormalEvening.isSelected()) {
                prefsSelected[0] = 1;
            } else if (cbProfLeastEvening.isSelected()) {
                prefsSelected[0] = 2;
            } else if (cbProfNA_Evening.isSelected()) {
                prefsSelected[0] = Integer.MAX_VALUE;
            }
        }
        currentProfessor.setPreference(prefsSelected);

        profList.put(currentProfessor.getProfName(), currentProfessor);
        if (!profListData.contains(currentProfessor.getProfName())) {
            profListData.addElement(currentProfessor.getProfName());
            Collections.sort(profListData);
            listProfs.setListData(profListData);
            spProfList.revalidate();
            spProfList.repaint();
        }

        updateProfessorIDs();
    }//GEN-LAST:event_btnSaveProfActionPerformed

    private void btnDeleteProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProfActionPerformed
        if (currentProfessor != null) {
            profList.remove(currentProfessor.getProfName());
            profListData.removeElement(currentProfessor.getProfName());
            listProfs.setListData(profListData);
            spProfList.revalidate();
            spProfList.repaint();
        }
    }//GEN-LAST:event_btnDeleteProfActionPerformed

    private void cbProfCourseTaughtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfCourseTaughtActionPerformed
        if (cbProfCourseTaught != null && cbProfCourseTaught.getSelectedIndex() >= 0) {
            String selectedCourse = cbProfCourseTaught.getSelectedItem().toString();
            if (currentProfessor != null) {
                if (currentProfessor.hasCourse(selectedCourse)) {
                    btnAddCourseTaught.setText("Remove");
                } else {
                    btnAddCourseTaught.setText("Add");
                }
            }
        }
    }//GEN-LAST:event_cbProfCourseTaughtActionPerformed

    private void btnAddCourseTaughtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCourseTaughtActionPerformed
        String courseTaught = cbProfCourseTaught.getSelectedItem().toString();
        if (currentProfessor == null) {
            currentProfessor = new Professor(-1, "Temp", -1);
        }
        if (btnAddCourseTaught.getText().equals("Add")) {
            currentProfessor.addCourseTaught(courseTaught);
            btnAddCourseTaught.setText("Remove");
        } else {
            currentProfessor.removeCourseTaught(courseTaught);
            btnAddCourseTaught.setText("Add");
        }
        Object[] temp = currentProfessor.getCoursesTaught();
        Arrays.sort(temp);
        listCourseTaught.setListData(temp);
        spCourseTaughtList.revalidate();
        spCourseTaughtList.repaint();
    }//GEN-LAST:event_btnAddCourseTaughtActionPerformed

    private void listProfsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listProfsValueChanged
        if (listProfs.getSelectedIndex() >= 0) {
            chkProfNoPreference.setSelected(false);
            cbProfHighestMorning.setSelected(false);
            cbProfHighestAfternoon.setSelected(false);
            cbProfHighestEvening.setSelected(false);
            cbProfNormalMorning.setSelected(false);
            cbProfNormalAfternoon.setSelected(false);
            cbProfNormalEvening.setSelected(false);
            cbProfLeastMorning.setSelected(false);
            cbProfLeastAfternoon.setSelected(false);
            cbProfLeastEvening.setSelected(false);
            cbProfNA_Morning.setSelected(false);
            cbProfNA_Afternoon.setSelected(false);
            cbProfNA_Evening.setSelected(false);

            currentProfessor = profList.get(listProfs.getSelectedValue());
            txtProfGeneratedID.setText(String.valueOf(currentProfessor.getProfID()));
            txtProfName.setText(currentProfessor.getProfName());
            spinnerCreditsAssigned.setValue(currentProfessor.getCredits());

            switch (currentProfessor.getPreference()[0]) {
                case 100:
                    cbProfNA_Morning.setSelected(true);
                    break;
                case 2:
                    cbProfLeastMorning.setSelected(true);
                    break;
                case 1:
                    cbProfNormalMorning.setSelected(true);
                    break;
                case 0:
                    cbProfHighestMorning.setSelected(true);
                    break;
                case -1:
                    chkProfNoPreference.setSelected(true);
                    break;
            }

            switch (currentProfessor.getPreference()[1]) {
                case 100:
                    cbProfNA_Afternoon.setSelected(true);
                    break;
                case 2:
                    cbProfLeastAfternoon.setSelected(true);
                    break;
                case 1:
                    cbProfNormalAfternoon.setSelected(true);
                    break;
                case 0:
                    cbProfHighestAfternoon.setSelected(true);
                    break;
                case -1:
                    chkProfNoPreference.setSelected(true);
                    break;
            }

            switch (currentProfessor.getPreference()[2]) {
                case 100:
                    cbProfNA_Evening.setSelected(true);
                    break;
                case 2:
                    cbProfLeastEvening.setSelected(true);
                    break;
                case 1:
                    cbProfNormalEvening.setSelected(true);
                    break;
                case 0:
                    cbProfHighestEvening.setSelected(true);
                    break;
                case -1:
                    chkProfNoPreference.setSelected(true);
                    break;
            }
            Object[] temp = currentProfessor.getCoursesTaught();
            Arrays.sort(temp);
            listCourseTaught.setListData(temp);
            cbProfCourseTaught.setEnabled(true);
            btnAddCourseTaught.setEnabled(true);
            btnAddCourseTaught.setText("Add");
            cbProfCourseTaught.removeAllItems();
            Collections.sort(courseListData);
            for (Object s : courseListData) {
                cbProfCourseTaught.addItem(s.toString());
            }

            updateProfTimePreferenceBoxes();
        }
    }//GEN-LAST:event_listProfsValueChanged

    private void btnNewCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewCourseActionPerformed
        currentCourse = null;
        txtCourseCreditValue.setText("");
        int newID = courseList.size();
        boolean switchSides = false;
        while (courseIDs.contains(newID)) {
            if (!switchSides) {
                newID--;
            } else {
                newID++;
            }
            if (newID == -1) {
                switchSides = true;
                newID = courseList.size() + 1;
            }
        }
        txtCourseGeneratedID.setText(String.valueOf(newID));
        txtCourseID.setText("");
        txtCourseTitle.setText("");
        spinnerSections.setValue(0);
        dropIncompCourses.setEnabled(false);
        btnModifyIncomp.setEnabled(false);
        chkCourseNoPreference.setSelected(false);
        cbCourseHighestMorning.setSelected(false);
        cbCourseHighestAfternoon.setSelected(false);
        cbCourseHighestEvening.setSelected(false);
        cbCourseNormalMorning.setSelected(false);
        cbCourseNormalAfternoon.setSelected(false);
        cbCourseNormalEvening.setSelected(false);
        cbCourseLeastMorning.setSelected(false);
        cbCourseLeastAfternoon.setSelected(false);
        cbCourseLeastEvening.setSelected(false);
        cbCourseNA_Morning.setSelected(false);
        cbCourseNA_Afternoon.setSelected(false);
        cbCourseNA_Evening.setSelected(false);
        dropIncompCourses.removeAllItems();
        Iterator it = courseList.keySet().iterator();
        while (it.hasNext()) {
            dropIncompCourses.addItem(it.next().toString());
        }
        listIncompCourses.setListData(new Vector<>());
    }//GEN-LAST:event_btnNewCourseActionPerformed

    private void listCoursesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listCoursesValueChanged
        if (listCourses.getSelectedIndex() >= 0) {
            chkCourseNoPreference.setSelected(false);
            cbCourseHighestMorning.setSelected(false);
            cbCourseHighestAfternoon.setSelected(false);
            cbCourseHighestEvening.setSelected(false);
            cbCourseNormalMorning.setSelected(false);
            cbCourseNormalAfternoon.setSelected(false);
            cbCourseNormalEvening.setSelected(false);
            cbCourseLeastMorning.setSelected(false);
            cbCourseLeastAfternoon.setSelected(false);
            cbCourseLeastEvening.setSelected(false);
            cbCourseNA_Morning.setSelected(false);
            cbCourseNA_Afternoon.setSelected(false);
            cbCourseNA_Evening.setSelected(false);
            currentCourse = courseList.get(listCourses.getSelectedValue());
            txtCourseGeneratedID.setText(String.valueOf(currentCourse.getGeneratedID()));
            txtCourseTitle.setText(currentCourse.getTitle());
            txtCourseID.setText(currentCourse.getID());
            txtCourseCreditValue.setText(String.valueOf(currentCourse.getCreditValue()));
            spinnerSections.setValue(currentCourse.getSectionCount());
            switch (currentCourse.getPreferences()[0]) {
                case 100:
                    cbCourseNA_Morning.setSelected(true);
                    break;
                case 2:
                    cbCourseLeastMorning.setSelected(true);
                    break;
                case 1:
                    cbCourseNormalMorning.setSelected(true);
                    break;
                case 0:
                    cbCourseHighestMorning.setSelected(true);
                    break;
                case -1:
                    chkCourseNoPreference.setSelected(true);
                    break;
            }
            switch (currentCourse.getPreferences()[1]) {
                case 100:
                    cbCourseNA_Afternoon.setSelected(true);
                    break;
                case 2:
                    cbCourseLeastAfternoon.setSelected(true);
                    break;
                case 1:
                    cbCourseNormalAfternoon.setSelected(true);
                    break;
                case 0:
                    cbCourseHighestAfternoon.setSelected(true);
                    break;
                case -1:
                    chkCourseNoPreference.setSelected(true);
                    break;
            }
            switch (currentCourse.getPreferences()[2]) {
                case 100:
                    cbCourseNA_Evening.setSelected(true);
                    break;
                case 2:
                    cbCourseLeastEvening.setSelected(true);
                    break;
                case 1:
                    cbCourseNormalEvening.setSelected(true);
                    break;
                case 0:
                    cbCourseHighestEvening.setSelected(true);
                    break;
                case -1:
                    chkCourseNoPreference.setSelected(true);
                    break;
            }

            Vector<String> temp = currentCourse.getIncompCourses();
            Collections.sort(temp);
            listIncompCourses.setListData(temp);
            dropIncompCourses.setEnabled(true);
            btnModifyIncomp.setEnabled(true);
            btnModifyIncomp.setText("Add");
            dropIncompCourses.removeAllItems();
            courseListData.stream().filter((s) -> (!s.equals(currentCourse.getID()))).forEach((s) -> {
                dropIncompCourses.addItem(s);
            });
            updateCourseTimePreferenceBoxes();
        }
    }//GEN-LAST:event_listCoursesValueChanged

    private void btnDeleteCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCourseActionPerformed
        if (currentCourse != null) {
            Vector<String> incomps = currentCourse.getIncompCourses();
            for (String s : incomps) {
                courseList.get(s).removeIncompatibleCourse(currentCourse.getID());
            }
            courseListData.remove(currentCourse.getID());
            courseList.remove(currentCourse.getID());
            listCourses.setListData(courseListData);
            spCourseList.revalidate();
            spCourseList.repaint();

            for (int i = 1; i <= currentCourse.getSectionCount(); i++) {
                courseSectionListData.removeElement(currentCourse.getID() + "(" + String.valueOf(i) + ")");
                unscheduledCourses.removeElement(currentCourse.getID() + "(" + String.valueOf(i) + ")");
                scheduledCoursesList.remove(currentCourse.getID() + "(" + String.valueOf(i) + ")");
            }

            currentCourse = null;
        }
    }//GEN-LAST:event_btnDeleteCourseActionPerformed

    private void btnModifyIncompActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyIncompActionPerformed
        String incompCourse = dropIncompCourses.getSelectedItem().toString();
        if (btnModifyIncomp.getText().equals("Add")) {
            currentCourse.addIncompatibleCourse(incompCourse);
            courseList.get(incompCourse).addIncompatibleCourse(currentCourse.getID());
            btnModifyIncomp.setText("Remove");
        } else {
            courseList.get(incompCourse).removeIncompatibleCourse(currentCourse.getID());
            currentCourse.removeIncompatibleCourse(incompCourse);
        }
        Vector<String> temp = currentCourse.getIncompCourses();
        Collections.sort(temp);
        listIncompCourses.setListData(temp);
        spCourseIncomp.revalidate();
        spCourseIncomp.repaint();
    }//GEN-LAST:event_btnModifyIncompActionPerformed

    private void dropIncompCoursesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropIncompCoursesActionPerformed
        if (dropIncompCourses != null && dropIncompCourses.getSelectedIndex() >= 0) {
            String selectedIncomp = dropIncompCourses.getSelectedItem().toString();
            if (currentCourse != null) {
                if (currentCourse.hasIncomp(selectedIncomp)) {
                    btnModifyIncomp.setText("Remove");
                } else {
                    btnModifyIncomp.setText("Add");
                }
            }
        }
    }//GEN-LAST:event_dropIncompCoursesActionPerformed

    private void btnSaveCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCourseActionPerformed
        if (!chkCourseNoPreference.isSelected()) {
            if (!(cbCourseHighestMorning.isSelected() || cbCourseNormalMorning.isSelected() || cbCourseLeastMorning.isSelected() || cbCourseNA_Morning.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for Morning.");
                return;
            }

            if (!(cbCourseHighestAfternoon.isSelected() || cbCourseNormalAfternoon.isSelected() || cbCourseLeastAfternoon.isSelected() || cbCourseNA_Afternoon.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for Afternoon.");
                return;
            }

            if (!(cbCourseHighestEvening.isSelected() || cbCourseNormalEvening.isSelected() || cbCourseLeastEvening.isSelected() || cbCourseNA_Evening.isSelected())) {
                JOptionPane.showMessageDialog(pnlContainer, "Please select a time preference for Evening.");
                return;
            }
        }

        if (txtCourseTitle.getText().isEmpty()) {
            txtCourseTitle.setBorder(new LineBorder(Color.red));
            JOptionPane.showMessageDialog(pnlContainer, "Please enter a course title.");
            return;
        } else {
            txtCourseTitle.setBorder(new LineBorder(Color.lightGray));
        }
        if (txtCourseID.getText().isEmpty()) {
            txtCourseID.setBorder((new LineBorder(Color.red)));
            JOptionPane.showMessageDialog(pnlContainer, "Please enter a course code.");
            return;
        } else {
            txtCourseID.setBorder((new LineBorder(Color.lightGray)));
        }
        if (txtCourseCreditValue.getText().isEmpty()) {
            txtCourseCreditValue.setBorder(new LineBorder(Color.red));
            JOptionPane.showMessageDialog(pnlContainer, "Please enter a course credit value.");
            return;
        } else {
            txtCourseCreditValue.setBorder(new LineBorder(Color.lightGray));
        }
        double credits = -1;
        int sections = -1;
        try {
            credits = Double.parseDouble(txtCourseCreditValue.getText());
            sections = Integer.parseInt(spinnerSections.getValue().toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(rootPane, e.getMessage());
        }

        int oldSectionCount = -1;
        boolean newCourse = false;
        if (courseList.containsKey(txtCourseID.getText().toLowerCase())) {
            currentCourse = courseList.get(txtCourseID.getText().toLowerCase());
            oldSectionCount = currentCourse.getSectionCount();
            if (!currentCourse.getTitle().equals(txtCourseTitle.getText())) {
                currentCourse.setTitle(txtCourseTitle.getText());
            }
            if (currentCourse.getCreditValue() != credits) {
                currentCourse.setCreditValue(credits);
            }
            if (currentCourse.getSectionCount() != sections) {
                currentCourse.setSectionCount(sections);
            }
        } else {
            newCourse = true;
            currentCourse = new Course(txtCourseID.getText(), txtCourseTitle.getText(), courseList.size(), credits, sections);
        }

        int highest = -1, normal = -1, least = -1;
        int[] prefSelected = new int[3]; //m-a-e
        if (!chkCourseNoPreference.isSelected()) {
            if (cbCourseHighestMorning.isSelected()) {
                prefSelected[0] = 0;
            } else if (cbCourseHighestAfternoon.isSelected()) {
                prefSelected[1] = 0;
            } else if (cbCourseHighestEvening.isSelected()) {
                prefSelected[2] = 0;
            }

            if (cbCourseNormalMorning.isSelected()) {
                prefSelected[0] = 1;
            } else if (cbCourseNormalAfternoon.isSelected()) {
                prefSelected[1] = 1;
            } else if (cbCourseNormalEvening.isSelected()) {
                prefSelected[2] = 1;
            }

            if (cbCourseLeastMorning.isSelected()) {
                prefSelected[0] = 2;
            } else if (cbCourseLeastAfternoon.isSelected()) {
                prefSelected[1] = 2;
            } else if (cbCourseLeastEvening.isSelected()) {
                prefSelected[2] = 2;
            }

            if (cbCourseNA_Morning.isSelected()) {
                prefSelected[0] = 100;
            } else if (cbCourseNA_Afternoon.isSelected()) {
                prefSelected[1] = 100;
            } else if (cbCourseNA_Evening.isSelected()) {
                prefSelected[2] = 100;
            }
        } else {
            prefSelected[0] = -1;
            prefSelected[1] = -1;
            prefSelected[2] = -1;
        }
        currentCourse.setPreferences(prefSelected);
        courseList.put(currentCourse.getID(), currentCourse);
        if (!courseListData.contains(currentCourse.getID())) {
            courseListData.addElement(currentCourse.getID());
            Collections.sort(courseListData);
            listCourses.setListData(courseListData);
            spCourseList.revalidate();
            spCourseList.repaint();
        }
        //btnNewCourseActionPerformed(null);
        dropIncompCourses.setEnabled(true);
        btnModifyIncomp.setEnabled(true);
        btnModifyIncomp.setText("Add");
        dropIncompCourses.removeAllItems();
        Iterator it = courseList.keySet().iterator();
        while (it.hasNext()) {
            String inc = it.next().toString();
            if (!inc.equals(currentCourse.getID())) {
                dropIncompCourses.addItem(inc);
            }
        }

        if (oldSectionCount > sections) {
            for (int i = oldSectionCount; i > sections; i--) {
                String elementID = currentCourse.getID() + "(" + String.valueOf(i) + ")";
                courseSectionListData.removeElement(elementID);
                unscheduledCourses.removeElement(elementID);
                RemoveSection(elementID);
                int row = FindRowInSchedule(elementID);
                if (row != -1) {
                    dtm.removeRow(row);
                }

            }
        }
        if ((newCourse && sections > 0) || (oldSectionCount < sections)) {
            int i = 1;
            if (oldSectionCount < sections) {
                i = oldSectionCount + 1;
            }
            for (; i <= sections; i++) {
                String elementID = currentCourse.getID() + "(" + String.valueOf(i) + ")";
                courseSectionListData.addElement(elementID);
                unscheduledCourses.addElement(elementID);
            }
            Collections.sort(courseSectionListData);
            Collections.sort(unscheduledCourses);
        }

        if (newCourse) {
            updateCourseGeneratedIDs();
        }
    }//GEN-LAST:event_btnSaveCourseActionPerformed

    private void txtCourseCreditValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCourseCreditValueKeyTyped

    }//GEN-LAST:event_txtCourseCreditValueKeyTyped

    private void txtCourseTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCourseTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCourseTitleActionPerformed

    private void txtResultPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtResultPathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtResultPathActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

    }//GEN-LAST:event_formComponentResized

    private void pnlScheduleComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlScheduleComponentResized

    }//GEN-LAST:event_pnlScheduleComponentResized

    private void btnBrowseResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseResultActionPerformed
        JFileChooser fc = new JFileChooser();
        if (!txtResultPath.getText().isEmpty()) {
            fc = new JFileChooser(new File(txtResultPath.getText()));
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter("Result File", new String[]{"rlt"}));
        int returnVal = fc.showOpenDialog(pnlContainer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = fc.getSelectedFile().getAbsolutePath();
            if (!fileName.endsWith(".rlt")) {
                fileName += ".rlt";
            }
            txtResultPath.setText(fileName);
            //btnOpenResult.setEnabled(true);
            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader(txtResultPath.getText()));
                String currentLine;
                boolean startReading = false;
                while ((currentLine = br.readLine()) != null) {
                    if (currentLine.equals("**BEGINRESULT**")) {
                        startReading = true;
                    } else if (currentLine.equals("**ENDRESULT**")) {
                        break;
                    } else if (startReading) {
                        String[] tuple = currentLine.split(",");
                        int section = Integer.parseInt(tuple[0].trim());
                        int profID = Integer.parseInt(tuple[1].trim());
                        int timeID = Integer.parseInt(tuple[2].trim());
                        Schedule sc = new Schedule();
                        sc.course = sectionLookup.get(section);
                        sc.prof = profLookup.get(profID);
                        sc.time = timeslotLookup.get(timeID);
                        resultListBySections.put(sc.course, sc);
                        String course = sc.course.substring(0, sc.course.indexOf("("));
                        ArrayList<Schedule> ass;
                        if (resultListByCourses.containsKey(course)) {
                            ass = resultListByCourses.get(course);
                            ass.add(sc);
                        } else {
                            ass = new ArrayList<>();
                            ass.add(sc);
                        }
                        resultListByCourses.put(course, ass);

                        if (resultListByProfessor.containsKey(sc.prof)) {
                            ass = resultListByProfessor.get(sc.prof);
                            ass.add(sc);
                        } else {
                            ass = new ArrayList<>();
                            ass.add(sc);
                        }
                        resultListByProfessor.put(sc.prof, ass);
                    }
                }
                rdoCourse.setEnabled(true);
                rdoProfessor.setEnabled(true);
                listViewBySelection.setEnabled(true);
                //cbScheduleSection.setEnabled(false);
                cbProfessorSelection.setEnabled(true);
                cbTimeSelection.setEnabled(true);
                btnStatistics.setEnabled(true);
                btnResultChangeUpdate.setEnabled(true);
                btnGenerateResult.setEnabled(true);
                if (incompatibleSectionList.isEmpty()) {
                    incompatibleSectionList = GenerateIncompatibleSectionArray();
                }
                String validation = validateSchedule(true);
                if (validation.isEmpty()) {
                    String[] options = {"OK"};
                    String message = "<html>The result schedule is valid.</html>";
                    int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Schedule Validation", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "OK");
                    txtResultStatus.setText("Schedule: VALID.");
                } else {
                    String[] options = {"OK"};
                    String message = "<html>The result schedule is not valid due to:" + validation + "</html>";
                    int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Schedule Validation", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "OK");
                    txtResultStatus.setText("Schedule: INVALID.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }//GEN-LAST:event_btnBrowseResultActionPerformed

    private void rdoProfessorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rdoProfessorItemStateChanged
        if (rdoProfessor.isSelected()) {
            listViewBySelection.setListData(profListData);
        } else {
            listViewBySelection.setListData(courseListData);
        }

        spViewBySelection.revalidate();
        spViewBySelection.repaint();
    }//GEN-LAST:event_rdoProfessorItemStateChanged

    private void listViewBySelectionValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listViewBySelectionValueChanged
        if (listViewBySelection.getSelectedIndex() >= 0) {
            if (rdoCourse.isSelected()) {
                cbScheduleSection.removeAllItems();
                for (Object s : courseSectionListData) {
                    cbScheduleSection.addItem(s.toString());
                }
                String course = listViewBySelection.getSelectedValue();
                String section = resultListByCourses.get(listViewBySelection.getSelectedValue()).get(0).course;
                cbScheduleSection.setSelectedItem(section);
                cbProfessorSelection.setSelectedItem(resultListBySections.get(section).prof);
                cbTimeSelection.setSelectedItem(resultListBySections.get(section).time);
                UpdateResultsView(course, true);
            } else {
                cbProfessorSelection.removeAllItems();
                cbProfessorSelection.addItem(listViewBySelection.getSelectedValue());
                cbProfessorSelection.setSelectedItem(listViewBySelection.getSelectedValue());
                ArrayList<Schedule> coursesTaught = resultListByProfessor.get(listViewBySelection.getSelectedValue());
                cbScheduleSection.removeAllItems();
                cbTimeSelection.removeAllItems();
                for (Schedule s : coursesTaught) {
                    cbScheduleSection.addItem(s.course);
                }
                UpdateResultsView(listViewBySelection.getSelectedValue(), false);
            }
        }
    }//GEN-LAST:event_listViewBySelectionValueChanged

    private void cbScheduleSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbScheduleSectionActionPerformed
        if (cbScheduleSection.getSelectedIndex() >= 0) {
            String course = cbScheduleSection.getSelectedItem().toString();
            course = course.substring(0, course.indexOf("("));
            if (courseList.get(course) != null) {
                cbProfessorSelection.removeAllItems();
                for (Object s : profListData) {
                    if (profList.get(s.toString()).hasCourse(course)) {
                        cbProfessorSelection.addItem(s.toString());
                    }
                }

                double credvalue = courseList.get(course).getCreditValue();
                cbTimeSelection.removeAllItems();
                for (Iterator it = timeslotListData.iterator(); it.hasNext();) {
                    Object s = it.next();
                    if (timeslotList.get(s.toString()).getCredits() == credvalue) {
                        cbTimeSelection.addItem(s.toString());
                    }
                }
            }
        }
    }//GEN-LAST:event_cbScheduleSectionActionPerformed

    private void btnResultChangeUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResultChangeUpdateActionPerformed
        if (!btnResultChangeUndo.isEnabled()) {
            btnResultChangeUndo.setEnabled(true);
        }
        ScheduleReplace redo = new ScheduleReplace();
        Schedule current = resultListBySections.get(cbScheduleSection.getSelectedItem().toString()), updated = new Schedule();

        updated.course = current.course;
        updated.prof = cbProfessorSelection.getSelectedItem().toString();
        updated.time = cbTimeSelection.getSelectedItem().toString();

        redo.original = current;
        redo.updated = updated;
        undoList.addLast(redo);
        String course = current.course;
        course = course.substring(0, course.indexOf("("));
        ArrayList<Schedule> ass = resultListByCourses.get(course);
        ass.remove(current);
        ass.add(updated);
        resultListByCourses.put(course, ass);

        ass = resultListByProfessor.get(current.prof);
        ass.remove(current);
        resultListByProfessor.put(current.prof, ass);
        ass = resultListByProfessor.get(updated.prof);
        ass.add(updated);
        resultListByProfessor.put(updated.prof, ass);

        resultListBySections.put(updated.course, updated);

        String validation = validateSchedule(true);

        if (!validation.isEmpty()) {
            String[] options = {"Yes", "No"};
            String message = "<html>The initial schedule is not valid due to:<br />" + validation + "<br />Continue with the change?</html>";
            int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Invalid Schedule", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "No");
            if (choice == 1) {
                btnResultChangeUndoActionPerformed(evt);
                return;
            }
        }

        UpdateResultsView();


    }//GEN-LAST:event_btnResultChangeUpdateActionPerformed

    private void UpdateResultsView() {
        UpdateResultsView(resultKey, resultCourse);
    }

    private void btnResultChangeUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResultChangeUndoActionPerformed
        ScheduleReplace undo = undoList.removeLast();
        if (undoList.isEmpty()) {
            btnResultChangeUndo.setEnabled(false);
        }

        resultListBySections.put(undo.original.course, undo.original);

        ArrayList<Schedule> ass = resultListByProfessor.get(undo.updated.prof);
        ass.remove(undo.updated);
        resultListByProfessor.put(undo.updated.prof, ass);
        ass = resultListByProfessor.get(undo.original.prof);
        if (!ass.contains(undo.original)) {
            ass.add(undo.original);
        }
        resultListByProfessor.put(undo.original.prof, ass);

        String courseID_original = undo.original.course;
        courseID_original = courseID_original.substring(0, courseID_original.indexOf("("));
        String courseID_updated = undo.updated.course;
        courseID_updated = courseID_updated.substring(0, courseID_updated.indexOf("("));
        ass = resultListByCourses.get(courseID_updated);
        ass.remove(undo.updated);
        resultListByCourses.put(courseID_updated, ass);
        ass = resultListByCourses.get(courseID_original);
        ass.add(undo.original);
        resultListByCourses.put(courseID_original, ass);

        UpdateResultsView();
    }//GEN-LAST:event_btnResultChangeUndoActionPerformed

    private void btnValidateInitialScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidateInitialScheduleActionPerformed
        String validation = validateSchedule(false);
        if (validation.isEmpty()) {
            String[] options = {"OK"};
            String message = "<html>The initial schedule is valid.</html>";
            int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Schedule Validation", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "OK");
        } else {
            String[] options = {"OK"};
            String message = "<html>The initial schedule is not valid due to:" + validation + "</html>";
            int choice = JOptionPane.showOptionDialog(pnlContainer, message, "Schedule Validation", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "OK");
        }
    }//GEN-LAST:event_btnValidateInitialScheduleActionPerformed

    private void chkCourseNoPreferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkCourseNoPreferenceActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_chkCourseNoPreferenceActionPerformed

    private void cbCourseHighestMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseHighestMorningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseHighestMorningActionPerformed

    private void cbCourseNormalMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNormalMorningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNormalMorningActionPerformed

    private void cbCourseLeastMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseLeastMorningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseLeastMorningActionPerformed

    private void cbCourseNA_MorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNA_MorningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNA_MorningActionPerformed

    private void cbCourseHighestAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseHighestAfternoonActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseHighestAfternoonActionPerformed

    private void cbCourseHighestEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseHighestEveningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseHighestEveningActionPerformed

    private void cbCourseNormalAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNormalAfternoonActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNormalAfternoonActionPerformed

    private void cbCourseNormalEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNormalEveningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNormalEveningActionPerformed

    private void cbCourseLeastAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseLeastAfternoonActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseLeastAfternoonActionPerformed

    private void cbCourseLeastEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseLeastEveningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseLeastEveningActionPerformed

    private void cbCourseNA_AfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNA_AfternoonActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNA_AfternoonActionPerformed

    private void cbCourseNA_EveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCourseNA_EveningActionPerformed
        updateCourseTimePreferenceBoxes();
    }//GEN-LAST:event_cbCourseNA_EveningActionPerformed

    private void chkProfNoPreferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkProfNoPreferenceActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_chkProfNoPreferenceActionPerformed

    private void cbProfHighestMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfHighestMorningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfHighestMorningActionPerformed

    private void cbProfHighestAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfHighestAfternoonActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfHighestAfternoonActionPerformed

    private void cbProfHighestEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfHighestEveningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfHighestEveningActionPerformed

    private void cbProfNormalMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNormalMorningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNormalMorningActionPerformed

    private void cbProfNormalAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNormalAfternoonActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNormalAfternoonActionPerformed

    private void cbProfNormalEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNormalEveningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNormalEveningActionPerformed

    private void cbProfLeastMorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfLeastMorningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfLeastMorningActionPerformed

    private void cbProfLeastAfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfLeastAfternoonActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfLeastAfternoonActionPerformed

    private void cbProfLeastEveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfLeastEveningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfLeastEveningActionPerformed

    private void cbProfNA_MorningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNA_MorningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNA_MorningActionPerformed

    private void cbProfNA_AfternoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNA_AfternoonActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNA_AfternoonActionPerformed

    private void cbProfNA_EveningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbProfNA_EveningActionPerformed
        updateProfTimePreferenceBoxes();
    }//GEN-LAST:event_cbProfNA_EveningActionPerformed

    private void miOpenConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenConfigActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_miOpenConfigActionPerformed

    private void miSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveConfigActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_miSaveConfigActionPerformed

    private void updateCourseTimePreferenceBoxes() {
        if (chkCourseNoPreference.isSelected()) {
            cbCourseHighestMorning.setEnabled(false);
            cbCourseHighestAfternoon.setEnabled(false);
            cbCourseHighestEvening.setEnabled(false);
            cbCourseNormalMorning.setEnabled(false);
            cbCourseNormalAfternoon.setEnabled(false);
            cbCourseNormalEvening.setEnabled(false);
            cbCourseLeastMorning.setEnabled(false);
            cbCourseLeastAfternoon.setEnabled(false);
            cbCourseLeastEvening.setEnabled(false);
            cbCourseNA_Morning.setEnabled(false);
            cbCourseNA_Afternoon.setEnabled(false);
            cbCourseNA_Evening.setEnabled(false);
        } else {
            if (!cbCourseHighestAfternoon.isSelected() && !cbCourseHighestEvening.isSelected() && !cbCourseNormalMorning.isSelected()
                    && !cbCourseLeastMorning.isSelected() && !cbCourseNA_Morning.isSelected()) {
                cbCourseHighestMorning.setEnabled(true);
            } else {
                cbCourseHighestMorning.setEnabled(false);
            }

            if (!cbCourseHighestMorning.isSelected() && !cbCourseHighestEvening.isSelected() && !cbCourseNormalAfternoon.isSelected()
                    && !cbCourseLeastAfternoon.isSelected() && !cbCourseNA_Afternoon.isSelected()) {
                cbCourseHighestAfternoon.setEnabled(true);
            } else {
                cbCourseHighestAfternoon.setEnabled(false);
            }

            if (!cbCourseHighestAfternoon.isSelected() && !cbCourseHighestMorning.isSelected() && !cbCourseNormalEvening.isSelected()
                    && !cbCourseLeastEvening.isSelected() && !cbCourseNA_Evening.isSelected()) {
                cbCourseHighestEvening.setEnabled(true);
            } else {
                cbCourseHighestEvening.setEnabled(false);
            }

            if (!cbCourseNormalAfternoon.isSelected() && !cbCourseNormalEvening.isSelected() && !cbCourseHighestMorning.isSelected()
                    && !cbCourseLeastMorning.isSelected() && !cbCourseNA_Morning.isSelected()) {
                cbCourseNormalMorning.setEnabled(true);
            } else {
                cbCourseNormalMorning.setEnabled(false);
            }

            if (!cbCourseNormalMorning.isSelected() && !cbCourseNormalEvening.isSelected() && !cbCourseHighestAfternoon.isSelected()
                    && !cbCourseLeastAfternoon.isSelected() && !cbCourseNA_Afternoon.isSelected()) {
                cbCourseNormalAfternoon.setEnabled(true);
            } else {
                cbCourseNormalAfternoon.setEnabled(false);
            }

            if (!cbCourseNormalAfternoon.isSelected() && !cbCourseNormalMorning.isSelected() && !cbCourseHighestEvening.isSelected()
                    && !cbCourseLeastEvening.isSelected() && !cbCourseNA_Evening.isSelected()) {
                cbCourseNormalEvening.setEnabled(true);
            } else {
                cbCourseNormalEvening.setEnabled(false);
            }

            if (!cbCourseLeastAfternoon.isSelected() && !cbCourseLeastEvening.isSelected() && !cbCourseNormalMorning.isSelected()
                    && !cbCourseHighestMorning.isSelected() && !cbCourseNA_Morning.isSelected()) {
                cbCourseLeastMorning.setEnabled(true);
            } else {
                cbCourseLeastMorning.setEnabled(false);
            }

            if (!cbCourseLeastMorning.isSelected() && !cbCourseLeastEvening.isSelected() && !cbCourseNormalAfternoon.isSelected()
                    && !cbCourseHighestAfternoon.isSelected() && !cbCourseNA_Afternoon.isSelected()) {
                cbCourseLeastAfternoon.setEnabled(true);
            } else {
                cbCourseLeastAfternoon.setEnabled(false);
            }

            if (!cbCourseLeastAfternoon.isSelected() && !cbCourseLeastMorning.isSelected() && !cbCourseNormalEvening.isSelected()
                    && !cbCourseHighestEvening.isSelected() && !cbCourseNA_Evening.isSelected()) {
                cbCourseLeastEvening.setEnabled(true);
            } else {
                cbCourseLeastEvening.setEnabled(false);
            }

            if (!cbCourseNA_Afternoon.isSelected() && !cbCourseNA_Evening.isSelected() && !cbCourseNormalMorning.isSelected()
                    && !cbCourseLeastMorning.isSelected() && !cbCourseHighestMorning.isSelected()) {
                cbCourseNA_Morning.setEnabled(true);
            } else {
                cbCourseNA_Morning.setEnabled(false);
            }

            if (!cbCourseNA_Morning.isSelected() && !cbCourseNA_Evening.isSelected() && !cbCourseNormalAfternoon.isSelected()
                    && !cbCourseLeastAfternoon.isSelected() && !cbCourseHighestAfternoon.isSelected()) {
                cbCourseNA_Afternoon.setEnabled(true);
            } else {
                cbCourseNA_Afternoon.setEnabled(false);
            }

            if (!cbCourseNA_Afternoon.isSelected() && !cbCourseNA_Morning.isSelected() && !cbCourseNormalEvening.isSelected()
                    && !cbCourseLeastEvening.isSelected() && !cbCourseHighestEvening.isSelected()) {
                cbCourseNA_Evening.setEnabled(true);
            } else {
                cbCourseNA_Evening.setEnabled(false);
            }
        }
    }

    private void updateProfTimePreferenceBoxes() {
        if (chkProfNoPreference.isSelected()) {
            cbProfHighestMorning.setEnabled(false);
            cbProfHighestAfternoon.setEnabled(false);
            cbProfHighestEvening.setEnabled(false);
            cbProfNormalMorning.setEnabled(false);
            cbProfNormalAfternoon.setEnabled(false);
            cbProfNormalEvening.setEnabled(false);
            cbProfLeastMorning.setEnabled(false);
            cbProfLeastAfternoon.setEnabled(false);
            cbProfLeastEvening.setEnabled(false);
            cbProfNA_Morning.setEnabled(false);
            cbProfNA_Afternoon.setEnabled(false);
            cbProfNA_Evening.setEnabled(false);
        } else {
            if (!cbProfHighestAfternoon.isSelected() && !cbProfHighestEvening.isSelected() && !cbProfNormalMorning.isSelected()
                    && !cbProfLeastMorning.isSelected() && !cbProfNA_Morning.isSelected()) {
                cbProfHighestMorning.setEnabled(true);
            } else {
                cbProfHighestMorning.setEnabled(false);
            }

            if (!cbProfHighestMorning.isSelected() && !cbProfHighestEvening.isSelected() && !cbProfNormalAfternoon.isSelected()
                    && !cbProfLeastAfternoon.isSelected() && !cbProfNA_Afternoon.isSelected()) {
                cbProfHighestAfternoon.setEnabled(true);
            } else {
                cbProfHighestAfternoon.setEnabled(false);
            }

            if (!cbProfHighestAfternoon.isSelected() && !cbProfHighestMorning.isSelected() && !cbProfNormalEvening.isSelected()
                    && !cbProfLeastEvening.isSelected() && !cbProfNA_Evening.isSelected()) {
                cbProfHighestEvening.setEnabled(true);
            } else {
                cbProfHighestEvening.setEnabled(false);
            }

            if (!cbProfNormalAfternoon.isSelected() && !cbProfNormalEvening.isSelected() && !cbProfHighestMorning.isSelected()
                    && !cbProfLeastMorning.isSelected() && !cbProfNA_Morning.isSelected()) {
                cbProfNormalMorning.setEnabled(true);
            } else {
                cbProfNormalMorning.setEnabled(false);
            }

            if (!cbProfNormalMorning.isSelected() && !cbProfNormalEvening.isSelected() && !cbProfHighestAfternoon.isSelected()
                    && !cbProfLeastAfternoon.isSelected() && !cbProfNA_Afternoon.isSelected()) {
                cbProfNormalAfternoon.setEnabled(true);
            } else {
                cbProfNormalAfternoon.setEnabled(false);
            }

            if (!cbProfNormalAfternoon.isSelected() && !cbProfNormalMorning.isSelected() && !cbProfHighestEvening.isSelected()
                    && !cbProfLeastEvening.isSelected() && !cbProfNA_Evening.isSelected()) {
                cbProfNormalEvening.setEnabled(true);
            } else {
                cbProfNormalEvening.setEnabled(false);
            }

            if (!cbProfLeastAfternoon.isSelected() && !cbProfLeastEvening.isSelected() && !cbProfNormalMorning.isSelected()
                    && !cbProfHighestMorning.isSelected() && !cbProfNA_Morning.isSelected()) {
                cbProfLeastMorning.setEnabled(true);
            } else {
                cbProfLeastMorning.setEnabled(false);
            }

            if (!cbProfLeastMorning.isSelected() && !cbProfLeastEvening.isSelected() && !cbProfNormalAfternoon.isSelected()
                    && !cbProfHighestAfternoon.isSelected() && !cbProfNA_Afternoon.isSelected()) {
                cbProfLeastAfternoon.setEnabled(true);
            } else {
                cbProfLeastAfternoon.setEnabled(false);
            }

            if (!cbProfLeastAfternoon.isSelected() && !cbProfLeastMorning.isSelected() && !cbProfNormalEvening.isSelected()
                    && !cbProfHighestEvening.isSelected() && !cbProfNA_Evening.isSelected()) {
                cbProfLeastEvening.setEnabled(true);
            } else {
                cbProfLeastEvening.setEnabled(false);
            }

            if (!cbProfNA_Afternoon.isSelected() && !cbProfNA_Evening.isSelected() && !cbProfNormalMorning.isSelected()
                    && !cbProfLeastMorning.isSelected() && !cbProfHighestMorning.isSelected()) {
                cbProfNA_Morning.setEnabled(true);
            } else {
                cbProfNA_Morning.setEnabled(false);
            }

            if (!cbProfNA_Morning.isSelected() && !cbProfNA_Evening.isSelected() && !cbProfNormalAfternoon.isSelected()
                    && !cbProfLeastAfternoon.isSelected() && !cbProfHighestAfternoon.isSelected()) {
                cbProfNA_Afternoon.setEnabled(true);
            } else {
                cbProfNA_Afternoon.setEnabled(false);
            }

            if (!cbProfNA_Afternoon.isSelected() && !cbProfNA_Morning.isSelected() && !cbProfNormalEvening.isSelected()
                    && !cbProfLeastEvening.isSelected() && !cbProfHighestEvening.isSelected()) {
                cbProfNA_Evening.setEnabled(true);
            } else {
                cbProfNA_Evening.setEnabled(false);
            }
        }
    }

    private void updateCourseGeneratedIDs() {
        int arraySize = courseList.size();
        int leftID = 0, rightID = 0;
        HashMap<Integer, Course> updateList = new HashMap<>();
        TreeSet<Integer> ids = new TreeSet<>();
        for (String s : courseList.keySet()) {
            if (courseList.get(s).getGeneratedID() > rightID) {
                rightID = courseList.get(s).getGeneratedID();
                updateList.put(rightID, courseList.get(s));
            }
        }

        courseIDs.clear();
        courseList.clear();
        for (int i = 0; i < ids.size(); i++) {
            if (!ids.contains(i)) {
                Course c = updateList.get(ids.last());
                c.setGeneratedID(i);
                courseIDs.add(i);
                ids.remove(ids.last());
                courseList.put(c.getID(), c);
            }
        }
    }

    private void updateProfessorIDs() {
        int id = 0;
        HashMap<String, Professor> redoneList = new HashMap<>();
        for (String s : profList.keySet()) {
            Professor p = profList.get(s);
            p.setProfID(id++);
            redoneList.put(p.getProfName(), p);
        }
        profList.clear();
        profList.putAll(redoneList);
        id = 0;
        for (String s : profList.keySet()) {
            Professor p = profList.get(s);
            p.setProfID(id++);
            redoneList.put(p.getProfName(), p);
        }
        profList.clear();
        profList.putAll(redoneList);
    }

    private void updateTimeSlotIDs() {
        int id = 0;
        HashMap<String, TimeSlot> redoneList = new HashMap<>();
        for (String s : timeslotList.keySet()) {
            TimeSlot p = timeslotList.get(s);
            p.setID(id++);
            redoneList.put(p.toString(), p);
        }
        timeslotList.clear();
        timeslotList.putAll(redoneList);
        id = 0;
        for (String s : timeslotList.keySet()) {
            TimeSlot p = timeslotList.get(s);
            p.setID(id++);
            redoneList.put(p.toString(), p);
        }
        timeslotList.clear();
        timeslotList.putAll(redoneList);
    }

    private String validateSchedule(boolean isResult) {
        StringBuilder errors = new StringBuilder("<ul>");

        boolean invalid = false;
        int DELTA_MIN = 2, DELTA_MAX = -2;
        //First ensure that no professor is scheduled at the same time twice
        //And ensure that the professor is scheduled within +/-DELTA course load
        HashMap<String, ArrayList<String>> assignmentVerifier = new HashMap<>();
        HashMap<String, Double> profCreditsAssigned = new HashMap<>();
        ArrayList<Schedule> schedulesToValidate;

        if (isResult) {
            schedulesToValidate = new ArrayList<>(resultListBySections.values());
        } else {
            schedulesToValidate = new ArrayList<>(scheduledCoursesList.values());
        }

        for (Schedule s : schedulesToValidate) {
            if (assignmentVerifier.containsKey(s.prof) && assignmentVerifier.get(s.prof).contains(s.time)) {
                invalid = true;
                errors.append("<li>");
                errors.append("ERROR! Prof. ").append(s.prof).append(" is assigned at ").append(s.time).append(" twice!");
                errors.append("</li>");
            } else {
                if (!assignmentVerifier.containsKey(s.prof)) {
                    assignmentVerifier.put(s.prof, new ArrayList<>());
                }
                assignmentVerifier.get(s.prof).add(s.time);
            }
            if (profCreditsAssigned.containsKey(s.prof)) {
                String course = s.course;
                course = course.substring(0, course.indexOf("("));
                profCreditsAssigned.put(s.prof, profCreditsAssigned.get(s.prof) + courseList.get(course).getCreditValue());
            }
        }

        for (String s : profCreditsAssigned.keySet()) {
            double creditsAvailable = profList.get(s).getCredits();
            creditsAvailable -= profCreditsAssigned.get(s);
            if (creditsAvailable < DELTA_MAX || creditsAvailable > DELTA_MIN) {
                invalid = true;
                errors.append("<li>");
                errors.append("WARNING! Prof. ").append(s).append(" is scheduled for about ");
                if (creditsAvailable < DELTA_MAX) {
                    errors.append(String.valueOf(Math.ceil(Math.abs(creditsAvailable))));
                    errors.append(" more credits than allocated.");
                } else {
                    errors.append(String.valueOf(Math.ceil(Math.abs(creditsAvailable))));
                    errors.append(" less credits than allocated.");
                }
                errors.append("</li>");
            }
        }

        //Ensure that no incompatible sections are scheduled at the same time.
        if (incompatibleSectionList.isEmpty()) {
            incompatibleSectionList = GenerateIncompatibleSectionArray();
        }

        HashMap<String, HashSet<Integer>> incompChecklist = new HashMap<>();
        for (Schedule s : schedulesToValidate) {
            int currentSectionIndex = courseSectionListData.indexOf(s.course);
            if (!incompChecklist.containsKey(s.time)) {
                incompChecklist.put(s.time, new HashSet<>());
                incompChecklist.get(s.time).add(currentSectionIndex);
            } else {
                for (int i = 0; i < incompatibleSectionList.get(currentSectionIndex).size(); i++) {
                    if (incompChecklist.get(s.time).contains(incompatibleSectionList.get(currentSectionIndex).get(i))) {
                        invalid = true;
                        errors.append("<li>");
                        errors.append("Error! Incompatible sections ");
                        errors.append(sectionLookup.get(currentSectionIndex));
                        errors.append(" and ");
                        errors.append(sectionLookup.get(incompatibleSectionList.get(currentSectionIndex).get(i)));
                        errors.append(" are scheduled for the same time!");
                        errors.append("</li>");
                    }
                }
            }
        }

        if (invalid) {
            errors.append("</ul>");
            return errors.toString();
        } else {
            return "";
        }
    }

    private String ConvertToRegularTime(int hour, int minute) {
        if (hour > 12) {
            return String.valueOf(hour - 12) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + " PM";
        } else if (hour < 12) {
            return String.valueOf(hour) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + " AM";
        } else {
            return String.valueOf(hour) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + " PM";
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUIForm.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new GUIForm().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCourseTaught;
    private javax.swing.JButton btnBrowseGeneratedFileName;
    private javax.swing.JButton btnBrowseResult;
    private javax.swing.JButton btnBrowseSetupFileName;
    private javax.swing.JButton btnDeleteCourse;
    private javax.swing.JButton btnDeleteProf;
    private javax.swing.JButton btnDeleteSchedule;
    private javax.swing.JButton btnDeleteTimeSlot;
    private javax.swing.JButton btnGenerateInputFile;
    private javax.swing.JButton btnGenerateResult;
    private javax.swing.JButton btnModifyIncomp;
    private javax.swing.JButton btnNewCourse;
    private javax.swing.JButton btnNewProf;
    private javax.swing.JButton btnNewTimeslot;
    private javax.swing.JButton btnResultChangeUndo;
    private javax.swing.JButton btnResultChangeUpdate;
    private javax.swing.JButton btnSaveCourse;
    private javax.swing.JButton btnSaveProf;
    private javax.swing.JButton btnSaveSchedule;
    private javax.swing.JButton btnSaveSetup;
    private javax.swing.JButton btnSaveTimeSlot;
    private javax.swing.JButton btnStatistics;
    private javax.swing.JButton btnValidateInitialSchedule;
    private javax.swing.JCheckBox cbCourseHighestAfternoon;
    private javax.swing.JCheckBox cbCourseHighestEvening;
    private javax.swing.JCheckBox cbCourseHighestMorning;
    private javax.swing.JCheckBox cbCourseLeastAfternoon;
    private javax.swing.JCheckBox cbCourseLeastEvening;
    private javax.swing.JCheckBox cbCourseLeastMorning;
    private javax.swing.JCheckBox cbCourseNA_Afternoon;
    private javax.swing.JCheckBox cbCourseNA_Evening;
    private javax.swing.JCheckBox cbCourseNA_Morning;
    private javax.swing.JCheckBox cbCourseNormalAfternoon;
    private javax.swing.JCheckBox cbCourseNormalEvening;
    private javax.swing.JCheckBox cbCourseNormalMorning;
    private javax.swing.JComboBox<String> cbProfCourseTaught;
    private javax.swing.JCheckBox cbProfHighestAfternoon;
    private javax.swing.JCheckBox cbProfHighestEvening;
    private javax.swing.JCheckBox cbProfHighestMorning;
    private javax.swing.JCheckBox cbProfLeastAfternoon;
    private javax.swing.JCheckBox cbProfLeastEvening;
    private javax.swing.JCheckBox cbProfLeastMorning;
    private javax.swing.JCheckBox cbProfNA_Afternoon;
    private javax.swing.JCheckBox cbProfNA_Evening;
    private javax.swing.JCheckBox cbProfNA_Morning;
    private javax.swing.JCheckBox cbProfNormalAfternoon;
    private javax.swing.JCheckBox cbProfNormalEvening;
    private javax.swing.JCheckBox cbProfNormalMorning;
    private javax.swing.JComboBox<String> cbProfessorSelection;
    private javax.swing.JComboBox<String> cbScheduleCourse;
    private javax.swing.JComboBox<String> cbScheduleProfessor;
    private javax.swing.JComboBox<String> cbScheduleSection;
    private javax.swing.JComboBox<String> cbScheduleTimeslot;
    private javax.swing.JComboBox<String> cbTimeSelection;
    private javax.swing.JCheckBox chkCourseNoPreference;
    private javax.swing.JCheckBox chkProfNoPreference;
    private javax.swing.JPanel courseData;
    private javax.swing.JComboBox<String> dropIncompCourses;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lbFridayStart;
    private javax.swing.JLabel lbSaturdayStart;
    private javax.swing.JLabel lbSetupFileName;
    private javax.swing.JLabel lbThursdayStart;
    private javax.swing.JLabel lblChangeProfessorTo;
    private javax.swing.JLabel lblChangeTimeTo;
    private javax.swing.JLabel lblCourseCreditValue;
    private javax.swing.JLabel lblCourseGeneratedID;
    private javax.swing.JLabel lblCourseID;
    private javax.swing.JLabel lblCourseSectionCount;
    private javax.swing.JLabel lblCourseTimeAfternoon;
    private javax.swing.JLabel lblCourseTimeEvening;
    private javax.swing.JLabel lblCourseTimeHighest;
    private javax.swing.JLabel lblCourseTimeLeast;
    private javax.swing.JLabel lblCourseTimeMorning;
    private javax.swing.JLabel lblCourseTimeNA;
    private javax.swing.JLabel lblCourseTimeNormal;
    private javax.swing.JLabel lblFridayColumn;
    private javax.swing.JLabel lblFridayEnd;
    private javax.swing.JLabel lblGeneratedFileName;
    private javax.swing.JLabel lblGeneratedID;
    private javax.swing.JLabel lblGeneratedTSID;
    private javax.swing.JLabel lblGenerations;
    private javax.swing.JLabel lblMondayColumn;
    private javax.swing.JLabel lblMondayEnd;
    private javax.swing.JLabel lblMondayStart;
    private javax.swing.JLabel lblMutationProbabilty;
    private javax.swing.JLabel lblPopulationSize;
    private javax.swing.JLabel lblProfCredits;
    private javax.swing.JLabel lblProfName;
    private javax.swing.JLabel lblProfTimeAfternoon;
    private javax.swing.JLabel lblProfTimeEvening;
    private javax.swing.JLabel lblProfTimeHighest;
    private javax.swing.JLabel lblProfTimeLeast;
    private javax.swing.JLabel lblProfTimeMorning;
    private javax.swing.JLabel lblProfTimeNA;
    private javax.swing.JLabel lblProfTimeNormal;
    private javax.swing.JLabel lblReplacementWait;
    private javax.swing.JLabel lblResultFile;
    private javax.swing.JLabel lblSaturdayColumn;
    private javax.swing.JLabel lblSaturdayEnd;
    private javax.swing.JLabel lblScheduleCourse;
    private javax.swing.JLabel lblScheduleProfessor;
    private javax.swing.JLabel lblScheduleSectionLabel;
    private javax.swing.JLabel lblScheduleTimeslot;
    private javax.swing.JLabel lblScheduledCourses;
    private javax.swing.JLabel lblSundayColumn;
    private javax.swing.JLabel lblThursdayColumn;
    private javax.swing.JLabel lblThursdayEnd;
    private javax.swing.JLabel lblTimeColumn;
    private javax.swing.JLabel lblTimeSlotCreditValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTuesdayColumn;
    private javax.swing.JLabel lblTuesdayEnd;
    private javax.swing.JLabel lblTuesdayStart;
    private javax.swing.JLabel lblUnscheduledCourses;
    private javax.swing.JLabel lblWednesdayColumn;
    private javax.swing.JLabel lblWednesdayEnd;
    private javax.swing.JLabel lblWednesdayStart;
    private javax.swing.JList<Object> listCourseTaught;
    private javax.swing.JList<String> listCourses;
    private javax.swing.JList<String> listIncompCourses;
    private javax.swing.JList<String> listProfs;
    private javax.swing.JList<String> listTimeslots;
    private javax.swing.JList<String> listUnscheduledCourses;
    private javax.swing.JList<String> listViewBySelection;
    private javax.swing.JMenu menuAbout;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem miOpenConfig;
    private javax.swing.JMenuItem miSaveConfig;
    private javax.swing.JPanel pnlAdvancedConfig;
    private javax.swing.JPanel pnlConfiguration;
    private javax.swing.JPanel pnlContainer;
    private javax.swing.JPanel pnlCourseTimePreference;
    private javax.swing.JPanel pnlCourses;
    private javax.swing.JPanel pnlFriday;
    private javax.swing.JPanel pnlFridayColumn;
    private javax.swing.JPanel pnlIncompatibleCourses;
    private javax.swing.JPanel pnlInitSchedule;
    private javax.swing.JPanel pnlMonday;
    private javax.swing.JPanel pnlMondayColumn;
    private javax.swing.JPanel pnlProfCourseTaught;
    private javax.swing.JPanel pnlProfData;
    private javax.swing.JPanel pnlProfTimePreference;
    private javax.swing.JPanel pnlProfessor;
    private javax.swing.JPanel pnlResultContainer;
    private javax.swing.JPanel pnlResultScheduleLabel_Friday;
    private javax.swing.JPanel pnlResultScheduleLabel_Monday;
    private javax.swing.JPanel pnlResultScheduleLabel_Saturday;
    private javax.swing.JPanel pnlResultScheduleLabel_Sunday;
    private javax.swing.JPanel pnlResultScheduleLabel_Thursday;
    private javax.swing.JPanel pnlResultScheduleLabel_Time;
    private javax.swing.JPanel pnlResultScheduleLabel_Tuesday;
    private javax.swing.JPanel pnlResultScheduleLabel_Wednesday;
    private javax.swing.JPanel pnlResultScheduleLabels;
    private javax.swing.JPanel pnlResults;
    private javax.swing.JPanel pnlSaturday;
    private javax.swing.JPanel pnlSaturdayColumn;
    private javax.swing.JPanel pnlSchedule;
    private javax.swing.JPanel pnlScheduleDays;
    private javax.swing.JPanel pnlSundayColumn;
    private javax.swing.JPanel pnlThursday;
    private javax.swing.JPanel pnlThursdayColumn;
    private javax.swing.JPanel pnlTimeColumn;
    private javax.swing.JPanel pnlTimeSlots;
    private javax.swing.JPanel pnlTuesday;
    private javax.swing.JPanel pnlTuesdayColumn;
    private javax.swing.JPanel pnlViewByControls;
    private javax.swing.JPanel pnlWednesday;
    private javax.swing.JPanel pnlWednesdayColumn;
    private javax.swing.JRadioButton rdoCourse;
    private javax.swing.JRadioButton rdoProfessor;
    private javax.swing.JScrollPane scrollSchedule;
    private javax.swing.JScrollPane spCourseIncomp;
    private javax.swing.JScrollPane spCourseList;
    private javax.swing.JScrollPane spCourseTaughtList;
    private javax.swing.JScrollPane spProfList;
    private javax.swing.JScrollPane spTableSchedule;
    private javax.swing.JScrollPane spTimeslotList;
    private javax.swing.JScrollPane spUnscheduledCourses;
    private javax.swing.JScrollPane spViewBySelection;
    private javax.swing.JSpinner spinnerCreditsAssigned;
    private javax.swing.JSpinner spinnerGenerations;
    private javax.swing.JSpinner spinnerMutationProbabilty;
    private javax.swing.JSpinner spinnerPopulationSize;
    private javax.swing.JSpinner spinnerReplacementWait;
    private javax.swing.JSpinner spinnerSections;
    private javax.swing.JTabbedPane tabbedPanels;
    private javax.swing.JTable tableSchedule;
    private javax.swing.JTextField txtCourseCreditValue;
    private javax.swing.JTextField txtCourseGeneratedID;
    private javax.swing.JTextField txtCourseID;
    private javax.swing.JTextField txtCourseTitle;
    private javax.swing.JTextField txtFridayEnd;
    private javax.swing.JTextField txtFridayStart;
    private javax.swing.JTextField txtGeneratedFileName;
    private javax.swing.JTextField txtMondayEnd;
    private javax.swing.JTextField txtMondayStart;
    private javax.swing.JTextField txtProfGeneratedID;
    private javax.swing.JTextField txtProfName;
    private javax.swing.JTextField txtResultPath;
    private javax.swing.JTextField txtResultStatus;
    private javax.swing.JTextField txtSaturdayEnd;
    private javax.swing.JTextField txtSaturdayStart;
    private javax.swing.JTextField txtSetupFileName;
    private javax.swing.JTextField txtTSGeneratedID;
    private javax.swing.JTextField txtThursdayEnd;
    private javax.swing.JTextField txtThursdayStart;
    private javax.swing.JTextField txtTimeSlotCreditValue;
    private javax.swing.JTextField txtTuesdayEnd;
    private javax.swing.JTextField txtTuesdayStart;
    private javax.swing.JTextField txtWednesdayEnd;
    private javax.swing.JTextField txtWednesdayStart;
    private javax.swing.ButtonGroup viewByGroup;
    // End of variables declaration//GEN-END:variables
}
