/*
 *  BTGPSRawLog
 *  Copyright (C) 2010-2013 Christian Lins <christian@lins.me>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package btgpsrawlog;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import btgpsrawlog.events.BluetoothDeviceListController;
import btgpsrawlog.events.EventController;
import btgpsrawlog.forms.AboutForm;
import btgpsrawlog.forms.BluetoothDeviceList;
import btgpsrawlog.forms.LoggerForm;
import btgpsrawlog.forms.MainForm;
import btgpsrawlog.forms.SaveLogForm;

/**
 * Main MIDlet.
 * 
 * @author Christian Lins
 */
public class BTGPSRawLogMidlet extends MIDlet {

    protected BluetoothDeviceListController btDevListController;
    protected Displayable                   currentDisplay;
    protected EventController               eventController;
    protected RawLogger                     rawLogger   = null;

    protected AboutForm                     aboutForm   = new AboutForm(this);
    protected BluetoothDeviceList           btDevList   = new BluetoothDeviceList(this);
    protected LoggerForm                    loggerForm  = new LoggerForm();
    protected MainForm                      mainForm    = new MainForm();
    protected SaveLogForm                   saveLogForm = new SaveLogForm();

    public BTGPSRawLogMidlet() {
        this.eventController = new EventController(this);
        this.btDevListController = new BluetoothDeviceListController(btDevList, this);
        btDevList.setCommandListener(this.btDevListController);
        loggerForm.setCommandListener(eventController);
        mainForm.setCommandListener(eventController);
        saveLogForm.setCommandListener(eventController);
        aboutForm.setCommandListener(eventController);

        this.currentDisplay = mainForm;
    }

    public RawLogger getLogger() {
        return this.rawLogger;
    }

    public LoggerForm getLoggerForm() {
        return this.loggerForm;
    }

    public MainForm getMainForm() {
        return this.mainForm;
    }

    public SaveLogForm getSaveLogForm() {
        return this.saveLogForm;
    }

    public void setRawLogger(RawLogger rawLogger) {
        this.rawLogger = rawLogger;
    }

    public void startApp() {
        Display display = Display.getDisplay(this);
        display.setCurrent(currentDisplay);
    }

    public void pauseApp() {
        Display display = Display.getDisplay(this);
        this.currentDisplay = display.getCurrent();
    }

    public void showMainForm() {
        Display.getDisplay(this).setCurrent(this.mainForm);
    }

    public void showAboutForm() {
        Display.getDisplay(this).setCurrent(this.aboutForm);
    }

    public void showBluetoothDeviceList() {
        Display.getDisplay(this).setCurrent(this.btDevList);

        // Start the search for bluetooth device immediately
        this.btDevListController.commandAction(BluetoothDeviceList.SEARCH, this.btDevList);
    }

    /**
     * Called by the program manager when the app is about to destroyed.
     * 
     * @param unconditional
     */
    public void destroyApp(boolean unconditional) {
        if (this.rawLogger != null) {
            this.rawLogger.stop();
        }
    }
}
