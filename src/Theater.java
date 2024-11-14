import java.util.*;
import java.sql.*;

public class Theater {
    int id;
    String name;
    String password;
    String location;

    @Override
    public String toString() {
        return "ID = " + id + ", Name = " + name + ", Location = " + location;
    }

    public Theater() {
    }

    public Theater(int id, String name, String password, String location) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getLocation() {
        return location;
    }

    public Scanner getSc() {
        return sc;
    }

    Scanner sc = new Scanner(System.in);

    public void theaterLogin(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Theater ID : ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter the password : ");
        String pass = sc.nextLine();
        String query = "SELECT * FROM theater WHERE theater_id = ? AND password = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, id);
        pstmt.setString(2, pass);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("Admin Login Successfull");
            Theater theater = new Theater(id, rs.getString("name"), pass, rs.getString("location"));
            theaterOptions(con, theater); // to display menu and perform operations
        } else {
            System.out.println("Invalid Theater Id And Password");
        }
    }

    void theaterOptions(Connection con, Theater theater) throws Exception {
        boolean notExit = true;
        while (notExit) {
            System.out.println();
            System.out.println(
                    "What do you want to do?\n1.Add Movie\n2.Delete Movie\n3.Update Movie\n4.Display Movie\n5.Exit");
            int opt = sc.nextInt();
            // sc.nextLine();
            switch (opt) {
                case 1:
                    theater.addMovie(con, theater.getId());
                    break;
                case 2:
                    theater.deleteMovie(con, theater.getId());
                    break;
                case 3:
                    theater.updateMovie(con, theater.getId());
                    break;
                case 4:
                    theater.displayMovie(con, theater.getId());
                    break;
                case 5:
                    System.out.println("Exiting...");
                    System.out.println();
                    notExit = false;

                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }
        }
    }

    void addMovie(Connection con, int theater_id) throws Exception {
        int id;
        while (true) {
            System.out.println("Enter movie id: ");
            id = sc.nextInt();
            sc.nextLine();
            String checkQuery = "SELECT COUNT(*) FROM movie WHERE movie_id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            checkPst.setInt(1, id);
            ResultSet rs = checkPst.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                break;
            }
            System.out.println("Movie ID already exists. Please enter a different Movie ID.");
        }
        String movie;
        while (true) {
            System.out.println("Enter the name of the movie:");
            movie = sc.nextLine().toLowerCase().trim();

            String movieCheckQuery = "SELECT COUNT(*) FROM movie WHERE LOWER(movie_name) = ?";
            PreparedStatement movieCheckPst = con.prepareStatement(movieCheckQuery);
            movieCheckPst.setString(1, movie);
            ResultSet movieRs = movieCheckPst.executeQuery();

            if (movieRs.next() && movieRs.getInt(1) == 0) {
                break;
            }
            System.out.println("Movie " + movie + " already exists ");
        }

        // Proceed with entering the genre, price, rating, and release date as usual
        System.out.println("Enter the genre of the movie: ");
        String genre = sc.nextLine();
        System.out.println("Enter price: ");
        double price = sc.nextDouble();
        System.out.println("Enter rating out of 10: ");
        double rating = sc.nextDouble();
        sc.nextLine();
        String release_date = "";
        while (true) {
            System.out.println("Enter release Date (YYYY-MM-DD) : ");
            release_date = sc.nextLine();
            if (isValidDate(release_date)) {
                break;
            } else {
                System.out.println("Invalid Syntax Enter again");
            }

        }

        String query = "INSERT INTO Movie VALUES (?,?,?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setInt(1, id);
        pst.setString(2, movie);
        pst.setString(3, genre);
        pst.setString(4, release_date);
        pst.setDouble(5, price);
        pst.setDouble(6, rating);
        pst.setInt(7, theater_id);

        int r = pst.executeUpdate();
        System.out.println((r > 0) ? "Successfully added movie " : "Try again");

        String ans;
        do {
            // Here Do while Used for means the theater needs to add atleast one showTime
            // and showdate for movie
            System.out.println("Enter show time detail: ");
            int showId;

            while (true) {
                System.out.println("Enter show id: ");
                showId = sc.nextInt();
                sc.nextLine();
                String checkQuery = "SELECT COUNT(*) FROM showTime WHERE id = ?";
                PreparedStatement checkPst = con.prepareStatement(checkQuery);
                checkPst.setInt(1, showId);
                ResultSet rs = checkPst.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    break;
                }
                System.out.println("Show ID already exists. Please enter a different Show ID.");
            }
            String date = "";

            // Show date must be greater then curr date and also greater then release date
            // too
            while (true) {
                System.out.println("Enter show date (YYYY-MM-DD): ");
                date = sc.nextLine();
                if (isValidDate(date)) {
                    String countQuery = "SELECT COUNT(*) FROM movie WHERE movie_id = ? AND ? >= CURRENT_DATE && ?>=release_date ";
                    PreparedStatement checkDate = con.prepareStatement(countQuery);
                    checkDate.setInt(1, id);
                    checkDate.setString(2, date);
                    checkDate.setString(3, date);

                    ResultSet checkDateResultSet = checkDate.executeQuery();
                    if (checkDateResultSet.next() && checkDateResultSet.getInt(1) > 0) {
                        System.out.println("Show date is valid.");
                        break;
                    } else {
                        System.out.println(
                                "The show date must be greater than or equal to the current Date And Show Date. Please enter again.");
                    }
                }
            }
            String time = "";
            while (true) {
                System.out.println("Enter Time (HH:MM:SS): ");
                time = sc.nextLine();
                if (isValidTime(time)) {
                    break;
                } else {
                    System.out.println("Invalid Syntax Enter again");
                }
            }

            int seats;
            while (true) {
                System.out.println("Enter total seats: ");
                seats = sc.nextInt();
                sc.nextLine();
                if (seats < 1) {
                    System.out.println("Seats can`t be " + seats);
                } else {
                    break;
                }

            }
            String query1 = "INSERT INTO Showtime VALUES (?,?,?,?,?,?,?)";
            PreparedStatement pstm = con.prepareStatement(query1);
            pstm.setInt(1, showId);
            pstm.setInt(2, id);
            pstm.setInt(3, theater_id);
            pstm.setString(4, date);
            pstm.setString(5, time);
            pstm.setInt(6, seats);
            pstm.setInt(7, seats);

            int r1 = pstm.executeUpdate();
            System.out.println((r1 > 0) ? "Successfully added showtime " : "Try again");

            System.out.println("Do you want to continue adding showtime for this movie (y/n): ");
            ans = sc.next();
        } while (ans.equalsIgnoreCase("y"));
        sc.nextLine();
    }

    public static boolean isValidTime(String time) {

        if (time.length() != 8) {
            System.out.println("Invalid time format: Incorrect length.");
            return false;
        }

        if (time.charAt(2) != ':' || time.charAt(5) != ':') {
            System.out.println("Invalid time format: Colons missing or misplaced.");
            return false;
        }

        int hours = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(3, 5));
        int seconds = Integer.parseInt(time.substring(6, 8));

        // Validate hours
        if (hours < 0 || hours > 23) {
            System.out.println("Invalid time: Hours must be between 00 and 23.");
            return false;
        }

        // Validate minutes
        if (minutes < 0 || minutes > 59) {
            System.out.println("Invalid time: Minutes must be between 00 and 59.");
            return false;
        }

        // Validate seconds
        if (seconds < 0 || seconds > 59) {
            System.out.println("Invalid time: Seconds must be between 00 and 59.");
            return false;
        }

        return true;

    }

    public static boolean isValidDate(String date) {
        if (date.length() != 10) {
            System.out.println("Invalid date format: Incorrect length.");
            return false;

        }
        if (date.charAt(4) != '-' || date.charAt(7) != '-') {
            System.out.println("Invalid date format: Hyphens missing or misplaced.");
            return false;
        }

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));

        // Validate month
        if (month < 1 || month > 12) {
            System.out.println("Invalid date: Month must be between 01 and 12.");
            return false;
        }

        // Validate day based on the month
        if (day < 1 || day > 31) {
            System.out.println("Invalid date: Day must be between 01 and 31.");
            return false;
        }

        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
            System.out.println("Invalid date: The month you entered has only 30 days.");
            return false;
        }

        if (month == 2) {
            // For leap Year
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (isLeapYear) {
                if (day > 29) {
                    System.out.println("Invalid date: February in a leap year can only have up to 29 days.");
                    return false;
                }
            } else {
                if (day > 28) {
                    System.out.println("Invalid date: February can only have up to 28 days.");
                    return false;
                }
            }
        }

        return true;
    }

    void displayMovie(Connection con, int theater_id) throws Exception {
        // THis is for Theater only (This method gets invoke when theater wants to see
        // movie)
        String query = "SELECT * FROM Movie WHERE theater_id = ?";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setInt(1, theater_id);
        ResultSet rst = pst.executeQuery();
        if (!rst.next()) {
            System.out.println("There are no added movies currently!");
        } else {
            System.out.println();
            System.out.println("Displaying all the movies: ");
            System.out.println();
            System.out.println("-------------------------------------");

            do {
                System.out.println("Movie id: " + rst.getInt(1));
                System.out.println("Movie name: " + rst.getString(2));
                System.out.println("Genre: " + rst.getString(3));
                System.out.println("Price: " + rst.getDouble(5));
                System.out.println("Rating: " + rst.getDouble(6) + "/10");
                System.out.println("Release Date : " + rst.getString("release_date"));
                System.out.println("-------------------------------------");
                System.out.println();
            } while (rst.next());
        }
    }

    void deleteMovie(Connection con, int theater_id) throws Exception {
        con.setAutoCommit(false);
        System.out.println("Enter the name of the movie you want to delete: ");
        String name = sc.nextLine();
        String checkQuery = "SELECT COUNT(*) FROM Movie WHERE movie_name = ? AND theater_id = ?";
        PreparedStatement checkStmt = con.prepareStatement(checkQuery);
        checkStmt.setString(1, name);
        checkStmt.setInt(2, theater_id);

        ResultSet rs = checkStmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            // Checking if user already have a ticket booked on that movie
            String checkUserConnetedQuery = "SELECT COUNT(*) " +
                    "FROM bookMovie bm JOIN movie m ON bm.movie_id = m.movie_id WHERE m.movie_name = ?;";
            PreparedStatement checkUser = con.prepareStatement(checkUserConnetedQuery);
            checkUser.setString(1, name);
            ResultSet rst = checkUser.executeQuery();

            if (rst.next() && rst.getInt(1) > 0) {
                System.out.println("Users Already Booked ticket for this Movie Can`t delete");

            } else {
                String deleteQuery = "DELETE FROM Movie WHERE movie_name = ? AND theater_id = ?";
                PreparedStatement deleteStmt = con.prepareStatement(deleteQuery);
                deleteStmt.setString(1, name);
                deleteStmt.setInt(2, theater_id);

                System.out.println("Do you want to delete the movie " + name + " (y/n):");
                String ans = sc.next();

                if (ans.equalsIgnoreCase("y")) {
                    deleteStmt.executeUpdate();
                    con.commit();
                    System.out.println("Deleted Movie : " + name);
                } else {
                    con.rollback();
                    System.out.println("Deletion cancelled.");
                }
                deleteStmt.close();
            }
        } else {
            System.out.println("Movie not found.");
            con.rollback();
        }
    }

    void updateMovie(Connection con, int theater_id) throws Exception {
        System.out.println("Enter the Movie ID whose detail you want to update: ");
        int id = sc.nextInt();
        sc.nextLine();
        String checkQuery = "SELECT COUNT(*) FROM Movie WHERE movie_id = ?";

        PreparedStatement checkQueryPs = con.prepareStatement(checkQuery);
        checkQueryPs.setInt(1, id);
        ResultSet checkQueryRs = checkQueryPs.executeQuery();

        if (checkQueryRs.next()) {
            int count = checkQueryRs.getInt(1);
            if (count <= 0) {
                System.out.println("Movie Id not Exist ");
                return;
            }
        }
        System.out.println("Enter updated price: ");
        double price = sc.nextDouble();
        System.out.println("Enter updated rating out of 10: ");
        double rating = sc.nextDouble();
        sc.nextLine();
        System.out.println("Enter updated genre of the movie: ");
        String genre = sc.nextLine();

        String query = "UPDATE Movie SET genre=?, cost =?, rating =? WHERE movie_id = ?";
        PreparedStatement pst = con.prepareStatement(query);

        pst.setString(1, genre);
        pst.setDouble(2, price);
        pst.setDouble(3, rating);
        pst.setInt(4, id);
        int r = pst.executeUpdate();
        System.out.println((r > 0) ? "Successfully updated movie " : "Something Went Wrong");

    }
}
