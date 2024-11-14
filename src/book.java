import java.sql.*;
import java.util.*;





public class book {
    public static void main(String[] args) throws Exception {
        String dburl = "jdbc:mysql://localhost:3306/MovieTicketBooking";
        String dbuser = "root";
        String dbpass = "";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        Connection con = DriverManager.getConnection(dburl, dbuser, dbpass);
        if (con != null) {
            System.out.println("Successful !");
        } else {
            System.out.println("Failed !");
        }
        Scanner sc = new Scanner(System.in);

        Admin a = new Admin();
        User u = new User();
        Theater t = new Theater();

        boolean flag = true;
        while (flag) {
            System.out.println();
            System.out.println(
                    "Welcome to Movie Ticket Management System\nChoose From the Following \n1. Admin \n2. Theater \n3. User \n4. Exit");
            int opt = sc.nextInt();
            switch (opt) {
                case 1:
                    a.adminLogin(con, sc);
                    break;
                case 2:
                    t.theaterLogin(con, sc);
                    break;
                case 3:
                    handleUser(con, sc, u);
                    break;
                case 4:
                    flag = false;
                    System.out.println("Exiting Movie Ticket Management System");
                    sc.close();
                    con.close();
                    break;
                default:
                    System.out.println("Enter Valid Choice.");
                    break;
            }
        }
    }

    public static void handleUser(Connection con, Scanner sc, User user) throws Exception {
        boolean flag = true;
        while (flag) {
            System.out.println();
            System.out.println("Enter \n1.SignUp \n2.LogIn\n3.Exit");
            int opt = sc.nextInt();
            switch (opt) {
                case 1:
                    user.addUser(con, sc);
                    break;
                case 2:
                    user.userLogin(con, sc);
                    break;
                case 3:
                    flag = false;
                    System.out.println("Exiting");
                    break;
                default:
                    System.out.println("Enter Valid Choice.");
                    break;
            }
        }
    }
}
