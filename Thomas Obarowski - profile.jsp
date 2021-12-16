<%-- 
   Document   : profile
   Created on : Mar 13, 2019, 12:57:39 PM
   Author     : Thomas Obarowski

   This code sample is provided to show knowledge in HTML structure and JSP.

--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="locale" content="US">
        <meta name="language" content="en">
        <meta name="country" content="US">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <title>Drummin' Deals</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="./css/stylesheet.css">
        <link rel="stylesheet" href="./css/bootstrap.css">
        <script src="./js/jquery.min.js"></script>
        <script src="./js/bootstrap.js"></script>
        <script src="./js/drummindeals.js"></script>
    </head>
    <body class="cui">
        <jsp:include page="./header.jsp"/>
        <jsp:include page="./user-navigation.jsp"/>
        <jsp:include page="./site-navigation.jsp"/>
        <main>
            <div id="profileBody">
                <h1>Hello, <strong><c:out value="${user.firstName}!"></c:out> <c:out value="${user.lastName}!"></c:out></strong>! </h1>
                <hr/>
                </br>
                <h4>Your email address is: </h4>
                <p><c:out value="${user.email}!"></c:out></p>
                </br>
                <h4>Your address is: </h4>
                <p>
                    <c:out value="${user.addressOne}">Not provided</c:out>
                    <c:out value="${user.city}"></c:out>, <c:out value="${user.state}"></c:out> <c:out value="${user.zip}"></c:out>, <c:out value="${user.country}"></c:out>
                </p>
                </br>
                </br>
                <hr/>
                <h2> Your Favorite Items </h2>
                <br>
                <table class="table table-dark savedList">
                    <thead>
                        <tr>
                            <th scope="col">Item Image</th>
                            <th scope="col">Item Name</th>
                            <th scope="col">Category</th>
                            <th scope="col">Rating</th>
                            <th scope="col">Have You Purchased This?</th>
                            <th scope="col"></th>
                            <th scope="col"></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${userProfile.getItems()}">
                            <tr>
                                <td id="imageCol"><img id="listingImage" src="<c:out value="${item.item.imageURL}"></c:out>" alt="<c:out value="${item.item.itemCode}"></c:out>"/></td>
                                <td><a href="./catalog?item=<c:out value="${item.item.itemCode}"></c:out>"><c:out value="${item.item.itemName}"></c:out></a></td>
                                <td><c:out value="${item.item.catalogCategory}"></c:out></td>
                                <td><c:out value="${item.rating}"></c:out></td>
                                <td><c:out value="${item.madeIt}"></c:out></td>
                                <td>
                                    <form action="./profile?action=updateUserItem&item=<c:out value="${item.item.itemCode}"></c:out>" method="post">
                                        <button type="submit" class="btn btn-outline-primary btn-sm">Update Saved Item</button>
                                    </form>
                                </td>
                                <td>
                                    <form action="./profile?action=removeItem&item=<c:out value="${item.item.itemCode}"></c:out>" method="post">
                                        <button type="submit" class="btn btn-outline-primary btn-sm">Remove Item</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <div class="logout">
                    <form action="./profile?action=logout" method="post">
                        <button type="submit" class="btn btn-outline-primary">Sign Out</button>
                    </form>
                </div>
            </div>

        </main>
        <jsp:include page="./footer.jsp"/>
    </body>
</html>