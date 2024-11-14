
import java.util.*;

import java.sql.*;
class Admin {
    int id;
    String name;
    String password;
    static Scanner sc = new Scanner(System.in);

    public Admin() {
    }

    public Admin(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    // Admin Login Method(without infinite loop)
    public void adminLogin(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Admin ID : ");
        int id = sc.nextInt();
        sc.nextLine();
        // getPassword method checks if password contains total 8 character and more ,
        // (also 4 must be digits)
        String pass = setPassword();
        String query = "SELECT * FROM Admin WHERE admin_id = ? AND password = ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, id);
        pstmt.setString(2, pass);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            System.out.println("Admin Login Successfull");
            Admin admin = new Admin(id, name, pass);
            adminOptions(con, admin);
        } else {
            System.out.println("Invalid Admin ID or Password");
        }
    }

    String setPassword() {
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
                System.out.println("Password must be 8 Characters Long with 4 Digits");
            }
        }
        return pass;
    }

    void adminOptions(Connection con, Admin admin) throws Exception {

        boolean notExit = true;

        while (notExit) {
            System.out.println();
            System.out.println(
                    "What do you want to do?\n1.Add Theater\n2.Delete Theater\n3.Update Theater\n4.Display All Theaters\n5.Exit ");
            int opt = sc.nextInt();
            switch (opt) {
                case 1:
                    admin.addTheater(con);
                    break;
                case 2:
                    admin.deleteTheater(con);
                    break;
                case 3:
                    admin.updateTheater(con);
                    break;
                case 4:
                    admin.displayAllTheater(con);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    System.out.println();
                    notExit = false;
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    void addTheater(Connection con) throws Exception {
        String checkExistQuery = "SELECT COUNT(*) FROM Theater WHERE theater_id = ?";
        String insertQuery = "INSERT INTO Theater VALUES (?, ?, ?, ?)";
        boolean validId = false;
        while (!validId) {
            System.out.print("Enter Theater ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            PreparedStatement checkStmt = con.prepareStatement(checkExistQuery);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            // rs.next() gonna move the pointer to first Line (which is count if count>0
            // then Id already Exist)
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Theater ID already exists. Please enter a different ID.");
            } else {
                validId = true;
                System.out.print("Enter Theater Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Theater Location: ");
                String location = sc.nextLine();
                String pass = setPassword();

                PreparedStatement insertStmt = con.prepareStatement(insertQuery);
                insertStmt.setInt(1, id);
                insertStmt.setString(2, name);
                insertStmt.setString(3, pass);
                insertStmt.setString(4, location);

                int r = insertStmt.executeUpdate();
                System.out.println((r > 0) ? "Theater added successfully" : "Try again");

            }

        }
    }

    void updateTheater(Connection con) throws Exception {
        int theaterId;
        while (true) {
            System.out.println("Enter Theater id: ");
            theaterId = sc.nextInt();
            sc.nextLine();
            String checkQuery = "SELECT COUNT(*) as count FROM Theater WHERE theater_id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            checkPst.setInt(1, theaterId);
            ResultSet rs = checkPst.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                break;
            }
            System.out.println("Theater Id not exist,Please enter a valid theater id");
        }
        String pass = updatePassword();
        System.out.print("Enter new name: ");
        String name = sc.nextLine();
        System.out.print("Enter new location: ");
        String location = sc.nextLine();
        String query = "UPDATE Theater SET name= ? ,password = ? ,location = ? WHERE theater_id = ?";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, name);
        pst.setString(2, pass);
        pst.setString(3, location);
        pst.setInt(4, theaterId);
        int r = pst.executeUpdate();
        System.out.println((r > 0) ? "Successfully updated" : "Id not found ");
    }

    String updatePassword() {
        boolean flag = true;
        String pass = "";
        while (flag) {
            System.out.print("Enter new Password : ");
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
                System.out.println("Password must be 8 Characters Long with 4 Digits");
            }
        }
        return pass;
    }

    void deleteTheater(Connection con) throws Exception {
        con.setAutoCommit(false);
        System.out.print("Enter the Theater ID of the theater you want to delete: ");
        int id = sc.nextInt();

        String checkQuery = "SELECT COUNT(*) FROM bookMovie WHERE theater_id = ?";
        PreparedStatement checkPst = con.prepareStatement(checkQuery);
        checkPst.setInt(1, id);
        ResultSet checkResultSet = checkPst.executeQuery();

        if (checkResultSet.next() && checkResultSet.getInt(1) > 0) {
            System.out.println("Theater cannot be deleted as it has associated bookings.");
        } else {
            String deleteQuery = "DELETE FROM Theater WHERE theater_id = ?";
            PreparedStatement deletePst = con.prepareStatement(deleteQuery);
            deletePst.setInt(1, id);
            int rowAffected = deletePst.executeUpdate();
            if (rowAffected > 0) {
                System.out.println("Do you want to delete the theater with ID " + id + " (y/n):");
                String ans = sc.next();
                if (ans.equalsIgnoreCase("y")) {
                    con.commit();
                    System.out.println("Deleted Theater with ID: " + id);
                } else if (ans.equalsIgnoreCase("n")) {

                    con.rollback();
                } else {
                    System.out.println("Invalid Input ,Deletion Unsuccessfull");
                    con.rollback();
                }
            } else {
                System.out.println("Theater ID not found.");
                con.rollback();
            }
        }
    }

    public void displayAllTheater(Connection con) throws Exception {
        LList theaters = new LList();
        String query = "SELECT * FROM Theater";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println();
        System.out.println("Displaying all theaters:");
        boolean flag = true;
        while (rs.next()) {
            flag = false;
            int theaterId = rs.getInt("theater_id");
            String name = rs.getString("name");
            String location = rs.getString("location");
            String password = rs.getString("password");
            Theater th = new Theater(theaterId, name, password, location);
            theaters.insert(th);
        }
        if (!flag) {
            System.out.println("----------------------------------------------------");
            Node currentTheater = theaters.head;
            while (currentTheater != null) {
                System.out.println(currentTheater.Th.toString());
                currentTheater = currentTheater.next;
                System.out.println("----------------------------------------------------");
            }
        } else {
            System.out.println("No Theaters ");
        }

    }

}