package zzik2.hytale.zkoreanpatch;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ZKoreanPatch extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static String VERSION = "0.0.4";
    public static final String FONT_NAME = "Mulmaru";

    public ZKoreanPatch(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Version " + VERSION + " loaded.");

        // C:\Users\<username>\AppData\Roaming\Hytale\UserData\Saves\<worldname>\mods\<modname>
        Path path = this.getDataDirectory().toAbsolutePath();
        File modFile = this.getFile().toFile();

        Path hytalePath = path;
        while (hytalePath != null && !hytalePath.getFileName().toString().equalsIgnoreCase("Hytale")) {
            hytalePath = hytalePath.getParent();
        }

        if (hytalePath == null) {
            LOGGER.atSevere().log("Could not find Hytale directory from path: " + path);
            return;
        }

        Path gamePath = hytalePath.resolve("install/release/package/game/latest");

        try {
            Path langPath = gamePath.resolve("Client/Data/Shared/Language/ko-KR");
            Path propertiesPath = gamePath.resolve("Client/Data/ZKOREANPATCH.properties");

            if (!Files.exists(gamePath)) {
                LOGGER.atSevere().log("Game path does not exist: " + gamePath);
                return;
            }

            if (Files.exists(propertiesPath)) {
                Properties props = new Properties();
                try (InputStream is = Files.newInputStream(propertiesPath)) {
                    props.load(is);
                    String installedVersion = props.getProperty("version", "0.0.0");
                    String currentVersion = VERSION;

                    if (compareVersion(installedVersion, currentVersion) >= 0) {
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.atWarning().log("Failed to check version from properties file, proceeding with installation.");
                }
            }

            if (!Files.exists(langPath)) {
                Files.createDirectories(langPath);
            }

            try (JarFile jar = new JarFile(modFile)) {
                JarEntry propEntry = jar.getJarEntry("ZKOREANPATCH.properties");
                if (propEntry != null) {
                    copyEntry(jar, propEntry, propertiesPath);
                }

                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith("assets/hytale/zkoreanpatch/") && !entry.isDirectory()) {
                        String subPath = name.substring("assets/hytale/zkoreanpatch/".length());

                        if (subPath.startsWith("Client/") || subPath.startsWith("Server/") || subPath.startsWith("Common/")) {
                            Path dest = gamePath.resolve(subPath);
                            copyEntry(jar, entry, dest);

                            if ((subPath.startsWith("Server/") || subPath.startsWith("Common/")) && name.endsWith(".lang")) {
                                Path langDest = langPath.resolve(new File(name).getName());
                                copyEntry(jar, entry, langDest);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to setup ZKoreanPatch files");
            e.printStackTrace();
        }

        replaceFonts(gamePath);
    }

    private void replaceFonts(Path gamePath) {
        try {
            Path fontsPath = gamePath.resolve("Client/Data/Shared/Fonts");
            String[] targets = {
                    "Lexend-Bold",
                    "NotoMono-Regular",
                    "NunitoSans-ExtraBold",
                    "NunitoSans-Medium"
            };

            Path sourceJson = fontsPath.resolve(FONT_NAME + ".json");
            Path sourcePng = fontsPath.resolve(FONT_NAME + ".png");

            if (!Files.exists(sourceJson) || !Files.exists(sourcePng)) {
                LOGGER.atWarning().log(FONT_NAME + " font files not found, skipping font replacement.");
                return;
            }

            for (String target : targets) {
                Path targetJson = fontsPath.resolve(target + ".json");
                Path targetPng = fontsPath.resolve(target + ".png");

                Files.copy(sourceJson, targetJson, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(sourcePng, targetPng, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to replace fonts");
            e.printStackTrace();
        }
    }

    private int compareVersion(String v1, String v2) {
        String[] p1 = v1.split("\\.");
        String[] p2 = v2.split("\\.");
        int length = Math.max(p1.length, p2.length);

        for (int i = 0; i < length; i++) {
            int n1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
            int n2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;

            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }
        return 0;
    }

    private void copyEntry(JarFile jar, JarEntry entry, Path dest) throws IOException {
        if (dest.getParent() != null) {
            Files.createDirectories(dest.getParent());
        }
        try (InputStream is = jar.getInputStream(entry)) {
            Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    protected void start() {
    }
}
