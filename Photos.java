
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/************************** 
** Collection class of photos
** This class stores the photos
** Provides add/delete photo from Photos
** Will be used to interact with database
**************************/
public class Photos implements Serializable{
    private ArrayList<Photo> photos = new ArrayList<>();
    
    public Photos(){
        readPhoto();
    }
    
    public ArrayList<Photo> getPhotos(){
        return photos;
    }
    
    public void deletePhoto(int index){
        photos.remove(index);
    }
    
    public void addPhoto(Photo photo){
        photos.add(photo);
    }
    
    public void addPhoto(int index, Photo photo){
        photos.add(index, photo);
    }
    
    public void savePhoto(){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("PhotoAlbum.txt"));
            oos.writeObject(photos);
            oos.close();
        }
        catch(Exception e){
            System.out.println(e);
        } 
    }
    
    public final void readPhoto(){
          try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PhotoAlbum.txt"));
            photos = (ArrayList<Photo>)ois.readObject();
            ois.close();
        }
        catch(Exception e){
            System.out.println(e);
        } 
    }
}
