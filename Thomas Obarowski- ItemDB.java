package drummindeal.DB;

import drummindeals.business.Item;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Thomas Obarowski
 *
 * This Java class was created during my "Network Based Application Development" course, and is used to retrieve and store information pertaining to the "Items" that 
 * were sold on the website  within a MySQL database.
 * 
 * The purpose of this file is to show my experience in both Java and MySQL. 
 */
public class ItemDB {

    private ArrayList<Item> items = new ArrayList<>();
    private String userName = "development";
    private String password = "Cisco123!";

    public ItemDB() {
        getAllItems();
    }
    
    public ArrayList<Item> getUserAddedItems(String UserID) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Item> userAddedItems = new ArrayList<>();

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";
        //Has to be wrapped in a try/catch/finally
        try {
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            ps = conn.prepareStatement("SELECT item.* FROM useradded INNER JOIN item ON useradded.ItemCode = item.ItemCode WHERE useradded.UserID = ?");
            ps.setString(1, UserID);
            //execute query and save it to the result set
            rs = ps.executeQuery();

            //iterate through the result set to get attribute data WHILE there is a next result
            while (rs.next()) {
                //have to construct item object
                Item item = new Item();
                item.setItemCode(rs.getString("ItemCode"));
                item.setItemName(rs.getString("ItemName"));
                item.setCatalogCategory(rs.getString("CatalogCategory"));
                item.setDescription(rs.getString("describeItem"));
                item.setRating(rs.getString("Rating"));
                item.setImageURL(rs.getString("ImageURL"));
                //save item object to the ArrayList
                userAddedItems.add(item);
            }
            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return userAddedItems;
    }

    public Item getItem(String ItemCode) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Item item = null;
        //EVERYTIME WE RUN THIS WE WILL JUST CLEAR THE ITEMS LIST AND GATHER JUST WHATS IN THE DATABASE (avoiding mass duplicates)
        //Scalability is a concern here. This current implementation could become extremely expensive to perform when scaling to many more items. 
        items.clear();

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        //Has to be wrapped in a try/catch/finally
        try {
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            ps = conn.prepareStatement("SELECT * FROM item WHERE ItemCode = ?");
            ps.setString(1, ItemCode);
            //execute query and save it to the result set
            rs = ps.executeQuery();

            //have to construct item object
            //this tripped me up, the initial 
            rs.next();
            item = new Item();
            item.setItemCode(rs.getString("ItemCode"));
            item.setItemName(rs.getString("ItemName"));
            item.setCatalogCategory(rs.getString("CatalogCategory"));
            item.setDescription(rs.getString("describeItem"));
            item.setRating(rs.getString("Rating"));
            item.setImageURL(rs.getString("ImageURL"));
            //save item object to the ArrayList

            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return item;
    }

    public ArrayList<Item> getAllItems() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        //EVERYTIME WE RUN THIS WE WILL JUST CLEAR THE ITEMS LIST AND GATHER JUST WHATS IN THE DATABASE (avoiding mass duplicates)
        items.clear();

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        //Has to be wrapped in a try/catch/finally
        try {
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            ps = conn.prepareStatement("SELECT * FROM item");
            //execute query and save it to the result set
            rs = ps.executeQuery();

            //iterate through the result set to get attribute data WHILE there is a next result
            while (rs.next()) {
                //have to construct item object
                Item item = new Item();
                item.setItemCode(rs.getString("ItemCode"));
                item.setItemName(rs.getString("ItemName"));
                item.setCatalogCategory(rs.getString("CatalogCategory"));
                item.setDescription(rs.getString("describeItem"));
                item.setRating(rs.getString("Rating"));
                item.setImageURL(rs.getString("ImageURL"));
                //save item object to the ArrayList
                this.items.add(item);
            }
            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.items;
    }

    public ArrayList<Item> getItemsByCategory(String category) {
        //Call the getAllItems method to make sure the list is populated
        getAllItems();
        ArrayList<Item> itemsByCat = new ArrayList<>();
        for (Item item : items) {
            if (item.getCatalogCategory().equalsIgnoreCase(category)) {
                itemsByCat.add(item);
            }
        }
        return itemsByCat;
    }

