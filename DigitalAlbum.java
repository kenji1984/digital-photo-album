import java.awt.EventQueue;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * *************
 ** Interactive class * This class handles all the listeners for component of
 * the picture Frame * Composition of Collection of photos and a picture frame *
 * Has photoIndex to keep track of which photo is being displayed **************
 */
public class DigitalAlbum extends JFrame implements ActionListener, KeyListener {

    private final Photos photos = new Photos();
    private final PictureFrame pictureFrame = new PictureFrame();
    private int photoIndex = 0;

    private final JMenuItem maintain;
    private final JMenuItem browse;
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                DigitalAlbum photoAlbum = new DigitalAlbum();
                photoAlbum.setTitle("Digital Photo Album");
                photoAlbum.setSize(600, 700);
                photoAlbum.setVisible(true);
                photoAlbum.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    public DigitalAlbum() {
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
        add(pictureFrame);

        //display the picture
        updateDisplay();

        //display the buttons
        pictureFrame.browseMode();

        //add handler for all items
        pictureFrame.NextButton().addActionListener(this);
        pictureFrame.PreviousButton().addActionListener(this);
        pictureFrame.DeleteButton().addActionListener(this);
        pictureFrame.AddPhotoButton().addActionListener(this);
        pictureFrame.SaveChangesButton().addActionListener(this);
        pictureFrame.getDescriptionTextArea().addKeyListener(this);
        pictureFrame.getDateTextBox().addKeyListener(this);
        browse.addActionListener(this);
        maintain.addActionListener(this);

        //add handler for current index label
        pictureFrame.getImageIndex().addKeyListener(this);

    }

    //advance to the next photo
    public Photo nextPhoto() {
        return photos.getPhotos().get(++photoIndex);
    }

    //go back to previous photo
    public Photo previousPhoto() {
        return photos.getPhotos().get(--photoIndex);
    }
    
    public Photo currentPhoto(){
        return photos.getPhotos().get(photoIndex);
    }

    //this function updates all display according to current photoIndex
    final void updateDisplay() {
        //update the image
        pictureFrame.setImageIndex(photoIndex);

        //update current image index label
        pictureFrame.setTotalImageLabel(photos.getPhotos().size());

        //if there're no photos
        if(!photos.getPhotos().isEmpty()){
            //if arraylist is not empty. set the photo to frame
            pictureFrame.DeleteButton().setEnabled(true);
            pictureFrame.setFramePhoto(photos.getPhotos().get(photoIndex));
            
            //if there is only 1 photo in frame. disable both buttons
            if(photos.getPhotos().size() == 1){
                pictureFrame.NextButton().setEnabled(false);
                pictureFrame.PreviousButton().setEnabled(false);                
            }
            //if there're photo in the frame. load frames with photo
            else if (photoIndex == 0) {
                pictureFrame.PreviousButton().setEnabled(false);
                pictureFrame.NextButton().setEnabled(true);
            } 
            //disable next button if index is arraysize - 1
            else if (photoIndex == photos.getPhotos().size() - 1) {
                pictureFrame.NextButton().setEnabled(false);
                pictureFrame.PreviousButton().setEnabled(true);
            } 
            
            else {
                pictureFrame.NextButton().setEnabled(true);
                pictureFrame.PreviousButton().setEnabled(true);
            }
        }
        else{
            //if there is no photo in frame, set next and previous to display 0
            pictureFrame.setImageIndex(-1);
            pictureFrame.setFramePhoto();
            pictureFrame.PreviousButton().setEnabled(false);
            pictureFrame.NextButton().setEnabled(false);
            pictureFrame.DeleteButton().setEnabled(false);
            pictureFrame.getDescriptionTextArea().setEnabled(false);
            pictureFrame.getDateTextBox().setEnabled(false);
        }
    }

    
    public void deletePhoto(){
        
        //if there's only 1 photo in album, delete without decrementing index
        if(photos.getPhotos().size() == 1){
            photos.deletePhoto(photoIndex);
        }
        //current photo is the last photo in album, need to decrement index after delete
        else if(photoIndex == photos.getPhotos().size() - 1){
            photos.deletePhoto(photoIndex--);
        }
        else{ //if item delete is not last item, don't need to decrement index
            photos.deletePhoto(photoIndex);
            System.out.println(photoIndex);
        }
        updateDisplay();
        pictureFrame.SaveChangesButton().setEnabled(true);
    }
    
    public Photo choosePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            return new Photo(file.getAbsolutePath(), "", "");
        }
        else{
            return null;
        }
    }

    //implement listener actions
    @Override
    public void actionPerformed(ActionEvent e) {
        
        //if next or previous button is press inside maintain mode, switch back to browse mode
        if (e.getSource() == pictureFrame.NextButton()) {
            nextPhoto();
            updateDisplay();
        } 
        
        else if (e.getSource() == pictureFrame.PreviousButton()) {
            previousPhoto();
            updateDisplay();
        } 
        
        else if (e.getSource() == browse) {
            pictureFrame.browseMode();
            updateDisplay();
        } 
        
        else if (e.getSource() == maintain) {
            pictureFrame.maintainMode();
            updateDisplay();
        } 
        
        else if (e.getSource() == pictureFrame.DeleteButton()){
            deletePhoto();
        } 
        
        else if (e.getSource() == pictureFrame.AddPhotoButton()){
            
            Photo temp = choosePhoto();
            
            //clear the textfields for new photo
            pictureFrame.getDescriptionTextArea().setText("");
            pictureFrame.getDateTextBox().setText("");
            
            //if temp has a photo
            if(temp != null){
                
                //add date and description to temp
                temp.setDate(pictureFrame.getDateText());
                temp.setDescription(pictureFrame.getDescriptionText());
                
                System.out.println(temp.getDate());
                
                //if album is not empty, add photo after current index and display the photo added(after current)
                if(!photos.getPhotos().isEmpty()){
                    photos.addPhoto(photoIndex + 1, temp);
                    nextPhoto();
                }
                //if album is empty, add photo at current index (0)
                else{
                    photos.addPhoto(photoIndex, temp);
                }
                
                pictureFrame.SaveChangesButton().setEnabled(true);
            }
            updateDisplay();
        } 
        
        //if save changes button is clicked, serialize the arraylist and set save changes button to false
        else if (e.getSource() == pictureFrame.SaveChangesButton()){
            photos.savePhoto();
            pictureFrame.SaveChangesButton().setEnabled(false);
        } 
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //if user type anything in description and date, enable save changes button
        if (e.getSource() == pictureFrame.getDescriptionTextArea() ||
                e.getSource() == pictureFrame.getDateTextBox())
            pictureFrame.SaveChangesButton().setEnabled(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //if enter key is press in the Image Index textfield
        //change the photoIndex and update the display to show picture at new index
        if (e.getSource() == pictureFrame.getImageIndex()) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                int numberInput = Integer.parseInt(pictureFrame.getImageIndexInput());
                if (numberInput >= 1 && numberInput <= photos.getPhotos().size()) {
                    photoIndex = numberInput - 1;
                    updateDisplay();
                } else {
                    JOptionPane.showMessageDialog(null, numberInput + " is out of range");
                    updateDisplay();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //while user is typing, save the inputs to the model
        //this was used due to keyTyped not recording the last key typed
        if (e.getSource() == pictureFrame.getDescriptionTextArea() ||
                e.getSource() == pictureFrame.getDateTextBox()){
            currentPhoto().setDescription(pictureFrame.getDescriptionText());
            currentPhoto().setDate(pictureFrame.getDateText());
        }
    }
}
