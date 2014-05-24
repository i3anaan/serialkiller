package application.UserInterface;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *  Extension of JPanel where an image is used to 
 *  draw a transparent background
 */
public class BackgroundPanel extends JPanel
{
	private Image image;

	/**
	 *  Creates a new BackgroundPanel with the specified image as
	 *  a background
	 */
	public BackgroundPanel(Image image)
	{
		this.image = image;
		setLayout(new BorderLayout());
		repaint();
	}

	/**
	 *  Override method to shorthand our add method with null constraint
	 */
	public void add(JComponent component)
	{
		add(component, null);
	}

	/**
	 *  Overrides the add method to make the added component transparent
	 */
	public void add(JComponent component, Object constraints)
	{
		component.setOpaque(false);
		super.add(component, constraints);
	}

	/**
	 *  Modifies the painter of this component to draw a background image
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		// Call the original painter
		super.paintComponent(g);

		//  Draws the image if there is one
		if (image != null ){
		Dimension d = getSize();
		g.drawImage(image, 0, 0, d.width, d.height, null);
		}
	}

}