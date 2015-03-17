package com.datazuul.apps.imageviewer;

//==============================================================================
//Example Java Slide Show Applications
//Copyright: Jasper Potts 2005
//==============================================================================

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

public class SlideShowApp {

    private static List IMAGES = new ArrayList();
    Frame mainFrame;
    GraphicsConfiguration gc = null;

    public SlideShowApp(int numBuffers, GraphicsDevice device) {
	try {
	    gc = device.getDefaultConfiguration();
	    mainFrame = new Frame(gc);
	    mainFrame.addMouseListener(new MouseInputAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    System.exit(0);
		}
	    });
	    mainFrame.setUndecorated(true);
	    mainFrame.setIgnoreRepaint(true);
	    device.setFullScreenWindow(mainFrame);

	    Rectangle bounds = mainFrame.getBounds();
	    mainFrame.createBufferStrategy(numBuffers);
	    BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();

	    BufferedImage oTranslucentImage = gc.createCompatibleImage(bounds.width, bounds.height,
		    Transparency.TRANSLUCENT);

	    BufferedImage oImage = loadScaled((File) IMAGES.get(0), bounds.width, bounds.height);

	    BufferedImage oNextImage;
	    for (int iImageIndex = 0; iImageIndex < (IMAGES.size() - 1); iImageIndex++) {
		oNextImage = null;
		for (float a = 1; a <= 10; a += 0.75) {
		    Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
		    if (!bufferStrategy.contentsLost()) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, bounds.width, bounds.height);
			if (oNextImage == null) {
			    oNextImage = loadScaled((File) IMAGES.get(iImageIndex + 1), bounds.width, bounds.height);
			    Graphics2D g2 = oTranslucentImage.createGraphics();
			    g2.setColor(new Color(0, 0, 0, 255));
			    g2.fillRect(0, 0, bounds.width, bounds.height);
			    g2.drawImage(oNextImage, (bounds.width - oNextImage.getWidth()) / 2,
				    (bounds.height - oNextImage.getHeight()) / 2, null);
			    g2.dispose();
			}
			g.drawImage(oImage, (bounds.width - oImage.getWidth()) / 2,
				(bounds.height - oImage.getHeight()) / 2, null);
			float fAlpha = (float) Math.log10(a);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fAlpha));
			g.drawImage(oTranslucentImage, (bounds.width - oTranslucentImage.getWidth()) / 2,
				(bounds.height - oTranslucentImage.getHeight()) / 2, null);
			bufferStrategy.show();
			g.dispose();
		    }
		    try {
			Thread.sleep((a == 1) ? 2000 : 1);
		    } catch (InterruptedException e) {
		    }
		}
		oImage = oNextImage;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    device.setFullScreenWindow(null);
	    JOptionPane.showMessageDialog(null, "Error \"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(1);
	} finally {
	    device.setFullScreenWindow(null);
	}
    }

    public BufferedImage loadScaled(File i_oImageFile, int i_iWidth, int i_iHeight) throws Exception {
	BufferedImage oImage = ImageIO.read(i_oImageFile);
	double dScale = calculateScaleFactor(oImage.getWidth(), oImage.getHeight(), i_iWidth, i_iHeight);
	if (dScale < 1) {
	    int iWidth = (int) (oImage.getWidth() * dScale);
	    int iHeight = (int) (oImage.getHeight() * dScale);
	    BufferedImage oScaledImage = gc.createCompatibleImage(iWidth, iHeight);
	    Graphics2D g = oScaledImage.createGraphics();
	    g.drawImage(oImage, 0, 0, iWidth, iHeight, null);
	    g.dispose();
	    return oScaledImage;
	} else {
	    return oImage;
	}
    }

    public static void main(String[] args) throws Exception {
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	JFileChooser oFileChooser = new JFileChooser();
	if (args.length > 0)
	    oFileChooser.setSelectedFile(new File(args[0]));
	oFileChooser.setDialogTitle("Select a directory containing jpeg images");
	oFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	if (oFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	    // scan for images
	    findImages(oFileChooser.getSelectedFile());
	    if (IMAGES.size() == 0) {
		JOptionPane.showMessageDialog(null, "No jpeg images found in \"" + oFileChooser.getSelectedFile()
			+ "\"", "No Images Found", JOptionPane.ERROR_MESSAGE);
	    } else {
		// do slideshow
		int numBuffers = 2;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		new SlideShowApp(numBuffers, device);
	    }
	}
    }

    private static void findImages(File i_oFile) {
	if (i_oFile.isDirectory()) {
	    File[] oChildren = i_oFile.listFiles(new FileFilter() {
		public boolean accept(File i_oFile) {
		    return (i_oFile.isDirectory()) || (i_oFile.getName().toLowerCase().endsWith(".jpg"));
		}
	    });
	    for (int i = 0; i < oChildren.length; i++) {
		findImages(oChildren[i]);
	    }
	} else {
	    IMAGES.add(i_oFile);
	}
    }

    public static final double calculateScaleFactor(int i_iSrcWidth, int i_iSrcHeight, int i_iReqWidth, int i_iReqHeight) {
	double dXscale = (double) i_iReqWidth / (double) i_iSrcWidth;
	double dYscale = (double) i_iReqHeight / (double) i_iSrcHeight;
	return Math.min(dXscale, dYscale);
    }

}