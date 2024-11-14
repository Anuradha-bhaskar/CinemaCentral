import java.util.*;
import java.sql.*;
class Movie {
    static Scanner sc = new Scanner(System.in);
    int movie_id;
    String movie_name;
    String genre;
    double price;
    String date;
    double rating;
    String description;

    public Movie() {
        // default constructor
    }

    private Movie(int movie_id, String movie_name, String genre, double price, String date, double rating) {
        this.movie_id = movie_id;
        this.movie_name = movie_name;
        this.genre = genre;
        this.price = price;
        this.date = date;
        this.rating = rating;
    }

    public void setId(int movie_id) {
        this.movie_id = movie_id;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public void setName(String movie_name) {
        this.movie_name = movie_name;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getId() {
        return movie_id;
    }

    public String getName() {
        return movie_name;
    }

    public String getGenre() {
        return genre;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public void displayAllMovies(Connection con) throws Exception {
        List<Movie> movies = new ArrayList<>();
        // This is invoked by user to see Movies according to ratings

        String query = "SELECT movie_id, movie_name, genre, rating, release_date, cost FROM Movie where release_date>=CURRENT_DATE";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            Movie movie = new Movie(movie_id, movie_name, genre, price, date, rating);
            movie.setId(rs.getInt("movie_id"));
            movie.setName(rs.getString("movie_name"));
            movie.setGenre(rs.getString("genre"));
            movie.setRating(rs.getDouble("rating"));
            movie.setDate(rs.getString("release_date"));
            movie.setPrice(rs.getDouble("cost"));
            movies.add(movie);
        }

        // Sort movies by rating in descending order
        movies.sort(Comparator.comparingDouble(Movie::getRating).reversed());

        // Display sorted movies
        for (Movie movie : movies) {
            System.out.println();
            System.out.println("---------------------------------------");
            System.out.println("ID: " + movie.getId());
            System.out.println("Title: " + movie.getName());
            System.out.println("Genre: " + movie.getGenre());
            System.out.println("Rating: " + movie.getRating());
            System.out.println("Release Date : " + movie.getDate());
            System.out.println("Price: " + movie.getPrice());
            System.out.println("---------------------------------------");
        }

    }

}
