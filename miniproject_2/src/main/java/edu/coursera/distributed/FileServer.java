package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs)
            throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {

            final Socket accept = socket.accept();

             String path = getFileName(accept.getInputStream());

            final OutputStream outputStream = accept.getOutputStream();
            if(path == null || path.isEmpty()){
                write404(outputStream);

            } else {
                final PCDPPath pcdpPath = new PCDPPath(path);
                final String s = fs.readFile(pcdpPath);
                if (s == null) {
                    write404(outputStream);
                } else{
                    writeFile200(outputStream, s);
                }
            }
            outputStream.flush();
            outputStream.close();
        }
    }

    private void writeFile200(OutputStream outputStream, String s) {
        final PrintStream printer = new PrintStream(outputStream);
        printer.print("HTTP/1.0 200 OK\r\n");
        printer.print("Server: FileServer\r\n");
        printer.print("Content-Length: " + s.length() + "\r\n");
        printer.print("\r\n");
        printer.print(s);
        printer.print("\r\n");
    }

    private void write404(OutputStream outputStream) throws IOException {
        String s = "HTTP/1.0 404 Not Found\\r\\n\n" +
                "   Server: FileServer\\r\\n\n" +
                "   \r\\n";
        outputStream.write(s.getBytes());
    }

    public static String getFileName(InputStream in) {

        Scanner scanner = new Scanner(in).useDelimiter("\r\n");
        final String next = scanner.next();
        Pattern pattern = Pattern.compile("GET (.+) HTTP.*");
        Matcher matcher = pattern.matcher(next);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
}