    public void saveRating(String ItemCode, String rating) {
        Connection conn = null;
        PreparedStatement ps = null;
        //EVERYTIME WE RUN THIS WE WILL JUST CLEAR THE ITEMS LIST AND GATHER JUST WHATS IN THE DATABASE (avoiding mass duplicates)
        items.clear();

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        //Has to be wrapped in a try/catch/finally
        try {
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            ps = conn.prepareStatement("UPDATE item SET Rating = ? WHERE ItemCode = ?");
            ps.setString(1, rating);
            ps.setString(2, ItemCode);
            //execute query and save it to the result set
            ps.executeUpdate();

            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public boolean makeItem(String UserID, String itemName, String CatalogCategory, String describeItem, String Rating, String ImageURL) {
        /**
         * THIS WILL ADD THE ITEM TO THE ITEM DATABASE *
         */
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        boolean successfulItem = true;
        String itemCode = findItemCode(CatalogCategory);
        //Has to be wrapped in a try/catch/finally
        try {
            /**
             * This code is for the item table
             */
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, passcode);
            ps = conn.prepareStatement("INSERT INTO `drummindeals`.`item` (`ItemCode`, `ItemName`,`CatalogCategory`,`describeItem`,`Rating`,`ImageURL`) VALUES "
                    + "(?, ?, ?, ?, ?, ?)");
            //set the parameters within the prep statement
            ps.setString(1, itemCode);
            ps.setString(2, itemName);
            ps.setString(3, CatalogCategory);
            ps.setString(4, describeItem);
            ps.setString(5, Rating);
            ps.setString(6, ImageURL);
            //execute query and save it to the result set
            ps.execute();
            /**
             * this code is for the useradded table
             */
            ps = conn.prepareStatement("INSERT INTO `drummindeals`.`useradded` (`UserID`, `ItemCode`) VALUES (?,?)");

            //set the parameters within the prep statement
            ps.setString(1, UserID);
            ps.setString(2, itemCode);
            //execute query and save it to the result set
            ps.execute();
            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            successfulItem = false;
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return successfulItem;
    }

    private String findItemCode(String Category) {
        /**
         * In this method we are going to generate the itemcode for the new item. We
         * do this by querying all itemcodes for the category the user selects.
         * we then split the number portion off of the code, and add them all to
         * an array then we take the max value of the array, and add one, and
         * concat that with the correct item code prefix, to make an item code
         * that is not a duplicate. We return this string back to the calling
         * method so it can insert its item record*
         *
         * This is not an inherintely scalable way to accomplish this. A better implementation would have been
         * to create a random fixed length String as an ID, but that was not what was asked by the assignment.
         */
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Integer> codes = new ArrayList<>();

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        //Has to be wrapped in a try/catch/finally
        try {
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            ps = conn.prepareStatement("SELECT ItemCode FROM item WHERE CatalogCategory = ?");
            ps.setString(1, Category);
            //execute query and save it to the result set
            rs = ps.executeQuery();

            //iterate through the result set to get attribute data WHILE there is a next result
            while (rs.next()) {
                String itemcode = rs.getString("ItemCode");
                itemcode = itemcode.substring(itemcode.length() - 2);
                codes.add(Integer.parseInt(itemcode));
            }
            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        String itemCodeToReturn = "";
        if (Category.equalsIgnoreCase("Cymbal")) {
            int codeToGlue = Collections.max(codes) + 1;
            if (codeToGlue < 10) {
                itemCodeToReturn = "cy0" + codeToGlue;
            } else {
                itemCodeToReturn = "cy" + codeToGlue;
            }
        } else {
            int codeToGlue = Collections.max(codes) + 1;
            if (codeToGlue < 10) {
                itemCodeToReturn = "sd0" + codeToGlue;
            } else {
                itemCodeToReturn = "sd" + codeToGlue;
            }
        }
        return itemCodeToReturn;
    }
    
    public void updateAddedItem(String ItemCode, String itemName, String CatalogCategory, String describeItem, String Rating, String ImageURL) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        //Setting up connection information
        String url = "jdbc:mysql://localhost:3306/drummindeals";
        String driver = "com.mysql.cj.jdbc.Driver";

        //Has to be wrapped in a try/catch/finally
        try {
            /**
             * This code is for the item table
             */
            //Register driver, get the connection, and user a prepared statement
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, userName, passcode);
            ps = conn.prepareStatement("UPDATE item SET ItemName = ?, CatalogCategory = ?, describeItem = ?, Rating = ?, ImageURL = ? WHERE item.ItemCode = ?");
            //set the parameters within the prep statement
            ps.setString(1, itemName);
            ps.setString(2, CatalogCategory);
            ps.setString(3, describeItem);
            ps.setString(4, Rating);
            ps.setString(5, ImageURL);
            ps.setString(6, ItemCode);
            //execute query and save it to the result set
            ps.execute();

            //Catch a generic exception (do not believe we need to handle these 100%)
        } catch (Exception e) {
            System.out.println(e);
            //In the finally we MUST close the connection, prepared statement, and result set
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
