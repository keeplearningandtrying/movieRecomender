package ramo.klevis.ui;

import ramo.klevis.ml.CollaborationFiltering;
import ramo.klevis.ml.Movie;
import ramo.klevis.ml.PrepareData;
import ramo.klevis.ui.comp.StarRaterEditor;
import ramo.klevis.ui.comp.StarRaterRenderer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableColumn;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by klevis.ramo on 10/29/2017.
 */
public class UI {
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 400;

    private JFrame mainFrame;
    private JPanel mainPanel;
    private JProgressBar progressBar;
    private PrepareData prepareData;
    private RatingsTableModel ratingsTableModel;
    private JTable table;
    private final CollaborationFiltering collaborationFiltering;
    private SuggestionTableModel suggestionTableModel;

    public UI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("Button.font", new FontUIResource(new Font("Dialog", Font.BOLD, 16)));
        UIManager.put("ProgressBar.font", new FontUIResource(new Font("Dialog", Font.BOLD, 16)));
        prepareData = new PrepareData();
        initUI();
        collaborationFiltering = new CollaborationFiltering();
    }

    private void initUI() throws IOException {
        mainFrame = createMainFrame();
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        createProgressBar(mainFrame);
        addTopPanel();
        addMovieTables();
        addSignature();

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private void addMovieTables() {
        ratingsTableModel = new RatingsTableModel();
        table = new JTable(ratingsTableModel);
        GridLayout gridLayout = new GridLayout(1, 2);
        JPanel tablePanel = new JPanel(gridLayout);
        tablePanel.add(new JScrollPane(table));

        suggestionTableModel = new SuggestionTableModel();
        tablePanel.add(new JScrollPane(new JTable(suggestionTableModel)));
        mainPanel.add(tablePanel, BorderLayout.CENTER);
    }

    private void addTopPanel() throws IOException {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Genres"));
        JComboBox<String> jComboBox = new JComboBox<>();
        Collection<String> allGenres = prepareData.getAllGenres();
        allGenres.stream().forEach(e -> jComboBox.addItem(e));
        jComboBox.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == 1) {
                loadMovieByGenre(e);
            }
        });
        topPanel.add(jComboBox);
        JButton reset = new JButton("Reset Ratings");
        reset.addActionListener(e -> {
            resetMoviesRate();
        });
        topPanel.add(reset);

        JButton train = new JButton("Suggest Movies");
        train.addActionListener(e -> {
            try {
                List<Movie> topTenRated = collaborationFiltering.train(prepareData.getMovies());
                suggestionTableModel.restAndAddNewMovies(topTenRated);
                suggestionTableModel.fireTableDataChanged();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        topPanel.add(train);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void resetMoviesRate() {
        try {
            prepareData = new PrepareData();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        ratingsTableModel.restAndAddNewMovies(new ArrayList<>());
        ratingsTableModel.fireTableDataChanged();
    }

    private void loadMovieByGenre(ItemEvent e) {
        String genre = (String) e.getItem();
        List<Movie> moviesList = prepareData.getMoviesByGenre(genre);
        ratingsTableModel.restAndAddNewMovies(moviesList);
        ratingsTableModel.fireTableDataChanged();
        TableColumn col = table.getColumnModel().getColumn(1);
        col.setCellEditor(new StarRaterEditor(ratingsTableModel));
        col.setCellRenderer(new StarRaterRenderer(ratingsTableModel));
    }

    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Email Spam Detector");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        ImageIcon imageIcon = new ImageIcon("spam.png");
        mainFrame.setIconImage(imageIcon.getImage());

        return mainFrame;
    }


    private void showProgressBar() {
        SwingUtilities.invokeLater(() -> {
            progressBar = createProgressBar(mainFrame);
            progressBar.setString("Training Algorithm!Please wait it may take one or two minutes");
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            mainPanel.add(progressBar, BorderLayout.NORTH);
            mainFrame.repaint();
        });
    }

    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        jProgressBar.setVisible(false);
        mainFrame.add(jProgressBar, BorderLayout.NORTH);
        return jProgressBar;
    }

    private void addSignature() {
        JLabel signature = new JLabel("ramok.tech", JLabel.HORIZONTAL);
        signature.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 20));
        signature.setForeground(Color.BLUE);
        mainPanel.add(signature, BorderLayout.SOUTH);
    }
}
