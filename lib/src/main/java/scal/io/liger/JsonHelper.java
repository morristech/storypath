package scal.io.liger;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 7/14/14.
 */
public class JsonHelper {
    private static final String TAG = "JsonHelper";
    private static final String LIGER_DIR = "Liger";
    private static File selectedJSONFile = null;
    private static String selectedJSONPath = null;
    private static ArrayList<File> jsonFileList = null;
    private static ArrayList<String> jsonPathList = null;
    private static String sdLigerFilePath = null;

    //private static String language = null; // TEMP

    public static String loadJSONFromPath(String jsonPath, String language) {

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonPath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonPath.lastIndexOf("-" + language + jsonPath.substring(jsonPath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonPath.substring(0, jsonPath.lastIndexOf(".")) + "-" + language + jsonPath.substring(jsonPath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSONFromPath() - LOCALIZED PATH: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File jsonFile = new File(jsonPath);
                InputStream jsonStream = new FileInputStream(jsonFile);

                File localizedFile = new File(localizedFilePath);
                // if there is a file at the localized path, use that instead
                if ((localizedFile.exists()) && (!jsonPath.equals(localizedFilePath))) {
                    Log.d("LANGUAGE", "loadJSONFromPath() - USING LOCALIZED FILE: " + localizedFilePath);
                    jsonStream = new FileInputStream(localizedFile);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException e) {
                Log.e(TAG, "READING JSON FILE FROM SD CARD FAILED: " + e.getMessage());
            }
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return jsonString;
    }

    public static String loadJSON(String language) {
        return loadJSON(selectedJSONFile, language);
    }

    public static String loadJSON(File file, String language) {
        if(null == file) {
            return null;
        }

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = file.getPath();

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (file.getPath().lastIndexOf("-" + language + file.getPath().substring(file.getPath().lastIndexOf("."))) < 0) {
                localizedFilePath = file.getPath().substring(0, file.getPath().lastIndexOf(".")) + "-" + language + file.getPath().substring(file.getPath().lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSON() - LOCALIZED PATH: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(file);

                File localizedFile = new File(localizedFilePath);
                // if there is a file at the localized path, use that instead
                if ((localizedFile.exists()) && (!file.getPath().equals(localizedFilePath))) {
                    Log.d("LANGUAGE", "loadJSON() - USING LOCALIZED FILE: " + localizedFilePath);
                    jsonStream = new FileInputStream(localizedFile);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException e) {
                Log.e(TAG, "READING JSON FILE FRON SD CARD FAILED: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "SD CARD NOT FOUND");
        }

        return jsonString;
    }

    // NEW

    public static String loadJSONFromZip(Context context, String language) {
        return loadJSONFromZip(selectedJSONPath, context, language);
    }

    public static String loadJSONFromZip(String jsonFilePath, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadJSONFromZip CALLED FOR " + jsonFilePath);

        if(null == jsonFilePath) {
            return null;
        }

        String jsonString = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSONFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!jsonFilePath.equals(localizedFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadJSONFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
            }

        return jsonString;
    }

    public static String getSdLigerFilePath() {
        return sdLigerFilePath;
    }

    private static void copyFilesToSdCard(Context context, String basePath) {
        copyFileOrDir(context, basePath, ""); // copy all files in assets folder in my project
    }

    private static void copyFileOrDir(Context context, String basePath, String path) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            Log.i("tag", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(context, basePath, path);
            } else {
                String fullPath =  basePath + path;
                Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir(context, basePath, p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private static void copyFile(Context context, String basePath, String filename) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName = basePath + filename.substring(0, filename.length()-4);
            else
                newFileName = basePath + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of "+newFileName);
            Log.e("tag", "Exception in copyFile() "+e.toString());
        }

    }

    public static void setupFileStructure(Context context) {
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            String sdCardFolderPath = Environment.getExternalStorageDirectory().getPath();
            sdLigerFilePath = sdCardFolderPath + File.separator + LIGER_DIR + File.separator;
            // based on http://stackoverflow.com/questions/4447477/android-how-to-copy-files-from-assets-folder-to-sdcard/8366081#8366081
            copyFilesToSdCard(context, sdLigerFilePath);
        } else {
            Log.e(TAG, "SD CARD NOT FOUND");
        }
    }

    public static String[] getJSONFileList() {
        //ensure path has been set
        if(null == sdLigerFilePath) {
            return null;
        }

        ArrayList<String> jsonFileNamesList = new ArrayList<String>();
        jsonFileList = new ArrayList<File>();
        jsonPathList = new ArrayList<String>();

        // HARD CODING LIST

        File ligerFile_1 = new File(sdLigerFilePath + "/default/default_library/default_library.json");
        File ligerFile_2 = new File(sdLigerFilePath + "/default/learning_guide_TEST.json");
        File ligerFile_3 = new File(sdLigerFilePath + "/default/LIB_1/LIB_1_TEST.json");
        File ligerFile_4 = new File(sdLigerFilePath + "/default/LIB_2/LIB_2_TEST.json");
        File ligerFile_5 = new File(sdLigerFilePath + "/default/learning_guide_library.json");
        File ligerFile_6 = new File(sdLigerFilePath + "/default/learning_guide_library_SAVE.json");

        jsonFileNamesList.add(ligerFile_1.getName());
        jsonFileNamesList.add(ligerFile_2.getName());
        jsonFileNamesList.add(ligerFile_3.getName());
        jsonFileNamesList.add(ligerFile_4.getName());
        jsonFileNamesList.add(ligerFile_5.getName());
        jsonFileNamesList.add(ligerFile_6.getName());

        jsonFileList.add(ligerFile_1);
        jsonFileList.add(ligerFile_2);
        jsonFileList.add(ligerFile_3);
        jsonFileList.add(ligerFile_4);
        jsonFileList.add(ligerFile_5);
        jsonFileList.add(ligerFile_6);

        jsonPathList.add("default/default_library/default_library.json");
        jsonPathList.add("default/learning_guide_TEST.json");
        jsonPathList.add("default/LIB_1/LIB_1_TEST.json");
        jsonPathList.add("default/LIB_2/LIB_2_TEST.json");
        jsonPathList.add("default/learning_guide_library.json");
        jsonPathList.add("default/learning_guide_library_SAVE.json");

        /*
        File ligerDir = new File(sdLigerFilePath);
        if (ligerDir != null) {
            for (File file : ligerDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    jsonFileNamesList.add(file.getName());
                    jsonFileList.add(file);
                }
            }
        }

        File defaultLigerDir = new File(sdLigerFilePath + "/default/");
        if (defaultLigerDir != null) {
            for (File file : defaultLigerDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    jsonFileNamesList.add(file.getName());
                    jsonFileList.add(file);
                }
            }
        }
        */

        return jsonFileNamesList.toArray(new String[jsonFileNamesList.size()]);
    }

    public static File setSelectedJSONFile(int index) {
        selectedJSONFile = jsonFileList.get(index);
        return selectedJSONFile;
    }

    public static String setSelectedJSONPath(int index) {
        selectedJSONPath = jsonPathList.get(index);
        return selectedJSONPath;
    }

    private static void addFileToSDCard(InputStream jsonInputStream, String filePath) {
        OutputStream outputStream = null;

        try {
            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(new File(sdLigerFilePath + filePath));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = jsonInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jsonInputStream != null) {
                try {
                    jsonInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static StoryPathLibrary loadStoryPathLibrary(String jsonFilePath, Context context, String language) {

        //Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathLibrary CALLED FOR " + jsonFilePath);

        String storyPathLibraryJson = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathLibrary() - LOCALIZED PATH: " + localizedFilePath);
        }

        File f = new File(localizedFilePath);
        if ((!f.exists()) && (!localizedFilePath.equals(jsonFilePath))) {
            f = new File(jsonFilePath);
        } else {
            Log.d("LANGUAGE", "loadStoryPathLibrary() - USING LOCALIZED FILE: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(f);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathLibraryJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        return deserializeStoryPathLibrary(storyPathLibraryJson, f.getPath(), context);

    }

    // NEW

    public static StoryPathLibrary loadStoryPathLibraryFromZip(String jsonFilePath, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathLibraryFromZip CALLED FOR " + jsonFilePath);

        String storyPathLibraryJson = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathLibraryFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!localizedFilePath.equals(jsonFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadStoryPathLibraryFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathLibraryJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
                return null;
            }

        return deserializeStoryPathLibrary(storyPathLibraryJson, jsonFilePath, context);
    }

    public static StoryPathLibrary deserializeStoryPathLibrary(String storyPathLibraryJson, String jsonFilePath, Context context) {

        //Log.d(" *** TESTING *** ", "NEW METHOD deserializeStoryPathLibrary CALLED FOR " + jsonFilePath);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathLibrary.class, new StoryPathLibraryDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        StoryPathLibrary storyPathLibrary = gson.fromJson(storyPathLibraryJson, StoryPathLibrary.class);

        // a story path library model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPathLibrary.getFileLocation() == null) || (storyPathLibrary.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path library " + storyPathLibrary.getId() + " could not be determined");
                return null;
            }
        } else {
            storyPathLibrary.setFileLocation(jsonFilePath);
        }

        storyPathLibrary.setCardReferences();
        storyPathLibrary.initializeObservers();
        storyPathLibrary.setContext(context);

        return storyPathLibrary;
    }

    public static String saveStoryPathLibrary(StoryPathLibrary storyPathLibrary) {

        //Log.d(" *** TESTING *** ", "NEW METHOD saveStoryPathLibrary CALLED FOR " + storyPathLibrary.getId());

        Date timeStamp = new Date();
        String jsonFilePath = storyPathLibrary.buildPath(storyPathLibrary.getId() + "_" + timeStamp.getTime() + ".json");

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathLibraryFile = new File(jsonFilePath);
                FileOutputStream storyPathLibraryStream = new FileOutputStream(storyPathLibraryFile);
                if (!storyPathLibraryFile.exists()) {
                    storyPathLibraryFile.createNewFile();
                }

                String storyPathLibraryJson = serializeStoryPathLibrary(storyPathLibrary);

                byte storyPathLibraryData[] = storyPathLibraryJson.getBytes();
                storyPathLibraryStream.write(storyPathLibraryData);
                storyPathLibraryStream.flush();
                storyPathLibraryStream.close();
            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        // update file location
        storyPathLibrary.setFileLocation(jsonFilePath);

        return jsonFilePath;
    }

    public static String serializeStoryPathLibrary(StoryPathLibrary storyPathLibrary) {

        //Log.d(" *** TESTING *** ", "NEW METHOD serializeStoryPathLibrary CALLED FOR " + storyPathLibrary.getId());

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        String storyPathLibraryJson = gson.toJson(storyPathLibrary);

        return storyPathLibraryJson;
    }

    public static StoryPath loadStoryPath(String jsonFilePath, StoryPathLibrary storyPathLibrary, Context context, String language) {

        //Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPath CALLED FOR " + jsonFilePath);

        String storyPathJson = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPath() - LOCALIZED PATH: " + localizedFilePath);
        }

        File f = new File(localizedFilePath);
        if ((!f.exists()) && (!localizedFilePath.equals(jsonFilePath))) {
            f = new File(jsonFilePath);
        } else {
            Log.d("LANGUAGE", "loadStoryPath() - USING LOCALIZED FILE: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(f);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        return deserializeStoryPath(storyPathJson, f.getPath(), storyPathLibrary, context);
    }

    // NEW

    public static StoryPath loadStoryPathFromZip(String jsonFilePath, StoryPathLibrary storyPathLibrary, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathFromZip CALLED FOR " + jsonFilePath);

        String storyPathJson = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!localizedFilePath.equals(jsonFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadStoryPathFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
                return null;
            }

        return deserializeStoryPath(storyPathJson, jsonFilePath, storyPathLibrary, context);
    }

    public static StoryPath deserializeStoryPath(String storyPathJson, String jsonFilePath, StoryPathLibrary storyPathLibrary, Context context) {

        //Log.d(" *** TESTING *** ", "NEW METHOD deserializeStoryPath CALLED FOR " + jsonFilePath);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        StoryPath storyPath = gson.fromJson(storyPathJson, StoryPath.class);

        // a story path model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPath.getFileLocation() == null) || (storyPath.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path " + storyPath.getId() + " could not be determined");
            }
        } else {
            storyPath.setFileLocation(jsonFilePath);
        }

        storyPath.setCardReferences();
        storyPath.initializeObservers();
        storyPath.setStoryPathLibrary(storyPathLibrary);
        // THIS MAY HAVE UNINTENDED CONSEQUENCES...
        if (storyPath.getStoryPathLibraryFile() == null) {
            storyPath.setStoryPathLibraryFile(storyPathLibrary.getFileLocation());

        }

        storyPath.setContext(context);

        return storyPath;
    }

    public static String saveStoryPath(StoryPath storyPath) {

        //Log.d(" *** TESTING *** ", "NEW METHOD saveStoryPath CALLED FOR " + storyPath.getId());

        Date timeStamp = new Date();
        String jsonFilePath = storyPath.buildPath(storyPath.getId() + "_" + timeStamp.getTime() + ".json");

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathFile = new File(jsonFilePath);
                FileOutputStream storyPathStream = new FileOutputStream(storyPathFile);
                if (!storyPathFile.exists()) {
                    storyPathFile.createNewFile();
                }

                String storyPathJson = serializeStoryPath(storyPath);

                byte storyPathData[] = storyPathJson.getBytes();
                storyPathStream.write(storyPathData);
                storyPathStream.flush();
                storyPathStream.close();
            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        // update file location
        storyPath.setFileLocation(jsonFilePath);

        return jsonFilePath;
    }

    public static String serializeStoryPath(StoryPath storyPath) {

        //Log.d(" *** TESTING *** ", "NEW METHOD serializeStoryPath CALLED FOR " + storyPath.getId());

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        // set aside references to prevent circular dependencies when serializing
        Context tempContext = storyPath.getContext();

        StoryPathLibrary tempStoryPathLibrary = storyPath.getStoryPathLibrary();
        storyPath.setContext(null);
        storyPath.setStoryPathLibrary(null);
        storyPath.clearObservers();
        storyPath.clearCardReferences();

        String storyPathJson = gson.toJson(storyPath);

        // restore references
        storyPath.setCardReferences();
        storyPath.initializeObservers();
        storyPath.setStoryPathLibrary(tempStoryPathLibrary);
        storyPath.setContext(tempContext);

        return storyPathJson;
    }

}
