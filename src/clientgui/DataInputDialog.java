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
public class DataInputDialog extends JDialog {

    JSpinner spPercentage;
    Container cp;
    JLabel lblDialog;
    JButton btnDone;
    GroupLayout layout;

    public DataInputDialog(JFrame fr, String title, String message, SpinnerNumberModel model) {
        super(fr, title, true);

        cp = getContentPane();
        spPercentage = new JSpinner(model);
        lblDialog = new JLabel(message);
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

    public int getData() {
        return (Integer) spPercentage.getValue();
    }
}
