import java.awt.EventQueue;
import java.awt.event.*;
import java.io.File;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * *************
 ** Interactive class * This class handles all the listeners for component of
 ** Retrieves photo from database based on index
 ** All database interaction is on separate thread.
 ** Has photoIndex to keep track of which photo is being displayed
 ** albumSize has total number of photo in the database
 */
public class DigitalAlbumThread extends JFrame implements ActionListener, KeyListener {

    private DatabaseAbstractionLayer dal;
    private final PictureFrameDB pictureFrameDB = new PictureFrameDB();
    private static File file;
    private PhotoDB photo = new PhotoDB();
    private int photoIndex = 0;
    private int albumSize = 0;

    private JMenuItem maintain;
    private JMenuItem browse;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                DigitalAlbumThread photoAlbum;
                try {
                    photoAlbum = new DigitalAlbumThread();
                    photoAlbum.setTitle("Digital Photo Album");
                    photoAlbum.setSize(600, 700);
                    photoAlbum.setVisible(true);
                    photoAlbum.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                } catch (SQLException ex) {
                    Logger.getLogger(DigitalAlbumThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public DigitalAlbumThread() throws SQLException {
        dal = new DatabaseAbstractionLayer();
        //add a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //add the Menus for menubar
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        //add menu items for View Menu
        browse = new JMenuItem("Browse");
        browse.setMnemonic(KeyEvent.VK_B);
        maintain = new JMenuItem("Maintain");
        maintain.setMnemonic(KeyEvent.VK_M);

        viewMenu.add(browse);
        viewMenu.add(maintain);
        add(pictureFrameDB);

        //display the buttons
        pictureFrameDB.browseMode();

        //add handler for all items
        pictureFrameDB.NextButton().addActionListener(this);
        pictureFrameDB.PreviousButton().addActionListener(this);
        pictureFrameDB.DeleteButton().addActionListener(this);
        pictureFrameDB.AddPhotoButton().addActionListener(this);
        pictureFrameDB.SaveChangesButton().addActionListener(this);
        pictureFrameDB.getDescriptionTextArea().addKeyListener(this);
        pictureFrameDB.getDateTextBox().addKeyListener(this);
        browse.addActionListener(this);
        maintain.addActionListener(this);

        //add handler for current index label
        pictureFrameDB.getImageIndex().addKeyListener(this);

        albumSize = dal.rowCount();

        //display the picture
        updateUI();

    }

    //implement listener actions
    @Override
    public void actionPerformed(ActionEvent e) {

        //if next or previous button is press inside maintain mode, switch back to browse mode
        if (e.getSource() == pictureFrameDB.NextButton()) {
            nextPhoto();
            updateUI();
        } else if (e.getSource() == pictureFrameDB.PreviousButton()) {
            previousPhoto();
            updateUI();
        } else if (e.getSource() == browse) {
            pictureFrameDB.browseMode();
            updateUI();
        } else if (e.getSource() == maintain) {
            pictureFrameDB.maintainMode();
            updateUI();
        } else if (e.getSource() == pictureFrameDB.DeleteButton()) {
            ImageIcon icon = new ImageIcon("warning.png");
            int confirm = JOptionPane.showConfirmDialog(this, "Deleting is permanent! Are you sure you want to delete?",
                    "Warning", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, icon);
            if (confirm == JOptionPane.YES_OPTION) {
                deletePhoto();
            }
            
        } //on add button, just grab the file
        else if (e.getSource() == pictureFrameDB.AddPhotoButton()) {

            String button = pictureFrameDB.AddPhotoButton().getText();

            //if button is add photo, let user choose file. if it's cancel, refresh display
            if (button == "Add Photo") {
                //if user click ok when choosing file
                if (chooseFile()) {
                    //clear the textfields for new photo
                    pictureFrameDB.getDescriptionTextArea().setText("");
                    pictureFrameDB.getDateTextBox().setText("");

                    //if albumSize is 0, then description and date textboxes were disabled
                    if (albumSize == 0) {
                        pictureFrameDB.getDescriptionTextArea().setEnabled(true);
                        pictureFrameDB.getDateTextBox().setEnabled(true);
                    }

                    //preview the current photo to the frame
                    pictureFrameDB.previewPhoto(new ImageIcon(file.getAbsolutePath()));

                    pictureFrameDB.SaveChangesButton().setEnabled(true);
                    pictureFrameDB.NextButton().setEnabled(false);
                    pictureFrameDB.PreviousButton().setEnabled(false);
                    pictureFrameDB.AddPhotoButton().setText("Cancel");
                    pictureFrameDB.DeleteButton().setEnabled(false);
                }
            } else {
                pictureFrameDB.SaveChangesButton().setEnabled(false);
                pictureFrameDB.AddPhotoButton().setText("Add Photo");
                updateUI();
            }

        } //if save changes button is clicked, serialize the arraylist and set save changes button to false
        else if (e.getSource() == pictureFrameDB.SaveChangesButton()) {
            addPhoto();
            pictureFrameDB.AddPhotoButton().setEnabled(true);
            pictureFrameDB.AddPhotoButton().setText("Add Photo");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //if user type anything in description and date, enable save changes button
        if (e.getSource() == pictureFrameDB.getDescriptionTextArea()
                || e.getSource() == pictureFrameDB.getDateTextBox()) {
            pictureFrameDB.SaveChangesButton().setEnabled(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //if enter key is press in the Image Index textfield
        //change the photoIndex and update the display to show picture at new index
        if (e.getSource() == pictureFrameDB.getImageIndex()) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                int temp = photoIndex;
                photoIndex = pictureFrameDB.getImageIndexInput() - 1;
                if (photoIndex < 0 || photoIndex >= albumSize) {
                    JOptionPane.showMessageDialog(this, "index is out of range.");
                    photoIndex = temp;
                }
                updateUI();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    //advance to the next photo
    public void nextPhoto() {
        if (albumSize != 0 && photoIndex != albumSize - 1) {
            ++photoIndex;
        }
    }

    //go back to previous photo
    public void previousPhoto() {
        if (albumSize != 0 && photoIndex != 0) {
            --photoIndex;
        }
    }

    //update components then displays the photo
    private void updateUI() {
        updateDisplay();
        displayPhotoThread(photoIndex);
    }
    
    //displays the components of pictureFrame accordingly
    private final void updateDisplay(){
        //update the image
        pictureFrameDB.setImageIndex(photoIndex);

        //update current image index label
        pictureFrameDB.setTotalImageLabel(albumSize);

        //if there're no photos
        if (albumSize != 0) {
            //if arraylist is not empty. set the photo to frame
            pictureFrameDB.DeleteButton().setEnabled(true);
            
            //if there is only 1 photo in frame. disable both buttons
            if (albumSize == 1) {
                pictureFrameDB.NextButton().setEnabled(false);
                pictureFrameDB.PreviousButton().setEnabled(false);
            } //if there're photo in the frame. load frames with photo
            else if (photoIndex == 0) {
                pictureFrameDB.PreviousButton().setEnabled(false);
                pictureFrameDB.NextButton().setEnabled(true);
            } //disable next button if index is arraysize - 1
            else if (photoIndex == albumSize - 1) {
                pictureFrameDB.NextButton().setEnabled(false);
                pictureFrameDB.PreviousButton().setEnabled(true);
            } else {
                pictureFrameDB.NextButton().setEnabled(true);
                pictureFrameDB.PreviousButton().setEnabled(true);
            }
        } else {
            //if there is no photo in frame, set next and previous to display 0
            pictureFrameDB.setImageIndex(-1);
            pictureFrameDB.setFramePhoto();
            pictureFrameDB.PreviousButton().setEnabled(false);
            pictureFrameDB.NextButton().setEnabled(false);
            pictureFrameDB.DeleteButton().setEnabled(false);
            pictureFrameDB.getDescriptionTextArea().setEnabled(false);
            pictureFrameDB.getDateTextBox().setEnabled(false);
        }
    }

    //create a thread to get photo from database then call invoke later to set photo to pictureframe
    private void displayPhotoThread(final int index) {
        if(albumSize != 0){
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    photo = dal.getPhoto(photoIndex);
                    SwingUtilities.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            pictureFrameDB.setFramePhoto(photo);
                        }
                    });
                }
            });
            t.start();
        }
    }
    
