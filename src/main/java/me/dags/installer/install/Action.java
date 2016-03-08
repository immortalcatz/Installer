package me.dags.installer.install;

import javax.swing.JPanel;

public abstract class Action extends JPanel implements Runnable
{
    abstract void perform();
}
