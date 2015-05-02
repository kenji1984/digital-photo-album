
import java.awt.*;
import java.text.SimpleDateFormat;
import javax.swing.*;

/*****************************
** This is the frame or GUI of the photo album
 Have getters and setters for components that needs handlers only
 Have a setImage method to set a Photo to the frame along with its other attributess
**********************************/

public class PictureFrame extends JPanel{
    private final JLabel photoLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel descriptionLabel = new JLabel("Description");
    private final JLabel dateLabel = new JLabel("Date");
    private JLabel totalImageLabel = new JLabel();
    
    private JTextArea descriptionText = new JTextArea(3, 30);
    private JTextField dateText = new JTextField(10);
    private JTextField imageIndex = new JTextField(3);
    
    private final JButton prev = new JButton("<Prev");
    private final JButton next = new JButton("Next>");
    private final JButton delete = new JButton("Delete");
    private final JButton save = new JButton("Save Changes");
    private final JButton add = new JButton("Add Photo");
    
    public PictureFrame(){
        
        setLayout(new BorderLayout());        
        
        //make scrollPanel for photoLabel
        JScrollPane scrollPane = new JScrollPane(photoLabel);
        
        //create border for photoLabel and descriptionText box
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        descriptionText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        
        //add components to the descriptionPanel
        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        descriptionPanel.add(descriptionLabel);
        descriptionPanel.add(descriptionText);
        
        //add component to the datePanel
        dateLabel.setPreferredSize(new Dimension(descriptionLabel.getPreferredSize()));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        datePanel.add(dateLabel);
        datePanel.add(dateText);
        
        //panel for maintenance options
        JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        maintenancePanel.add(delete);
        maintenancePanel.add(save);
        maintenancePanel.add(add);
        
        //make a panel to hold date and maintenancePanel
        JPanel midCombinationPanel = new JPanel(new GridLayout());
        midCombinationPanel.add(datePanel);
        midCombinationPanel.add(maintenancePanel);
        
        //add components to the bottomPanel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bottomPanel.add(imageIndex);
        bottomPanel.add(totalImageLabel);
        bottomPanel.add(prev);
        bottomPanel.add(next);
        //create border for bottomPanel
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        //make a combinationPanel to hold the above 3 panels
        JPanel combinationPanel = new JPanel(new BorderLayout(10, 10));
        combinationPanel.add(descriptionPanel, BorderLayout.NORTH);
        combinationPanel.add(midCombinationPanel, BorderLayout.CENTER);
        combinationPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(scrollPane, BorderLayout.CENTER);
        add(combinationPanel, BorderLayout.SOUTH);
        
    }
    
    public void setFramePhoto(){
        photoLabel.setIcon(null);
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionText.setText(" ");
        dateText.setText(" ");
    }
    
    //set the picture, description, and date to the frame
    public void setFramePhoto(Photo photo){
        photoLabel.setIcon(photo.getImage());
        descriptionText.setText(photo.getDescription());
        if(photo.getDate() != null){
            SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy");
            dateText.setText(df.format(photo.getDate()));
        }
        else
            dateText.setText(" ");
    }
    
    //return Next button
    public JButton NextButton(){
        return next;
    }
    
    //return Previous Button
    public JButton PreviousButton(){
        return prev;
    }
    
    public JButton DeleteButton(){
        return delete;
    }
    
    public JButton SaveChangesButton(){
        return save;
    }
    
    public JButton AddPhotoButton(){
        return add;
    }
    
    //get image index jtextfield
    public JTextField getImageIndex(){
        return imageIndex;
    }
    
    //set the index label for the current image
    //changing the index label also changes the photo
    public void setImageIndex(int index){
        imageIndex.setText(Integer.toString(index + 1));
    }
    
    //set the max number of image label
    public void setTotalImageLabel(int size){
        totalImageLabel.setText("of " + size);
    }
    
    //return description text inside text area
    public JTextArea getDescriptionTextArea(){
        return descriptionText;
    }
    
    //set the description of the description text area
    public void setDescriptionTextArea(String desc){
        descriptionText.setText(desc);
    }
    
    public JTextField getDateTextBox(){
        return dateText;
    }
    
    public void setDateTextBox(String date){
        if(date.trim().length() != 0){
            SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy");
            dateText.setText(df.format(date));
        }
        else
            dateText.setText(" ");
    }
    
    //get text inputs for the textfields
    public String getImageIndexInput(){
        return imageIndex.getText();
    }
    
    public String getDescriptionText() {
        return descriptionText.getText();
    }
    
    public String getDateText(){
        return dateText.getText();
    }
    
    //enable and show buttons for maitenance mode
    final void maintainMode() {
        delete.setVisible(true);
        save.setVisible(true);
        save.setEnabled(false);
        add.setVisible(true);
        descriptionText.setEditable(true);
        dateText.setEditable(true);
    }

    //enable and show buttons for browse mode
    final void browseMode() {
        delete.setVisible(false);
        save.setVisible(false);
        add.setVisible(false);
        descriptionText.setEditable(false);
        dateText.setEditable(false);
        save.setEnabled(false);
    }
}
