package application.UserInterface;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *  Support custom painting on a panel in the form of
 *
 *  a) images - that can be scaled, tiled or painted at original size
 *  b) non solid painting - that can be done by using a Paint object
 *
 *  Also, any component added directly to this panel will be made
 *  non-opaque so that the custom painting can show through.
 */
public class BackgroundPanel extends JPanel
{
	public static final int SCALED = 0;
	
	private Paint painter;
	private Image image;
	private int style = SCALED;
	
	private boolean isTransparentAdd = true;

	/**
	 *  Set image as the background with the SCALED style
	 */
	public BackgroundPanel(Image image)
	{
		this(image, SCALED);
	}

	/**
	 *  Set image as the background with the specified style
	 */
	public BackgroundPanel(Image image, int style)
	{
		setImage( image );
		setStyle( style );
		setLayout( new BorderLayout() );
	}

	/**
	 *  Use the Paint interface to paint a background
	 */
	public BackgroundPanel(Paint painter)
	{
		setPaint( painter );
		setLayout( new BorderLayout() );
	}

	/**
	 *	Set the image used as the background
	 */
	public void setImage(Image image)
	{
		this.image = image;
		repaint();
	}

	/**
	 *	Set the style used to paint the background image
	 */
	public void setStyle(int style)
	{
		this.style = style;
		repaint();
	}

	/**
	 *	Set the Paint object used to paint the background
	 */
	public void setPaint(Paint painter)
	{
		this.painter = painter;
		repaint();
	}

	/**
	 *  Override method so we can make the component transparent
	 */
	public void add(JComponent component)
	{
		add(component, null);
	}

	/**
	 *  Override to provide a preferred size equal to the image size
	 */
	@Override
	public Dimension getPreferredSize()
	{
		if (image == null)
			return super.getPreferredSize();
		else
			return new Dimension(image.getWidth(null), image.getHeight(null));
	}

	/**
	 *  Override method so we can make the component transparent
	 */
	public void add(JComponent component, Object constraints)
	{
		if (isTransparentAdd)
		{
			makeComponentTransparent(component);
		}

		super.add(component, constraints);
	}

	/**
	 *  Controls whether components added to this panel should automatically
	 *  be made transparent. That is, setOpaque(false) will be invoked.
	 *  The default is set to true.
	 */
	public void setTransparentAdd(boolean isTransparentAdd)
	{
		this.isTransparentAdd = isTransparentAdd;
	}

	/**
	 *	Try to make the component transparent.
	 *  For components that use renderers, like JTable, you will also need to
	 *  change the renderer to be transparent. An easy way to do this it to
	 *  set the background of the table to a Color using an alpha value of 0.
	 */
	private void makeComponentTransparent(JComponent component)
	{
		component.setOpaque( false );

		if (component instanceof JScrollPane)
		{
			JScrollPane scrollPane = (JScrollPane)component;
			JViewport viewport = scrollPane.getViewport();
			viewport.setOpaque( false );
			Component c = viewport.getView();

			if (c instanceof JComponent)
			{
				((JComponent)c).setOpaque( false );
			}
		}
	}

	/**
	 *  Add custom painting
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		//  Invoke the painter for the background

		if (painter != null)
		{
			Dimension d = getSize();
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(painter);
			g2.fill( new Rectangle(0, 0, d.width, d.height) );
		}

		//  Draw the image

		if (image == null ) return;

		switch (style)
		{
			case SCALED :
				drawScaled(g);
				break;

			default:
            	drawScaled(g);
		}
	}

	/**
	 *  Custom painting code for drawing a SCALED image as the background
	 */
	private void drawScaled(Graphics g)
	{
		Dimension d = getSize();
		g.drawImage(image, 0, 0, d.width, d.height, null);
	}

}