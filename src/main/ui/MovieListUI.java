package ui;

import exception.DuplicateException;
import exception.NoMoviesFoundException;
import model.Event;
import model.EventLog;
import model.Movie;
import model.MovieList;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

//Referenced: https://github.students.cs.ubc.ca/CPSC210/AlarmSystem.git
//Represents MovieList GUI Frame
public class MovieListUI extends JFrame implements LogPrinter {
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;
    public static final int BORDER_GAP = 13;
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    public static final Color BORDER_COLOR = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.lightGray;

    private String listTitle = "Movies";
    private MovieList ml;
    private Movie movie;
    private List<Movie> listOfMovie;

    private JsonReader jsonReader;
    private JsonWriter jsonWriter;
    public static final String JSON_DESTINATION = "./data/movieList.json";

    private JList<String> javaList;
    private final DefaultListModel<String> listModel;
    private JPanel listArea;


    //EFFECTS: Constructs and displays GUI of MovieList Application
    public MovieListUI() throws FileNotFoundException {
        super("Movie List");
        ml = new MovieList();
        jsonReader = new JsonReader(JSON_DESTINATION);
        jsonWriter = new JsonWriter(JSON_DESTINATION);
        listModel = new DefaultListModel<>();
        javaList = new JList<>(listModel);
        javaList.setBackground(BACKGROUND_COLOR);
        javaList.setForeground(TEXT_COLOR);
        openCloseOperations();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        getContentPane().setBackground(BACKGROUND_COLOR);

        ((JPanel) getContentPane()).setBorder(new EmptyBorder(BORDER_GAP, BORDER_GAP, BORDER_GAP, BORDER_GAP));
        setLayout(new BorderLayout());

        addAllButtons();
        addListArea();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
        defaultListModel();
    }

