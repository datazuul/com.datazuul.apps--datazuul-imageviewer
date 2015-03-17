package com.datazuul.apps.imageviewer.yiv;

// YIV (Yura's Image Viewer)
// writen by Yura Mamyrin (yura@yura.net)
// Copyright (c) 2004 yura.net
// for license go to: http://www.gnu.org/licenses/gpl.html

/*

 ToDo:

 when privew is on and HUGE images are used, it runs out of mem

 Change Name to something better (PVC: ######## Viewing Center) (SYJIVB)
 add a Jump to File option (J)
 add support for other image types
 add multiMonitor support
 add sort by date
 add saved setting, like default dir
 add simple shell integration for windows and linux (kde and gnome)
 add as screensaver for windows and linux

 */
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.font.TextLayout;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import javax.swing.Action;
import javax.swing.JSlider;

/**
 * <p> Main Frame </p>
 *
 * @author Yura Mamyrin
 */
public class yiv extends JFrame { // implements ImageObserver

    private File currentDir;
    private File[] files;
    private BufferedImage MainImage;
    private int currentImage;
    private int width;
    private int height;
    private boolean preview;
    private boolean showname;
    private boolean strechsmall;
    private Timer timer;
    private int time;

    public yiv(File input) {

        currentDir = input;

        preview = false;
        showname = false;
        strechsmall = false;

        time = 3000; // 3 seconds

        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent key) {


                if (key.getKeyCode() == KeyEvent.VK_RIGHT) {

                    changeImage(currentImage + 1);

                } else if (key.getKeyCode() == KeyEvent.VK_LEFT) {

                    changeImage(currentImage - 1);
                } else if (key.getKeyCode() == KeyEvent.VK_C) {

                    getNewDir();

                } else if (key.getKeyCode() == KeyEvent.VK_R) {

                    SortFiles(false);
                    changeImage(0);

                } else if (key.getKeyCode() == KeyEvent.VK_S) {

                    SortFiles(true);
                    changeImage(0);

                } else if (key.getKeyCode() == KeyEvent.VK_F) {

                    changeImage(0);

                } else if (key.getKeyCode() == KeyEvent.VK_P) {

                    if (preview) {
                        preview = false;
                    } else {
                        preview = true;
                    }
                    changeImage(currentImage);

                } else if (key.getKeyCode() == KeyEvent.VK_N) {

                    if (showname) {
                        showname = false;
                    } else {
                        showname = true;
                    }
                    changeImage(currentImage);

                } else if (key.getKeyCode() == KeyEvent.VK_B) {

                    if (strechsmall) {
                        strechsmall = false;
                    } else {
                        strechsmall = true;
                    }
                    changeImage(currentImage);

                } else if (key.getKeyCode() == KeyEvent.VK_G) {

                    if (timer != null) {
                        timer.stop();
                        timer = null;
                    } else {
                        if (setTime()) {
                            timer = new Timer(time, AutoChangeAction());
                            timer.start();
                        }
                    }

                } else if (key.getKeyCode() == KeyEvent.VK_A) {

                    JOptionPane.showMessageDialog(yiv.this, "Yura Image Viewer\nVersion 1.1\nCopyright (c) 2004-2006 yura.net\nA Picture's Worth a Thousand Words,\nbut it uses up a thousand times the memory.", "About YIV", JOptionPane.INFORMATION_MESSAGE);

                } else if (key.getKeyCode() == KeyEvent.VK_F1 || key.getKeyCode() == KeyEvent.VK_H) {

                    JOptionPane.showMessageDialog(yiv.this, "H or F1: help\nQ or ESC: exit\nA: about\n\nR: randomly shuffle images\nS: sort images by name\nP: toggle preview\nN: toggle names\nF: go to first image\nC: choose image directory\nG: auto change images\nB: strech small images\n\nRIGHT: Next Image\nLEFT: Previous Image\n", "Help", JOptionPane.INFORMATION_MESSAGE);

                } else if (key.getKeyCode() == KeyEvent.VK_ESCAPE || key.getKeyCode() == KeyEvent.VK_Q) {

                    System.exit(0);

                }


            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        width = screenSize.width;
        height = screenSize.height;

        if (currentDir == null) {
            getNewDir();
        } else {

            try {
                loadNewDir(currentDir);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    /**
     * @return Action
     */
    public Action AutoChangeAction() {
        return new AbstractAction("text load action") {
            public void actionPerformed(ActionEvent e) {

                changeImage(currentImage + 1);

            }
        };
    }

    public boolean setTime() {

        JSlider Number = new JSlider();

        Number.setPaintTicks(true);
        Number.setMajorTickSpacing(1);

        Number.setPaintLabels(true);
        Number.setSnapToTicks(true);

        Number.setMaximum(10);
        Number.setMinimum(1);
        Number.setValue(time / 1000);

        String[] options = {
            "OK",
            "Cancel"
        };

        int a = JOptionPane.showOptionDialog(
                this, // the parent that the dialog blocks 
                Number, // the dialog message array 
                "How long between slide changes?", // the title of the dialog window 
                JOptionPane.DEFAULT_OPTION, // option type 
                JOptionPane.QUESTION_MESSAGE, // message type 
                null, // optional icon, use null to use the default icon 
                options, // options string array, will be made into buttons 
                options[0] // option that should be made into a default button 
                );

        if (a == 0) {
            time = Number.getValue() * 1000;
            return true;
        }

        return false;

    }

    public void getNewDir() {

        File oldFile = currentDir;

        final JFileChooser fc;

        if (currentDir != null) {
            fc = new JFileChooser(currentDir);
        } else {
            fc = new JFileChooser();
        }

        javax.swing.filechooser.FileFilter fileFilter = new javax.swing.filechooser.FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory());
            }

            public String getDescription() {
                return "All Directories";
            }
        };

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(fileFilter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Select Image Directory");

        int returnVal = fc.showDialog(yiv.this, "Select");

        // if a file is selected instead of a dir, the return value is the same as if you cancel

        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {

            // Write your code here what to do with selected file

            try {

                currentDir = fc.getSelectedFile();
                loadNewDir(currentDir);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(this, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                currentDir = oldFile;
                getNewDir();

            }


        } else {

            // Write your code here what to do if user has canceled Open dialog
            if (currentDir == null) {
                System.exit(0);
            }

        }


    }

    public void loadNewDir(File dir) throws Exception {

        if (!dir.isDirectory()) {
            throw new Exception("Directory not found.");
        }

        // This filter only returns .jpg files and .png files
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {

                return !file.isDirectory() && (file.getName().length() > 3) && ((file.getName()).substring(file.getName().length() - 4).equalsIgnoreCase(".jpg") || (file.getName()).substring(file.getName().length() - 4).equalsIgnoreCase(".png") || (file.getName()).substring(file.getName().length() - 4).equalsIgnoreCase(".bmp"));
            }
        };

        File[] tempfiles = dir.listFiles(fileFilter);

        if (tempfiles.length == 0) {
            throw new Exception("There are no supported images found in that directory.");
        } else {

            files = tempfiles;
            SortFiles(true);
            changeImage(0);
        }

    }

    public void SortFiles(boolean n) {

        List l = Arrays.asList(files);

        if (n) {
            Collections.sort(l);
        } else {
            Collections.shuffle(l);
        }

        files = (File[]) l.toArray();

    }

    private void changeImage(int c) {

        try {

            BufferedImage tempImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
            Graphics2D g = (Graphics2D) tempImage.getGraphics();

            if (c >= files.length) {
                c = 0;
            } else if (c < 0) {
                c = files.length - 1;
            }

            BufferedImage NewImage = ImageIO.read(files[c]);
            GoodPaint(NewImage, 0, 0, width, height, g);

            if (showname) {
                TextLayout tl = new TextLayout(files[c].getName(), g.getFont(), g.getFontRenderContext());

                int w = (int) tl.getAdvance();
                int h = (int) tl.getAscent() + (int) tl.getDescent();

                g.setColor(new Color(255, 255, 255, 150));
                g.fill(new Rectangle2D.Float((float) ((width - tl.getBounds().getWidth()) / 2) - 1, (float) 2, w + 1, h - 1));

                g.setColor(Color.black);
                tl.draw(g, (float) ((width - tl.getBounds().getWidth()) / 2), (float) 12);
            }

            if (preview) { // preview is 14% of normal size

                int w = Math.round(width * 0.14f);
                int h = Math.round(height * 0.14f);

                BufferedImage prevImage = ImageIO.read(files[ (c == 0) ? (files.length - 1) : (c - 1)]);
                GoodPaint(prevImage, 0, (height - h), w, h, g);

                BufferedImage nextImage = ImageIO.read(files[ (c == (files.length - 1)) ? (0) : (c + 1)]);
                GoodPaint(nextImage, (width - w), (height - h), w, h, g);
            }
            currentImage = c;
            MainImage = tempImage;
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void GoodPaint(BufferedImage image, int Ax, int Ay, int Aw, int Ah, Graphics g) {

        int w = image.getWidth();
        int h = image.getHeight();

        //System.out.print("1 w="+w+" h="+h+"\n");

        if (w > Aw) {

            float w1 = Aw;
            float w2 = w;

            h = Math.round((w1 / w2) * h);

            w = Aw;
        }

        if (h > Ah) {

            float h1 = Ah;
            float h2 = h;

            w = Math.round((h1 / h2) * w);

            h = Ah;
        }


        if (strechsmall) {

            if (w < Aw && h < Ah) {

                float w1 = Aw;
                float w2 = w;
                float h1 = Ah;
                float h2 = h;

                float m = ((h1 / h2) > (w1 / w2)) ? (w1 / w2) : (h1 / h2);

                h = Math.round(m * h);

                w = Math.round(m * w);
            }

        }

        //System.out.print("2 w="+w+" h="+h+"\n");

        g.drawImage(image, ((Aw - w) / 2) + Ax, ((Ah - h) / 2) + Ay, w, h, this);

    }

    public void paint(Graphics g) {

        //System.out.print("repainting\n");
        g.drawImage(MainImage, 0, 0, this);

    }

//    public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
//	repaint();
//	System.out.print("THIS!\n");
//	return true;
//    }
    /**
     * This runs the program
     *
     * @param argv
     */
    public static void main(String[] argv) {


        // set up system Look&Feel
        try {

            //    String os = System.getProperty("os.name");
            //    String jv = System.getProperty("java.version");
            //
            //    if ( jv.startsWith("1.4.2") && os != null && os.startsWith("Linux")) {
            //	UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            //    }
            //    else {
            //	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //    }

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        String newFileName = "";
        if (argv.length > 0) {
            newFileName = argv[0];
        }
        for (int c = 1; c < argv.length; c++) {
            newFileName = newFileName + " " + argv[c];
        }

        File newfile = new File(newFileName);

        //System.out.print("loading: "+newfile.getAbsolutePath()+"\n");

        JFrame gui = new yiv((newfile.exists() && newfile.isDirectory()) ? (newfile) : (null));

        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //Dimension frameSize = gui.getSize();
        //frameSize.height = ((frameSize.height > screenSize.height) ? screenSize.height : frameSize.height);
        //frameSize.width = ((frameSize.width > screenSize.width) ? screenSize.width : frameSize.width);
        //gui.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(gui);

        gui.setVisible(true);

    }
}
