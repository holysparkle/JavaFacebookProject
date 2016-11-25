package kristianfacebookproject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MainWindowActions extends MainWindow implements ActionListener, ItemListener {

    private static Logger logger = Logger.getLogger(MainWindowActions.class.getName());

    private JButton btnLoadData;
    private JComboBox cmbAccounts;
    private JTable tblData;
    private JTextArea txtarSummaryText;

    private String selectedAccount;
    private DefaultTableModel tableModel;

    public MainWindowActions() {
        super();

        logger.setLevel(Level.OFF); // Set to OFF to disable all logging
        
        this.btnLoadData = getbtnLoadData();
        this.cmbAccounts = getAccountsComboBox();
        this.tblData = getTable();
        this.txtarSummaryText = getSummaryPoleArea();

        addListeners();
        populateAccounts();

        this.selectedAccount = cmbAccounts.getSelectedItem().toString();
    }

    private void addListeners() {
        btnLoadData.addActionListener(this);
        cmbAccounts.addItemListener(this);
        tableModel = (DefaultTableModel) tblData.getModel();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (selectedAccount.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select account first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        for(int i = 0; i < tableModel.getRowCount();) {
            tableModel.removeRow(i);
        }
        AccountData.likes = 0;
        AccountData.shares = 0;
        AccountData.talking_about_count = 0;
        AccountData.comments_on_posts = 0;
        AccountData.videos = 0;
        AccountData.pictures = 0;
        AccountData.links = 0;
        if(AccountData.posts == null) {
            AccountData.posts = new ArrayList<Post>();
        } else {
            AccountData.posts.clear();
        }
        
        try {
            Statement stmt = Database.getInstance().createStatement();
            String query = "SELECT response from Nodes WHERE response LIKE '%" + selectedAccount + "%'";
            ResultSet rs = stmt.executeQuery(query);
            
            rs.next();
            String response = rs.getString("response");
            rs.close();
            
            JSONParser jp = new JSONParser();
            Object obj = jp.parse(response);
            JSONObject jo = (JSONObject) obj;
            AccountData.id = jo.get("id").toString();
            if(jo.containsKey("likes")) {
                AccountData.likes = Long.parseLong(jo.get("likes").toString());
            }
            if(jo.containsKey("name")) {
                AccountData.name = jo.get("name").toString();
            }
            if(jo.containsKey("talking_about_count")) {
                AccountData.talking_about_count = Long.parseLong(jo.get("talking_about_count").toString());
            }
            
            query = "SELECT response from Nodes WHERE objectid LIKE '%" + AccountData.id + "%'";
            rs = stmt.executeQuery(query);
            while(rs.next()) {
                response = rs.getString("response");
                obj = jp.parse(response);
                jo = (JSONObject) obj;
                if(jo.containsKey("type")) {
                    String t = jo.get("type").toString();
                    logger.info("Type: " + t);
                    if(t.equals("video")) {
                        AccountData.videos++;
                    } else if(t.equals("photo")) {
                        AccountData.pictures++;
                    } else if(t.equals("link")) {
                        AccountData.links++;
                    }
                }
                if(jo.containsKey("comments")) {
                    JSONObject comments = (JSONObject) jo.get("comments");
                    JSONArray arr = (JSONArray) comments.get("data");
                    AccountData.comments_on_posts += arr.size(); // size of comments
                }
                if(jo.containsKey("shares")) {
                    JSONObject shares = (JSONObject) jo.get("shares");
                    AccountData.shares += Long.parseLong(shares.get("count").toString()); // count of shares
                }
                
                if(jo.containsKey("name") && jo.containsKey("message") && jo.containsKey("created_time")) {
                    Post post = new Post();
                    post.setTitle(jo.get("name").toString());
                    post.setMessage(jo.get("message").toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    String timestamp = jo.get("created_time").toString();
                    post.setCreationDate(sdf.parse(timestamp.substring(0, timestamp.length() - 5)));
                    AccountData.posts.add(post);
                }
            }
            rs.close();
            stmt.close();
            
            //logger.info("Id: " + AccountData.id);
            logger.info("Account name: " + AccountData.name);
            logger.info("Likes: " + AccountData.likes);
            logger.info("Videos: " + AccountData.videos);
            logger.info("Pictures: " + AccountData.pictures);
            logger.info("Links: " + AccountData.links);
            if(AccountData.talking_about_count > 0) {
                logger.info("People talking about this user: " + AccountData.talking_about_count);
            }
            if(AccountData.comments_on_posts > 0) {
                logger.info("Comments on posts: " + AccountData.comments_on_posts);
            }
            if(AccountData.shares > 0) {
                logger.info("Shares: " + AccountData.shares);
            }
            if(AccountData.posts.size() > 0) {
                logger.info("Posts: " + AccountData.posts.size());
            }
        } catch (SQLException ex) {
            logger.severe(ex.getMessage());
        } catch(ParseException ex) {
            logger.severe(ex.getMessage());
        } catch (java.text.ParseException ex) {
            logger.severe(ex.getMessage());
        }
        
        tableModel.addRow(new Object[] {"Name", AccountData.name});
        tableModel.addRow(new Object[] {"Likes", AccountData.likes});
        tableModel.addRow(new Object[] {"Videos", AccountData.videos});
        tableModel.addRow(new Object[] {"Pictures", AccountData.pictures});
        tableModel.addRow(new Object[] {"Links", AccountData.links});
        if(AccountData.posts.size() > 0) {
            tableModel.addRow(new Object[] {"Posts", AccountData.posts.size()});        
        }
        if(AccountData.talking_about_count > 0) {
            tableModel.addRow(new Object[] {"People talking about this user", AccountData.talking_about_count});        
        }
        if(AccountData.comments_on_posts > 0) {
            tableModel.addRow(new Object[] {"Comments on posts", AccountData.comments_on_posts});
        }
        if(AccountData.shares > 0) {
            tableModel.addRow(new Object[] {"Shares", AccountData.shares});
        }
        
        // Do whatherver you want with the posts
        /*
        for(Post post: AccountData.posts) {
            String title = post.getTitle();
            String message = post.getMessage();
            Date creationDate = post.getCreationDate();
            
            // creationDate.getTime(); // Call this to get the date in epoch fromat. Then you can -, +, * or %            
            // After that you can create new date like that
            // Date newD = new Date(newDate); Where newDate is the result from math operation
            
        }
        */
        
        txtarSummaryText.setText("Output from algorithm");
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        selectedAccount = ie.getItemSelectable().getSelectedObjects()[0].toString();
        logger.info("Selected account: " + selectedAccount);
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindowActions().setVisible(true);
            }
        });
    }

    private void populateAccounts() {
        cmbAccounts.addItem("");
        try {
            Statement stmt = Database.getInstance().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT objectid from Nodes WHERE objecttype='seed'");
            while (rs.next()) {
                String account = rs.getString("objectid");
                cmbAccounts.addItem(account);
                logger.info("Found account: " + account);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            logger.severe(ex.getMessage());
        }
    }
}
