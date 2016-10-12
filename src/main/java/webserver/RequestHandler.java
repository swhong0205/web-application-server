package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            int i = 0;
            byte[] body = "".getBytes();
            do {
                line = bufferedReader.readLine();
                if (line == null) {
                    return;
                }
                if (i == 0) {
                    String urlPath = getUrlPath(line);
                    if (!"".equals(urlPath)) {
                        body = Files.readAllBytes(Paths.get("./webapp", urlPath));
                        log.info("urlPath: {}", urlPath);
                    }
                }
                log.info("#{}: {}", ++i, line);
            } while (!"".equals(line));

            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "**Hello Lapin World**".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getUrlPath(String line) {
        List<String> lines = Lists.newArrayList(line.split(" "));
        if (lines.size() >= 3) {
            return lines.get(1);
        }
        return "";
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
