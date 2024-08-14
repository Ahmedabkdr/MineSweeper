import java.awt.GridLayout;

import javax.swing.JPanel;

public class Grid extends JPanel{

	private static final long serialVersionUID = 2892865424401791072L;
	
	static int numberOfMines;
	static int rows;
	static int columns;
	static int side; //side length
	
	Square[][] squares;
	
	Grid(){
		
		this.setLayout(null);	
		
		squares = new Square[rows][columns];
		
		for(int i=0;i<rows;i++) {
			
			Square.switchNextColor();
			
			for(int j=0;j<columns;j++) {
				
				Square.switchNextColor();
				
				this.squares[i][j] = new Square(i,j);
				this.squares[i][j].setBounds(j*side,i*side,side,side);
				
				this.addNeighbors(i,j);
								
				this.add(this.squares[i][j]);	
			}
			
		}
		
		Square.squares = squares;
		
	}
	
	public void addNeighbors(int i, int j) {
		
		if (i>0) {
			
			this.squares[i][j].neighbors.add(new Integer[] {i-1,j});
			
			if (j>0) {
				
				this.squares[i][j].neighbors.add(new Integer[] {i-1,j-1});
				
			}
			
			if (j<columns-1) {
				
				this.squares[i][j].neighbors.add(new Integer[] {i-1,j+1});
				
			}
			
		}
		
		if (i<rows-1) {
			
			this.squares[i][j].neighbors.add(new Integer[] {i+1,j});
			
			if (j>0) {
				
				this.squares[i][j].neighbors.add(new Integer[] {i+1,j-1});
				
			}
			
			if (j<columns-1) {
				
				this.squares[i][j].neighbors.add(new Integer[] {i+1,j+1});
				
			}
			
		} 
			
		if (j>0) {
			
			this.squares[i][j].neighbors.add(new Integer[] {i,j-1});
			
		}
		
		if (j<columns-1) {
			
			this.squares[i][j].neighbors.add(new Integer[] {i,j+1});
			
		} 
		
	}
}

