import java.sql.*;
import java.util.*;
class User {
    int id;
    String name;
    String password;

    void addUser(Connection con, Scanner sc) throws Exception {
        while (true) {
            System.out.print("Enter User id : ");
            int id = sc.nextInt();
            sc.nextLine();

            String checkQuery = "SELECT COUNT(*) FROM User WHERE user_id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            checkPst.setInt(1, id);
            ResultSet rs = checkPst.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("User ID already exists. Please try a different ID.");
            } else {
                String pass = getPassword(sc);
                System.out.print("Enter Full Name : ");
                String name = sc.nextLine();
                String query = "INSERT INTO User VALUES (?,?,?)";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setInt(1, id);
                pst.setString(2, name);
                pst.setString(3, pass);
                int r = pst.executeUpdate();
                System.out.println((r > 0) ? "Sign Up successful. Please login" : "Try again");
                System.out.println();
                userLogin(con, sc);
                break;
            }
        }
    }

    public void userLogin(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter User ID : ");
        int id = sc.nextInt();
        sc.nextLine();
        String pass = getPassword(sc);
        String query = "SELECT * FROM User WHERE user_id = ? AND password = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, id);
        pstmt.setString(2, pass);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("User Login Successfull");
            userOptions(con, sc, id); // to display user menu and perform operations
        } else {
            System.out.println("Invalid User ID or Password");
        }
    }

    String getPassword(Scanner sc) {
        boolean flag = true;
        String pass = "";

        while (flag) {
            System.out.print("Enter Password : ");
            pass = sc.nextLine();
            int digitCount = 0;
            for (char c : pass.toCharArray()) {
                if (Character.isDigit(c)) {
                    digitCount++;
                }
            }
            if (pass.length() >= 8 && digitCount >= 4) {
                flag = false;
            } else {
                System.out.println("Password must be greater than 8 Characters with more than 4 Digits");
            }
        }
        return pass;
    }

    void userOptions(Connection con, Scanner sc, int user_id) throws Exception {
        boolean notExit = true;
        while (notExit) {
            System.out.println();
            System.out.println(
                    "What do you want to do?\n1.Book Ticket\n2.Cancel Booking\n3.Show Movies Based on Rating \n4.list Watched Movies\n5.list Booked Upcoming Movies\n6.Exit  ");
            int opt = sc.nextInt();
            Booking b = new Booking();
            switch (opt) {
                case 1:
                    b.bookTicket(con, user_id);
                    break;
                case 2:
                    b.cancelTicket(con, user_id, sc);
                    break;
                case 3:
                    Movie m = new Movie();
                    m.displayAllMovies(con);
                    break;
                case 4:
                    b.listWatchedMovies(con, user_id);
                    break;
                case 5:
                    b.listBookedUpcomingMovies(con, user_id);
                    break;
                case 6:
                    System.out.println("Exiting ...");
                    System.out.println();
                    notExit = false;
                    break;
                default:
                    System.out.println("Invalid Input ");
                    break;
            }
        }

    }

}

class Booking {
    static Scanner sc = new Scanner(System.in);

    void bookTicket(Connection con, int user_id) throws Exception {
        chooseTheater(con, user_id);
    }

