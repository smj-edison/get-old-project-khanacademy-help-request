package com.mason.getOldRequest;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

class MyJFrame extends JFrame {
	//body, so the menu bar is not in the flow layout
	static JPanel f;
	//progress bar, not implemented
	static JProgressBar prog;
	static int contentLength = 100;
	static int downloaded = 0;
	//"Downloading, please wait." label
	static JLabel l;
	//so text is copyable
	static JTextPane out;
	// min/max for picking
	static int min = 25;
	static int max = 300;
	static String[] links;
	//to call commands
	public static Main m;
	//menu bar
	static JMenuBar bar;
	static JMenu file;
	static JMenuItem download;
	static JMenuItem getThing;
	static JMenuItem setMinMax;
	public MyJFrame(String s, Main m){
		/////////////init/////////////
		super(s);
		f = new JPanel();
		this.m = m;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		f.setLayout(new FlowLayout());
		f.setSize(200, 75);
		
		setSize(200, 100);
		setVisible(true);
		/////////////menu bar/////////////
		out = new JTextPane();
		out.setContentType("text/html");
		out.setEditable(false);
		out.setBackground(null);
		out.setBorder(null);
		out.setVisible(false);
		
		bar = new JMenuBar();
		file = new JMenu("File");
		download = new JMenuItem("Re-download links");
		getThing = new JMenuItem("Get link (copys to clipboard)");
		setMinMax = new JMenuItem("Set min/max project number to pick from");
		
		//start the download
		download.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				startDownload();
			}
		});
		//copy link to clipboard and show it
		getThing.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				out.setVisible(true);
				int r = rand(min, max);
				out.setText("https://khanacademy.org" + links[r]);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        clipboard.setContents(new StringSelection("https://khanacademy.org" + links[r]), new StringSelection("khanacademy.org" + "link here:"));
			}
		});
		setMinMax.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				min = Integer.parseInt(JOptionPane.showInputDialog(null, "Type in new Minimum:", "Set Min:", JOptionPane.PLAIN_MESSAGE));
				max = Integer.parseInt(JOptionPane.showInputDialog(null, "Type in new Maximum  (effects how many are downloaded):", "Set Max:", JOptionPane.PLAIN_MESSAGE));
				//save data file
				Main.saveData(min, max);
				startDownload();
			}
		});
		//menu bar
		file.add(setMinMax);
		file.add(getThing);
		file.add(download);
		bar.add(file);
		add(bar, BorderLayout.NORTH);
		/////////////download progress/////////////
		l = new JLabel("Downloading, please wait.");
		
		prog = new JProgressBar(0, 4);
		prog.setStringPainted(true);
		
		l.setVisible(false);
		prog.setVisible(false);
		/////////////add everything/////////////
		f.add(out);
		f.add(l);
		f.add(prog);
		
		add(f, BorderLayout.CENTER);
	}
	//pick random int
	static public int rand(int mi, int ma){
		ma++;
		return (int) Math.floor(Math.random() * (ma - mi) + mi);
	}
	void setLink(String[] links){
		this.links = links;
	}
	int getMin(){
		return min;
	}
	int getMax(){
		return max;
	}
	void setMin(int min){
		if(new Integer(min) == null){
			min = 0;
		}
		this.min = min;
	}
	void setMax(int max){
		if(new Integer(max) == null){
			max = 0;
		}
		this.max = max;
	}
	void startDownload(){
		out.setVisible(false);
		l.setVisible(true);
		//progress bar will not progress
		//prog.setVisible(true);
		prog.setValue(0);
		//starts thread for downloading in main
		m.downloading = true;
	}
	void doneDownloading(){
		l.setVisible(false);
		prog.setVisible(false);
		m.downloading = false;
	}
	public static void setDownloaded(int d){
		downloaded = d;
		prog.setValue(d);
	}
	public static void setContentLength(int c){
		contentLength = c;
		prog.setMaximum(c);
	}
}
public class Main {
	static volatile boolean downloading = false;
	public static volatile MyJFrame frame;
	static final String DATA_FOLDER_NAME = System.getProperty("user.home") + System.getProperty("file.separator") + ".old_programs";
	static final Path DATA_FOLDER = Paths.get(DATA_FOLDER_NAME);
	static final Path LINK_FILE = Paths.get(DATA_FOLDER_NAME + System.getProperty("file.separator") + "helpRequests.txt");
	static final Path SAVE_FILE = Paths.get(DATA_FOLDER_NAME + System.getProperty("file.separator") + "minmax.txt");
	public static String parseString = "";
	static boolean shouldDownload = false;
	public static List<byte[]> l;
	public static String s;
	public static int min = 25;
	public static int max = 300;
	public static String[] links;
	private void constructGUI(){
		frame = new MyJFrame("Get old help requests", this);
		//make sure all folders and files defined
		if(!Files.exists(DATA_FOLDER)){
			try {
				Files.createDirectory(DATA_FOLDER);
			} catch(IOException l){
				l.printStackTrace();
				System.exit(1);
			}
		}
		if(!Files.exists(LINK_FILE)){
			shouldDownload = true;
		} else {
			read();
		}
		if(!Files.exists(SAVE_FILE)){
			try {
				Files.createFile(SAVE_FILE);
				saveData(frame.getMin(), frame.getMax());
			} catch(IOException m){
				m.printStackTrace();
				System.exit(1);
			}
		}
		//read data file
		readData();
		//
		if(shouldDownload){
			frame.startDownload();
		}
	}
	public static void main(String[] args){
		final Main m = new Main();
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				m.constructGUI();
			}
		});
		//TODO: keep while loop from disabling swing window
		while(true){
			//wait until ready to download maybe TODO: use interrups from MyJFrame to start download
			while(!downloading){
				try {
					Thread.sleep(500);
				} catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						Parser.main(m, frame.getMax());
					}
				});
			} catch(InvocationTargetException | InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	static public int rand(int mi, int ma){
		ma++;
		return (int) Math.floor(Math.random() * (ma - mi) + mi);
	}
	static public int constrain(int va, int mi, int ma){
		return Math.min(Math.max(va, mi), ma);
	}
	//save min/max maybe more in the future
	static public void saveData(int min, int max){
		try {
			Files.write(SAVE_FILE, ("" + min + "," + max).getBytes("US-ASCII"));
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	//read min/max
	static public void readData(){
		String st;
		String[] sa;
		try {
			st = Files.readAllLines(SAVE_FILE, Charset.forName("US-ASCII")).get(0);
			sa = st.split("[,]+");
			if(st.indexOf(",") > 0){
				frame.setMin(Integer.parseInt(sa[0]));
				frame.setMax(Integer.parseInt(sa[1]));
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	//read all links
	static public void read(){
		try {
			File file = LINK_FILE.toFile(); 
			FileInputStream fip = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fip);
			Scanner i = new Scanner(in);
			s = i.nextLine();
			i.close();
			links = s.split("[,]+");
			frame.setLink(links);
			min = frame.getMin();
			max = frame.getMax();
			if(max <= min){
				min = 1;
				max = 100;
			}
			min = constrain(min, 0, links.length - 1);
			max = constrain(max, 0, links.length - 1);
			frame.setMin(min);
			frame.setMax(max);
			frame.doneDownloading();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
}
