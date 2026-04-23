package core.webserver;

import java.io.*;
import java.net.Socket;

import core.response.HttpResponse;
import core.response.HttpResponseBuilder;
import file.FileContentReader;
import core.request.HttpRequest;
import file.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import core.routing.RouteType;
import core.http.StatusCode;
import core.response.ResponseData;
import core.routing.RoutedInfo;
import core.routing.Router;
import core.view.ResourceResolver;
import core.view.WanjaTemplateEngine;

// TODO: 기능 주도 패키징 vs 계층 주도 패키징 | 클린 아키텍처 + 헥사고날 아키텍처 | DDD
public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String EXTENSION_SEPARATOR = ".";

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}",
                connection.getInetAddress(), connection.getPort());
        
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            logger.debug("[Client's HTTP Message]");
            HttpRequest httpRequest = HttpRequest.of(in);
            logger.debug(httpRequest.getCoreRequestInfo());

            RoutedInfo routedInfo = Router.route(httpRequest);
            RouteType routeType = routedInfo.routeType();
            FileData fileData = extractFileData(routedInfo, routeType);

            ResponseData responseData = HttpResponseBuilder.build(routedInfo, fileData, httpRequest);
            HttpResponse httpResponse = HttpResponse.of(responseData);
            httpResponse.send(out);

            // TODO: 에러처리 --> 페이지로 라우팅
        } catch (EOFException e){
            logger.debug("Client disconnected: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("I/O error occurred", e);
        }
    }

    private FileData extractFileData(RoutedInfo routedInfo, RouteType routeType) throws IOException {
        if(routeType == RouteType.REDIRECT){
            return FileData.processRedirect();
        } else{
            File safeFile = ResourceResolver.resolve(routedInfo.routedPath(), routeType);
            ByteArrayOutputStream contents = FileContentReader.extract(safeFile);

            if(routeType == RouteType.DYNAMIC){
                contents = WanjaTemplateEngine.convertFile(contents, routedInfo.models());
            }

            return new FileData(contents, safeFile.getName(),
                    extractExtension(routedInfo.routedPath()), StatusCode.OK);
        }
    }
    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        return idx == -1 ? "" : fileName.substring(idx + 1);
    }
}
