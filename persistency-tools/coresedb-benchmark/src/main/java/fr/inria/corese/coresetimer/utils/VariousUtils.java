/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.coresetimer.utils;

import fr.inria.wimmics.coresetimer.CoreseTimer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edemairy
 */
public class VariousUtils {
    private static Logger logger = Logger.getLogger(VariousUtils.class.getName());

    /**
     * @param envName       Environment variable to read.
     * @param defaultResult Default result provided if the environment
     *                      variable is not set.
     * @return The content of the environment variable if set, defaultResult
     * otherwise.
     */
    public static String getEnvWithDefault(String envName, String defaultResult) {
        String result = System.getenv(envName);
        if (result == null) {
            result = defaultResult;
        }
        return result;
    }

    public static void createDir(String dirName, String permissions) {
        Path dirPath = Paths.get(dirName);
        Set<PosixFilePermission> filePermissions = PosixFilePermissions.fromString(permissions);
        FileAttribute<Set<PosixFilePermission>> attributes = PosixFilePermissions.asFileAttribute(filePermissions);
        try {
            Files.createDirectories(dirPath, attributes);
        } catch (IOException ex) {
            Logger.getLogger(CoreseTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.info("Directory created at: " + dirPath.toString());
    }

    // Add end string wether dirName does not ends with it.
    public static String ensureEndWith(String dirName, String end) {
        String result = (dirName.endsWith(end)) ? dirName : dirName + "/";
        return result;
    }
}
