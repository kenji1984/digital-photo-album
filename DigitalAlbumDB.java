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
 * the picture Frame * Composition of Collection of photos and a picture frame *
 * Has photoIndex to keep track of which photo is being displayed **************
 */
public class DigitalAlbumDB extends JFrame implements ActionListener, KeyListener {

    private DatabaseAbstractionLayer dal;
    private final PictureFrameDB pictureFrameDB = new PictureFrameDB();
    private static File file;
    private int photoIndex = 0;
    private int albumSize = 0;

    private JMenuItem maintain;
    private JMenuItem browse;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                DigitalAlbumDB photoAlbum;
                try {
                    photoAlbum = new DigitalAlbumDB();
                    photoAlbum.setTitle("Digital Photo Album");
                    photoAlbum.setSize(600, 700);
                    photoAlbum.setVisible(true);
                    photoAlbum.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                } catch (SQLException ex) {
                    Logger.getLogger(DigitalAlbumDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public DigitalAlbumDB() throws SQLException {
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

        //get the row counts from database
        albumSize = dal.rowCount();

        //display the picture
        updateDisplay();

    }

    //implement listener actions
    @Override
    public void actionPerformed(ActionEvent e) {

        //if next or previous button is press inside maintain mode, switch back to browse mode
        if (e.getSource() == pictureFrameDB.NextButton()) {
            nextPhoto();
            updateDisplay();
        } else if (e.getSource() == pictureFrameDB.PreviousButton()) {
            previousPhoto();
            updateDisplay();
        } else if (e.getSource() == browse) {
            pictureFrameDB.browseMode();
            updateDisplay();
        } else if (e.getSource() == maintain) {
            pictureFrameDB.maintainMode();
            updateDisplay();
        } else if (e.getSource() == pictureFrameDB.DeleteButton()) {
            ImageIcon icon = new ImageIcon("warning.png");
            int confirm = JOptionPane.showConfirmDialog(this, "Deleting is permanent! Are you sure you want to delete?",
                    "Warning", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, icon);
            if (confirm == JOptionPane.YES_OPTION) {
                deletePhoto();
            }
            updateDisplay();
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
            }
            else{
                pictureFrameDB.SaveChangesButton().setEnabled(false);
                pictureFrameDB.AddPhotoButton().setText("Add Photo");
                updateDisplay();
            }
            
        } //if save changes button is clicked, serialize the arraylist and set save changes button to false
        else if (e.getSource() == pictureFrameDB.SaveChangesButton()) {
            addPhoto();
            updateDisplay();
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
                if(photoIndex < 0 || photoIndex >= albumSize){
                    JOptionPane.showMessageDialog(this, "index is out of range.");
                    photoIndex = temp;
                }
                updateDisplay();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    //advance to the next photo
    public PhotoDB nextPhoto() {
        if (albumSize != 0) {
            return dal.getPhoto(++photoIndex);
        } else {
            return null;
        }
    }

    //go back to previous photo
    public PhotoDB previousPhoto() {
        return dal.getPhoto(--photoIndex);
    }

    public PhotoDB currentPhoto() {
        return dal.getPhoto(photoIndex);
    }

    //this function updates all display according to current photoIndex
    private final void updateDisplay() {
        //update the image
        pictureFrameDB.setImageIndex(photoIndex);

        //update current image index label
        pictureFrameDB.setTotalImageLabel(albumSize);

        //if there're no photos
        if (albumSize != 0) {
            //if arraylist is not empty. set the photo to frame
            pictureFrameDB.DeleteButton().setEnabled(true);
            pictureFrameDB.setFramePhoto(dal.getPhoto(photoIndex));

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

    private void deletePhoto() {

        //if there's only 1 photo in album, delete without decrementing index
        if (albumSize == 1) {
            dal.deletePhoto(photoIndex);
        } //current photo is the last photo in album, need to decrement index after delete
        else if (photoIndex == albumSize - 1) {
            dal.deletePhoto(photoIndex--);
        } else { //if item delete is not last item, don't need to decrement index
            dal.deletePhoto(photoIndex);
        }

        --albumSize;
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
                    dal.savePhoto(photoIndex - 1, new PhotoDB(image, description, date));
                } else {
                    dal.savePhoto(photoIndex++, new PhotoDB(image, description, date));
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