    //EFFECTS: sets up how to handle user starting/exiting the application
    public void openCloseOperations() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                saveBeforeClosing();
            }
        });
        loadAfterOpening();
    }

    //MODIFIES: this
    //EFFECTS: Creates area to display Movie List
    public void addListArea() {
        listArea = new JPanel();
        listArea.setLayout(new GridLayout(0, 1));
        listArea.setSize(new Dimension(0, 0));
        listArea.setPreferredSize(new Dimension(WIDTH / 2, HEIGHT - (2 * BORDER_GAP)));
        add(listArea, BorderLayout.WEST);
        listArea.add(javaList);
        listArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), listTitle));
    }

    //MODIFIES: this
    //EFFECTS: Creates buttons and area to display buttons
    public void addAllButtons() {
        JPanel buttonArea = new JPanel();
        buttonArea.setLayout(new GridLayout(0, 1));
        buttonArea.setSize(new Dimension(0, 0));
        buttonArea.setPreferredSize(new Dimension(WIDTH / 3, HEIGHT - (2 * BORDER_GAP)));
        buttonArea.setBackground(BACKGROUND_COLOR);
        add(buttonArea, BorderLayout.EAST);

        buttonArea.add(new JButton(new AllMovieAction()));
        buttonArea.add(new JButton(new AddMovieAction()));
        buttonArea.add(new JButton(new RateMovieAction()));
        buttonArea.add(new JButton(new SearchAction()));
        buttonArea.add(new JButton(new CategoryAction()));
        buttonArea.add(new JButton(new RatingAction()));
        buttonArea.add(new JButton(new UnwatchedAction()));
    }

    //MODIFIES: this
    //EFFECTS: displaying all Movies upon Action event
    private class AllMovieAction extends AbstractAction {

        AllMovieAction() {
            super("All Movies");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            defaultListModel();
        }
    }

    //MODIFIES: this
    //EFFECTS: creates a Movie and add it to MovieList upon Action event
    private class AddMovieAction extends AbstractAction {

        AddMovieAction() {
            super("Add Movie");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String inputTitle = JOptionPane.showInputDialog("Title:");
            String inputCategory = JOptionPane.showInputDialog("Category:");
            movie = new Movie(inputTitle, inputCategory);
            try {
                ml.addMovieToList(movie);
                defaultListModel();
            } catch (DuplicateException e) {
                errorPane(e.getMessage());
            }
        }
    }

    //MODIFIES: this
    //EFFECTS: search and rate a Movie upon Action event
    private class RateMovieAction extends AbstractAction {

        RateMovieAction() {
            super("Rate Movie");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String inputTitle = JOptionPane.showInputDialog("Enter movie title to rate:");
            try {
                movie = ml.findMovie(inputTitle);
            } catch (NoMoviesFoundException e) {
                errorPane(e.getMessage());
            }
            String r = JOptionPane.showInputDialog("Enter rating (integer):");
            movie.setRating(Integer.parseInt(r));
            defaultListModel();
        }
    }


    //MODIFIES: this
    //EFFECTS: search and displays Movie upon Action event
    private class SearchAction extends AbstractAction {

        SearchAction() {
            super("Search");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String inputTitle = JOptionPane.showInputDialog("Enter movie title:");
            try {
                movie = ml.findMovie(inputTitle);
            } catch (NoMoviesFoundException e) {
                errorPane(e.getMessage());
            }
            listTitle = "Result for '" + inputTitle + "'";
            listOfMovie.clear();
            listOfMovie.add(movie);
            updateListModel();
        }
    }

    //MODIFIES: this
    //EFFECTS: displays filtered MovieList by category upon Action event
    private class CategoryAction extends AbstractAction {

        CategoryAction() {
            super("Filter by Category");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String inputCategory = JOptionPane.showInputDialog("Enter category:");
            try {
                listOfMovie = ml.filterCategory(inputCategory);
            } catch (NoMoviesFoundException e) {
                errorPane(e.getMessage());
            }
            listTitle = inputCategory + " Movies";
            updateListModel();
        }
    }

    //MODIFIES: this
    //EFFECTS: displays filtered MovieList by rating upon Action event
    private class RatingAction extends AbstractAction {

        RatingAction() {
            super("Filter by Rating");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String inputMinRating = JOptionPane.showInputDialog("Enter minimum rating:");
            int r = Integer.parseInt(inputMinRating);
            try {
                listOfMovie = ml.filterRating(r);
            } catch (NoMoviesFoundException e) {
                errorPane(e.getMessage());
            }
            listTitle = "Movies rated at least " + inputMinRating;
            updateListModel();
        }
    }

    //MODIFIES: this
    //EFFECTS: displays unwatched MovieList upon Action event
    private class UnwatchedAction extends AbstractAction {

        UnwatchedAction() {
            super("Unwatched Movies");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                listOfMovie = ml.getListOfUnwatched();
            } catch (NoMoviesFoundException e) {
                errorPane(e.getMessage());
            }
            listTitle = "Unwatched/unrated Movies";
            updateListModel();
        }
    }

    //MODIFIES: this
    //EFFECTS: saves MovieList
    public void save() {
        try {
            jsonWriter.open();
            jsonWriter.write(ml);
            jsonWriter.close();
            JOptionPane.showMessageDialog(null, "Your Movie List has been saved",
                    null, JOptionPane.INFORMATION_MESSAGE, directorIcon());
        } catch (FileNotFoundException e) {
            errorPane("Unable to save file");
        }
    }

    //MODIFIES: this
    //EFFECTS: loads MovieList from file
    public void load() {
        try {
            ml = jsonReader.read();
            JOptionPane.showMessageDialog(null, "Loaded your Movie List from "
                    + JSON_DESTINATION, null, JOptionPane.INFORMATION_MESSAGE, directorIcon());
        } catch (IOException e) {
            errorPane("Unable to read file");
        }
    }

    //MODIFIES: this
    //EFFECTS: prompts user to save before exiting
    public void saveBeforeClosing() {
        int answer = JOptionPane.showConfirmDialog(null, "Would you like to save your Movie List?",
                null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, directorIcon());
        if (answer == JOptionPane.YES_OPTION) {
            save();
        }
        printLog(EventLog.getInstance());
        System.exit(0);
    }

    //EFFECTS: asks User if they want to load a Movie list when they open the application
    public void loadAfterOpening() {
        int answer = JOptionPane.showConfirmDialog(null, "Would you like to load a saved Movie List?",
                null, JOptionPane.YES_NO_CANCEL_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            load();
        }
    }

    //EFFECTS: creates an ImageIcon from directorIcon.gif
    public ImageIcon directorIcon() {
        ImageIcon directorIcon = new ImageIcon(new ImageIcon(getClass().getResource("directorIcon.gif")).getImage()
                .getScaledInstance(70, 70, Image.SCALE_DEFAULT));
        return directorIcon;
    }

    //EFFECTS: displays the given error message in an JOptionPane
    public void errorPane(String errorMsg) {
        JOptionPane.showMessageDialog(null, errorMsg, null,
                JOptionPane.ERROR_MESSAGE);
    }


    //MODIFIES: this
    //EFFECTS: adds filtered movie list to listModel
    public void defaultListModel() {
        listOfMovie = ml.getListOfMovie();
        listTitle = "Movies";
        listModel.clear();
        updateListModel();
    }

    //MODIFIES: this
    //EFFECTS: clears listModel and adds each element of list to listModel
    public void updateListModel() {
        listModel.clear();
        for (Movie m : listOfMovie) {
            listModel.addElement(m.movieToString());
        }
        listArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), listTitle));
    }


    @Override
    //EFFECTS:prints out Event log ot console
    public void printLog(EventLog eventLog) {
        for (Event event : eventLog) {
            System.out.println(event + "\n");
        }
    }


    //EFFECTS: Creates a new instance of the GUI
    public static void main(String[] args) {
        try {
            new MovieListUI();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Could not find file", null,
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
