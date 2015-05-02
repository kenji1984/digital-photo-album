import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/********************
** Simple Photo class
** Attributes include image, description, and date
** Multiple overloaded Constructors to take in ImageIcon or URL for image
** and Date type or String type for date.
** Includes getters and setters for all attributes
************************************/
public class Photo implements Serializable{
    private ImageIcon image;
    private String description;
    private Date date;
    private static SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy");
    
    public Photo(){
        
    }
    
    //constructor that takes in a photo object
    public Photo(Photo photo){
        this.image = photo.getImage();
        this.description = photo.getDescription();
        this.date = photo.getDate();
    }
    //constructor takes in url, description, and a date object to make a photo
    public Photo(String url, String desc, Date date){
        this.image = new ImageIcon(url);
        this.date = date;
        this.description = desc;
    }
    
    //constructor takes in url, description, and a date string to make a photo
    public Photo(String url, String desc, String date){
        this.image = new ImageIcon(url);
        this.description = desc;
        if(!date.isEmpty()){
            try {
                this.date = df.parse(date);
            } catch (ParseException ex) {
                //Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setImage(ImageIcon image){
        this.image = image;
    }
    
    public void setImage(String url){
        this.image = new ImageIcon(url);
    }
    
    public void setPhoto(ImageIcon image, String desc, Date date){
        this.image = image;
        this.description = desc;
        this.date = date;
    }
    
    public ImageIcon getImage(){
        return image;
    }
    
    public void setDescription(String desc){
        this.description = desc;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDate(Date date){
        this.date = date;
    }
    
    public void setDate(String date){
        if(!(date.trim().length() == 0)){
            try {
                this.date = df.parse(date);
            } catch (ParseException ex) {
                //Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Date getDate(){
        return date;
    }
}
