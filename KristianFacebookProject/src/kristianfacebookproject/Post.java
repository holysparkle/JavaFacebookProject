package kristianfacebookproject;

import java.util.Date;


public class Post {
    private String title;
    private String message; // i.e. the content of the post
    private Date createDate;
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setCreationDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public Date getCreationDate() {
        return this.createDate;
    }
}
