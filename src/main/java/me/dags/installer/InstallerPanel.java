package me.dags.installer;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class InstallerPanel extends JPanel
{
    private static final long serialVersionUID = 447602866903348986L;

    private final JTextField path = new JTextField();
    private final JRadioButton install = new JRadioButton();
    private final JRadioButton extract = new JRadioButton();
    private final JComboBox<String> forgeVersions = new JComboBox<>();
    private final JButton ok = new JButton();
    private final JButton close = new JButton();

    private final Versions versions = new Versions();
    private File targetDir = new File("");

    public InstallerPanel()
    {
        this.targetDir = new File(Installer.properties().mcDir, Installer.properties().target_dir);

        final int windowWidth = 600;
        final int buttonWidth = 75;
        final int chooseWidth = 80;
        final int panelHeight = 30;

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
        versionsLabel.setPreferredSize(new Dimension(chooseWidth, panelHeight));

        forgeVersions.setPreferredSize(new Dimension(windowWidth - chooseWidth * 2, panelHeight));
        if (versions.empty())
        {
            forgeVersions.setEnabled(false);
            forgeVersions.addItem("No forge installs detected for mc version " + Installer.properties().minecraft_version + "!");
        }
        else
        {
            versions.getVersions().forEach(forgeVersions::addItem);
        }

        JPanel versionsContainer = new JPanel();
        versionsContainer.setPreferredSize(new Dimension(windowWidth, panelHeight + 10));
        versionsContainer.add(versionsLabel);
        versionsContainer.add(forgeVersions);
        this.add(versionsContainer);

        path.setPreferredSize(new Dimension(windowWidth - chooseWidth * 2, panelHeight));
        path.setText(this.targetDir.getAbsolutePath());
        this.add(path);

        JButton choose = new JButton();
        choose.setText("Choose");
        choose.addActionListener(this.fileExplorer(path));
        choose.setPreferredSize(new Dimension(chooseWidth, panelHeight));

        JPanel filesPane = new JPanel();
        filesPane.setMinimumSize(new Dimension(windowWidth, panelHeight));
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
        installOptionsPane.setMinimumSize(new Dimension(windowWidth, 25));
        installOptionsPane.add(install);
        installOptionsPane.add(extract);

        install.addActionListener(this.toggleRadio(install, extract));
        extract.addActionListener(this.toggleRadio(extract, install));

        JPanel spacer = new JPanel();
        spacer.setPreferredSize(new Dimension(25, panelHeight));

        ok.setName("ok");
        ok.setText("Ok");
        ok.setPreferredSize(new Dimension(buttonWidth, panelHeight));
        ok.addActionListener(this.install());
        ok.setEnabled(forgeVersions.isEnabled());

        close.setName("close");
        close.setText("Close");
        close.setPreferredSize(new Dimension(buttonWidth, panelHeight));
        close.addActionListener(cancel());

        installOptionsPane.add(spacer);
        installOptionsPane.add(ok);
        installOptionsPane.add(close);

        this.add(filesPane);
        this.add(installOptionsPane);
    }

    private ActionListener fileExplorer(JTextField pathField)
    {
        return e -> {
            if (!path.getText().equals(targetDir.getAbsolutePath()))
            {
                targetDir = new File(path.getText());
            }

            if (!targetDir.exists() && targetDir.mkdirs()) ;
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(targetDir);
            dirChooser.setSelectedFile(targetDir);

            int response = dirChooser.showOpenDialog(InstallerPanel.this);
            if (response == JFileChooser.APPROVE_OPTION)
            {
                targetDir = dirChooser.getSelectedFile();
                pathField.setText(targetDir.getAbsolutePath());
                Installer.phase("settings").log("Set installation dir to {}", targetDir);
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
            if (!path.getText().equals(targetDir.getAbsolutePath()))
            {
                targetDir = new File(path.getText());
            }

            JFrame frame = new JFrame();
            String forgeVersion = extract.isSelected() ? "" : forgeVersions.getSelectedItem().toString();
            InstallProcess install = new InstallProcess(frame, ok, targetDir, forgeVersion);
            frame.setLayout(new GridBagLayout());
            frame.add(install);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            install.perform();
        };
    }

    private ActionListener cancel()
    {
        return arg0 -> System.exit(0);
    }
}
