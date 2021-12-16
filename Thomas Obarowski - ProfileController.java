package drummindeals.business;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.util.*;
import drummindeal.DB.*;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.apache.commons.validator.*;
import org.apache.commons.text.*;
import org.apache.commons.fileupload.*;

/**
 *
 * @author tjobarow
 *
 * This class is an example of a Servlet I created for my "Network Based Application Development Course". 
 * It controls many functions of the web application, such as logging in/out an user, setting session cookies, etc.
 * 
 * The purpose of including this code sample is to show my understanding of the Controller portion of the MVC design pattern,
 * as well as show my understanding of sessions, requests, and responses.
 *
 */
public class ProfileController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //set response type
        response.setContentType("text/html;charset=UTF-8");

        //create and initialize needed variables
        String url = "/index.jsp";
        String action = "";
        String itemCode = "";
        String sessionStatus = "";
        boolean redirectSent = false;

        //Open database objects
        UserDB UserDB = new UserDB();
        ItemDB ItemDB = new ItemDB();
        UserItemDB uiDB = new UserItemDB();

        //get the current session if the user has one
        HttpSession session = request.getSession();

        //Creating another validator since ESAPI doesnt have URL validating that I can find
        UrlValidator urlValidator = new UrlValidator();
        StringEscapeUtils escape = new StringEscapeUtils();

        //validating the action input
        try {
            action = ESAPI.validator().getValidInput("toAction", request.getParameter("action"), "SafeString", 200, false);
        } catch (ValidationException | IntrusionException e) {}
        //validate itemCode parameter
        try {
            //the last parameter of this fuction is allowNull. Since I am not getting an itemCode every time I enter this line of 
            //code I set it to true. Before this I was throwing an exception :)
            itemCode = ESAPI.validator().getValidInput("toItemCode", request.getParameter("item"), "SafeString", 200, true);
        } catch (ValidationException | IntrusionException e) {}

        //if the loggedIn attribute does not exist, we create it and set it to false
        if (session.getAttribute("loggedIn") == null) {
            session.setAttribute("loggedIn", "false");
        }

        //Validate the loggedIn session attribute
        try {
            sessionStatus = ESAPI.validator().getValidInput("toLoggedIn", (String) session.getAttribute("loggedIn"), "SafeString", 200, false);
        } catch (ValidationException | IntrusionException e) {}

        //if the action is to login
        if (action != null && action.equalsIgnoreCase("login")) {
            //we get the hashmap of the users, and get the username and password from the form
            HashMap < String, User > userHash = UserDB.getUserHash();

            //create and initialize userID and password
            String userID = "";
            String password = "";

            try {
                userID = ESAPI.validator().getValidInput("toUserID", request.getParameter("userID"), "SafeString", 200, false);
            } catch (ValidationException | IntrusionException e) {}

            try {
                password = ESAPI.validator().getValidInput("toPassword", request.getParameter("password"), "SafeString", 200, false);
            } catch (ValidationException | IntrusionException e) {}

            //we check to see if the user hashmap contains the userID
            boolean userExists = userHash.containsKey(userID);
            boolean passwordValid;
            if (userExists) {
                //if it does we get the user object itself
                User userObject = userHash.get(userID);
                //we then check the password to make sure it is correct
                passwordValid = userObject.checkCredentials(password);
                if (passwordValid) {
                    //if it checks out we attach the user, userprofile object ot the session and also create a "loggedIn" attribute
                    // to be able to verify the user has logged in
                    session.setAttribute("user", userObject);
                    session.setAttribute("userProfile", UserDB.getUserProfHash().get(userID));
                    session.setAttribute("loggedIn", "true");
                    //at this point I just redirect to the profile
                    url = "/profile?action=profile";
                } else {
                    url = "/errorpages/failedlogin.jsp";
                    getServletContext().getRequestDispatcher(url).forward(request, response);
                }
                //if the username is not contained with the user hashmap we just redirect to a failed login page
            } else {
                url = "/errorpages/failedlogin.jsp";
                getServletContext().getRequestDispatcher(url).forward(request, response);
            }
        }
        //else if the user has logged in we can proceed with normal serving of pages

        //if the user wants to log out, we invalidate the session and send them to a simple signout page
        if (action != null && action.equalsIgnoreCase("logout")) {
            session.setAttribute("loggedIn", "false");
            session.invalidate();
            url = "/signoutlanding.jsp";
            //if the user wants to add an item to their profile
            //we validate that the action is add and is not null

        } else if (action != null && action.equalsIgnoreCase("add") && ((String) session.getAttribute("loggedIn")).equals("true")) {
            //we make sure the itemcode itself is not null
            if (itemCode != null) {
                //if it is not null we them get the item from the ItemDB
                Item item = ItemDB.getItem(itemCode);
                //if the item returns as null it must not exist so we serve an error page
                if (item == null) {
                    url = "/errorpages/itemAddError.jsp";
                } else {
                    //else the item exists, so we create a new UserItem instance
                    UserItem useritem = new UserItem();
                    useritem.setItem(item);
                    useritem.setMadeIt("No");
                    useritem.setRating(item.getRating());

                    //We grab the UserProfile from the current session (cast it as a UserProfile)
                    UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");

                    //we add the useritem to their profile IF it is not a duplicate
                    if (userprofile.duplicateItem(itemCode)) {
                        url = "/errorpages/duplicateError.jsp";
                        session.setAttribute("userProfile", userprofile);
                    } else {
                        //userprofile.addItem(useritem);
                        //Adding item to UserItem table
                        uiDB.addUserItem(useritem, userprofile.getUserID());
                        //we reattach the userProfile object to the session
                        //I AM NOT SURE IF THERE IS A NEED TO REATTACH IT, I AM NOT SURE IF SINCE WE ARE GRABBING THE OBJECT FROM THE SESSION
                        //AND THEN ADDING TO IT, DOES IT UPDATE THE STATE OF THE OBJECT IN THE SESSION BY ITSELF?
                        session.setAttribute("userProfile", userprofile);
                        //we then just redirect to the profile for the user
                        url = "/profile?action=profile";
                    }
                }
            }
        } else if (action != null && action.equalsIgnoreCase("removeItem") && ((String) session.getAttribute("loggedIn")).equals("true")) {
            //we make sure the itemcode itself is not null
            if (itemCode != null) {
                //if it is not null we them get the item from the ItemDB
                Item item = ItemDB.getItem(itemCode);
                //if the item returns as null it must not exist so we serve an error page
                if (item == null) {
                    url = "/errorpages/itemAddError.jsp";
                } else {
                    //We grab the UserProfile from the current session (cast it as a UserProfile)
                    UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");

                    //we remove the useritem to their profile
                    //userprofile.removeItem(item);
                    //we reattach the userProfile object to the session
                    //Also deleting item fro Useritem table
                    uiDB.removeUserItem(itemCode, userprofile.getUserID());
                    //I AM NOT SURE IF THERE IS A NEED TO REATTACH IT, I AM NOT SURE IF SINCE WE ARE GRABBING THE OBJECT FROM THE SESSION
                    //AND THEN ADDING TO IT, DOES IT UPDATE THE STATE OF THE OBJECT IN THE SESSION BY ITSELF?
                    session.setAttribute("userProfile", userprofile);
                    //we then just redirect to the profile for the user
                    url = "/profile?action=profile";
                }
            } else {
                url = "/errorpages/itemAddError.jsp";
            }
        } //If they want the profile
        else if (action != null && action.equalsIgnoreCase("profile") && session.getAttribute("loggedIn").equals("true")) {
            UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");
            userprofile.getAllProfItems();
            //session.setAttribute("userProfile", userprofile);
            url = "/profile.jsp";
        } //If they want to view their listings
        else if (action != null && action.equalsIgnoreCase("listings") && session.getAttribute("loggedIn").equals("true")) {
            UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");
            userprofile.addUserAddedItems(ItemDB.getUserAddedItems(userprofile.getUserID()));
            session.setAttribute("userProfile", userprofile);
            url = "/myItems.jsp";
        } //if they want to rate an item
        else if (action != null && action.equalsIgnoreCase("rateItem") && session.getAttribute("loggedIn").equals("true")) {
            if (itemCode != null) {
                //if it is not null we them get the item from the ItemDB
                Item item = ItemDB.getItem(itemCode);
                //if the item returns as null it must not exist so we serve an error page
                if (item == null) {
                    url = "/errorpages/itemAddError.jsp";
                } else {
                    session.setAttribute("sinItem", item);
                    //we then just redirect to the feedback page
                    url = "/feedback.jsp";
                }
            } else {
                url = "/errorpages/itemAddError.jsp";
            }
        } //this saves the rating
        else if (action != null && action.equalsIgnoreCase("saveRating") && session.getAttribute("loggedIn").equals("true")) {
            if (itemCode != null) {
                //if it is not null we them get the item from the ItemDB
                Item item = ItemDB.getItem(itemCode);
                //if the item returns as null it must not exist so we serve an error page
                if (item == null) {
                    url = "/errorpages/itemAddError.jsp";
                } else {
                    String rating = request.getParameter("itemRating");
                    item.setRating(rating);
                    ItemDB.saveRating(itemCode, rating);
                    //we then just redirect to the item page
                    url = "./catalog?item=" + item.getItemCode();
                    response.sendRedirect(url);
                    redirectSent = true;
                }
            } else {
                url = "/errorpages/itemAddError.jsp";
            }
        } else if (action != null && action.equalsIgnoreCase("updateUserItem") && session.getAttribute("loggedIn").equals("true")) {
            if (itemCode != null) {
                //if it is not null we them get the item from the ItemDB
                UserProfile userProf = (UserProfile)(session.getAttribute("userProfile"));
                UserItem useritem = userProf.getUserItem(itemCode);
                //if the item returns as null it must not exist so we serve an error page
                if (useritem == null) {
                    url = "/errorpages/itemAddError.jsp";
                } else {
                    session.setAttribute("userItem", useritem);
                    //we then just redirect to the feedback page
                    url = "/updateItem.jsp";
                }
            } else {

                url = "/errorpages/itemAddError.jsp";
            }
        } else if (action != null && action.equalsIgnoreCase("saveUpdate") && session.getAttribute("loggedIn").equals("true")) {
            //if it is not null we them get the item from the ItemDB
            UserProfile userProf = (UserProfile)(session.getAttribute("userProfile"));
            if (itemCode != null && userProf.getUserItem(itemCode) != null) {
                //userProf.updateItem(request.getParameter("boughtIt"), request.getParameter("itemRating"), itemCode);
                uiDB.addItemRating(itemCode, userProf.getUserID(), request.getParameter("itemRating"));
                uiDB.addMadeIt(itemCode, userProf.getUserID(), request.getParameter("boughtIt"));
                //we then just redirect to the feedback page
                url = "/profile?action=profile";
            } else {
                url = "/errorpages/itemAddError.jsp";
            }
        } else if (action != null && action.equalsIgnoreCase("contact")) {
            if (session.getAttribute("loggedIn").equals("true")) {
                UserProfile prof = (UserProfile) session.getAttribute("userProfile");
                String userID = prof.getUserID();
                User user = UserDB.getUser(userID);
                String fullName = user.getFirstName() + " " + user.getLastName();
                request.setAttribute("fullName", ESAPI.encoder().encodeForHTML(fullName));
                request.setAttribute("email", ESAPI.encoder().encodeForHTMLAttribute(user.getEmail()));
            }
            url = "/contact.jsp";
        } else if (action != null && action.equalsIgnoreCase("contactSend")) {
            String name = "";
            String email = "";
            String message = "";
            try {
                name = ESAPI.validator().getValidInput("toAction", request.getParameter("name"), "SafeString", 200, false);
            } catch (ValidationException | IntrusionException e) {}
            try {
                email = ESAPI.validator().getValidInput("toAction", request.getParameter("email"), "Email", 200, false);
            } catch (ValidationException | IntrusionException e) {}
            try {
                message = ESAPI.validator().getValidInput("toAction", request.getParameter("message"), "SafeString", 500, false);
            } catch (ValidationException | IntrusionException e) {}
            url = "/contact-confirmation.jsp";
        } else if (action != null && action.equalsIgnoreCase("register") && session.getAttribute("loggedIn").equals("false")) {
            url = "/register.jsp";
        } else if (action != null && action.equalsIgnoreCase("register") && session.getAttribute("loggedIn").equals("true")) {
            url = "/errorpages/registerError.jsp";
        } else if (action != null && action.equalsIgnoreCase("registerAccount") && session.getAttribute("loggedIn").equals("false")) {
            String userID = "", password = "", email = "", firstName = "", lastName = "", address = "", city = "", state = "", zip = "", country = "";
            try {
                userID = ESAPI.validator().getValidInput("toUserID", request.getParameter("userID"), "SafeString", 200, false);
                password = ESAPI.validator().getValidInput("toPassword", request.getParameter("password"), "SafeString", 200, false);
                email = ESAPI.validator().getValidInput("toEmail", request.getParameter("email"), "Email", 200, false);
                firstName = ESAPI.validator().getValidInput("toFirstName", request.getParameter("firstName"), "SafeString", 200, false);
                lastName = ESAPI.validator().getValidInput("toLastName", request.getParameter("lastName"), "SafeString", 200, false);
                address = ESAPI.validator().getValidInput("toAddress", request.getParameter("address"), "SafeString", 200, false);
                city = ESAPI.validator().getValidInput("toCity", request.getParameter("city"), "SafeString", 200, false);
                state = ESAPI.validator().getValidInput("toState", request.getParameter("state"), "SafeString", 200, false);
                zip = ESAPI.validator().getValidInput("toZip", request.getParameter("zip"), "SafeString", 200, false);
                country = ESAPI.validator().getValidInput("toCountry", request.getParameter("country"), "SafeString", 200, false);
            } catch (ValidationException | IntrusionException e) {}
            boolean success = UserDB.makeUser(userID, password, email, firstName, lastName, address, city, state, zip, country);
            if (success) {
                url = "/registerLanding.jsp";
            } else {
                url = "/errorpages/userIDTaken.jsp";
            }

        } else if (action != null && action.equalsIgnoreCase("enterItem") && !session.getAttribute("loggedIn").equals("false")) {
            url = "/enterItem.jsp";
        } else if (action != null && action.equalsIgnoreCase("addItem") && !session.getAttribute("loggedIn").equals("false")) {
            String itemName = "", CatalogCategory = "", describeItem = "", Rating = "", imageURL = "";
            /**
             * Okay so here is the deal. In drums sizes are commonly referred to
             * as 14" for 14 inch. When I try to input 14" snare into the form
             * on the webpage It returns 14\" snare into java. This is an
             * illegal string in java. I cannot use .replace("\\",""), or regex
             * to get rid of this stupid character. I have been trying to think
             * of a way for hours. I cannot find any solution. What I have done
             * above is use apache commons to escape the string for HTML, change
             * 14" snare into 14&quot; snare. This still will not validate.
             * Also, the HCI website with the different "types" for ESAPI is
             * currently down. So I cannot check to see what other types I can
             * try. Right now I have no clue what to do other than not validate
             * it, and ask on the piazza for help. There is not much time left
             * to do this assignment so I may have to regretfully take a
             * deduction on this. I really tried everything.
             */
            itemName = request.getParameter("itemName");
            try {
                CatalogCategory = ESAPI.validator().getValidInput("toCatCategory", request.getParameter("catalogCategory"), "SafeString", 200, false);
                describeItem = ESAPI.validator().getValidInput("toDescribeItem", request.getParameter("describeItem"), "SafeString", 750, false);
                Rating = ESAPI.validator().getValidInput("toFirstName", request.getParameter("rating"), "SafeString", 1, false);
            } catch (ValidationException | IntrusionException | Error e) {
                System.out.println("The error IS: " + e);
            }
            //I couldnt find URI validation with ESAPI so I just went with Apache Commons. Hopefully this is okay. 
            if (!urlValidator.isValid(request.getParameter("imageURL"))) {
                url = "/errorpages/badItem.jsp";
                getServletContext().getRequestDispatcher(url).forward(request, response);
            } else {
                imageURL = request.getParameter("imageURL");
            }
            UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");
            boolean success = ItemDB.makeItem(userprofile.getUserID(), itemName, CatalogCategory, describeItem, Rating, imageURL);
            if (success) {
                url = "/itemLanding.jsp";
            } else {
                url = "/errorpages/itemCreateError.jsp";
            }

        } else if (action != null && action.equalsIgnoreCase("editItemView") && !session.getAttribute("loggedIn").equals("false")) {
            Item item = ItemDB.getItem(request.getParameter("item"));
            request.setAttribute("editItem", item);
            url = "/editAddedItem.jsp";
        } else if (action != null && action.equalsIgnoreCase("editItem") && !session.getAttribute("loggedIn").equals("false")) {
            String itemName = "", CatalogCategory = "", describeItem = "", Rating = "", imageURL = "", ItemCode = "";
            /**
             * Okay so here is the deal. In drums sizes are commonly referred to
             * as 14" for 14 inch. When I try to input 14" snare into the form
             * on the webpage It returns 14\" snare into java. This is an
             * illegal string in java. I cannot use .replace("\\",""), or regex
             * to get rid of this stupid character. I have been trying to think
             * of a way for hours. I cannot find any solution. What I have done
             * above is use apache commons to escape the string for HTML, change
             * 14" snare into 14&quot; snare. This still will not validate.
             * Also, the HCI website with the different "types" for ESAPI is
             * currently down. So I cannot check to see what other types I can
             * try. Right now I have no clue what to do other than not validate
             * it, and ask on the piazza for help. There is not much time left
             * to do this assignment so I may have to regretfully take a
             * deduction on this. I really tried everything.
             */
            itemName = request.getParameter("itemName");
            try {
                CatalogCategory = ESAPI.validator().getValidInput("toCatCategory", request.getParameter("catalogCategory"), "SafeString", 200, false);
                describeItem = ESAPI.validator().getValidInput("toDescribeItem", request.getParameter("describeItem"), "SafeString", 750, false);
                Rating = ESAPI.validator().getValidInput("toFirstName", request.getParameter("rating"), "SafeString", 1, false);
                ItemCode = ESAPI.validator().getValidInput("toFirstName", request.getParameter("ItemCode"), "SafeString", 10, false);
            } catch (ValidationException | IntrusionException e) {}
            //I couldnt find URI validation with ESAPI so I just went with Apache Commons. Hopefully this is okay. 
            if (!urlValidator.isValid(request.getParameter("imageURL"))) {
                url = "/errorpages/badItem.jsp";
                getServletContext().getRequestDispatcher(url).forward(request, response);
            } else {
                imageURL = request.getParameter("imageURL");
            }
            UserProfile userprofile = (UserProfile) session.getAttribute("userProfile");
            ItemDB.updateAddedItem(ItemCode, itemName, CatalogCategory, describeItem, Rating, imageURL);
            url = "/itemUpdatedLanding.jsp";
        } //final condition to redirect if they are not logged in
        else if (session.getAttribute("loggedIn").equals("false")) {
            url = "/signin.jsp";
        }

        if (!redirectSent) {
            getServletContext().getRequestDispatcher(url).forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    } // </editor-fold>

}