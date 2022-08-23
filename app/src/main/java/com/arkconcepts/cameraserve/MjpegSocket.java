package com.arkconcepts.cameraserve;

import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.DataOutputStream;
import java.net.Socket;

public class MjpegSocket implements Runnable {
    private Socket socket;
    private String boundary = "CameraServeDataBoundary";

    public MjpegSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        try {
            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());

            stream.write(("HTTP/1.0 200 OK\r\n" +
                    "Server: CameraServe\r\n" +
                    "Connection: close\r\n" +
                    "Max-Age: 0\r\n" +
                    "Expires: 0\r\n" +
                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                    "Pragma: no-cache\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=" + boundary + "\r\n" +
                    "\r\n" +
                    "--" + boundary + "\r\n").getBytes());
            stream.flush();

            while (true) {
                YuvImage image = MainActivity.getYuvFrame();

                // Approximate, it seems like over-estimating is ok
                int[] strides = image.getStrides();
                int length = image.getHeight() * image.getWidth();
                for (int stride : strides) {
                    length *= stride;
                }

                stream.write(("Content-type: image/jpeg\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "\r\n").getBytes());

                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        100,
                        stream
                );

                stream.write(("\r\n--" + boundary + "\r\n").getBytes());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
