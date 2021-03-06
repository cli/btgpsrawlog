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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Form;

/**
 * Thread reading raw NMEA data from a Bluetooth GPS receiver.
 * 
 * @author Christian Lins
 */
public class RawLogger extends Thread {

    private StreamConnection connection     = null;
    private FileConnection   fileConnection = null;
    private InputStream      inputStream    = null;
    private OutputStream     outputStream   = null;
    private Form             outputForm     = null;
    private boolean          running        = false;
    private String           url            = null;
    private String           saveFile       = null;
    private String           name           = null;
    private int              bytesRead      = 0;

    public RawLogger(String url, String name) {
        if (!url.startsWith("btspp://")) {
            this.url = "btspp://" + url;
        } else {
            this.url = url;
        }
        this.name = name;
    }

    public int bytesRead() {
        return this.bytesRead;
    }

    public synchronized boolean connect() throws IOException {
        connection = (StreamConnection) Connector.open(url, Connector.READ);
        inputStream = connection.openInputStream();

        fileConnection = (FileConnection) Connector.open(saveFile, Connector.READ_WRITE);
        if (fileConnection.exists()) {
            try {
                fileConnection.delete();
            } catch (IOException ex) {
                return false;
            }
        }
        fileConnection.create();
        outputStream = fileConnection.openOutputStream();
        this.running = true;

        return true;
    }

    private void dbgmsg(String str) {
        if (this.outputForm != null) {
            this.outputForm.append(str);
        }
    }

    public synchronized void disconnect() throws IOException {
        this.running = false;

        inputStream.close();
        connection.close();

        outputStream.flush();
        outputStream.close();
        fileConnection.close();
    }

    public String getDeviceName() {
        return name;
    }

    public String getSaveFile() {
        return saveFile;
    }

    public String getURL() {
        return url;
    }

    public boolean isRunning() {
        return running;
    }

    public void run() {
        setPriority(MIN_PRIORITY);
        try {
            byte[] buf = new byte[128];
            while (running) {
                int read = inputStream.read(buf);
                if (read == -1) {
                    dbgmsg("End of stream.");
                    break;
                } else {
                    this.outputStream.write(buf, 0, read);
                    this.outputStream.flush();
                    this.bytesRead += read;
                }
            }

            try {
                inputStream.close();
            } catch (Exception ex) {
                dbgmsg("Exception catched: " + ex.toString());
            }
            try {
                connection.close();
            } catch (Exception ex) {
                dbgmsg("Exception catched: " + ex.toString());
                ex.printStackTrace();
            }
            try {
                outputStream.flush();
                outputStream.close();
                fileConnection.close();
            } catch (Exception ex) {
                dbgmsg("Exception catched: " + ex.toString());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            dbgmsg(ex.getMessage());
        }
        dbgmsg("Logging ended.");
    }

    public void setSaveFile(String saveFile) {
        this.saveFile = saveFile;
    }

    public void setOutputForm(Form form) {
        this.outputForm = form;
    }

    public void stop() {
        this.running = false;
    }
}
