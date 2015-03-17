package com.datazuul.apps.imageviewer2;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Author Dan Messerschmidt csc241 Title: ImageViewer.java Description: Slide show image viewer, main program Assignment
 * 1
 *
 */
public class ImageViewer extends JFrame implements Runnable {

    ImageDir iv = new ImageDir();              //file  functions
    private Thread tImg;
    private JSeparator panelSep;				//panel seperator
    private JLabel imgName, //path/file name of image
            imgIndex, //index of total images
            outImg;					//contains the image
    private JButton openDir, //button to open a directory
            openImage, //button to open a file
            saveAs, //button to save a file
            quit, //button to quit the program
            prev, //button to view previous image, in directory
            next;						//button to view the next image, in directory
    private JPanel btnPanel, //panel for buttons
            imgPanel;				//panel for image and image info
    private JSlider slider;					//slider control for timer to control slideshow
    private JToggleButton enSlideShow;		//enables/disables slidshow mode
    private ImageIcon img;						//holds selected image
    private int imgTime = 5, //slideshow intergration time
            rescaleX, //sizes image to current screen size, for x
            rescaleY, //sizes image to current screen size, for y
            fLen, //file size of image
            index;					//file location in directory
    private Timer ssTimer;				//timer for controling slideshow
    private String strFileName;		//holder for current file name
    private byte[] imgBuff, //buffer to load file into
            imgData;				//buffer to diplay image from
    private boolean isSlideShow = false, //if slideshow is enabled
            isDir = false;					//f a directory is choosen

    /**
     * default constructor
     *
     */
    public ImageViewer() {
        super("Image Viewer");
    }