    //delete photo from database on separate thread
    private void deletePhotoThread(final int index) throws InterruptedException {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                dal.deletePhoto(index);
                
                /* not sure if I'm doing this part right.
                 * here I'm calling invoke later to updateUI
                 * which creates a thread to get photo from database
                 * and then call invoke later again to display the photo
                 * look at updateUI() and displayPhotoThread() to see what I mean
                */
                SwingUtilities.invokeLater(new Runnable(){

                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        });
        t.start();
    }

    //calls deletePhotoThread
    private void deletePhoto() {

        try {
            //if there's only 1 photo in album, delete without decrementing index
            if (albumSize == 1) {
                deletePhotoThread(photoIndex);
            //current photo is the last photo in album, need to decrement index after delete
            } else if (photoIndex == albumSize - 1) {
                deletePhotoThread(photoIndex--);
            } else { //if item delete is not last item, don't need to decrement index
                deletePhotoThread(photoIndex);
            }
            
            --albumSize;

        } catch (InterruptedException ex) {
            Logger.getLogger(DigitalAlbumThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            return true;
        }
        return false;
    }

    //save photo to database on a separate thread.
    private void savePhotoThread(final int index, final PhotoDB photo){
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                dal.savePhoto(index, photo);
                SwingUtilities.invokeLater(new Runnable(){

                  @Override
                  public void run() {
                      updateUI();
                  }
              });
            }
        });
        t.start();
    }

    //calls savePhotoThread to add photos
    private void addPhoto() {
        byte[] image;
        String description = pictureFrameDB.getDescriptionText();
        String date = pictureFrameDB.getDateText();

        try {
            //save change case: when user add new file
            if (file != null) {
                image = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

                //add the photo after index. if album is empty, then 0 is after -1
                if (albumSize == 0) {
                    savePhotoThread(photoIndex - 1, new PhotoDB(image, description, date));
                } else {
                    savePhotoThread(photoIndex++, new PhotoDB(image, description, date));
                }
                ++albumSize;
                pictureFrameDB.SaveChangesButton().setEnabled(false);
                file = null;
            } //save change case: when use change description or date of current photo
            else {
                dal.updatePhotoData(photoIndex, description, date);
            }
        } catch (Exception ex) {

        }
        pictureFrameDB.AddPhotoButton().setEnabled(true);
        pictureFrameDB.SaveChangesButton().setEnabled(false);
    }

}
