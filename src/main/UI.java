package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import querygeneration.QueryGenerator;
import questionanalysis.QuestionAnalyzer;
import search.ThreadingSearcher;
import answerextraction.AnswerExtractor;
import content.AnalyzedQuestion;
import content.Query;
import content.Result;

public class UI extends JFrame{

	private static final String ANSWER_TEMPLATE = 
			"<p>[%d]<h2>%s</h2>%f<p>%s</p>Source:  <a href=\"%s\">%s</a></p><br> &nbsp";
	
	private JTextField textField;
	private JPanel contentPane;
	private JEditorPane editorPane;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (!QAMain.initializeAll()) {
						System.err.println("Failed to initialized!");
					} else {
						System.out.println("Done.");
					}
					UI frame = new UI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UI() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				QAMain.cleanupAll();
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage("QA-app-icon-512.png"));
		setTitle("MyQA");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 818, 542);
		contentPane = new JPanel();
		contentPane.setLocale(Locale.SIMPLIFIED_CHINESE);
		contentPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setToolTipText("In Progress...");
		progressBar.setVisible(false);
		progressBar.setBounds(602, 38, 87, 19);
		contentPane.add(progressBar);
		
		JPanel panel = new JPanel();
		panel.setBounds(248, 10, 304, 47);
		panel.setBackground(Color.DARK_GRAY);
		contentPane.add(panel);
		FlowLayout fl_panel = new FlowLayout(FlowLayout.CENTER, 5, 5);
		fl_panel.setAlignOnBaseline(true);
		panel.setLayout(fl_panel);
		
		JLabel lblNewLabel = new JLabel("My QA");
		panel.add(lblNewLabel);
		lblNewLabel.setForeground(new Color(255, 255, 255));
		lblNewLabel.setFont(new Font("Constantia", Font.BOLD, 30));
		
		JLabel lblAutomaticQuestion = new JLabel("Automatic Question Answering");
		panel.add(lblAutomaticQuestion);
		lblAutomaticQuestion.setFont(new Font("Aharoni", Font.PLAIN, 13));
		lblAutomaticQuestion.setForeground(new Color(220, 20, 60));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(-34, 62, 848, 47);
		panel_1.setBackground(Color.DARK_GRAY);
		contentPane.add(panel_1);
		FlowLayout fl_panel_1 = new FlowLayout(FlowLayout.CENTER, 5, 5);
		panel_1.setLayout(fl_panel_1);
		
		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					doQA();
				}
			}
		});
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		panel_1.add(textField);
		textField.setFont(new Font("ËÎÌå", Font.PLAIN, 13));
		textField.setColumns(70);
		
		JButton btnHelloSwing = new JButton("Ask MyQA!");
		panel_1.add(btnHelloSwing);
		btnHelloSwing.setForeground(Color.DARK_GRAY);
		btnHelloSwing.setBackground(new Color(153, 204, 0));
		btnHelloSwing.setFont(new Font("Consolas", Font.BOLD, 12));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 112, 748, 370);
		contentPane.add(scrollPane);
		
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		            if(Desktop.isDesktopSupported()) {
		                try {
		                    Desktop.getDesktop().browse(e.getURL().toURI());
		                }
		                catch (IOException ex) {
		                    ex.printStackTrace();
		                } catch (URISyntaxException ex) {
							ex.printStackTrace();
						}
		            }
		        }
			}
		});
		editorPane.setLocale(Locale.SIMPLIFIED_CHINESE);
		editorPane.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 12));
		editorPane.setContentType("text/html");
		scrollPane.setViewportView(editorPane);
		btnHelloSwing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doQA();
			}
		});
	}
	
	private void doQA() {
		progressBar.setVisible(true);
		progressBar.setIndeterminate(true);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				final StringBuilder sb = new StringBuilder();
				
				String question = textField.getText();
				if (question.equals("")) {
					return;
				}
				
				ThreadingSearcher threadingSearcher = new ThreadingSearcher();
				
				// Question Analysis
				AnalyzedQuestion analyzedQuestion = QuestionAnalyzer.analyze(question);
				System.out.println(analyzedQuestion);
				
				// Query Generation
				List<Query> queries = QueryGenerator.generateQueries(analyzedQuestion);
				System.out.println("Generated Queries: ");
				System.out.println(queries);
				
				// Search Bing
				List<Result> res;
				try {
					threadingSearcher.search(queries);
					res = threadingSearcher.getResult();
				} finally {
					threadingSearcher.cleanup();
				}
				
				// Answer Extraction
				List<Result> answers = AnswerExtractor.extractTopN(res, 5, 0.1);
				System.out.println("Final Result: ");
				if (answers.size() == 0) {
					sb.append("<strong>No result returned.</strong>");
				} else {
					int i = 1;
					for (Result r : answers) {
						System.out.println(r);
						sb.append(String.format(ANSWER_TEMPLATE, i++, r.getAnswer(), r.getScore(),
								r.getSentence(), r.getUrl(), r.getUrl()));
					}
				}

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						editorPane.setText(sb.toString());
						progressBar.setIndeterminate(false);
						progressBar.setVisible(false);
					}
				});
			}
		}).start();

	}
}



