import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Square extends JPanel implements MouseListener{
	
	
	private static final long serialVersionUID = -5854991122544829195L;
		
	static Square[][] squares;
	static int initialSquare;
	static Set<Integer> initialSquares;
//	static ArrayList<Integer[]> mineCoordinates;
	static int numberOfMines;
	static int numberOfEmpty;
	static int rows;
	static int columns;
	static int side;
	static Set<Integer> mines;
	static Random random = new Random();
 	static int flagCount;
 	static int sweepedCount = 0;
 	static int timeElapsed = 0;
 	
 	static int rowIterator = 0;
 	static int columnIterator = 0;
 	
 	static final String[] LOSERSONGS = {"vitas.wav","aqua.wav","seashanty2.wav","plasticbag.wav"};
 	static final int[] LOSERTIMERS = {3000,2075,4860,1440};
 	static int loserToggle = 1;
	
	static final Color HOVERGREEN = Color.decode("#BFE17D");
	static final Color LIGHTGREEN = Color.decode("#AAD751");
	static final Color DARKGREEN = Color.decode("#A2D149");
	static final Color LIGHTBROWN = Color.decode("#E5C29F");
	static final Color DARKBROWN = Color.decode("#D7B899");
	static final Color MINECOLOR = Color.decode("#8E2123");
	static final Color MINEBG = Color.decode("#DB3236");
	static Color nextColor = LIGHTGREEN; // light green, this is the color of the new square
	static boolean firstSweep = true;
	static boolean gameOver = false;
	static boolean multiSweep = false;
	static boolean chainedMultiSweep = false;
	
	Color squareColor; // this is the color of the current square
	Color earthColor;
	
	int[] coordinates;
	boolean isMine;
	boolean sweeped;
	boolean flagged;
	
	Integer adjacentMines;
	
	ArrayList<Integer[]> neighbors;
	
	static ImageIcon flag = new ImageIcon("flag.png");;
	static ImageIcon[] flowers = new ImageIcon[] {new ImageIcon("flower.png"), new ImageIcon("flower2.png"),
			new ImageIcon("flower3.png"),new ImageIcon("flower4.png"),new ImageIcon("flower5.png"),
			new ImageIcon("flower6.png"),new ImageIcon("flower7.png"),new ImageIcon("flower8.png"),
			new ImageIcon("flower9.png"),new ImageIcon("flower10.png"), new ImageIcon("flower11.png"),
			new ImageIcon("smallFlower.png"), new ImageIcon("smallFlower2.png"), new ImageIcon("smallFlower3.png"),
			new ImageIcon("smallFlower4.png"),new ImageIcon("smallFlower5.png"), new ImageIcon("smallFlower6.png"),
			new ImageIcon("smallFlower7.png"),new ImageIcon("smallFlower8.png"), new ImageIcon("smallFlower9.png"),
			new ImageIcon("smallFlower10.png"), new ImageIcon("smallFlower11.png")};
	
	JLabel face;
	
	static ActionListener addSecond = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(timeElapsed==999 || gameOver) {
				timer.stop();
				timeElapsed=0;
			}
			else {
				timeElapsed++;
				GameWindow.stopWatch.setText(String.format("%03d", Square.timeElapsed));
			}
			
		}
		
	};
	
	static ActionListener flagRemovalHelper = new ActionListener() {
		@Override
		
		
		public void actionPerformed(ActionEvent e) {
			if (rowIterator==rows-1 && columnIterator==columns-1) {
				singleFlagRemoval.stop();
				rowIterator=0;
				columnIterator=0;
				
				Timer flowerAddition = new Timer(500,new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						
						singleFlowerAddition.start();
						
					}
					
				});
				
				flowerAddition.setRepeats(false);
				flowerAddition.start(); 
			}
			else if (rowIterator<=rows-1 && columnIterator<columns-1) {
				squares[rowIterator][columnIterator].face.setIcon(null);
				columnIterator++;
			}
			else if (rowIterator<rows-1 && columnIterator==columns-1) {
				squares[rowIterator][columnIterator].face.setIcon(null);
				rowIterator++;
				columnIterator=0;
			}
		}	
	};
	
	static ActionListener flowerAdditionHelper = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		
			if (rowIterator==rows-1 && columnIterator==columns-1) {
				singleFlowerAddition.stop();
				rowIterator=0;
				columnIterator=0;
			}
			else if (rowIterator<=rows-1 && columnIterator<columns-1) {
				if (squares[rowIterator][columnIterator].isMine) {
					squares[rowIterator][columnIterator].face.setIcon(flowers[random.nextInt(11)+(numberOfMines/99*11)]);
				}
				columnIterator++;
			}
			else if (rowIterator<rows-1 && columnIterator==columns-1) {
				if (squares[rowIterator][columnIterator].isMine) {
					squares[rowIterator][columnIterator].face.setIcon(flowers[random.nextInt(11)+(numberOfMines/99*11)]);
				}
				rowIterator++;
				columnIterator=0;
			}
			
		}
		
	};
	
	static Timer timer = new Timer(1000, addSecond);
	static Timer singleFlagRemoval = new Timer(20, flagRemovalHelper); 
	static Timer singleFlowerAddition = new Timer(20,flowerAdditionHelper);
	
	static Timer loserToTheBeat = new Timer(480,new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (loserToggle==1) {
				GameWindow.layeredPane.setLayer(GameWindow.endPanel, Integer.valueOf(0));
				loserToggle=0;
			}
			else {
				GameWindow.layeredPane.setLayer(GameWindow.endPanel, Integer.valueOf(4));
				loserToggle=1;
			}
		}
	});
	
	static File file;
	static Clip clip;
	
	Square(int i, int j){
		
		this.coordinates = new int[] {i,j};
		
		this.squareColor = nextColor;
		this.isMine = false;
		this.sweeped = false;
		this.flagged = false;
		
		neighbors = new ArrayList<>();
		adjacentMines = 0;
		
		if(this.squareColor==LIGHTGREEN) {
			this.earthColor = LIGHTBROWN;
		}
		else {
			this.earthColor = DARKBROWN;
		}
		
		this.setBackground(squareColor);
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		
		if (GameWindow.difficulty=="Hard"){
			flag = new ImageIcon("hardFlag.png");
		}
		else {
			flag = new ImageIcon("flag.png");
		}
		
		face = new JLabel();
	    this.add(face,BorderLayout.CENTER);
	    face.setHorizontalAlignment(JLabel.CENTER);
		face.setVerticalAlignment(JLabel.CENTER);
		this.addMouseListener(this);
		
	}	
	
	public static void plantMines() {
		
		 mines = new LinkedHashSet<Integer>();
		
		while(mines.size()<numberOfMines) {
			
			Integer next = random.nextInt(rows*columns-1);
			
			if(!Square.initialSquares.contains(next)){
				
				mines.add(next);
				
			}
		}
		
		for(int i=0,squareNum=0;i<rows;i++) {
			
			for(int j=0;j<columns;j++,squareNum++) {
				
				if(mines.contains(squareNum)) {
					squares[i][j].isMine=true;
///					Square.mineCoordinates.add(new Integer[] {i,j});
					squares[i][j].neighbors.forEach((n) -> squares[n[0]][n[1]].adjacentMines+=1);
				}
							
			}
			
		}
		
		squares[initialSquare/columns][initialSquare%columns].sweep();
	}

	public static void switchNextColor(){
			
		if(nextColor==LIGHTGREEN) {
			nextColor=DARKGREEN; // dark green
		}
		else {
			nextColor=LIGHTGREEN; // light green
		}
				
	}
	
	public void toggleFlag() {
		
		if(flagged==true){
			
			audio("flagRemoved.wav");
			face.setIcon(null);
			flagged=false;
			flagCount++;
			GameWindow.flagCount.setText(String.valueOf(flagCount));
			
		}
		else {
			
			audio("flagPlaced.wav");
			face.setIcon(flag);
			flagged=true;
			flagCount--;
			GameWindow.flagCount.setText(String.valueOf(flagCount));
			
		}
		
	}
	
	public void sweep(){
		
		if (firstSweep) {
			
			initialSquare = this.coordinates[0]*columns+this.coordinates[1];
			
			initialSquares = new HashSet<Integer>();
			initialSquares.add(initialSquare);
			
			for (int i=0;i<this.neighbors.size();i++) {
				initialSquares.add(this.neighbors.get(i)[0]*columns+this.neighbors.get(i)[1]);
				
			}
	
			firstSweep=false;
			
			timer.start();
			
			plantMines();

		}
		
		else if (!this.sweeped){
			
			this.sweeped=true;
			
			if(this.isMine==true) {
				
//				audio("loserSound.wav");
				
				int songSelect = random.nextInt(LOSERSONGS.length);
				
				audio(LOSERSONGS[songSelect]);
				
				gameOver = true;
				
				this.revealMine();
				
				for (Iterator<Integer> itr = mines.iterator(); itr.hasNext();) {
						
						Integer mine = itr.next();
						squares[mine/columns][mine%columns].revealMine();
						
					}
				
				GameWindow.status.setForeground(MINECOLOR);
				GameWindow.playAgain.setForeground(MINECOLOR);
				GameWindow.status.setText("You Lose");
				GameWindow.endPanel.setBackground(MINEBG);
				GameWindow.playAgain.setBackground(Color.decode("#660912"));
				GameWindow.endPanel.setOpaque(true);
				GameWindow.endPanel.setBorder(BorderFactory.createLineBorder(MINECOLOR,3));
				
				Timer loserShow = new Timer(LOSERTIMERS[songSelect],new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						GameWindow.layeredPane.add(GameWindow.endPanel,Integer.valueOf(4));
						if (songSelect==3) {
							loserToTheBeat.start();

						}
					}
					
				});
				
				loserShow.setRepeats(false);
				loserShow.start();
				
			}
			
			else {
				
				sweepedCount++;
				face.setIcon(null);
				
				if (this.adjacentMines>0) {
					
					if (chainedMultiSweep==true) {
						
						Timer chainedMultiOffTimer = new Timer(50,new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								
								chainedMultiSweep = false;
								
							}
						});
						
						chainedMultiOffTimer.setRepeats(false);
						chainedMultiOffTimer.start();
						
					}
					
					face.setText(adjacentMines.toString());
					face.setFont(new Font("Times New Roman",Font.BOLD,side*4/5));
					
					if(adjacentMines==1) {
						if (multiSweep==false) {audio("oneRevealed.wav");}
						face.setForeground(Color.decode("#1A76D2"));
					}
					
					if(adjacentMines==2) {
						if (multiSweep==false) {audio("twoRevealed.wav");}
						face.setForeground(Color.decode("#388E3B"));
					}
					
					if(adjacentMines==3) {
						if (multiSweep==false) {audio("threeRevealed.wav");}
						face.setForeground(Color.decode("#D32F2F"));
					}
					
					if(adjacentMines==4) {
						if (multiSweep==false) {audio("fourRevealed.wav");}
						face.setForeground(Color.decode("#7B1FA2"));
					}
					
					if(adjacentMines==5) {
						if (multiSweep==false) {audio("fiveRevealed.wav");}
						face.setForeground(Color.decode("#FF8F00"));
					}
					
					if(adjacentMines==6) {
						if (multiSweep==false) {audio("fiveRevealed.wav");}
						face.setForeground(Color.decode("#0097A7"));
					}
					
					if(adjacentMines==7) {
						if (multiSweep==false) {audio("fiveRevealed.wav");}
						face.setForeground(Color.decode("#49423D"));
					}
					
					if(adjacentMines==8) {
						if (multiSweep==false) {audio("fiveRevealed.wav");}
						face.setForeground(Color.decode("#717171"));
					}
					
				}
				
				else {
					
					multiSweep=true;
					
					if (chainedMultiSweep==false) {
						audio("firstClick.wav");
					}
					
					chainedMultiSweep=true;
					
					this.neighbors.forEach((n) -> {
						
						if(!squares[n[0]][n[1]].isMine) {
							
							squares[n[0]][n[1]].sweep();
							
						}
					
					});
					
					Timer multiSweepOffTimer = new Timer(50, new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							multiSweep=false;
						}
						
					});
					
					multiSweepOffTimer.setRepeats(false);
					multiSweepOffTimer.start();
					
				}
				
				this.squareColor = earthColor;
				this.setBackground(squareColor);
				
				if (sweepedCount==numberOfEmpty) {
					
					if (gameOver==false) {
						gameOver=true;
					
						if (GameWindow.difficulty != "Easy") {
							audio("winnerSound.wav");
						} else {
							audio("winnereasy.wav");
						}
						
						Timer flagRemoval = new Timer(700,new ActionListener() {
	
							@Override
							public void actionPerformed(ActionEvent e) {
								
								singleFlagRemoval.start();
								
							}
							
						});
						
						flagRemoval.setRepeats(false);
						flagRemoval.start();
						
						GameWindow.status.setForeground(Color.decode("#348F36"));
						GameWindow.playAgain.setForeground(Color.decode("#043500"));
						GameWindow.status.setText("You Win!");
						GameWindow.playAgain.setBackground(Color.decode("#348F36"));
						GameWindow.endPanel.setBackground(LIGHTGREEN);
						GameWindow.endPanel.setOpaque(true);
						GameWindow.endPanel.setBorder(BorderFactory.createLineBorder(DARKGREEN,3));
						
						
						Timer winnerShow = new Timer(6500,new ActionListener() {
	
							@Override
							public void actionPerformed(ActionEvent e) {
								GameWindow.layeredPane.add(GameWindow.endPanel,Integer.valueOf(4));
							}
							
						});
						
						winnerShow.setRepeats(false);
						winnerShow.start();
						
					}
					
				}
				
			}
			
		}
		
	}
	
	public void revealMine() {
		
		this.remove(face);
		this.repaint();
		this.squareColor=MINEBG;
		this.setBackground(squareColor);
		this.sweeped=true;
		
	}

	
	public void paint(Graphics g) {
		super.paint(g);
		
		if(isMine==true && this.sweeped==true) {
			
			Graphics2D g2D = (Graphics2D) g;
			g2D.setPaint(MINECOLOR);
			g2D.fillOval(side*3/10,side*3/10,side*2/5,side*2/5);
			
		}
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		if(!this.sweeped && !gameOver) {
			
			if(e.getButton()==MouseEvent.BUTTON3) {
				toggleFlag();
			}
			
			else if(e.getButton()==MouseEvent.BUTTON1 && flagged==false) {
				
				this.sweep();
				
			}
			
		}
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
		if(!this.sweeped && !gameOver) {
			this.setBackground(HOVERGREEN);
		}
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		
		this.setBackground(this.squareColor);
		
	}
	
	public static void audio(String fileName) {
		
		if(!GameWindow.muted) {
		
		file = new File(fileName);
		AudioInputStream audioStream;
		
			try {
				
				audioStream = AudioSystem.getAudioInputStream(file);
				clip = AudioSystem.getClip();
				clip.open(audioStream);
				clip.start();
	
			} catch (UnsupportedAudioFileException | IOException| LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}
	
}
