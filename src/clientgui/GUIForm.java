/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import javax.swing.text.TabExpander;

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
    protected Vector courseListData;
    protected Vector courseSectionListData;
    protected Vector unscheduledCourses;
    protected Vector profListData;
    protected Vector timeslotListData;
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

    /**
     * Creates new form GUIForm
     */
    public GUIForm() {
        this.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent evt) {
                        String[] options = {"Yes", "No", "Browse"};
                        int choice = JOptionPane.showOptionDialog(pnlContainer, "<html>Would you like to save the current setup to<br />" + txtSetupFileName.getText() + "?</html>", "Save Setup To File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
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

        courseIDs = new HashSet<>();
        profIDs = new HashSet<>();
        timeslotIDs = new HashSet<>();
        courseList = new HashMap<>();
        profList = new HashMap<>();
        timeslotList = new HashMap<>();
        scheduledCoursesList = new HashMap<>();
        courseListData = new Vector();
        profListData = new Vector();
        timeslotListData = new Vector();
        courseSectionListData = new Vector();
        unscheduledCourses = new Vector();
        initComponents();
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
                    int row = tableSchedule.convertRowIndexToModel(rawIndex);
                    String course = dtm.getValueAt(row, 0).toString();
                    String prof = dtm.getValueAt(row, 1).toString();
                    String time = dtm.getValueAt(row, 2).toString();
                    cbScheduleCourse.setSelectedItem(course);
                    cbScheduleProfessor.setSelectedItem(prof);
                    cbScheduleTimeslot.setSelectedItem(time);
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
                            scheduledCoursesList = new HashMap<String, Schedule>();
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
                        timeslotIDs = (HashSet<Integer>) input.readObject();
                        if (timeslotIDs.size() != timeslotList.size()) {
                            timeslotIDs.clear();
                            for (String s : timeslotList.keySet()) {
                                timeslotIDs.add(timeslotList.get(s).getID());
                            }
                        }
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
            }
        } else {
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
        tableSchedule.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        int columnIndexToSort = 0;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
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
     * This method is called from within the constructor to
     * initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnNext = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
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
        pnlCoursePreference = new javax.swing.JPanel();
        lblCoursePrefHighest = new javax.swing.JLabel();
        lblCoursePrefNormal = new javax.swing.JLabel();
        lblCoursePrefLeast = new javax.swing.JLabel();
        cbCoursePrefHighest = new javax.swing.JComboBox();
        cbCoursePrefNormal = new javax.swing.JComboBox();
        cbCoursePrefLeast = new javax.swing.JComboBox();
        btnSaveCourse = new javax.swing.JButton();
        pnlIncompatibleCourses = new javax.swing.JPanel();
        spCourseIncomp = new javax.swing.JScrollPane();
        listIncompCourses = new JList(new Vector());
        dropIncompCourses = new javax.swing.JComboBox();
        btnModifyIncomp = new javax.swing.JButton();
        lblCourseSectionCount = new javax.swing.JLabel();
        spinnerSections = new javax.swing.JSpinner();
        btnDeleteCourse = new javax.swing.JButton();
        lblCourseGeneratedID = new javax.swing.JLabel();
        txtCourseGeneratedID = new javax.swing.JTextField();
        spCourseList = new javax.swing.JScrollPane();
        listCourses = new JList(courseListData);
        btnNewCourse = new javax.swing.JButton();
        pnlProfessor = new javax.swing.JPanel();
        spProfList = new javax.swing.JScrollPane();
        listProfs = new javax.swing.JList();
        pnlProfData = new javax.swing.JPanel();
        lblGeneratedID = new javax.swing.JLabel();
        lblProfName = new javax.swing.JLabel();
        txtProfGeneratedID = new javax.swing.JTextField();
        txtProfName = new javax.swing.JTextField();
        lblProfCredits = new javax.swing.JLabel();
        spinnerCreditsAssigned = new javax.swing.JSpinner();
        pnlProfPreference = new javax.swing.JPanel();
        lblProfPreferenceHighest = new javax.swing.JLabel();
        lblProfPreferenceNormal = new javax.swing.JLabel();
        lblProfPreferenceLeast = new javax.swing.JLabel();
        cbProfPrefHighest = new javax.swing.JComboBox();
        cbProfPrefNormal = new javax.swing.JComboBox();
        cbProfPrefLeast = new javax.swing.JComboBox();
        pnlProfCourseTaught = new javax.swing.JPanel();
        btnAddCourseTaught = new javax.swing.JButton();
        cbProfCourseTaught = new javax.swing.JComboBox();
        spCourseTaughtList = new javax.swing.JScrollPane();
        listCourseTaught = new javax.swing.JList();
        btnDeleteProf = new javax.swing.JButton();
        btnSaveProf = new javax.swing.JButton();
        btnNewProf = new javax.swing.JButton();
        pnlTimeSlots = new javax.swing.JPanel();
        btnNewTimeslot = new javax.swing.JButton();
        spTimeslotList = new javax.swing.JScrollPane();
        listTimeslots = new javax.swing.JList();
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
        cbScheduleCourse = new javax.swing.JComboBox();
        lblScheduleProfessor = new javax.swing.JLabel();
        cbScheduleProfessor = new javax.swing.JComboBox();
        lblScheduleTimeslot = new javax.swing.JLabel();
        cbScheduleTimeslot = new javax.swing.JComboBox();
        btnSaveSchedule = new javax.swing.JButton();
        btnDeleteSchedule = new javax.swing.JButton();
        spUnscheduledCourses = new javax.swing.JScrollPane();
        listUnscheduledCourses = new javax.swing.JList();
        lblUnscheduledCourses = new javax.swing.JLabel();
        lblScheduledCourses = new javax.swing.JLabel();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 800));

        btnNext.setText("Next");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnBack.setText("Back");
        btnBack.setEnabled(false);
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        pnlContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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

        pnlCoursePreference.setBorder(javax.swing.BorderFactory.createTitledBorder("Preference"));

        lblCoursePrefHighest.setLabelFor(cbCoursePrefHighest);
        lblCoursePrefHighest.setText("Highest");

        lblCoursePrefNormal.setLabelFor(cbCoursePrefNormal);
        lblCoursePrefNormal.setText("Normal");

        lblCoursePrefLeast.setLabelFor(cbCoursePrefLeast);
        lblCoursePrefLeast.setText("Least");

        cbCoursePrefHighest.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));

        cbCoursePrefNormal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));
        cbCoursePrefNormal.setSelectedIndex(1);

        cbCoursePrefLeast.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));
        cbCoursePrefLeast.setSelectedIndex(2);

        javax.swing.GroupLayout pnlCoursePreferenceLayout = new javax.swing.GroupLayout(pnlCoursePreference);
        pnlCoursePreference.setLayout(pnlCoursePreferenceLayout);
        pnlCoursePreferenceLayout.setHorizontalGroup(
            pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCoursePreferenceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlCoursePreferenceLayout.createSequentialGroup()
                        .addComponent(lblCoursePrefHighest)
                        .addGap(29, 29, 29)
                        .addComponent(cbCoursePrefHighest, 0, 607, Short.MAX_VALUE))
                    .addGroup(pnlCoursePreferenceLayout.createSequentialGroup()
                        .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCoursePrefNormal)
                            .addComponent(lblCoursePrefLeast))
                        .addGap(32, 32, 32)
                        .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbCoursePrefNormal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbCoursePrefLeast, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlCoursePreferenceLayout.setVerticalGroup(
            pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCoursePreferenceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCoursePrefHighest)
                    .addComponent(cbCoursePrefHighest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCoursePrefNormal)
                    .addComponent(cbCoursePrefNormal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlCoursePreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCoursePrefLeast)
                    .addComponent(cbCoursePrefLeast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                    .addComponent(spCourseIncomp, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .addGroup(pnlIncompatibleCoursesLayout.createSequentialGroup()
                        .addComponent(dropIncompCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnModifyIncomp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addComponent(spCourseIncomp, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
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

        javax.swing.GroupLayout courseDataLayout = new javax.swing.GroupLayout(courseData);
        courseData.setLayout(courseDataLayout);
        courseDataLayout.setHorizontalGroup(
            courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, courseDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlCoursePreference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlIncompatibleCourses, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, courseDataLayout.createSequentialGroup()
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCourseCreditValue)
                            .addComponent(lblCourseSectionCount, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerSections, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtCourseCreditValue)))
                    .addComponent(btnDeleteCourse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSaveCourse, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, courseDataLayout.createSequentialGroup()
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTitle)
                            .addComponent(lblCourseID)
                            .addComponent(lblCourseGeneratedID))
                        .addGap(18, 18, 18)
                        .addGroup(courseDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCourseGeneratedID)
                            .addComponent(txtCourseTitle)
                            .addComponent(txtCourseID))))
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
                .addGap(18, 18, 18)
                .addComponent(pnlCoursePreference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlIncompatibleCourses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        pnlProfPreference.setBorder(javax.swing.BorderFactory.createTitledBorder("Preference"));

        lblProfPreferenceHighest.setText("Highest Preference");

        lblProfPreferenceNormal.setText("Normal Preference");

        lblProfPreferenceLeast.setText("Least Preference");

        cbProfPrefHighest.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));

        cbProfPrefNormal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));
        cbProfPrefNormal.setSelectedIndex(1);

        cbProfPrefLeast.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Morning", "Afternoon", "Evening" }));
        cbProfPrefLeast.setSelectedIndex(2);

        javax.swing.GroupLayout pnlProfPreferenceLayout = new javax.swing.GroupLayout(pnlProfPreference);
        pnlProfPreference.setLayout(pnlProfPreferenceLayout);
        pnlProfPreferenceLayout.setHorizontalGroup(
            pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfPreferenceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfPreferenceHighest)
                    .addComponent(lblProfPreferenceLeast)
                    .addComponent(lblProfPreferenceNormal))
                .addGap(18, 18, 18)
                .addGroup(pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbProfPrefHighest, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbProfPrefNormal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbProfPrefLeast, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlProfPreferenceLayout.setVerticalGroup(
            pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfPreferenceLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfPreferenceHighest)
                    .addComponent(cbProfPrefHighest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfPreferenceNormal)
                    .addComponent(cbProfPrefNormal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfPreferenceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfPreferenceLeast)
                    .addComponent(cbProfPrefLeast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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
                    .addComponent(spCourseTaughtList, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
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
                .addComponent(spCourseTaughtList, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
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

        javax.swing.GroupLayout pnlProfDataLayout = new javax.swing.GroupLayout(pnlProfData);
        pnlProfData.setLayout(pnlProfDataLayout);
        pnlProfDataLayout.setHorizontalGroup(
            pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProfDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlProfPreference, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteProf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlProfCourseTaught, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlProfDataLayout.createSequentialGroup()
                        .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblGeneratedID)
                            .addComponent(lblProfName)
                            .addComponent(lblProfCredits))
                        .addGap(18, 18, 18)
                        .addGroup(pnlProfDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerCreditsAssigned)
                            .addComponent(txtProfGeneratedID)
                            .addComponent(txtProfName)))
                    .addComponent(btnSaveProf, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(pnlProfPreference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addComponent(txtMondayEnd, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
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
                        .addComponent(spTimeslotList, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE))
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

        btnSaveSchedule.setText("Add/Update Schedule");
        btnSaveSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveScheduleActionPerformed(evt);
            }
        });

        btnDeleteSchedule.setText("Delete Schedule");

        listUnscheduledCourses.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listUnscheduledCoursesValueChanged(evt);
            }
        });
        spUnscheduledCourses.setViewportView(listUnscheduledCourses);

        lblUnscheduledCourses.setText("Unscheduled Courses");

        lblScheduledCourses.setText("Scheduled Courses");

        javax.swing.GroupLayout pnlInitScheduleLayout = new javax.swing.GroupLayout(pnlInitSchedule);
        pnlInitSchedule.setLayout(pnlInitScheduleLayout);
        pnlInitScheduleLayout.setHorizontalGroup(
            pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUnscheduledCourses, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                            .addComponent(spUnscheduledCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                                .addComponent(lblScheduledCourses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(2, 2, 2))
                            .addComponent(spTableSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)))
                    .addGroup(pnlInitScheduleLayout.createSequentialGroup()
                        .addComponent(btnSaveSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(11, 11, 11)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblUnscheduledCourses)
                    .addComponent(lblScheduledCourses, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlInitScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spUnscheduledCourses)
                    .addComponent(spTableSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)
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
                .addContainerGap(346, Short.MAX_VALUE))
        );

        tabbedPanels.addTab("Configuration", pnlConfiguration);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnNext)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNext)
                    .addComponent(btnBack))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
        if (CurrentStep < 5) {
            CurrentStep++;
            switch (CurrentStep) {
                case 4:
                    if (btnNext.isEnabled()) {
                        btnNext.setEnabled(false);
                    }
                case 1:
                    if (!btnBack.isEnabled()) {
                        btnBack.setEnabled(true);
                    }
                default:
                    tabbedPanels.setSelectedIndex(CurrentStep);
                    break;
            }
        }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        if (CurrentStep > 0) {
            CurrentStep--;
            switch (CurrentStep) {
                case 0:
                    if (btnBack.isEnabled()) {
                        btnBack.setEnabled(false);
                    }
                    if (!btnNext.isEnabled()) {
                        btnNext.setEnabled(true);
                    }
                default:
                    tabbedPanels.setSelectedIndex(CurrentStep);
                    break;
            }
        }
    }//GEN-LAST:event_btnBackActionPerformed

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
        cbCoursePrefHighest.setSelectedIndex(0);
        cbCoursePrefNormal.setSelectedIndex(1);
        cbCoursePrefLeast.setSelectedIndex(2);
        dropIncompCourses.removeAllItems();
        Iterator it = courseList.keySet().iterator();
        while (it.hasNext()) {
            dropIncompCourses.addItem(it.next());
        }
        listIncompCourses.setListData(new Vector());
    }//GEN-LAST:event_btnNewCourseActionPerformed

    private void txtCourseTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCourseTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCourseTitleActionPerformed

    private void btnSaveCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCourseActionPerformed
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
            oldSectionCount = sections;
            currentCourse = new Course(txtCourseID.getText(), txtCourseTitle.getText(), courseList.size(), credits, sections);
        }
        int highest = cbCoursePrefHighest.getSelectedIndex(), normal = cbCoursePrefNormal.getSelectedIndex(), least = cbCoursePrefLeast.getSelectedIndex();
        int[] prefSelected = new int[3];
        switch (least) {
            case 0:
                prefSelected[0] = 2;
                break;
            case 1:
                prefSelected[1] = 2;
                break;
            case 2:
                prefSelected[2] = 2;
                break;
        }
        switch (normal) {
            case 0:
                prefSelected[0] = 1;
                break;
            case 1:
                prefSelected[1] = 1;
                break;
            case 2:
                prefSelected[2] = 1;
                break;
        }
        switch (highest) {
            case 0:
                prefSelected[0] = 0;
                break;
            case 1:
                prefSelected[1] = 0;
                break;
            case 2:
                prefSelected[2] = 0;
                break;
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

        if (oldSectionCount != sections) {
            for (int i = 1; i <= oldSectionCount; i++) {
                String elementID = currentCourse.getID() + "(" + String.valueOf(i) + ")";
                courseSectionListData.removeElement(elementID);
                unscheduledCourses.removeElement(elementID);
            }
        }
        if (sections > 0) {
            for (int i = 1; i <= sections; i++) {
                String elementID = currentCourse.getID() + "(" + String.valueOf(i) + ")";
                courseSectionListData.addElement(elementID);
                unscheduledCourses.addElement(elementID);
            }
            Collections.sort(courseSectionListData);
            Collections.sort(unscheduledCourses);
        }
        
        updateCourseGeneratedIDs();
    }//GEN-LAST:event_btnSaveCourseActionPerformed

    private void txtCourseCreditValueKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCourseCreditValueKeyTyped

    }//GEN-LAST:event_txtCourseCreditValueKeyTyped

    private void listCoursesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listCoursesValueChanged
        if (listCourses.getSelectedIndex() >= 0) {
            currentCourse = courseList.get(listCourses.getSelectedValue().toString());
            txtCourseGeneratedID.setText(String.valueOf(currentCourse.getGeneratedID()));
            txtCourseTitle.setText(currentCourse.getTitle());
            txtCourseID.setText(currentCourse.getID());
            txtCourseCreditValue.setText(String.valueOf(currentCourse.getCreditValue()));
            spinnerSections.setValue(currentCourse.getSectionCount());
            switch (currentCourse.getPreferences()[0]) {
                case 2:
                    cbCoursePrefHighest.setSelectedIndex(0);
                    break;
                case 1:
                    cbCoursePrefNormal.setSelectedIndex(0);
                    break;
                case 0:
                    cbCoursePrefLeast.setSelectedIndex(0);
            }
            switch (currentCourse.getPreferences()[1]) {
                case 2:
                    cbCoursePrefHighest.setSelectedIndex(1);
                    break;
                case 1:
                    cbCoursePrefNormal.setSelectedIndex(1);
                    break;
                case 0:
                    cbCoursePrefLeast.setSelectedIndex(1);
            }
            switch (currentCourse.getPreferences()[2]) {
                case 2:
                    cbCoursePrefHighest.setSelectedIndex(2);
                    break;
                case 1:
                    cbCoursePrefNormal.setSelectedIndex(2);
                    break;
                case 0:
                    cbCoursePrefLeast.setSelectedIndex(2);
            }
            Vector temp = currentCourse.getIncompCourses();
            Collections.sort(temp);
            listIncompCourses.setListData(temp);
            dropIncompCourses.setEnabled(true);
            btnModifyIncomp.setEnabled(true);
            btnModifyIncomp.setText("Add");
            dropIncompCourses.removeAllItems();
            courseListData.stream().filter((s) -> (!s.toString().equals(currentCourse.getID()))).forEach((s) -> {
                dropIncompCourses.addItem(s.toString());
            });
        }
    }//GEN-LAST:event_listCoursesValueChanged

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
        Vector temp = currentCourse.getIncompCourses();
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
        cbProfPrefHighest.setSelectedIndex(0);
        cbProfPrefLeast.setSelectedIndex(2);
        cbProfPrefNormal.setSelectedIndex(1);
        txtProfGeneratedID.setText(String.valueOf(newID));
        txtProfName.setText("");
        spinnerCreditsAssigned.setValue(0);
        Iterator it = courseList.keySet().iterator();
        while (it.hasNext()) {
            cbProfCourseTaught.addItem(it.next());
        }
        listCourseTaught.setListData(new Vector());
        cbProfPrefHighest.setSelectedIndex(0);
        cbProfPrefNormal.setSelectedIndex(1);
        cbProfPrefLeast.setSelectedIndex(2);
    }//GEN-LAST:event_btnNewProfActionPerformed

    private void btnSaveProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfActionPerformed
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
        int highest = cbProfPrefHighest.getSelectedIndex(), normal = cbProfPrefNormal.getSelectedIndex(), least = cbProfPrefLeast.getSelectedIndex();
        int[] prefsSelected = new int[3];
        switch (least) {
            case 2:
                prefsSelected[2] = 2;
                break;
            case 1:
                prefsSelected[1] = 2;
                break;
            case 0:
                prefsSelected[0] = 2;
                break;
        }
        switch (normal) {
            case 2:
                prefsSelected[2] = 1;
                break;
            case 1:
                prefsSelected[1] = 1;
                break;
            case 0:
                prefsSelected[0] = 1;
                break;
        }
        switch (highest) {
            case 2:
                prefsSelected[2] = 0;
                break;
            case 1:
                prefsSelected[1] = 0;
                break;
            case 0:
                prefsSelected[0] = 0;
                break;
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
            output.writeObject(timeslotIDs);
            output.writeObject(txtGeneratedFileName.getText());
            JOptionPane.showMessageDialog(pnlContainer, "<html><p>Setup Saved to<br />" + txtSetupFileName.getText() + "</p></html>", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(pnlContainer, "<html>Following error occured while saving: <br /><p>" + e.getMessage() + "</p></html>", "Save Failed", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException n) {
            n.printStackTrace();
        }
    }//GEN-LAST:event_btnSaveSetupActionPerformed

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

    private void listProfsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listProfsValueChanged
        if (listProfs.getSelectedIndex() >= 0) {
            currentProfessor = profList.get(listProfs.getSelectedValue().toString());
            txtProfGeneratedID.setText(String.valueOf(currentProfessor.getProfID()));
            txtProfName.setText(currentProfessor.getProfName());
            spinnerCreditsAssigned.setValue(currentProfessor.getCredits());
            switch (currentProfessor.getPreference()[0]) {
                case 2:
                    cbProfPrefLeast.setSelectedIndex(0);
                    break;
                case 1:
                    cbProfPrefNormal.setSelectedIndex(0);
                    break;
                case 0:
                    cbProfPrefHighest.setSelectedIndex(0);
                    break;
            }
            switch (currentProfessor.getPreference()[1]) {
                case 2:
                    cbProfPrefLeast.setSelectedIndex(1);
                    break;
                case 1:
                    cbProfPrefNormal.setSelectedIndex(1);
                    break;
                case 0:
                    cbProfPrefHighest.setSelectedIndex(1);
                    break;
            }
            switch (currentProfessor.getPreference()[2]) {
                case 2:
                    cbProfPrefLeast.setSelectedIndex(2);
                    break;
                case 1:
                    cbProfPrefNormal.setSelectedIndex(2);
                    break;
                case 0:
                    cbProfPrefHighest.setSelectedIndex(2);
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
        }
    }//GEN-LAST:event_listProfsValueChanged

    private void tabbedPanelsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPanelsStateChanged
        if (tabbedPanels.getSelectedIndex() == 3) {
            cbScheduleCourse.removeAllItems();
            for (Object s : courseSectionListData) {
                cbScheduleCourse.addItem(s.toString());
            }

            listUnscheduledCourses.setListData(unscheduledCourses);
            //listUnscheduledCourses.setListData(courseListData);
            spUnscheduledCourses.revalidate();
            spUnscheduledCourses.repaint();
        }
    }//GEN-LAST:event_tabbedPanelsStateChanged

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

    private void btnDeleteProfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProfActionPerformed
        if (currentProfessor != null) {
            profList.remove(currentProfessor.getProfName());
            profListData.removeElement(currentProfessor.getProfName());
            listProfs.setListData(profListData);
            spProfList.revalidate();
            spProfList.repaint();
        }
    }//GEN-LAST:event_btnDeleteProfActionPerformed

    private void btnNewTimeslotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewTimeslotActionPerformed
        int newID = timeslotList.size();
        boolean switchSides = false;
        while (timeslotIDs.contains(newID)) {
            if (!switchSides) {
                newID--;
            } else {
                newID++;
            }
            if (newID == -1) {
                switchSides = true;
                newID = timeslotList.size() + 1;
            }
        }
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
    }//GEN-LAST:event_btnNewTimeslotActionPerformed

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
        timeslotList.put(currentTimeslot.toString(), currentTimeslot);
        updateTimeSlotIDs();
    }//GEN-LAST:event_btnSaveTimeSlotActionPerformed

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

    private void listTimeslotsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTimeslotsValueChanged
        if (listTimeslots.getSelectedIndex() >= 0) {
            currentTimeslot = timeslotList.get(listTimeslots.getSelectedValue().toString());
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
                    }
                }
            }
        }
    }//GEN-LAST:event_listTimeslotsValueChanged

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

    private void btnGenerateInputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateInputFileActionPerformed
        while (txtGeneratedFileName.getText().isEmpty()) {
            btnBrowseGeneratedFileNameActionPerformed(null);
        }
        BufferedWriter writer = null;
        try {
            String inputFile = txtGeneratedFileName.getText();
            File outFile = new File(inputFile);
            writer = new BufferedWriter(new FileWriter(outFile));
            //Write Parameters
            writer.write("//START*PARAMETERS*\n");
            writer.write(spinnerGenerations.getValue().toString() + "\n");
            writer.write(spinnerPopulationSize.getValue().toString() + "\n");
            writer.write(spinnerReplacementWait.getValue().toString() + "\n");
            writer.write(spinnerMutationProbabilty.getValue().toString() + "\n");
            writer.write("*END*PARAMETERS\n");
            //Write Sections
            writer.write("//*START*SECTION*\n");
            writer.write("//courseID, sections\n");
            TreeSet<String> courses = new TreeSet<>();
            for (String c : courseList.keySet()) {
                courses.add(c + ", " + String.valueOf(courseList.get(c).getSectionCount()) + "\n");
            }
            for (String s : courses) {
                writer.write(s);
            }
            writer.write("*END*SECTION*\n");
            writer.write("//*START*COURSE*\n");
            writer.write("//depttcourse, credits[,deptt:course[,incompatible list]]\n");
            DecimalFormat df = new DecimalFormat("#.#");
            ArrayList<String> coursePref = new ArrayList<>();
            for (String c : courseList.keySet()) {
                Course cr = courseList.get(c);
                coursePref.add(cr.getID() + ", " + cr.getPreferenceString() + "\n");
                writer.write(c + ", " + df.format(cr.getCreditValue()) + ", ");
                for (String incomp : cr.getIncompCourses()) {
                    writer.write(incomp + ", ");
                }
                writer.write("\n");
            }
            writer.write("*END*COURSE*\n");
            writer.write("//*START*PROFESSOR*\n");
            writer.write("//profid, maxcredit, courses[, courses]\n");
            TreeSet<String> profSet = new TreeSet<>();
            ArrayList<String> profPref = new ArrayList<>();
            for (String pr : profList.keySet()) {
                Professor p = profList.get(pr);
                profPref.add(p.getProfID() + ", " + p.getPreferenceString() + "\n");
                String currLine = String.format("%02d", p.getProfID()) + ", " + df.format(p.getCredits());
                for (Object cTaught : p.getCoursesTaught()) {
                    currLine += ", " + cTaught.toString();
                }
                profSet.add(currLine);
            }
            for (String prof : profSet) {
                if (prof.startsWith("0")) {
                    prof = prof.substring(1);
                }
                writer.write(prof + "\n");
            }
            writer.write("*END*PROFESSOR*\n");
            writer.write("//*START*TIMESLOT*\n");
            writer.write("//timeslot_id	, credit rating, monday		, tuesday		, wednesday		, thursday		, friday		, saturday\n");
            TreeSet<String> timeSet = new TreeSet<>();
            for (String s : timeslotList.keySet()) {
                TimeSlot t = timeslotList.get(s);
                String currLine = String.format("%02d", t.getID()) + ", " + df.format(t.getCredits());
                for (int i = 0; i < 6; i++) {
                    currLine += ", " + t.GetTimeOnDay(i);
                }
                currLine += "\n";
                timeSet.add(currLine);
            }

            for (String time : timeSet) {
                writer.write(time);
            }
            writer.write("*END*TIMESLOT*\n");
            writer.write("//*START*COURSEPREF*\n");
            writer.write("//course, pref(m-a-e)\n");
            for (int i = 0; i < coursePref.size(); i++) {
                writer.write(coursePref.get(i));
            }
            writer.write("*END*COURSEPREF*\n");
            writer.write("//START*PROFPREF*\n");
            writer.write("//profID, pref(m-a-e)\n");
            for (int i = 0; i < profPref.size(); i++) {
                writer.write(profPref.get(i));
            }
            writer.write("*END*PROFPREF*\n");
            writer.write("//*START*INITIAL*\n");
            TreeSet<String> schedules = new TreeSet<>();
            for (String s : scheduledCoursesList.keySet()) {
                Schedule sc = scheduledCoursesList.get(s);
                System.out.println(sc.course + " " + sc.prof + " " + sc.time);
                int profID = profList.get(sc.prof).getProfID();
                int tsID = timeslotList.get(sc.time).getID();
                schedules.add(sc.course + "$" + sc.course.substring(0, sc.course.indexOf('(')) + ", " + String.valueOf(profID) + ", " + String.valueOf(tsID) + "\n");
            }
            for (String s : schedules) {
                writer.write(s.substring(s.indexOf('$') + 1));
            }
            writer.write("*END*INITIAL*\n");
            JOptionPane.showMessageDialog(pnlContainer, "<html><p>Input file generated to<br />" + txtGeneratedFileName.getText() + ".</p></html>", "Input File Generated Successfully", JOptionPane.INFORMATION_MESSAGE);
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

    private void listUnscheduledCoursesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listUnscheduledCoursesValueChanged
        if (listUnscheduledCourses.getSelectedIndex() >= 0) {
            cbScheduleCourse.setSelectedItem(listUnscheduledCourses.getSelectedValue());
        }
    }//GEN-LAST:event_listUnscheduledCoursesValueChanged

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
        int arraySize = profList.size();
        int leftID = 0, rightID = 0;
        HashMap<Integer, Professor> updateList = new HashMap<>();
        TreeSet<Integer> ids = new TreeSet<>();
        for (String s : profList.keySet()) {
            if (profList.get(s).getProfID() > rightID) {
                rightID = profList.get(s).getProfID();
                updateList.put(rightID, profList.get(s));
            }
        }

        profIDs.clear();
        profList.clear();
        for (int i = 0; i < ids.size(); i++) {
            if (!ids.contains(i)) {
                Professor c = updateList.get(ids.last());
                c.setProfID(i);
                profIDs.add(i);
                profList.put(c.getProfName(), c);
                ids.remove(ids.last());
            }
        }
    }

    private void updateTimeSlotIDs() {
        int arraySize = timeslotList.size();
        int rightID = 0;
        HashMap<Integer, TimeSlot> updateList = new HashMap<>();
        TreeSet<Integer> ids = new TreeSet<>();
        for (String s : timeslotList.keySet()) {
            if (timeslotList.get(s).getID() > rightID) {
                rightID = timeslotList.get(s).getID();
                updateList.put(rightID, timeslotList.get(s));
            }
        }

        timeslotIDs.clear();
        timeslotList.clear();
        for (int i = 0; i < ids.size(); i++) {
            if (!ids.contains(i)) {
                TimeSlot c = updateList.get(ids.last());
                c.setID(i);
                timeslotIDs.add(i);
                timeslotList.put(c.toString(),  c);
                ids.remove(ids.last());
            }
        }
    }

    private boolean validateInitialSchedule() {

        return false;
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
            java.util.logging.Logger.getLogger(GUIForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBrowseGeneratedFileName;
    private javax.swing.JButton btnBrowseSetupFileName;
    private javax.swing.JButton btnDeleteCourse;
    private javax.swing.JButton btnDeleteProf;
    private javax.swing.JButton btnDeleteSchedule;
    private javax.swing.JButton btnDeleteTimeSlot;
    private javax.swing.JButton btnGenerateInputFile;
    private javax.swing.JButton btnModifyIncomp;
    private javax.swing.JButton btnNewCourse;
    private javax.swing.JButton btnNewProf;
    private javax.swing.JButton btnNewTimeslot;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnSaveCourse;
    private javax.swing.JButton btnSaveProf;
    private javax.swing.JButton btnSaveSchedule;
    private javax.swing.JButton btnSaveSetup;
    private javax.swing.JButton btnSaveTimeSlot;
    private javax.swing.JComboBox cbCoursePrefHighest;
    private javax.swing.JComboBox cbCoursePrefLeast;
    private javax.swing.JComboBox cbCoursePrefNormal;
    private javax.swing.JComboBox cbProfCourseTaught;
    private javax.swing.JComboBox cbProfPrefHighest;
    private javax.swing.JComboBox cbProfPrefLeast;
    private javax.swing.JComboBox cbProfPrefNormal;
    private javax.swing.JComboBox cbScheduleCourse;
    private javax.swing.JComboBox cbScheduleProfessor;
    private javax.swing.JComboBox cbScheduleTimeslot;
    private javax.swing.JPanel courseData;
    private javax.swing.JComboBox dropIncompCourses;
    private javax.swing.JLabel lbFridayStart;
    private javax.swing.JLabel lbSaturdayStart;
    private javax.swing.JLabel lbSetupFileName;
    private javax.swing.JLabel lbThursdayStart;
    private javax.swing.JLabel lblCourseCreditValue;
    private javax.swing.JLabel lblCourseGeneratedID;
    private javax.swing.JLabel lblCourseID;
    private javax.swing.JLabel lblCoursePrefHighest;
    private javax.swing.JLabel lblCoursePrefLeast;
    private javax.swing.JLabel lblCoursePrefNormal;
    private javax.swing.JLabel lblCourseSectionCount;
    private javax.swing.JLabel lblFridayEnd;
    private javax.swing.JLabel lblGeneratedFileName;
    private javax.swing.JLabel lblGeneratedID;
    private javax.swing.JLabel lblGeneratedTSID;
    private javax.swing.JLabel lblGenerations;
    private javax.swing.JLabel lblMondayEnd;
    private javax.swing.JLabel lblMondayStart;
    private javax.swing.JLabel lblMutationProbabilty;
    private javax.swing.JLabel lblPopulationSize;
    private javax.swing.JLabel lblProfCredits;
    private javax.swing.JLabel lblProfName;
    private javax.swing.JLabel lblProfPreferenceHighest;
    private javax.swing.JLabel lblProfPreferenceLeast;
    private javax.swing.JLabel lblProfPreferenceNormal;
    private javax.swing.JLabel lblReplacementWait;
    private javax.swing.JLabel lblSaturdayEnd;
    private javax.swing.JLabel lblScheduleCourse;
    private javax.swing.JLabel lblScheduleProfessor;
    private javax.swing.JLabel lblScheduleTimeslot;
    private javax.swing.JLabel lblScheduledCourses;
    private javax.swing.JLabel lblThursdayEnd;
    private javax.swing.JLabel lblTimeSlotCreditValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTuesdayEnd;
    private javax.swing.JLabel lblTuesdayStart;
    private javax.swing.JLabel lblUnscheduledCourses;
    private javax.swing.JLabel lblWednesdayEnd;
    private javax.swing.JLabel lblWednesdayStart;
    private javax.swing.JList listCourseTaught;
    private javax.swing.JList listCourses;
    private javax.swing.JList listIncompCourses;
    private javax.swing.JList listProfs;
    private javax.swing.JList listTimeslots;
    private javax.swing.JList listUnscheduledCourses;
    private javax.swing.JPanel pnlAdvancedConfig;
    private javax.swing.JPanel pnlConfiguration;
    private javax.swing.JPanel pnlContainer;
    private javax.swing.JPanel pnlCoursePreference;
    private javax.swing.JPanel pnlCourses;
    private javax.swing.JPanel pnlFriday;
    private javax.swing.JPanel pnlIncompatibleCourses;
    private javax.swing.JPanel pnlInitSchedule;
    private javax.swing.JPanel pnlMonday;
    private javax.swing.JPanel pnlProfCourseTaught;
    private javax.swing.JPanel pnlProfData;
    private javax.swing.JPanel pnlProfPreference;
    private javax.swing.JPanel pnlProfessor;
    private javax.swing.JPanel pnlSaturday;
    private javax.swing.JPanel pnlScheduleDays;
    private javax.swing.JPanel pnlThursday;
    private javax.swing.JPanel pnlTimeSlots;
    private javax.swing.JPanel pnlTuesday;
    private javax.swing.JPanel pnlWednesday;
    private javax.swing.JScrollPane spCourseIncomp;
    private javax.swing.JScrollPane spCourseList;
    private javax.swing.JScrollPane spCourseTaughtList;
    private javax.swing.JScrollPane spProfList;
    private javax.swing.JScrollPane spTableSchedule;
    private javax.swing.JScrollPane spTimeslotList;
    private javax.swing.JScrollPane spUnscheduledCourses;
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
    // End of variables declaration//GEN-END:variables
}
