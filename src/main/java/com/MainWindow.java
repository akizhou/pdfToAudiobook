package com;

import com.cloudAPI.GoogleCloudAPI;
import com.enums.Gender;
import com.enums.Language;
import com.enums.PageReturns;
import com.exceptions.*;
import com.google.cloud.storage.StorageException;
import com.pdfUtil.PDFBoxConverter;
import com.pdfUtil.PDFConverter;
import com.queue.FileQueue;
import com.queue.IFileData;
import com.queue.PDFQueue;
import com.queue.QueueCellRenderer;

import javax.naming.TimeLimitExceededException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;


public class MainWindow extends JFrame
        implements ActionListener {

    private JPanel mainPanel;
    private JPanel settingsPanel;
    private JPanel queuePanel;
    private JPanel selectPanel;
    private JPanel statusBarPanel;

    private JLabel statusLabel;

    private JList pdfList;
    private JButton selectFileButton;
    private JButton downloadLocationButton;
    private JButton convertButton;
    private JButton removeButton;

    private JTextField startPageField;
    private JTextField endPageField;
    private JComboBox langComboBox;
    private JRadioButton maleRButton;
    private JRadioButton femaleRButton;
    private JRadioButton neutralRButton;
    private JSlider speedSlider;
    private JSlider pitchSlider;

    private String fileNamePath;
    private FileQueue pdfQueue;

    private DefaultListModel queueModel;
    private QueueCellRenderer queueCellRenderer;
    private PDFConverter pdfConverter;
    private GoogleCloudAPI gcsAPI;
    private String downloadLocation;

    private GridBagConstraints cons;


    public MainWindow() //pass in a reference to board
    {
        Color grayColor = new Color(218,218,218);
        setTitle("PDF To Audiobook");
        setBounds(200, 90, 620, 350);
        setMinimumSize(new Dimension(620,350));
        getContentPane().setBackground(grayColor);
        setLocationRelativeTo(null);
        setResizable(true);
        //setDefaultCloseOperation(EXIT_ON_CLOSE);

        JFrame frame = this;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                int result = JOptionPane.showConfirmDialog(frame,
                        "Once you terminate the program any unfinished conversion progress will be lost.\n" +
                                "Do you want to Exit ?", "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                else if (result == JOptionPane.NO_OPTION)
                    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        });

        queueModel = new DefaultListModel(); //we need an instance of the model, because otherwise its not mutable if you put it in the JList
        pdfQueue = new PDFQueue();
        queueCellRenderer = new QueueCellRenderer();
        pdfConverter = new PDFBoxConverter();

        mainPanel = new JPanel(new GridBagLayout());
        queuePanel = new JPanel(new GridBagLayout());
        settingsPanel = new JPanel(new GridBagLayout());
        selectPanel = new JPanel(new GridBagLayout());
        statusBarPanel = new JPanel(new GridBagLayout());
        getContentPane().add(mainPanel, cons);

        initUI();
        this.setVisible(true);

        try {
            String keyPath = System.getProperty("user.dir")+ File.separator + "resources" + File.separator + "key.json";
            gcsAPI = new GoogleCloudAPI(keyPath, "YOUR_UPLOAD_BUCKET_NAME", "YOUR_DOWNLOAD_BUCKET_NAME");
        }
        catch (CredentialsKeyError | MACAddressError err) {
            JOptionPane.showMessageDialog(null, err.getMessage(), "FATAL ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Clean up any residual files on gcs from past sessions
        gcsAPI.cleanup(gcsAPI.getRawFileBucketID());
        gcsAPI.cleanup(gcsAPI.getOutFileBucketID());
        downloadLocation = "";
    }

    public void initUI()
    {
        // Adding the queue panel which will contain JList and move/remove buttons
        populateQueuePanel();
        addComponentGridBagLayout(0,0,3,3,1,1,GridBagConstraints.BOTH);
        mainPanel.add(queuePanel, cons);

        // Adding the panel which holds all the settings
        populateSettingsPanel();
        Insets insets = new Insets(0, 5, 0, 5);
        addComponentGridBagLayout(3,0,2,2,0.25,1,GridBagConstraints.BOTH, insets);
        mainPanel.add(settingsPanel, cons);

        // Adding panel which holds select file and calculate button
        populateSelectPanel();
        addComponentGridBagLayout(3,2,2,1,0.25,0.25,GridBagConstraints.BOTH, insets);
        mainPanel.add(selectPanel, cons);

        statusLabel = new JLabel();
        addComponentGridBagLayout(0,0,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL);
        statusBarPanel.add(statusLabel,cons);
        addComponentGridBagLayout(0,3,GridBagConstraints.REMAINDER,1,0.1,0.1,GridBagConstraints.HORIZONTAL);
        mainPanel.add(statusBarPanel, cons);
    }

    private void populateSettingsPanel() {
        JLabel settingsLabel = new JLabel("Settings");
        settingsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        addComponentGridBagLayout(0,0,GridBagConstraints.REMAINDER,1,0,1,GridBagConstraints.HORIZONTAL);
        settingsLabel.setHorizontalAlignment(JLabel.CENTER);
        settingsPanel.add(settingsLabel, cons);

        JLabel startPageLabel = new JLabel("Start Page:");
        startPageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(0,1,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(startPageLabel, cons);

        startPageField = new JTextField();
        startPageField.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(1,1,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(startPageField, cons);

        JLabel endPageLabel = new JLabel("End Page:");
        endPageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(2,1,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(endPageLabel, cons);

        endPageField = new JTextField();
        endPageField.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(3,1,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(endPageField, cons);

        // Audio conversion settings
        JLabel langLabel = new JLabel("Language:");
        langLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(0,2,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(langLabel, cons);

        langComboBox = new JComboBox(Language.langNames());
        langComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        addComponentGridBagLayout(1,2,GridBagConstraints.REMAINDER,1,0,1, GridBagConstraints.NONE);
        settingsPanel.add(langComboBox, cons);

        JLabel voiceLabel = new JLabel("Voice type:");
        voiceLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(0,3,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(voiceLabel, cons);

        maleRButton = new JRadioButton("Male");
        maleRButton.setFont(new Font("Arial", Font.PLAIN, 13));
        addComponentGridBagLayout(1,3,1,1,1,1,GridBagConstraints.NONE);
        settingsPanel.add(maleRButton, cons);

        femaleRButton = new JRadioButton("Female", true);
        femaleRButton.setFont(new Font("Arial", Font.PLAIN, 13));
        addComponentGridBagLayout(2,3,1,1,1,1,GridBagConstraints.NONE);
        settingsPanel.add(femaleRButton, cons);

        neutralRButton = new JRadioButton("Neutral");
        neutralRButton.setFont(new Font("Arial", Font.PLAIN, 13));
        addComponentGridBagLayout(3,3,1,1,1,1,GridBagConstraints.NONE);
        settingsPanel.add(neutralRButton, cons);

        ButtonGroup voiceOptions = new ButtonGroup();
        voiceOptions.add(maleRButton);
        voiceOptions.add(femaleRButton);
        voiceOptions.add(neutralRButton);

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(0,4,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(speedLabel, cons);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 25, 400, 100);
        java.util.Hashtable<Integer,JLabel> speedTable = new java.util.Hashtable<Integer,JLabel>();
        speedTable.put(400, new JLabel("4.00"));
        speedTable.put(300, new JLabel("3.00"));
        speedTable.put(200, new JLabel("2.00"));
        speedTable.put(100, new JLabel("1.00"));
        speedTable.put(25, new JLabel("0.25"));
        speedSlider.setLabelTable(speedTable);
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        voiceLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(1,4,GridBagConstraints.REMAINDER,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(speedSlider, cons);

        JLabel pitchLabel = new JLabel("Pitch:");
        pitchLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(0,5,1,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(pitchLabel, cons);

        pitchSlider = new JSlider(JSlider.HORIZONTAL, -200, 200, 0);
        java.util.Hashtable<Integer,JLabel> pitchTable = new java.util.Hashtable<Integer,JLabel>();
        pitchTable.put(200, new JLabel("20.0"));
        pitchTable.put(100, new JLabel("10.0"));
        pitchTable.put(0, new JLabel("0.0"));
        pitchTable.put(-100, new JLabel("-10.0"));
        pitchTable.put(-200, new JLabel("-20.0"));
        pitchSlider.setLabelTable(pitchTable);
        pitchSlider.setMajorTickSpacing(10);
        pitchSlider.setPaintTicks(true);
        pitchSlider.setPaintLabels(true);
        voiceLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        addComponentGridBagLayout(1,5,GridBagConstraints.REMAINDER,1,1,1,GridBagConstraints.HORIZONTAL);
        settingsPanel.add(pitchSlider, cons);
    }

    private void populateSelectPanel() {
        JLabel downloadLocationLabel = new JLabel("(Optional)");
        downloadLocationLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        addComponentGridBagLayout(0,0,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL);
        selectPanel.add(downloadLocationLabel, cons);

        downloadLocationButton = new JButton("Choose download Location");
        downloadLocationButton.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        downloadLocationButton.addActionListener(this);
        addComponentGridBagLayout(0,1,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL);
        selectPanel.add(downloadLocationButton, cons);

        convertButton = new JButton("Convert");
        convertButton.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        convertButton.addActionListener(this);
        addComponentGridBagLayout(0,2,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL);
        selectPanel.add(convertButton, cons);
    }

    private void populateQueuePanel() {
        pdfList = new JList();
        pdfList.setCellRenderer(queueCellRenderer);
        pdfList.setModel(queueModel);
        // Set so the list can only ever select one cell at a time
        pdfList.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
        // Implement the ListSelectionListener interface on our list's selection model
        pdfList.getSelectionModel().addListSelectionListener(new PDFQueueSelectionHandler());

        addComponentGridBagLayout(0,0,GridBagConstraints.REMAINDER,1,1.0,1.0,GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH);
        queuePanel.add(pdfList, cons);

        selectFileButton = new JButton("Select File");
        selectFileButton.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        selectFileButton.addActionListener(this);
        addComponentGridBagLayout(0,1,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTHWEST);
        queuePanel.add(selectFileButton, cons);

        removeButton = new JButton("Remove File");
        removeButton.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        removeButton.addActionListener(this);
        addComponentGridBagLayout(1,1,1,1,0.1,0.1,GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTHEAST);
        queuePanel.add(removeButton, cons);
    }

    private void addComponentGridBagLayout(int gridx, int gridy, int width, int height,
                                           double weightx, double weighty, int fill) {
        cons = new GridBagConstraints();
        cons.gridx = gridx;
        cons.gridy = gridy;
        cons.gridwidth = width;
        cons.gridheight = height;
        cons.weightx = weightx;
        cons.weighty = weighty;
        cons.fill = fill;
    }

    private void addComponentGridBagLayout(int gridx, int gridy, int width, int height,
                                           double weightx, double weighty, int fill, Insets inset) {
        cons = new GridBagConstraints();
        cons.gridx = gridx;
        cons.gridy = gridy;
        cons.gridwidth = width;
        cons.gridheight = height;
        cons.weightx = weightx;
        cons.weighty = weighty;
        cons.fill = fill;
        cons.insets = inset;
    }

    private void addComponentGridBagLayout (int gridx, int gridy, int width, int height,
                                            double weightx, double weighty, int fill, int anchor) {
        cons = new GridBagConstraints();
        cons.gridx = gridx;
        cons.gridy = gridy;
        cons.gridwidth = width;
        cons.gridheight = height;
        cons.weightx = weightx;
        cons.weighty = weighty;
        cons.fill = fill;
        cons.anchor = anchor;
    }

    private boolean processFile(File file)
    {
        statusLabel.setText("Processing: " + file.getName());
        try {
            PageReturns setPageRes = setPagesToParse();
            if (setPageRes == PageReturns.ILLEGAL_ARG || setPageRes == PageReturns.EMPTY_ARG) {
                JOptionPane.showMessageDialog(null, "Selected page values must be numeric values from: " + pdfConverter.getStartPage() + " to " + pdfConverter.getEndPage(),
                        "Settings Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else if (setPageRes == PageReturns.WRONG_ORDER) {
                JOptionPane.showMessageDialog(null, "Start page must be less or equal to end page",
                        "Settings Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            File convertedFile = pdfConverter.convert2Text(file);

            // Read speech settings
            Gender voiceType;
            if (maleRButton.isSelected() &&
                    !Language.noMaleVoice(Objects.requireNonNull(langComboBox.getSelectedItem().toString()))) {
                voiceType = Gender.MALE;
            }
            else if (femaleRButton.isSelected()){
                voiceType = Gender.FEMALE;
            }
            else {
                JOptionPane.showMessageDialog(null, "The selected voice type for this language is " +
                                "not supported yet, altering to female voice.",
                        "Warning", JOptionPane.ERROR_MESSAGE);
                voiceType = Gender.FEMALE;
            }

            gcsAPI.reflectOptions(Language.codeOf(Objects.requireNonNull(langComboBox.getSelectedItem()).toString()),
                    voiceType, speedSlider.getValue()/100.0, pitchSlider.getValue()/10.0);

            // Upload parsed text file with the speech settings
            gcsAPI.uploadFile(convertedFile);
            pdfConverter.cleanUpFile();

            // Attempt to download
            gcsAPI.downloadFile(downloadLocation);
        }
        catch (IllegalArgumentException exc) {
            JOptionPane.showMessageDialog(null, exc.getMessage(),
                    "Settings Input Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (StorageException exc) {
            JOptionPane.showMessageDialog(null, "Failed to access gcs, check internet connection and retry",
                    "Server Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (IOException exc) {
            JOptionPane.showMessageDialog(null, exc.getMessage() + "\nEnsure file is not renamed, moved, or removed",
                    "File Access Error", JOptionPane.ERROR_MESSAGE);
            exc.printStackTrace();
            return false;
        }
        catch (TimeLimitExceededException exc) {
            JOptionPane.showMessageDialog(null, "\nConversion timed out!!!\nPlease ensure internet connection.\n" +
                            "Try again using PDF with less pages, by splitting PDF into portions.\n500 pages per PDF is recommended.",
                    "Conversion timeout", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private PageReturns setPagesToParse() {
        int start, end;
        try {
            start = Integer.parseInt(startPageField.getText());
            end = Integer.parseInt(endPageField.getText());
        }
        catch (NumberFormatException numExp) { // Unable to parseInt meaning there was a odd character such as a roman letter input
            return PageReturns.ILLEGAL_ARG;
        }
        catch (NullPointerException nullExp) { // Catches in case where JTextField is empty
            return PageReturns.EMPTY_ARG;
        }
        if (start < pdfConverter.getStartPage() || start > pdfConverter.getEndPage()
                || end > pdfConverter.getEndPage() || end < pdfConverter.getStartPage()) {
            return PageReturns.ILLEGAL_ARG;
        }
        else if (start > end) {
            return PageReturns.WRONG_ORDER;
        }
        pdfConverter.setStartPage(start);
        pdfConverter.setEndPage(end);
        return PageReturns.OK;
    }

    private void updateListGUI() //This is a very primitive method of updating the list; probably could change to observer pattern
    {
        queueModel = new DefaultListModel<String>();
        for(IFileData f : pdfQueue.GetQueue()){
            queueModel.addElement(f);
        }

        pdfList.setModel(queueModel);

        if(queueModel.getSize() != 0) //only want to select something if something exists.
        {
            pdfList.setSelectedIndex(queueModel.getSize() - 1);
        }
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == selectFileButton) { // select File is the button pertaining to enter
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
            fc.setFileFilter(filter);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setDialogTitle("Select file to convert");

            int returnVal = fc.showOpenDialog(MainWindow.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) { //If failed to pick PDF,it will display an error panel.
                File file = fc.getSelectedFile();

                if (pdfConverter.isValidPDF(file)) {
                    pdfQueue.Add(file);
                    updateListGUI();
                }
            }
        }

        if(e.getSource() == removeButton) //removes selected file from index.
        {
            if(pdfList.getSelectedValue() != null) //cant delete if nothing is selected
            {
                IFileData file = (IFileData) pdfList.getSelectedValue();
                pdfQueue.Remove(file);
                queueModel.removeElement(pdfList.getSelectedIndex());
                updateListGUI();
            }
        }

        if (e.getSource() == downloadLocationButton) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setDialogTitle("Select location to download");

            if (fc.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                downloadLocation = fc.getSelectedFile().getPath();
            }
        }

        if(e.getSource() == convertButton)
        {
            if(pdfList.getSelectedValue() != null) //cant convert if nothing is selected
            {
                IFileData fileData = (IFileData) pdfList.getSelectedValue();

                new Thread(() -> {
                    convertButton.setEnabled(false); //Disable the convert button while the thread is running
                    String location = downloadLocation.equals("") ? System.getProperty("user.dir") + File.separator + "audioFiles" : downloadLocation;

                    if (processFile(fileData.getFile())) {
                        pdfQueue.Remove(fileData);
                        queueModel.removeElement(pdfList.getSelectedIndex());
                        updateListGUI();

                        statusLabel.setText("Finished processing: " + fileData.getFile().getName() + " -> Downloaded to " + location);
                    }  // In the case where the processFile didn't work, an window should pop up and the status bar should be cleared
                    else {
                        statusLabel.setText("Failed to process file");
                    }
                    convertButton.setEnabled(true);
                }).start();
                //It may be wise to disable the download location button also, while we are processing the file.
            }
            else
            {
                //show dialog that you must select a file.
                JOptionPane.showMessageDialog(null,
                        "Please select a file to convert to audio.", "File selection error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public class PDFQueueSelectionHandler implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (pdfList.getSelectedValue() != null) {
                IFileData fileData = (IFileData) pdfList.getSelectedValue();
                try {
                    pdfConverter.loadParameters(fileData.getFile());
                }
                catch (IllegalStateException exc) {
                    exc.printStackTrace();  // If this ever happens it's most likely the programmer's fault
                }
                catch (IOException exc) {
                    JOptionPane.showMessageDialog(null, exc.getMessage() + "\nEnsure file is not renamed, moved, or removed",
                            "File Access Error", JOptionPane.ERROR_MESSAGE);
                }
                startPageField.setText(Integer.toString(pdfConverter.getStartPage()));
                endPageField.setText(Integer.toString(pdfConverter.getEndPage()));
            }
        }
    }
}
