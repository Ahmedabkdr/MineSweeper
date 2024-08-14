import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sound.sampled.FloatControl;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class GameWindow extends JFrame implements MouseListener{

	private static final long serialVersionUID = 1L;
	
	static JLayeredPane layeredPane;
	
	Grid grid;
	JPanel banner;
	static JPanel endPanel;
	static JComboBox<String> difficultyMenu;
	static JLabel flagCount;
	static JLabel stopWatch;
	static JLabel status;
	static JButton sound;
	static JButton playAgain;
	
	
	static ImageIcon audioOnIcon = new ImageIcon("audioOn.png");
	static ImageIcon mutedIcon = new ImageIcon("muted.png");
	
	static String difficulty = "Easy";
	static boolean muted = false;
	
	static int width;
	static int height;
	static int numberOfMines;
	static int rows;
	static int columns;
	static int side;
	
	GameWindow(){
		
		Square.loserToTheBeat.stop();
		
		if(difficulty=="Easy") {
			
			numberOfMines=Grid.numberOfMines=Square.numberOfMines=Square.flagCount=10;
			rows=Grid.rows=Square.rows=8;
			columns=Grid.columns=Square.columns=10;
			side=Grid.side=Square.side=50;
			width=Grid.side*columns; 
			height=Grid.side*rows+28+50; //28 for title and 50 for banner
			this.setSize(width,height);
			this.setLocationRelativeTo(null);
		}
		else if(difficulty=="Medium") {
			
			numberOfMines=Grid.numberOfMines=Square.numberOfMines=Square.flagCount=40;
			rows=Grid.rows=Square.rows=14;
			columns=Grid.columns=Square.columns=18;
			side=Grid.side=Square.side=42;
			width=Grid.side*columns; 
			height=Grid.side*rows+28+50; //28 for title and 50 for banner
			this.setBounds(
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2)-width/2,0,width,height
			); 
		}
		else {
			
			numberOfMines=Grid.numberOfMines=Square.numberOfMines=Square.flagCount=99;
			rows=Grid.rows=Square.rows=20;
			columns=Grid.columns=Square.columns=24;
			side=Grid.side=Square.side=30;
			width=Grid.side*columns; 
			height=Grid.side*rows+28+50; //28 for title and 50 for banner
			this.setBounds(
				(int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2)-width/2,0,width,height
			); 
		}
		
		Square.numberOfEmpty=rows*columns-numberOfMines;
		
		this.setTitle("MineSweeper");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
		this.setLayout(null);
		
		layeredPane = new JLayeredPane();
		layeredPane.setBounds(0,0,width,height);
		
		banner = new JPanel();
		banner.setBounds(0,0,width,50);
		banner.setBackground(Color.decode("#4A752C"));
		banner.setLayout(null);
		
		String[] difficulties = {"Easy","Medium","Hard"};
		difficultyMenu = new JComboBox<>(difficulties);
		for (int i=0; i<3; i++){
			if(difficulties[i]==difficulty) {
				difficultyMenu.setSelectedIndex(i);
			}
		}
		difficultyMenu.setBounds(10,8,115,40);
		difficultyMenu.setFont(new Font(null,Font.BOLD,15));
		difficultyMenu.addActionListener((e)-> {
				
			if (difficultyMenu.getSelectedItem()!=difficulty && Square.gameOver==false) {
				difficulty = (String) difficultyMenu.getSelectedItem();
				this.dispose();
				Square.timeElapsed=0;
				Square.timer.stop();
				Square.firstSweep=true;
				Square.sweepedCount=0;
				GameWindow window = new GameWindow();
			}
			
		});
		
		flagCount = new JLabel();
		flagCount.setBounds(width/2-69,5,125,40);
		flagCount.setIcon(Square.flag);
		flagCount.setText(String.valueOf(Square.flagCount));
		flagCount.setFont(new Font("Comic Sans",Font.BOLD,18));
		flagCount.setIconTextGap(9);
		flagCount.setForeground(Color.WHITE);
		
		ImageIcon stopWatchIcon = new ImageIcon("stopwatch.png");
		stopWatch= new JLabel();
		stopWatch.setBounds(width/2+9, 5, 100, 40);
		stopWatch.setIcon(stopWatchIcon);
		stopWatch.setText(String.format("%03d", Square.timeElapsed));
		stopWatch.setFont(new Font("Comic Sans",Font.BOLD,18));
		stopWatch.setIconTextGap(8);
		stopWatch.setForeground(Color.WHITE);
		
		sound = new JButton();
		sound.addMouseListener(this);
		sound.setBounds(width-55, 5, 50, 40); // top right corner
		sound.setBorderPainted(false);
		sound.setIcon(audioOnIcon);
		
		status = new JLabel();
		int statusFontSize = 50;
		int statusWidth = 239;
		
		status.setBounds((statusWidth+50)/2-statusWidth/2,5,statusWidth,statusFontSize);
		status.setLayout(new BorderLayout());
		status.setHorizontalTextPosition(JLabel.CENTER);
		status.setHorizontalAlignment(JLabel.CENTER);
		status.setFont(new Font("Lucida Bright",Font.BOLD,statusFontSize));
		
		
		playAgain = new JGradientButton("Play Again");
		int playFontSize = 20;
		int playWidth = 200;
		
		playAgain.setBounds((statusWidth+50)/2-playWidth/2,statusFontSize+100-playFontSize-55,playWidth,playFontSize+20);
		playAgain.setLayout(new BorderLayout());
		playAgain.setBorderPainted(false);
		playAgain.setHorizontalTextPosition(JButton.CENTER);
		playAgain.setHorizontalAlignment(JButton.CENTER);
		playAgain.setFont(new Font("Lucida Bright",Font.BOLD,playFontSize));
		playAgain.addActionListener(e -> {
			
			this.dispose();
			GameWindow window = new GameWindow();
			Square.gameOver=false;
			Square.firstSweep=true;
			Square.sweepedCount=0;
			Square.clip.stop();
			
			});
		
		endPanel = new JPanel();
		endPanel.setBounds(width/2-statusWidth/2-25,(height-28)/2-statusFontSize/2-50,statusWidth+50,statusFontSize+100);
		endPanel.setLayout(null);
	    endPanel.add(status);
		endPanel.add(playAgain);
		
		banner.add(difficultyMenu);
		banner.add(flagCount);
		banner.add(stopWatch);
		banner.add(sound);
		
		grid = new Grid();
		grid.setBounds(0,50,width,height-50);
		
		layeredPane.add(banner,Integer.valueOf(1));
		layeredPane.add(grid,Integer.valueOf(1));
		
		this.add(layeredPane);
		this.setResizable(false);
		this.setVisible(true);
		
		if (Square.clip!=null) {
			if (Square.clip.isOpen()){
				Square.clip.close();
			}
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		if (!muted) {
			muted=true;
			if(Square.clip!=null) {
				Square.clip.stop();
			}
			sound.setIcon(mutedIcon);
		}
		else {
			muted=false;
			if(Square.clip!=null) {
				Square.clip.start();
			}
			sound.setIcon(audioOnIcon);
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
