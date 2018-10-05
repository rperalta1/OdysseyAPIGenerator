/**************************************

    Copyright (C) 2018  
    Judicial Information Division,
    Administrative Office of the Courts,
    State of New Mexico

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

***************************************/
package example;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ValidateParameters extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldApiWsdl;
	private JTextField textFieldSiteId;
	private JTextField textFieldUserId;
	private JTextField textFieldCaseNumber;
	private boolean process = false;

	public ValidateParameters(ExampleParameters exampleParameters) {
		setTitle("Test Parameters");
		setBounds(100, 100, 786, 295);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblOdysseyApiWsdl = new JLabel("Odyssey APIs WSDL:");
		lblOdysseyApiWsdl.setBounds(12, 22, 141, 16);
		contentPanel.add(lblOdysseyApiWsdl);
		
		JLabel lblOdysseySiteId = new JLabel("Odyssey Site ID:");
		lblOdysseySiteId.setBounds(12, 56, 141, 16);
		contentPanel.add(lblOdysseySiteId);
		
		JLabel lblUser = new JLabel("User Id:");
		lblUser.setBounds(12, 94, 141, 16);
		contentPanel.add(lblUser);
		
		JLabel lblOdysseyCaseNumber = new JLabel("Odyssey Case Number:");
		lblOdysseyCaseNumber.setBounds(12, 132, 141, 16);
		contentPanel.add(lblOdysseyCaseNumber);
		
		textFieldApiWsdl = new JTextField();
		textFieldApiWsdl.setBounds(157, 19, 561, 22);
		contentPanel.add(textFieldApiWsdl);
		textFieldApiWsdl.setColumns(10);
		textFieldApiWsdl.setText(exampleParameters.getOdysseyApiWdls());
		
		textFieldSiteId = new JTextField();
		textFieldSiteId.setBounds(157, 53, 561, 22);
		contentPanel.add(textFieldSiteId);
		textFieldSiteId.setColumns(10);
		textFieldSiteId.setText(exampleParameters.getOdysseySiteId());
		
		textFieldUserId = new JTextField();
		textFieldUserId.setBounds(157, 92, 561, 22);
		contentPanel.add(textFieldUserId);
		textFieldUserId.setColumns(10);
		textFieldUserId.setText(exampleParameters.getOdysseyUserId());
		
		textFieldCaseNumber = new JTextField();
		textFieldCaseNumber.setBounds(157, 131, 561, 22);
		contentPanel.add(textFieldCaseNumber);
		textFieldCaseNumber.setColumns(10);
		textFieldCaseNumber.setText(exampleParameters.getOdysseyCaseNumber());
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exampleParameters.setOdysseyApiWdls(textFieldApiWsdl.getText());
						exampleParameters.setOdysseySiteId(textFieldSiteId.getText());
						exampleParameters.setOdysseyUserId(textFieldUserId.getText());
						exampleParameters.setOdysseyCaseNumber(textFieldCaseNumber.getText());
						process = true;
						close();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();						
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	
	public boolean process(){
		if(process) return true;
		else return false;
	}
	
	
	public void close() {
	    WindowEvent winClosingEvent = new WindowEvent( this, WindowEvent.WINDOW_CLOSING );
	    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent( winClosingEvent );
	}
}
