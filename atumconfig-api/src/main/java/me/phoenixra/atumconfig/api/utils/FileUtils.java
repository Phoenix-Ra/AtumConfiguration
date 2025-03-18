package me.phoenixra.atumconfig.api.utils;

import me.phoenixra.atumconfig.api.ConfigOwner;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import me.phoenixra.atumconfig.api.tuples.PairRecord;

public class FileUtils {
    public static void writeTextToFile(File f, boolean append, String... text) throws IOException {
        FileOutputStream fo = new FileOutputStream(f, append);
        OutputStreamWriter os = new OutputStreamWriter(fo, StandardCharsets.UTF_8);
        BufferedWriter writer = new BufferedWriter(os);
        if (text.length == 1) {
            writer.write(text[0]);
        } else if (text.length > 0) {
            for (String s : text) {
                writer.write(s + "\n");
            }
        }
        writer.flush();
        try {
            if (writer != null) {
                writer.close();
            }
            if (fo != null) {
                fo.close();
            }
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {}
    }

    public static List<String> getFileLines(File f) {
        List<String> list = new ArrayList<>();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            String line = in.readLine();
            while (line != null) {
                list.add(line);
                line = in.readLine();
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<String> getFiles(String path) {
        List<String> list = new ArrayList<>();
        File f = new File(path);
        if (f.exists()) {
            for (File file : f.listFiles()) {
                list.add(file.getAbsolutePath());
            }
        }

        return list;
    }

    public static List<String> getFilenames(String path, boolean includeExtension) {
        List<String> list = new ArrayList<>();
        File f = new File(path);
        if (f.exists()) {
            for (File file : f.listFiles()) {
                if (includeExtension) {
                    list.add(file.getName());
                } else {
                    list.add(getNameWithoutExtension(file.getName()));
                }
            }
        }

        return list;
    }
    public static String getNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(46);
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
    public static String generateAvailableFilename(String dir, String baseName, String extension) {
        File f = new File(dir);
        if (!f.exists() && f.isDirectory()) {
            f.mkdirs();
        }

        File f2 = new File(f.getPath() + "/" + baseName + "." + extension.replace(".", ""));
        int i = 1;
        while (f2.exists()) {
            f2 = new File(f.getPath() + "/" + baseName + "_" + i + "." + extension.replace(".", ""));
            i++;
        }

        return f2.getName();
    }

    public static boolean copyFile(File from, File to) {

        if (!from.getAbsolutePath().replace("\\", "/").equals(to.getAbsolutePath().replace("\\", "/"))) {
            if (from.exists() && from.isFile()) {
                File toParent = to.getParentFile();
                if ((toParent != null) && !toParent.exists()) {
                    toParent.mkdirs();
                }
                InputStream in = null;
                OutputStream out = null;

                try {
                    in = new BufferedInputStream(new FileInputStream(from));
                    out = new BufferedOutputStream(new FileOutputStream(to));
                    byte[] buffer = new byte[1024];
                    int lengthRead;
                    while ((lengthRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, lengthRead);
                        out.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    in.close();
                    out.close();

                    //Should never be triggered
                    int i = 0;
                    while (!to.exists() && (i < 10*20)) {
                        Thread.sleep(50);
                        i++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return to.exists();
            }
        }

        return false;
    }

    public static boolean moveFile(File from, File to) throws IOException, InterruptedException {

        if (!from.getAbsolutePath().replace("\\", "/").equals(to.getAbsolutePath().replace("\\", "/"))) {
            if (from.exists() && from.isFile()) {
                if (from.renameTo(to)) {
                    int i = 0;
                    //Should never be triggered
                    while (!to.exists() && (i < 10*20)) {
                        Thread.sleep(50);
                        i++;
                    }
                    return true;
                } else if (copyFile(from, to)) {
                    if (from.delete()) {
                        return true;
                    } else {
                        if (from.exists() && to.exists()) {
                            to.delete();
                            return false;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static void compressToZip(String pathToCompare, String zipFile) {
        byte[] buffer = new byte[1024];
        String source = new File(pathToCompare).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            for (String file: getFiles(pathToCompare)) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    FileInputStream in = new FileInputStream(file);
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            zos.closeEntry();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void compressToZip(List<String> filePathsToCompare, String zipFile) {
        byte[] buffer = new byte[1024];

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            for (String file: filePathsToCompare) {
                ZipEntry ze = new ZipEntry(getNameWithoutExtension(zipFile) + "/" + file);
                zos.putNextEntry(ze);
                try {
                    FileInputStream in = new FileInputStream(file);
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            zos.closeEntry();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unpackZip(String zipPath, String outputDir) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(outputDir,  entry.getName());
            if (entry.isDirectory()) {
                entryDestination.mkdirs();
            } else {
                entryDestination.getParentFile().mkdirs();
                InputStream in = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(entryDestination);
                copy(in, out);
            }
        }
        try {
            zipFile.close();
        } catch (Exception e) {}
    }
    public static List<PairRecord<String,File>> loadFiles(@NotNull File parentDir, boolean recursive) {
        File[] files = parentDir.listFiles();
        if(files==null || files.length==0) return new ArrayList<>();
        List<PairRecord<String,File>> result = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory() && recursive) {
                result.addAll(loadFiles(file,true));
                continue;
            }
            String fileName = file.getName().split("\\.")[0];
            result.add(new PairRecord<>(fileName,file));
        }
        return result;
    }


    public static Set<String> getAllPathsInResourceFolder(ConfigOwner configOwner,
                                                          String dir){
        Set<String> files = new LinkedHashSet<>();

        try {
            URI uri = configOwner.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path;
            // If the code is packaged in a jar, the URI scheme will be "jar"
            if ("jar".equalsIgnoreCase(uri.getScheme())) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    path = fs.getPath(dir);
                    try (Stream<Path> streamFiles = java.nio.file.Files.walk(path)) {
                        files = streamFiles
                                .filter(Objects::nonNull)
                                .map(Path::toString)
                                .sorted((o1, o2) -> {
                                    if (o1.contains(".") && !o2.contains(".")) {
                                        return 1;
                                    }
                                    if (!o1.contains(".") && o2.contains(".")) {
                                        return -1;
                                    }
                                    return 0;
                                })
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                    }
                }
            } else {
                // When running from a directory (non-jar), use the default file system.
                path = Paths.get(uri).resolve(dir);
                try (Stream<Path> streamFiles = java.nio.file.Files.walk(path)) {
                    files = streamFiles
                            .filter(Objects::nonNull)
                            .map(Path::toString)
                            .sorted((o1, o2) -> {
                                if (o1.contains(".") && !o2.contains(".")) {
                                    return 1;
                                }
                                if (!o1.contains(".") && o2.contains(".")) {
                                    return -1;
                                }
                                return 0;
                            })
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                }
            }
        } catch (Exception ex) {
            configOwner.logError("An error occurred while trying to load file ",ex);
        }

        return files;
    }


    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
            output.write(buffer, 0, n);
        }

        return count;
    }
}
