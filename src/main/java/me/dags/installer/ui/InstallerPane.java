package me.dags.installer.ui;

import me.dags.installer.Installer;
import me.dags.installer.Versions;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class InstallerPane extends JPanel
{
    private static final long serialVersionUID = 447602866903348986L;

    private final JRadioButton install = new JRadioButton();
    private final JRadioButton extract = new JRadioButton();
    private final JComboBox<String> forgeVersions = new JComboBox<>();
    private final JButton ok = new JButton();

    private final Versions versions = new Versions();
    private File targetDir = new File("");

    public InstallerPane()
    {
        this.targetDir = new File(Installer.profile().mcDir, Installer.profile().target_dir);

        final int windowWidth = 600;
        final int buttonWidth = 75;
        final int chooseWidth = 80;
        final int paneHeight = 30;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setVisible(true);

        try
        {
            BufferedImage image = ImageIO.read(this.getClass().getResource("/installer-banner.jpg"));
            JLabel icon = new JLabel(new ImageIcon(image));
            JPanel banner = new JPanel();
            banner.setPreferredSize(new Dimension(windowWidth, 320));
            banner.add(icon);
            this.add(banner);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        JLabel versionsLabel = new JLabel();
        versionsLabel.setText("Forge Versions:");
        versionsLabel.setPreferredSize(new Dimension(chooseWidth, paneHeight));

        forgeVersions.setPreferredSize(new Dimension(windowWidth - chooseWidth * 2, paneHeight));
        if (versions.empty())
        {
            forgeVersions.setEnabled(false);
            forgeVersions.addItem("No forge installs detected for mc version " + Installer.profile().minecraft_version + "!");
        }
        else
        {
            versions.getVersions().forEach(forgeVersions::addItem);
        }

        JPanel versionsContainer = new JPanel();
        versionsContainer.setPreferredSize(new Dimension(windowWidth, paneHeight + 10));
        versionsContainer.add(versionsLabel);
        versionsContainer.add(forgeVersions);
        this.add(versionsContainer);

        JTextField path = new JTextField();
        path.setPreferredSize(new Dimension(windowWidth - chooseWidth * 2, paneHeight));
        path.setText(this.targetDir.getAbsolutePath());
        this.add(path);

        JButton choose = new JButton();
        choose.setText("Choose");
        choose.addActionListener(this.fileExplorer(path));
        choose.setPreferredSize(new Dimension(chooseWidth, paneHeight));

        JPanel filesPane = new JPanel();
        filesPane.setMinimumSize(new Dimension(windowWidth, paneHeight));
        filesPane.add(path);
        filesPane.add(choose);

        install.setName("install");
        install.setText("Install");
        install.setToolTipText("Downloads & installs a new launcher profile for the modpack");
        install.setSelected(true);

        extract.setName("extract");
        extract.setText("Extract");
        extract.setToolTipText("Downloads & extracts the modpack files to the selected folder");
        extract.setSelected(false);

        JPanel installOptionsPane = new JPanel();
        installOptionsPane.setMinimumSize(new Dimension(windowWidth, paneHeight));
        installOptionsPane.add(install);
        installOptionsPane.add(extract);

        install.addActionListener(this.toggleRadio(install, extract));
        extract.addActionListener(this.toggleRadio(extract, install));

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(25, paneHeight));

        ok.setName("ok");
        ok.setText("Ok");
        ok.setPreferredSize(new Dimension(buttonWidth, paneHeight));
        ok.addActionListener(this.install());
        ok.setEnabled(forgeVersions.isEnabled());

        JButton cancel = new JButton();
        cancel.setName("cancel");
        cancel.setText("Cancel");
        cancel.setPreferredSize(new Dimension(buttonWidth, paneHeight));
        cancel.addActionListener(cancel());

        installOptionsPane.add(spacer);
        installOptionsPane.add(ok);
        installOptionsPane.add(cancel);

        this.add(filesPane);
        this.add(installOptionsPane);
    }

    private ActionListener fileExplorer(JTextField pathField)
    {
        return e -> {
            if (!targetDir.exists() && targetDir.mkdirs()) ;
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);

            int response = dirChooser.showOpenDialog(InstallerPane.this);
            if (response == JFileChooser.APPROVE_OPTION)
            {
                targetDir = dirChooser.getSelectedFile();
                pathField.setText(targetDir.getAbsolutePath());
                Installer.log("Set installation dir to {}", targetDir);
            }
        };
    }

    private ActionListener toggleRadio(JRadioButton active, JRadioButton other)
    {
        return arg0 -> {
            active.setSelected(true);
            other.setSelected(false);
            ok.setEnabled(forgeVersions.isEnabled() || active.getName().equals("extract"));
            ok.setText("Ok");
        };
    }

    private ActionListener install()
    {
        return arg0 -> {
            JFrame frame = new JFrame();
            String forgeVersion = extract.isSelected() ? "" : forgeVersions.getSelectedItem().toString();
            Action action = new Install(frame, ok, targetDir, forgeVersion);
            frame.setLayout(new GridBagLayout());
            frame.add(action);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            action.perform();
        };
    }

    private ActionListener cancel()
    {
        return arg0 -> System.exit(0);
    }
}
