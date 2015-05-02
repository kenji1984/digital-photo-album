
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/*
Connection instance to SQL Server 2012 database.
Change driver, connectionString, and probably SQL syntax to operate correctly.
*/

public class DatabaseAbstractionLayer {
    private final static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final static String connectionString = "jdbc:sqlserver://KEN;databaseName=JavaCS349;integratedSecurity=true";
    private static Connection con;
    private static Statement stmt;

    public DatabaseAbstractionLayer() throws SQLException {
        initConnection();
        createTable();
    }
    
    public static void main(String [] args) throws SQLException{
        DatabaseAbstractionLayer dal = new DatabaseAbstractionLayer();
        dal.rowCount();
    }
    
    public static void initConnection(){
        try{
            Class.forName(driver);
            con = DriverManager.getConnection(connectionString);
            if(con != null){
                System.out.println("Database is now connected.");
            }
            else{
                System.out.println("Connection failed. Check connection string.");
            }
            stmt = con.createStatement();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public int rowCount(){
        String sqlCmd = "select count(*) from Photo";
        try{
            ResultSet rs = stmt.executeQuery(sqlCmd);
            rs.next();
            int row = rs.getInt(1);
            rs.close();
            return row;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    
    public synchronized PhotoDB getPhoto(int index){
        int i;
        String sqlCmd = "select image, description, date from Photo where [index] = " + index;
        try{
            ResultSet rs = stmt.executeQuery(sqlCmd);

            boolean found = rs.next();
            if(found){
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream is = rs.getBinaryStream("image");

                while((i = is.read()) != -1){
                    os.write(i);
                }
                return new PhotoDB(os.toByteArray(), rs.getString("description"), rs.getDate("date"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public synchronized void savePhoto(int index, PhotoDB photo){
        String insertCmd = "insert into Photo([index], [image], description, [date]) " +
                           "values (?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        
        try{
            //before inserting, increment every element greater or equal to index number by 1
            stmt.executeUpdate("update Photo set [index] = [index]+1 where [index] > " + (index));
            
            pstmt = con.prepareStatement(insertCmd);
            ByteArrayInputStream is = new ByteArrayInputStream(photo.getImage());
            pstmt.setInt(1, index + 1);
            pstmt.setBinaryStream(2, is, (int)photo.getImage().length);
            pstmt.setString(3, photo.getDescription());
            if(photo.getDate() != null){
                pstmt.setDate(4, new java.sql.Date(photo.getDate().getTime()));
            }
            else{
                pstmt.setNull(4, java.sql.Types.DATE);
            }
            pstmt.executeUpdate();
            pstmt.close();
                    
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }
    
    public synchronized void updatePhotoData(int index, String desc, String dt){
        String updateCmd = "update Photo set [description] = ?, [date] = ? where [index] = " + index;
        SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy");
        java.util.Date date = null;
        PreparedStatement pstmt = null;
                
        if(!(dt.trim().length() == 0)){
            try {
                date = df.parse(dt);
            } catch (ParseException ex) {
                //Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try{
            pstmt = con.prepareStatement(updateCmd);
            pstmt.setString(1, desc);
            if(date != null){
                pstmt.setDate(2, new java.sql.Date(date.getTime()));
            }
            else{
                pstmt.setNull(2, java.sql.Types.DATE);
            }            
            pstmt.executeUpdate();
            pstmt.close();
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }
    
    public synchronized void deletePhoto(int index){
        String deleteCmd = "delete from Photo where [index] = " + index;
        String updateCmd = "update Photo set [index] = [index] -1 where [index] > " + index;
        
        try{
            stmt.executeUpdate(deleteCmd);
            stmt.executeUpdate(updateCmd);
        }
        catch(Throwable e){
            e.printStackTrace();
        }
    }
    
    public void createTable() throws SQLException {
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet rs = dbmd.getTables(null, null, "Photo", null);
        if (!rs.next()) { 
            String sqlCmd = "create table Photo (photo_id int identity(1,1) not null, " 
                    + "[index] int unique not null, [image] varbinary(max) not null, "
                    + "description nvarchar(200) null, date date null, "
                    + "primary key clustered(photo_id asc))";   
            stmt.executeUpdate(sqlCmd);
        }
    }
}
