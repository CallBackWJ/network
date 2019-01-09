package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;

public class RequestHandler extends Thread {
	private Socket socket;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// get IOStream
			OutputStream outputStream = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			consoleLog("connected from " + inetSocketAddress.getAddress().getHostAddress() + ":"
					+ inetSocketAddress.getPort());

			String request = null;
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;// 브라우저가 연결을 끊은 경우
				if (request == null)
					request = line;
				if ("".equals(line))
					break;// Header만 읽음

			}
			// consoleLog(request);

			String[] tokens = request.split(" ");
			if ("GET".equals(tokens[0])) {
				responseStaticResource(outputStream, tokens[1], tokens[2]);
			} else {
				// POST,DELETE,PUT,ETC
				// consoleLog("bad request:"+request);
				response400Error(outputStream, tokens[2]);
			
			}

		} catch (Exception ex) {
			consoleLog("error:" + ex);
		} finally {
			// clean-up
			try {
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}

			} catch (IOException ex) {
				consoleLog("error:" + ex);
			}
		}
	}

	private void responseStaticResource(OutputStream outputStream, String url, String protocol) throws IOException {
		// TODO Auto-generated method stub
		if ("/".equals(url))
			url = "/index.html";
		File file = new File("./webapp" + url);
		
		if (file.exists() == false) {
			response404Error(outputStream, protocol);
			return;
		}
		byte[] body = Files.readAllBytes(file.toPath()); // 파일의 내용을 byte[]단위로
															// 가져온다. java.nio.~~
															// (1.7버전)
		String contentType = Files.probeContentType(file.toPath());

		outputStream.write("HTTP/1.1 200 OK\r\n".getBytes("UTF-8"));
		outputStream.write(("Content-Type:" + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);
	}

	private void response400Error(OutputStream outputStream, String protocol)
			throws UnsupportedEncodingException, IOException {
		// TODO Auto-generated method stub
		File file = new File("./webapp/error/400.html");
		outputStream.write((protocol + " HTTP/1.0 404 File Not Found\r\n").getBytes("UTF-8"));
		outputStream.write(("Content-Type:text/html;charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(Files.readAllBytes(file.toPath()));
	}

	private void response404Error(OutputStream outputStream, String protocol) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("./webapp/error/404.html");
		outputStream.write((protocol + " HTTP/1.0 400 Bad Request\r\n").getBytes("UTF-8"));
		outputStream.write(("Content-Type:text/html\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(Files.readAllBytes(file.toPath()));
	}

	public void consoleLog(String message) {
		System.out.println("[RequestHandler#" + getId() + "] " + message);
	}
}