    /**
     * constructor sets up GUI and action listeners
     *
     */
    public ImageViewer(String title) {
        super(title);

        tImg = new Thread(this);
        //	tImg.start();

        Container c = getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        c.setLayout(gbl);
        setSize(new Dimension(800, 600));
        rescaleX = (int) (super.getWidth() * 0.9);
        rescaleY = (int) (0.66 * rescaleX);

        ssTimer = new Timer(imgTime, new OneTimeListener());   //timer for advancing images

        openDir = new JButton("Open Directory");
        openDir.setPreferredSize(new Dimension(150, 35));
        openDir.setVisible(true);

        openDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isDir = true;
                System.out.println("Open Directory Button Pushed");
                iv.getDir();
                strFileName = iv.getFileName(0);  //pass a 2 because we're selecting a dir.
                loadImg();
                displayImg();
                if (isSlideShow) {				//if slide show is enabled before a dir is selected
                    ssTimer.setDelay(imgTime * 1000);
                    ssTimer.start();
                }
            }
        });

        openImage = new JButton("Open Image");
        openImage.setPreferredSize(new Dimension(150, 35));
        openImage.setVisible(true);

        openImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isDir = false;
                System.out.println("Open Image Button Pushed");
                strFileName = iv.getFileName(1);		//pass a 1 because we're selecting a file
                loadImg();
                displayImg();
            }
        });

        saveAs = new JButton("Save As");
        saveAs.setPreferredSize(new Dimension(150, 35));
        saveAs.setVisible(true);

        saveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save As Button Pushed");
                iv.saveFile();
            }
        });

        quit = new JButton("Quit");
        quit.setPreferredSize(new Dimension(150, 35));
        quit.setVisible(true);

        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Quit Button Pushed");
                iv.pQuit();
            }
        });

        //NEXT button selects images forward in current directory
        //stays enabled during slideshow for advancing images quickly without changing slider
        next = new JButton("Next >>");
        next.setPreferredSize(new Dimension(150, 35));
        next.setVisible(true);

        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Next Button Pushed");
                if (isDir) {					//must have a directory selected
                    iv.setNextIndex();
                    strFileName = iv.getFileName(0);
                    loadImg();
                    displayImg();
                }
            }
        });

        //PREVIOUS button selects images backwards in current directory
        //stays enabled during slideshow for reversing images quickly without changing slider
        prev = new JButton("<< Previous");
        prev.setPreferredSize(new Dimension(150, 35));
        prev.setVisible(true);

        prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Previous Button Pushed");
                if (isDir) {						//must have a directory selected
                    iv.setPrevIndex();
                    strFileName = iv.getFileName(0);
                    loadImg();
                    displayImg();
                }
            }
        });


        //for slideshow
        enSlideShow = new JToggleButton("Slide Show");			//button to enable slideshow function
        enSlideShow.setPreferredSize(new Dimension(150, 35));
        enSlideShow.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                if (state == ItemEvent.SELECTED && isDir) {
                    isSlideShow = true;
                    iv.setNextIndex();											//load first buffer to get things going
                    strFileName = iv.getFileName(0);
                    loadImg();
                    ssTimer.setInitialDelay(500);						//start instantly, almost
                    ssTimer.setDelay(imgTime * 1000); 			//set delay based on slider
                    ssTimer.start();

                } else {												//disable the timer if not running slideshow
                    isSlideShow = false;
                    System.out.println("Select a Directory");
                    if (ssTimer.isRunning()) {
                        ssTimer.stop();
                    }
                }
            }
        });

        //slider sets time in seconds for advancing images for slideshow
        slider = new JSlider(0, 30, 5);
        slider.setPreferredSize(new Dimension(150, 35));
        slider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(5);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ssTimer.stop();									//disable timer if slider has been adjusted
                imgTime = slider.getValue();
                ssTimer.setDelay(imgTime * 1000);  //sets new value
                if (isSlideShow == true) {
                    ssTimer.restart();								//restart timer
                }
            }
        });


        //add compoments to jpanel, using GridBagLayout Manager
        //use 2 JPanels, 
        //imgPanel is for the image componemts
        imgPanel = new JPanel(gbl);
        imgPanel.setPreferredSize(new Dimension(750, 590)); //just under 800x600

        imgIndex = new JLabel();
        imgIndex.setPreferredSize(new Dimension(60, 15));
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        imgPanel.add(imgIndex, gbc);

        imgName = new JLabel();
        imgName.setPreferredSize(new Dimension(350, 15));
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        imgPanel.add(imgName, gbc);

        outImg = new JLabel();
        outImg.setPreferredSize(new Dimension(rescaleX, rescaleY));
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        imgPanel.add(new JScrollPane(outImg), gbc);


        //second JPanel is for the buttons and slider
        btnPanel = new JPanel(gbl);
        btnPanel.setPreferredSize(new Dimension(650, 50));

        gbc.gridx = 0;
        gbc.gridy = 0;
        btnPanel.add(openImage, gbc);

        gbc.gridx = GridBagConstraints.RELATIVE;
        btnPanel.add(openDir, gbc);
        btnPanel.add(saveAs, gbc);
        btnPanel.add(quit, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        btnPanel.add(prev, gbc);

        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 1;
        btnPanel.add(next, gbc);
        btnPanel.add(enSlideShow, gbc);
        btnPanel.add(slider, gbc);

        //add the two JPanels to the main Container
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(imgPanel, gbc);					//image panel

        panelSep = new JSeparator();
        gbc.weightx = 0.01;
        gbc.weighty = 0.01;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 0, 0, 0);
        c.add(panelSep, gbc);					//separator

        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);		//reset insets
        gbc.weightx = 0.05;
        gbc.weighty = 0.05;
        c.add(btnPanel, gbc);			  	//button panel

    }   //constructor

    /**
     * member funtion loadImg() gets the file & file length of selected image and loads it into buffer
     */
    public void loadImg() {
        fLen = iv.getFileLength();							//also loads image into buffer
        System.out.println(strFileName);
        imgName.setText(strFileName);

        imgBuff = new byte[fLen];								//returns image to file buffer
        imgBuff = iv.getImageByte();
        System.out.println(imgBuff.length);
        imgData = imgBuff;											//copy image from file buffer to display buffer
    }  //loadImg															//ready to be dispaled

    /**
     * member funtion displayImg() resizes image if necassary and displays it
     */
    public void displayImg() {
        ImageIcon tmpimg = new ImageIcon(imgData);

        if (tmpimg.getIconWidth() > rescaleX) {					//resize to fit text area
            img = new ImageIcon(tmpimg.getImage().getScaledInstance(rescaleX, -1, Image.SCALE_DEFAULT));
        } else {
            img = tmpimg;
        }

        outImg.setIcon(img);						//put image into label area
        String index = iv.getIndex();
        imgIndex.setText(index);					//dispaly index number

    }   //displayImg()

    /**
     * inner class for timer used to control slideshow calls run to load next image to buffer as current image is
     * displayed
     */
    class OneTimeListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.out.println("Timer Ticks  " + imgTime);

            while (tImg.isAlive()) //may never get called??
            {
                System.out.println("Waiting for thread to finish");
            }

            displayImg();					//display image in 1st buffer
            tImg.start();
            run();								//start to load next image into 2nd buffer
        }
    }		//OnetimeListener

    public void run() {
        iv.setNextIndex();
        strFileName = iv.getFileName(0);
        loadImg();
    }		//run()

    public static void main(String args[]) {
        ImageViewer app = new ImageViewer("Image Viewer");
        app.setVisible(true);
        app.addWindowListener(
                new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }  //main
}   //class ImageViewer

