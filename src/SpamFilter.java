//Import packages
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

//Main class
public class SpamFilter{
	//Declare variables
	static JFrame frame1;
	static Container pane;
	static JButton btnSubmit;
	static JLabel title, lblSubject, lblBody;
	static JTextField txtSubject;
	static JTextArea txtBody;
	static Insets insets;
	static PrintWriter writer;
	static ArrayList<ArrayList<String>> trainingSet;
	static ArrayList<ArrayList<String>> trainingSetPart;
	
	public static void main (String args[]) {
		//write to a file
		try {
			writer = new PrintWriter("output.txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		MySQLAccess mysql = new MySQLAccess();
	
		try {
			trainingSet = mysql.getTrainingSet();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//ANALYSIS	
		double[] precisionParts = new double[10];
		double[] recallParts = new double[10];
		for(int part = 1; part <= 10; part++){
			//get our trainingsets
			try {
				trainingSetPart = mysql.get9PartTrainingSet(part);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			//our classifier object
			NaiveBayes classifier = new NaiveBayes(trainingSetPart);
			int totalEmails = trainingSetPart.size();
			
			//get test emails
			ArrayList<ArrayList<String>> testEmails = new ArrayList<ArrayList<String>>();
			try {
				testEmails = mysql.getPartTrainingSet(part);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			int numHamClassifiedSpam = 0;
			int numSpamClassifiedHam = 0;
			
			int numCorrectHam = 0;
			//classify each test email
			for(int i = 0; i < testEmails.size(); i++){
				String subject = testEmails.get(i).get(0);
				String body = testEmails.get(i).get(1);
				int spam = Integer.parseInt(testEmails.get(i).get(2));
				//classify the email
				int guessedSpam = classifier.classifyEmail(subject, body);			
				if(spam == 0 && guessedSpam == 1){
					numHamClassifiedSpam++;
				}
				if(spam == 1 && guessedSpam == 0){
					numSpamClassifiedHam++;
				}
				if(spam == 0 && guessedSpam == 0){
					numCorrectHam++;
				}
				/*
				writer.println("Subject: " + testEmails.get(i).get(0));
				writer.println("Body: " + testEmails.get(i).get(1));
				writer.println("Spam: " + testEmails.get(i).get(2));
				writer.println("GuessSpam: " + guessedSpam);
				writer.println();
				*/
			}
			writer.println("Part" + part);
			writer.println("Precision: " + ((double)numCorrectHam / (numCorrectHam + numHamClassifiedSpam)));
			writer.println("Recall: " + ((double)numCorrectHam) / (numCorrectHam + numSpamClassifiedHam));
			writer.println("Number of ham emails classified as spam: " + numHamClassifiedSpam);
			writer.println("Number of ham emails correctly classified: " + numCorrectHam);
			writer.println("Number of spam emails classified as ham: " + numSpamClassifiedHam);
			writer.println("total emails: " + totalEmails);
			writer.println();
			writer.println();
			
			precisionParts[part-1] = ((double)numCorrectHam / (numCorrectHam + numHamClassifiedSpam));
			recallParts[part-1] = ((double)numCorrectHam) / (numCorrectHam + numSpamClassifiedHam);
		}

		//CALCULATE AVERAGES
		double precisionTotal = 0;
		double recallTotal = 0;
		for(int i = 0; i < precisionParts.length; i++){
			precisionTotal += precisionParts[i];
			recallTotal += recallParts[i];
		}
		writer.println("Precision Average: " + precisionTotal / precisionParts.length);
		writer.println("Recall Average: " + recallTotal / recallParts.length);
		writer.close();
		
		
		//BELOW IS THE UI FOR THE APPLICATION
		
		//Set Look and Feel
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}

		//Create the frame
		frame1 = new JFrame ();
		frame1.setSize (800,800);
		frame1.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		pane = frame1.getContentPane();
		insets = pane.getInsets();
		pane.setLayout (null);

		//Create controls
		title = new JLabel("Please enter your email below");
		btnSubmit = new JButton ("Submit");
		lblSubject = new JLabel ("Subject:");
		lblBody = new JLabel("Body: ");
		txtSubject = new JTextField (10);
		txtBody = new JTextArea();
		
		JScrollPane sp = new JScrollPane(txtBody);

		//Add all components to panel
		pane.add(title);
		pane.add (lblSubject);
		pane.add (txtSubject);
		pane.add(lblBody);
		pane.add(sp);
		pane.add (btnSubmit);

		int x = insets.left + 5;
		//Place all components
		title.setBounds(x, insets.top + 5, title.getPreferredSize().width, title.getPreferredSize().height);
		lblSubject.setBounds (x, title.getY() + title.getHeight() + 10, lblSubject.getPreferredSize().width, lblSubject.getPreferredSize().height);
		txtSubject.setBounds (x, lblSubject.getY() + lblSubject.getHeight() + 10, txtSubject.getPreferredSize().width, txtSubject.getPreferredSize().height);

		lblBody.setBounds(x, lblSubject.getY() + 50, lblBody.getPreferredSize().width, lblBody.getPreferredSize().height);
		sp.setBounds(x, lblBody.getY() + lblBody.getHeight() + 10, 600, 200);

		btnSubmit.setBounds (x, sp.getY() + sp.getHeight() + 10, btnSubmit.getPreferredSize().width, btnSubmit.getPreferredSize().height);

		//Set frame visible
		frame1.setVisible (true);

		//Button's action
		btnSubmit.addActionListener(new btnSubmitAction()); //Register action
		
	}

	
	public static class btnSubmitAction implements ActionListener{
		public void actionPerformed (ActionEvent e){
			NaiveBayes classifier = new NaiveBayes(trainingSet);
			String subject = txtSubject.getText();
			String body = txtBody.getText();
			
			System.out.println(classifier.classifyEmail(subject, body));
		}
	}
}
