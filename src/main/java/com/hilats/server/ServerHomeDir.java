package com.hilats.server;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by pduchesne on 12/02/15.
 */
public class ServerHomeDir {

    private File rootDir;

    public ServerHomeDir() {
        this.rootDir = getDefaultServerRootDir(true);
    }

    public ServerHomeDir(File rootDir) {
        this.rootDir = checkRootDir(rootDir, true);
    }

    private static File checkRootDir(File rootDir, boolean initDirs) {

        if (!rootDir.exists()) {
            if (!initDirs) {
                throw new IllegalArgumentException("Home directory not found : " + rootDir);
            } else {
                if (!rootDir.mkdirs())
                    throw new RuntimeException("Failed to create root dir "+rootDir);
                try {
                    for (String filename: new String[]{"README.md", "server.json"}) {
                        Files.copy(
                                ServerHomeDir.class.getResourceAsStream("/init_home/"+filename),
                                new File(rootDir, filename).toPath());
                    }


                } catch (Exception e) {
                    throw new RuntimeException("Failed to init root dir with samples", e);
                }
            }

        }

        if (!rootDir.isDirectory()) throw new IllegalArgumentException("Dataserver config directory path is not a directory: "+rootDir);
        if (!rootDir.canWrite()) throw new IllegalArgumentException("Dataserver config directory is not writable: "+rootDir);

        return rootDir;
    }

    public static File getDefaultServerRootDir(boolean initDirs) {
        String homeDirStr = System.getProperty("server.home");
        File homeDir;
        if (homeDirStr == null) {
            homeDirStr = System.getProperty("user.home");
            homeDir = new File(homeDirStr, ".hilats");
        } else {
            homeDir = new File(homeDirStr);
        }

        checkRootDir(homeDir, initDirs);

        return homeDir;

    }

    public File getRootDir() {
        return rootDir;
    }

    public File getResource(String path) {
        return new File(getRootDir(), path);
    }
}
