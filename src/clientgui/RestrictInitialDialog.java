/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientgui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Siddharth
 */
public class RestrictInitialDialog extends JDialog {

    JSpinner spPercentage;
    Container cp;
    JLabel lblDialog;
    JButton btnDone;
    GroupLayout layout;

    public RestrictInitialDialog(JFrame fr) {
        super(fr, "Restrict Initial Schedule", true);

        cp = getContentPane();
        spPercentage = new JSpinner(new SpinnerNumberModel(75, 0, 100, 1));
        lblDialog = new JLabel("Please enter the percentage of schedule to keep.");
        btnDone = new JButton("OK");
        btnDone.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        layout = new GroupLayout(cp);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(layout.createSequentialGroup().addComponent(lblDialog).addGap(5).addComponent(spPercentage)).addComponent(btnDone));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(lblDialog).addComponent(spPercentage)).addGap(5).addComponent(btnDone));

        cp.setLayout(layout);
        setSize(375, 100);
        setLocationRelativeTo(fr);
    }

    public int getPercentage() {
        return (Integer) spPercentage.getValue();
    }
}
