/*
This file is part of Wow Log Parser, a program to parse World of Warcraft combat log files.
Copyright (C) Gustav Haapalahti

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package wowlogparserbase.helpers;
import java.io.*;
import java.nio.channels.*;

/**
 *
 * @author racy
 */
public class FileHelper {

    public static String readTextFromResource(String name) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(name), "UTF8"));
            StringBuilder text = new StringBuilder();
            String line = "";
            while((line=reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString();
        } catch(IOException ex) {
        } finally {
            close(reader);
        }
        return "";
    }

    public static void copyFile(File in, File out)
            throws IOException {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(in);
            outStream = new FileOutputStream(out);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            close(inStream);
            close(outStream);
        }
    }

    public static void copyFile(InputStream inStream, File out) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = inStream.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } finally {
            close(fos);
        }
    }

    public static void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ex) {
        }
    }

    public static void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ex) {
        }
    }

    public static void close(Reader r) {
        try {
            if (r != null) {
                r.close();
            }
        } catch(IOException ex) {
        }
    }

    public static void close(Writer w) {
        try {
            if (w != null) {
                w.close();
            }
        } catch(IOException ex) {
        }
    }
}
