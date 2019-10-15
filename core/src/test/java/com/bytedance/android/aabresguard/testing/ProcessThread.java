package com.bytedance.android.aabresguard.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Used to execute shell cmd
 * <p>
 * Created by YangJing on 2019/04/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ProcessThread extends Thread {


    private InputStream is;
    private String printType;

    ProcessThread(InputStream is, String printType) {
        this.is = is;
        this.printType = printType;
    }

    public static boolean execute(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            new ProcessThread(process.getInputStream(), "INFO").start();
            new ProcessThread(process.getErrorStream(), "ERR").start();
            int value = process.waitFor();
            return value == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean execute(String cmd, Object... objects) {
        return execute(String.format(cmd, objects));
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (printType == "ERR") {
//                    System.out.println(printType + ">" + line);
                }
                System.out.println(printType + ">" + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
