package application.UserInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

/**
 * CloseableTabbedPane implementation based
 * on code project: 
 * http://www.codeproject.com/KB/tabs/JTabbedPane.aspx?display=Print
 */
public class ClosableTabbedPane extends JTabbedPane{
	
	/** The UI for drawing the close button */
	private TabCloseUI closeUI = new TabCloseUI(this);
	
	/** override of the tabbedPane paint method to
	 * also draw the close button UI
	 */
	@Override
	public void paint(Graphics g){
		super.paint(g);
		closeUI.paint(g);
	}
	
	/**
	 * Add a tab to the pane with added spacing
	 */
	@Override
	public void addTab(String title, Component component) {
		super.addTab(title+"    ", component);
	}
	
	/**
	 * Method to return the title of a tab
	 * @param index of the tab
	 * @return title of the tab
	 */
	public String getTabTitleAt(int index) {
		return super.getTitleAt(index).trim();
	}
	
	private class TabCloseUI implements MouseListener, MouseMotionListener {
		
		/** the tabbed pane being that is used by this instance */
		private ClosableTabbedPane  tabbedPane;
		
		/** position of the mouse and tab */
		private int closeX = 0 ,closeY = 0, meX = 0, meY = 0;
	
		/** the currently selected tab */
		private int selectedTab;
		
		/** the width of the close UI */
		private final int  width = 5;
		
		/** the height of the close UI */
		private final int  height = 5;
		
		/** selection rectangle for the close UI */
		private Rectangle rectangle = new Rectangle(0,0,width, height);
		
		public TabCloseUI(ClosableTabbedPane pane) {
			
			tabbedPane = pane;
			tabbedPane.addMouseMotionListener(this);
			tabbedPane.addMouseListener(this);
		}
		
		/** mouse events */
		@Override 
		public void mouseEntered(MouseEvent me) {}
		@Override
		public void mouseExited(MouseEvent me) {}
		@Override
		public void mousePressed(MouseEvent me) {}
		@Override
		public void mouseClicked(MouseEvent me) {}
		@Override
		public void mouseDragged(MouseEvent me) {}
		
		
		/** event when close button has been clicked and mouse has been released */
		@Override
		public void mouseReleased(MouseEvent me) {
			if(closeUnderMouse(me.getX(), me.getY())){
				boolean isToCloseTab = tabAboutToClose(selectedTab);
				if (isToCloseTab && selectedTab > -1){			
					tabbedPane.removeTabAt(selectedTab);
				}
				selectedTab = tabbedPane.getSelectedIndex();
			}
		}

		@Override
		public void mouseMoved(MouseEvent me) {	
			meX = me.getX();
			meY = me.getY();			
			if(mouseOverTab(meX, meY)){
				controlCursor();
				tabbedPane.repaint();
				
			}
		}

		private void controlCursor() {
			if(tabbedPane.getTabCount()>0)
				if(closeUnderMouse(meX, meY)){
					tabbedPane.setCursor(new Cursor(Cursor.HAND_CURSOR));	
					if(selectedTab > -1)
						tabbedPane.setToolTipTextAt(selectedTab, "Close " +tabbedPane.getTitleAt(selectedTab));
				}
				else{
					tabbedPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					if(selectedTab > -1)
						tabbedPane.setToolTipTextAt(selectedTab,"");
				}	
		}

		/**
		 * Method to close a tab when a close button is clicked
		 * during a mouse over event
		 * @param x coordinate of the mouse
		 * @param y coordinate of the mouse
		 * @return boolean
		 */
		private boolean closeUnderMouse(int x, int y) {		
			rectangle.x = closeX;
			rectangle.y = closeY;
			return rectangle.contains(x,y);
		}

		/**
		 * Main draw method of the close UI for 
		 * the tabbedPane
		 * @param Graphics component
		 */
		public void paint(Graphics g) {
			
			int tabCount = tabbedPane.getTabCount();
			for(int j = 0; j < tabCount; j++)
				if(tabbedPane.getComponent(j).isShowing()){			
					int x = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width -width-5;
					int y = tabbedPane.getBoundsAt(j).y +5;	
					drawClose(g,x,y);
					break;
				}
			if(mouseOverTab(meX, meY)){
				drawClose(g,closeX,closeY);
			}
		}

		/**
		 * Method to draw the close button
		 * @param Graphic component
		 * @param x coordinate of the mouse
		 * @param y coordinate of the mouse
		 */
		private void drawClose(Graphics g, int x, int y) {
			if(tabbedPane != null && tabbedPane.getTabCount() > 0){
				Graphics2D g2 = (Graphics2D)g;				
				drawColored(g2, isUnderMouse(x,y)? Color.DARK_GRAY : Color.GRAY, x, y);
			}
		}

		/**
		 * Core method to draw the close button
		 * @param Graphics2D component
		 * @param color of the button
		 * @param x coordinate where to draw
		 * @param y coordinate where to draw
		 */
		private void drawColored(Graphics2D g2, Color color, int x, int y) {
			
			// Draw outline of close button by creating a bigger button under it
			g2.setStroke(new BasicStroke(5,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
			g2.setColor(Color.WHITE);
			g2.drawLine(x, y, x + width, y + height);
			g2.drawLine(x + width, y, x, y + height);
			
			// Draw the inner part of the close button by drawing a smaller button on top
			g2.setColor(color);
			g2.setStroke(new BasicStroke(3, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
			g2.drawLine(x, y, x + width, y + height);
			g2.drawLine(x + width, y, x, y + height);

		}

		/**
		 * Method to check if the tab is located under
		 * the mouse pointer
		 * @param x coordinate of the mouse
		 * @param y coordinate of the mouse
		 * @return true if under mouse else false
		 */
		private boolean isUnderMouse(int x, int y) {
			if(Math.abs(x-meX)<width && Math.abs(y-meY)<height )
				return  true;		
			return  false;
		}

		/**
		 * Method for drawing the close button at a mouse over event
		 * @param x coordinate of the tab
		 * @param y coordinate of the tab
		 * @return
		 */
		private boolean mouseOverTab(int x, int y) {
			int tabCount = tabbedPane.getTabCount();
			for(int j = 0; j < tabCount; j++)
				if(tabbedPane.getBoundsAt(j).contains(meX, meY)){
					selectedTab = j;
					closeX = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width -width-5;
					closeY = tabbedPane.getBoundsAt(j).y +5;					
					return true;
				}
			return false;
		}

	}

	/**
	 * Actions to perform when a tab is about to close,
	 * use this to create confirmation dialogs or exit code
	 * @param index of the closing tab
	 * @return success of closure
	 */
	public boolean tabAboutToClose(int tabIndex) {
		return true;
	}

	
}