    void chooseTheater(Connection con, int user_id) throws Exception {

        String query = "SELECT m.movie_name, m.genre, m.rating "
                + "FROM movie m " +
                "JOIN showtime st ON m.movie_id = st.movie_id " +
                "WHERE st.show_date > CURRENT_DATE " +
                "OR (st.show_date = CURRENT_DATE AND st.show_time >= CURRENT_TIME)";
        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rst = pst.executeQuery();
        // HashSet to store distinct Names of movies
        Set<String> movieNames = new HashSet<>();

        if (!rst.next()) {
            System.out.println("No Upcoming movies available ");
            return;
        }
        int count = 1;
        System.out.println("All Upcoming Movie List ");
        System.out.println("-------------------------------------------");
        do {
            if (movieNames.contains(rst.getString("movie_name").toLowerCase())) {
                continue;
            }
            movieNames.add(rst.getString("movie_name").toLowerCase());

            String movieName = rst.getString("movie_name");
            String genre = rst.getString("genre");
            double rating = rst.getDouble("rating");
            // Displaying only the info which will be common in same name movies
            System.out.println(count + ":");
            System.out.println("Movie Name : " + movieName);
            System.out.println("Movie genre : " + genre);
            System.out.println("Movie Rating : " + rating);
            count++;
            System.out.println("-------------------------------------------");
        } while (rst.next());

        String movieName;
        while (true) {
            System.out.println("Enter the movie name you want to watch from the above list:");
            movieName = sc.nextLine().toLowerCase();
            // Checking if hashSet contains this name or not
            if (movieNames.contains(movieName)) {
                System.out.println("Movie selected: " + movieName);
                break;
            } else {
                System.out.println("Movie not found. Please enter a valid movie name.");
            }
        }
        // Now we will Display the theaters who are screening this movieName movies
        String theaterIdQuery = "SELECT theater_id FROM Movie WHERE movie_name = ? ";
        // HashMap to store TheaterId and Its name cuz if we directly check if theaterId
        // exist in theater or not
        // (the ans mayBe yes but we can`t make sure its this movie only whose Theater
        // id is selected hence we use hashMap )
        HashMap<Integer, String> theaterMap = new HashMap<>();
        PreparedStatement pst1 = con.prepareStatement(theaterIdQuery);
        pst1.setString(1, movieName);
        ResultSet rs = pst1.executeQuery();
        System.out.println();

        if (!rs.next()) {
            System.out.println("No Theater Screening " + movieName);
            return;
        }
        System.out.println("Displaying all Theater screeing movie " + movieName + ".");
        System.out.println("---------------------------------------");
        do {
            int theater_id = rs.getInt(1);
            String queryTh = "SELECT Theater.theater_id, Theater.name, Theater.location, Movie.cost " +
                    "FROM Theater " +
                    "JOIN Movie ON Theater.theater_id = Movie.theater_id " +
                    "WHERE Movie.movie_name = ? AND Theater.theater_id = ?";

            PreparedStatement pstm = con.prepareStatement(queryTh);
            pstm.setInt(2, theater_id);
            pstm.setString(1, movieName);
            ResultSet rset = pstm.executeQuery();
            if (rset.next()) {
                int theaterId = rset.getInt("theater_id");
                String theaterName = rset.getString("name");
                theaterMap.put(theaterId, theaterName);
                System.out.println("Theater Id: " + rset.getInt(1));
                System.out.println("Theater Name: " + capitalizeFirstLetter(theaterName));
                System.out.println("Theater Location: " + rset.getString("location"));
                System.out.println("Cost of Movie " + movieName + " is " + rset.getDouble("cost"));

            } else {
                System.out.println("Theater with this " + theater_id + " not found");
            }
            System.out.println("---------------------------------------");
        } while (rs.next());
        System.out.println();

        // Used this cuz after seeing price and location of theater what if user not
        // wants to book
        while (true) {
            System.out.println("Do you want to continue choose Theater : (y/n)");
            String ans = sc.nextLine();
            if (ans.equals("n")) {
                System.out.println("Thanks for Visiting ..");
                return;
            } else if (ans.equals("y")) {
                break;
            } else {
                System.out.println("Invalid Input, Enter valid input :");
            }
        }

        int theaterId = 0;
        while (true) {
            System.out.println("Enter the theater id from the above list: ");
            theaterId = sc.nextInt();
            sc.nextLine();
            if (theaterMap.containsKey(theaterId)) {
                break;
            } else {
                System.out.println("Theater ID not found for the selected movie. Please enter again.");
            }
        }

        ArrayList<Integer> showIds = new ArrayList<>();
        System.out.println();
        String showIdQuery = "SELECT st.id, st.show_date, st.show_time, m.movie_name, m.cost " +
                "FROM showTime st " +
                "JOIN movie m ON st.movie_id = m.movie_id " +
                "WHERE m.movie_name = ? AND m.theater_Id = ?";
        PreparedStatement showIdPs = con.prepareStatement(showIdQuery);
        showIdPs.setString(1, movieName);
        showIdPs.setInt(2, theaterId);
        ResultSet showIResultSet = showIdPs.executeQuery();
        System.out.println();
        if (!showIResultSet.next()) {
            System.out.println("No Shows Available for " + movieName + " in Theater having theater Id : " + theaterId);
            return;
        }
        System.out.println("Available Shows for the Selected Movie : ");
        System.out.println("--------------------------------------------------------------------------------");
        do {
            int show_id = showIResultSet.getInt("id");
            showIds.add(show_id);
            System.out.println("Show ID: " + show_id +
                    ", Date: " + showIResultSet.getDate("show_date") +
                    ", Time: " + showIResultSet.getTime("show_Time") +
                    ", Movie: " + showIResultSet.getString("movie_name") +
                    ", Price: " + showIResultSet.getDouble("cost"));
            System.out.println("---------------------------------------------------------------------------------");
        } while (showIResultSet.next());
        System.out.println();
        int showId = -1;
        while (true) {
            System.out.println("Enter The show Id ");
            showId = sc.nextInt();
            sc.nextLine();
            if (showIds.contains(showId)) {
                break;
            } else {
                System.out.println("Invalid show Id , Enter Valid ");
            }
        }

        // To check if the same user already connected to the same show
        String countQuery = "SELECT COUNT(*) " +
                "FROM bookMovie " +
                "WHERE user_id = ? AND show_id = ?;";
        PreparedStatement countPs = con.prepareStatement(countQuery);
        countPs.setInt(1, user_id);
        countPs.setInt(2, showId);
        ResultSet countRs = countPs.executeQuery();
        if (countRs.next() && countRs.getInt(1) > 0) {
            System.out.println("User Already Have a show !!");
            return;
        }

        int numberOfTickets = 0;
        while (true) {
            String sql3 = "Select remainingTickets from showtime where theater_id=?  and id=?";
            PreparedStatement pst3 = con.prepareStatement(sql3);
            pst3.setInt(1, theaterId);

            pst3.setInt(2, showId);
            ResultSet rst3 = pst3.executeQuery();
            rst3.next();

            int remainingTickets = rst3.getInt("remainingTickets");
            System.out.println(
                    "Enter the number of tickets to Book :(Total number of Tickets Are " + remainingTickets + " )");
            numberOfTickets = sc.nextInt();
            sc.nextLine();
            if (numberOfTickets < 1) {
                System.out.println("Invalid input");
            } else if (remainingTickets >= numberOfTickets) {
                System.out.println("Ticket Confirm");
                break;
            } else {
                System.out.println("More than available seats ");
            }

        }

        String updateTickets = "UPDATE showtime SET remainingTickets = remainingTickets - ? WHERE theater_id = ? and id=?";
        PreparedStatement pstUpdate = con.prepareStatement(updateTickets);
        pstUpdate.setInt(1, numberOfTickets);
        pstUpdate.setInt(2, theaterId);
        pstUpdate.setInt(3, showId);
        pstUpdate.executeUpdate();
        int movie_id = 0;
        String movieIdQuery = "SELECT movie_id FROM showTime WHERE id = ? AND theater_id = ?";

        PreparedStatement movieIdQueryPs = con.prepareStatement(movieIdQuery);
        movieIdQueryPs.setInt(1, showId);
        movieIdQueryPs.setInt(2, theaterId);

        ResultSet movieIdQueryRs = movieIdQueryPs.executeQuery();

        if (movieIdQueryRs.next()) {
            movie_id = movieIdQueryRs.getInt("movie_id");
        }

        String insertBooking = "INSERT INTO bookmovie (user_id, movie_id, theater_id, show_id, numberOfTickets) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstInsert = con.prepareStatement(insertBooking);
        pstInsert.setInt(1, user_id);
        pstInsert.setInt(2, movie_id);
        pstInsert.setInt(3, theaterId);
        pstInsert.setInt(4, showId);
        pstInsert.setInt(5, numberOfTickets);
        pstInsert.executeUpdate();

        String bookIdQuery = "SELECT id FROM bookMovie WHERE user_id = ? AND show_id = ?";
        PreparedStatement pstmt = con.prepareStatement(bookIdQuery);
        pstmt.setInt(1, user_id);
        pstmt.setInt(2, showId);

        ResultSet rst1 = pstmt.executeQuery();
        int book_id = 0;
        if (rst1.next()) {
            book_id = rst1.getInt("id");
        }

        String costQuery = "SELECT cost FROM movie WHERE movie_id = ?";
        PreparedStatement costPs = con.prepareStatement(costQuery);
        costPs.setInt(1, movie_id);
        ResultSet costRs = costPs.executeQuery();
        double cost = 0;
        if (costRs.next()) {
            cost = costRs.getDouble("cost");
        }
        double amountToPay = (numberOfTickets * cost);

        System.out.println();
        System.out.println("Booking Confirm Of User : " + user_id + "\nBooking Id of User :" + book_id
                + "\nProceed Payment : " + amountToPay);
        System.out.println();
        if (paymentGateWay(amountToPay, con, book_id, user_id)) {
            viewTicket(user_id, showId, con);
        } else {
            try {
                String deleteSQL = "DELETE FROM bookMovie WHERE id = ?";
                PreparedStatement deleteStmt = con.prepareStatement(deleteSQL);
                deleteStmt.setInt(1, book_id);

                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Booking Cancelled");
                } else {
                    System.out.println("No booking record found to delete.");
                }

                String updateSql = "UPDATE showtime SET remainingTickets = remainingTickets + ?  where id=?";
                PreparedStatement updateSqlPs = con.prepareStatement(updateSql);
                updateSqlPs.setInt(1, numberOfTickets);
                updateSqlPs.setInt(2, showId);
                updateSqlPs.execute();

            } catch (SQLException e) {
                System.err.println("Error deleting booking record: " + e.getMessage());
            }
        }
    }

    public boolean paymentGateWay(double amountToPay, Connection con, int book_id, int user_id) {
        System.out.println("Enter card number(16 Digits) : ");
        String cardNumber = sc.nextLine();

        System.out.println("Enter expiration date (MM/YY): ");
        String expirationDate = sc.nextLine();

        System.out.println("Enter CVV(3 digits): ");
        String cvv = sc.nextLine();
        System.out.println("Processing Payment ...  ");
        String sql = "{CALL insertPayment(?, ?, ?, ?,?,?,?)}";
        try {
            CallableStatement pstmt = con.prepareCall(sql);
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, expirationDate);
            pstmt.setString(3, cvv);
            pstmt.setInt(5, user_id);
            pstmt.setInt(4, book_id);
            pstmt.setDouble(6, amountToPay);
            pstmt.registerOutParameter(7, Types.BOOLEAN);

            pstmt.execute();

            boolean result = pstmt.getBoolean(7);
            if (result) {
                System.out.println("Payment record inserted successfully!");
                return true;
            } else {
                System.out.println("Failed to insert payment record.");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return false;

    }

    public void cancelTicket(Connection con, int user_id, Scanner sc) throws Exception {
        System.out.println("Enter the booking ID to cancel: ");
        int book_id = sc.nextInt();
        sc.nextLine();
        // This Query Shows One can only cancel ticket if showdate is yet to come or if
        // showdate is todays date then showtime needs to be greater then currtime
        String checkQuery = "SELECT bm.numberOfTickets, s.theater_id, s.id ,bm.show_id " +
                "FROM bookMovie bm " +
                "JOIN showtime s ON bm.show_id = s.id " +
                "WHERE bm.id = ? AND bm.user_id = ? AND (s.show_date > CURRENT_DATE OR (s.show_date = CURRENT_DATE AND s.show_time >= CURRENT_TIME))";
        String deleteBookingQuery = "DELETE FROM bookMovie WHERE id = ?";
        String updateTicketsQuery = "UPDATE showTime SET remainingTickets = remainingTickets + ? WHERE theater_id = ? AND id = ?";

        PreparedStatement checkStmt = con.prepareStatement(checkQuery);
        checkStmt.setInt(1, book_id);
        checkStmt.setInt(2, user_id);

        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int numberOfTickets = rs.getInt("numberOfTickets");
            int theater_id = rs.getInt("theater_id");
            int show_id = rs.getInt("show_id");

            PreparedStatement deleteBookingStmt = con.prepareStatement(deleteBookingQuery);
            deleteBookingStmt.setInt(1, book_id);
            deleteBookingStmt.executeUpdate();
            deleteBookingStmt.close();

            PreparedStatement updateTicketsStmt = con.prepareStatement(updateTicketsQuery);
            updateTicketsStmt.setInt(1, numberOfTickets);
            updateTicketsStmt.setInt(2, theater_id);
            updateTicketsStmt.setInt(3, show_id);
            updateTicketsStmt.executeUpdate();
            updateTicketsStmt.close();

            System.out.println("Ticket cancelled successfully.");
            System.out.println("Refund Transfer Successfully ");
        } else {
            System.out.println("Cannot cancel. The show date and time have already passed or booking not found.");
        }
    }

    public void viewTicket(int user_id, int show_id, Connection con) throws Exception {

        String query = " SELECT bm.id, bm.movie_id, m.movie_name, m.genre, m.release_date, m.cost, m.rating, " +
                " bm.theater_id, bm.show_id, bm.numberOfTickets, st.show_date, st.show_time " +
                " FROM bookMovie bm " +
                " JOIN movie m ON bm.movie_id = m.movie_id " +
                " JOIN showTime st ON bm.theater_id = st.theater_id AND bm.show_id = st.id " +
                " WHERE bm.user_id = ? AND bm.show_id = ? ";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, user_id);
        pstmt.setInt(2, show_id);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            int bookingId = rs.getInt("id");
            int movieId = rs.getInt("movie_id");
            String movieName = rs.getString("movie_name");
            String genre = rs.getString("genre");
            String releaseDate = rs.getString("release_date");
            double cost = rs.getDouble("cost");
            double rating = rs.getDouble("rating");
            int theaterId = rs.getInt("theater_id");
            int showId = rs.getInt("show_id");
            String show_date = rs.getString("show_date");
            String show_time = rs.getString("show_time");

            int numberOfTickets = rs.getInt("numberOfTickets");

            // Print or return the necessary information
            System.out.println();
            System.out.println("Printing the ticket details");
            System.out.println("-----------------------------");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Movie Name: " + movieName);
            System.out.println("Genre: " + genre);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Show Date : " + show_date);
            System.out.println("Show Time : " + show_time);
            System.out.println("Cost: " + cost);
            System.out.println("Rating: " + rating);
            System.out.println("Theater ID: " + theaterId);
            System.out.println("Show ID: " + showId);
            System.out.println("Number of Tickets: " + numberOfTickets);
            System.out.println("-----------------------------");
        }

    }

    public void listWatchedMovies(Connection con, int user_id) throws Exception {
        // Query gonna give old watched Movies by checking the showDate and showTime
        String pastTicketQuery = " SELECT bm.id, bm.movie_id, m.movie_name, m.genre, m.release_date, m.cost, m.rating,"
                +
                " bm.theater_id,bm.show_id,bm.numberOfTickets,st.show_date, st.show_time FROM bookMovie bm " +
                " JOIN movie m ON bm.movie_id = m.movie_id " +
                " JOIN showTime st ON bm.theater_id = st.theater_id AND bm.show_id = st.id "
                + " WHERE bm.user_id = ? AND st.show_date < CURRENT_DATE " +
                " OR (st.show_date = CURRENT_DATE AND st.show_time < CURRENT_TIME);";

        PreparedStatement pstmt = con.prepareStatement(pastTicketQuery);
        pstmt.setInt(1, user_id);
        ResultSet rs = pstmt.executeQuery();
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int bookingId = rs.getInt("id");
            int movieId = rs.getInt("movie_id");
            String movieName = rs.getString("movie_name");
            String genre = rs.getString("genre");
            String releaseDate = rs.getString("release_date");
            double cost = rs.getDouble("cost");
            double rating = rs.getDouble("rating");
            int theaterId = rs.getInt("theater_id");
            int showId = rs.getInt("show_id");
            String show_date = rs.getString("show_date");
            String show_time = rs.getString("show_time");

            int numberOfTickets = rs.getInt("numberOfTickets");

            // Print or return the necessary information
            System.out.println();
            System.out.println("Printing the ticket details");
            System.out.println("-----------------------------");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Movie Name: " + movieName);
            System.out.println("Genre: " + genre);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Show Date : " + show_date);
            System.out.println("Show Time : " + show_time);
            System.out.println("Cost: " + cost);
            System.out.println("Rating: " + rating);
            System.out.println("Theater ID: " + theaterId);
            System.out.println("Show ID: " + showId);
            System.out.println("Number of Tickets: " + numberOfTickets);
            System.out.println("-----------------------------");
        }
        if (!hasResults) {
            // Inform user if no past tickets were found
            System.out.println("No past tickets found.");
        }
    }

    public void listBookedUpcomingMovies(Connection con, int user_id) throws Exception {
        // Query gonna give upcoming Movies by checking the showDate and showTime

        String futureTicketsQuery = "SELECT bm.id, bm.movie_id, m.movie_name, m.genre, m.release_date, m.cost, m.rating,"
                +
                " bm.theater_id,bm.show_id,bm.numberOfTickets,st.show_date, st.show_time FROM bookMovie bm " +
                " JOIN movie m ON bm.movie_id = m.movie_id " +
                " JOIN showTime st ON bm.theater_id = st.theater_id AND bm.show_id = st.id "
                + " WHERE bm.user_id = ? AND st.show_date > CURRENT_DATE " +
                " OR (st.show_date = CURRENT_DATE AND st.show_time > CURRENT_TIME);";

        PreparedStatement pstmt = con.prepareStatement(futureTicketsQuery);
        pstmt.setInt(1, user_id);
        ResultSet rs = pstmt.executeQuery();
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            int bookingId = rs.getInt("id");
            int movieId = rs.getInt("movie_id");
            String movieName = rs.getString("movie_name");
            String genre = rs.getString("genre");
            String releaseDate = rs.getString("release_date");
            double cost = rs.getDouble("cost");
            double rating = rs.getDouble("rating");
            int theaterId = rs.getInt("theater_id");
            int showId = rs.getInt("show_id");
            String show_date = rs.getString("show_date");
            String show_time = rs.getString("show_time");

            int numberOfTickets = rs.getInt("numberOfTickets");

            // Print or return the necessary information
            System.out.println();
            System.out.println("Printing the ticket details");
            System.out.println("-----------------------------");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("Movie Name: " + movieName);
            System.out.println("Genre: " + genre);
            System.out.println("Release Date: " + releaseDate);
            System.out.println("Show Date : " + show_date);
            System.out.println("Show Time : " + show_time);
            System.out.println("Cost: " + cost);
            System.out.println("Rating: " + rating);
            System.out.println("Theater ID: " + theaterId);
            System.out.println("Show ID: " + showId);
            System.out.println("Number of Tickets: " + numberOfTickets);
            System.out.println("-----------------------------");
        }
        if (!hasResults) {
            // Inform user if no past tickets were found
            System.out.println("No future booked tickets found.");
        }
    }

    static String capitalizeFirstLetter(String str) {

        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
