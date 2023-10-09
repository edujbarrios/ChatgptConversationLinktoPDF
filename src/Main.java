import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame("URL to PDF Enhanced");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        JPanel panel = new JPanel(new BorderLayout());

        JTextField textFieldURL = new JTextField(30);
        JTextField textFieldExclude = new JTextField(30);
        JTextArea previewArea = new JTextArea();
        previewArea.setEditable(false);
        JScrollPane previewScrollPane = new JScrollPane(previewArea);

        JButton buttonGeneratePDF = new JButton("Generate PDF");
        JButton buttonOpenInBrowser = new JButton("Open in Browser");
        JButton buttonLoadConfig = new JButton("Load Config");
        JButton buttonSaveConfig = new JButton("Save Config");
        JButton buttonPreview = new JButton("Preview");

        JLabel statusBar = new JLabel("Ready");

        buttonGeneratePDF.addActionListener(e -> {
            String url = textFieldURL.getText();
            String exclude = textFieldExclude.getText();
            generatePDF(url, exclude, previewArea, statusBar);
        });

        buttonOpenInBrowser.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(textFieldURL.getText()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonLoadConfig.addActionListener(e -> {
            try (BufferedReader reader = new BufferedReader(new FileReader("config.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("URL=")) {
                        textFieldURL.setText(line.substring(4));
                    } else if (line.startsWith("Exclude=")) {
                        textFieldExclude.setText(line.substring(8));
                    }
                }
                statusBar.setText("Configuration loaded.");
            } catch (IOException ex) {
                ex.printStackTrace();
                statusBar.setText("Failed to load configuration.");
            }
        });

        buttonSaveConfig.addActionListener(e -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("config.txt"))) {
                writer.write("URL=" + textFieldURL.getText());
                writer.newLine();
                writer.write("Exclude=" + textFieldExclude.getText());
                statusBar.setText("Configuration saved.");
            } catch (IOException ex) {
                ex.printStackTrace();
                statusBar.setText("Failed to save configuration.");
            }
        });

        buttonPreview.addActionListener(e -> {
            String url = textFieldURL.getText();
            String exclude = textFieldExclude.getText();
            previewContent(url, exclude, previewArea, statusBar);
        });

        JPanel upperPanel = new JPanel(new FlowLayout());
        upperPanel.add(new JLabel("URL: "));
        upperPanel.add(textFieldURL);
        upperPanel.add(new JLabel("Exclude: "));
        upperPanel.add(textFieldExclude);
        upperPanel.add(buttonGeneratePDF);
        upperPanel.add(buttonOpenInBrowser);
        upperPanel.add(buttonLoadConfig);
        upperPanel.add(buttonSaveConfig);
        upperPanel.add(buttonPreview);

        panel.add(upperPanel, BorderLayout.NORTH);
        panel.add(previewScrollPane, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void generatePDF(String url, String exclude, JTextArea previewArea, JLabel statusBar) {
        org.jsoup.nodes.Document doc;
        try {
            doc = Jsoup.connect(url).get();
            for (Element el : doc.select(exclude)) {
                el.remove();
            }
            String content = doc.text();

            Document pdfDoc = new Document();
            PdfWriter.getInstance(pdfDoc, new FileOutputStream("output.pdf"));
            pdfDoc.open();
            pdfDoc.add(new Paragraph(content));
            pdfDoc.close();

            statusBar.setText("PDF Generated.");
            previewArea.setText(content);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            statusBar.setText("Failed to generate PDF.");
        }
    }

    public static void previewContent(String url, String exclude, JTextArea previewArea, JLabel statusBar) {
        org.jsoup.nodes.Document doc;
        try {
            doc = Jsoup.connect(url).get();
            for (Element el : doc.select(exclude)) {
                el.remove();
            }
            String content = doc.text();
            previewArea.setText(content);
            statusBar.setText("Preview generated.");
        } catch (IOException e) {
            e.printStackTrace();
            statusBar.setText("Failed to generate preview.");
        }
    }
}
