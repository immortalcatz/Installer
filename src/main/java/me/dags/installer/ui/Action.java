package me.dags.installer.ui;

import javax.swing.JPanel;

public abstract class Action extends JPanel implements Runnable
{
    abstract void perform();
}
