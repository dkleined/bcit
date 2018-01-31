package fileserver.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static fileserver.util.LogUtil.logStr;

/**
 * Handles basic operations related to accessing files on the server. The files are stored
 * in the following manner:
 *
 * -> ${basepath}
 * --> hostname
 * ----> file
 *
 * The base path is configurable in application.properties
 *
 * Save a file:
 * Saves a file to the server using the organizational structure explained above.
 *
 * Get a file:
 * Straight forward, provide a filename and receive that file if it exists. If the file doesn't
 * exist, then an empty byte array will be returned instead.
 *
 * Get all filenames:
 * Gets all filenames under given connection folder.
 *
 * @author dklein
 */
@Service
public class FileService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String basePath;

    public FileService(String basepath) {
        this.basePath = basepath;
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Saves a file to the server.
     *
     * Saving a file to the server is done through a multi threaded executor service. There is a small
     * chance that the file will not be saved to the disk, but an acknowledgement will be returned
     * to the user. This risk is accepted in return for the performance boost of the main thread
     * not waiting on the save function, as it can be quite slow if the file is large.
     *
     * @param hostString user saving the file (used for organization)
     * @param filename filename being saved
     * @param bytes file bytes
     * @return returns true if file save was successful, else false
     */
    public boolean saveFile(final String hostString, final String filename, byte[] bytes) {
        log.info(logStr(hostString, "saveFile", filename));
        File dir = new File(basePath + "/" + hostString);
        File subDir = new File(basePath + "/" + hostString);
        if (!dir.exists() || !subDir.exists()) {
            dir.mkdir();
            subDir.mkdir();
        }
        File file = new File(basePath + "/" + hostString + "/" + filename);
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Gets all the filenames under a given connection folder. Goes through the folder
     * recursively, so it will return all files in extension folders.
     *
     * @param hostString the host string which is also the storage folder
     * @return complete list of files in connection folder
     */
    public List<String> getAllFileNames(String hostString) {
        try {
            return FileUtils.listFiles(new File(basePath + "/" + hostString), null, true)
                    .stream()
                    .map(File::getName)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException eatIt) {
            return Collections.emptyList();
        }
    }

    /**
     * Gets a specified file from the server. If the file doesn't exist, it will return an empty
     * byte array.
     *
     * @param hostString the host string which is also the storage folder
     * @param filename filename being requested
     * @return the byte array for a file if it exists, or an empty byte array if it doesn't or the
     * read fails
     */
    public byte[] getFile(String hostString, String filename) {
        log.info(logStr(hostString, "getFile", filename));
        File file = new File(basePath + "/" + hostString + "/" + filename);
        if (!file.exists()) {
            return new byte[0];
        }
        Path path = Paths.get(basePath + "/" + hostString + "/" + filename);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
