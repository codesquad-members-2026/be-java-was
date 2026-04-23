package core.view;

import exception.ForbiddenException;
import exception.PayloadTooLargeException;
import exception.ResourceNotFoundException;
import core.routing.RouteType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ResourceResolver {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String BASE_STATIC_DIR
            = Path.of("src", "main", "resources", "static").toString();
    private static final String BASE_TEMPLATES_DIR
            = Path.of("src", "main", "resources", "templates").toString();

    // TODO: 상단 캐싱?
    private static final String ABSOLUTE_STATIC_DIR;
    private static final String ABSOLUTE_TEMPLATE_DIR;

    static {
        try {
            ABSOLUTE_TEMPLATE_DIR = new File(BASE_STATIC_DIR).getCanonicalPath();
            ABSOLUTE_STATIC_DIR = new File(BASE_TEMPLATES_DIR).getCanonicalPath();
        } catch (IOException ex) {
            throw new RuntimeException("경로 초기화 실패", ex);
        }
    }

    private ResourceResolver() {}

    public static File resolve(String routedPath, RouteType routeType) throws IOException {
        String removeDuplicationString = removePathDuplication(routedPath);
        String absolutePath = routeType == RouteType.STATIC
                ? BASE_STATIC_DIR + removeDuplicationString : BASE_TEMPLATES_DIR + removeDuplicationString;
        File file = new File(absolutePath);

        // 403, 404, 500 --> 예외 처리
        validateFile(file);

        return file;
    }

    private static String removePathDuplication(String routedPath) {
        if(routedPath.startsWith("/static"))
            return routedPath.substring("/static".length());
        else if(routedPath.startsWith("/templates"))
            return routedPath.substring("/templates".length());

        return routedPath;
    }

    private static void validateFile(File file) throws IOException {
        checkValidPath(file);
        checkFileExist(file);
        checkCanAccess(file);
        checkIsFile(file);
        checkIsHiddenFile(file);
        checkFileSize(file);
    }
    private static void checkValidPath(File file) throws IOException {
        String targetCanonicalPath = file.getCanonicalPath();

        if(!targetCanonicalPath.startsWith(ABSOLUTE_STATIC_DIR)
                && !targetCanonicalPath.startsWith(ABSOLUTE_TEMPLATE_DIR)){

            throw new ForbiddenException("Invalid path: " + targetCanonicalPath);
        }
    }
    private static void checkFileExist(File file) {
        if(!file.exists()){
            throw new ResourceNotFoundException("The file does not exist: " + file.getName());
        }
    }
    private static void checkCanAccess(File file){
        if(!file.canRead()){
            throw new ForbiddenException("The file is not readable: " + file.getName());
        }
    }
    private static void checkIsFile(File file){
        if(!file.isFile()){
            throw new ResourceNotFoundException("The file is a directory: " + file.getName());
        }
    }
    private static void checkIsHiddenFile(File file){
        if(file.isHidden()){
            throw new ForbiddenException("The file is hidden: " + file.getName());
        }
    }
    private static void checkFileSize(File file){
        if(file.length() > MAX_FILE_SIZE){
            throw new PayloadTooLargeException("File size exceeded limit: " + file.getName());
        } // TODO: 설명할 수 있어야 함
    }
}