package dev.tildejustin.accesswidenerloader;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.*;
import net.fabricmc.loader.impl.lib.accesswidener.*;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("DataFlowIssue")
public class AccessWidenerLoader {
    static Path awFolder = FabricLoader.getInstance().getConfigDir().resolve("accesswideners");
    static List<Path> accessWideners;
    static FabricLauncher launcher = FabricLauncherBase.getLauncher();
    static String currNamespace = launcher.getTargetNamespace();
    static AccessWidenerReader awReader = new AccessWidenerReader(FabricLoaderImpl.INSTANCE.getAccessWidener());

    static {
        try {
            if (!Files.exists(awFolder)) Files.createDirectory(awFolder);
            accessWideners = Arrays.stream(awFolder.toFile().listFiles()).map(File::toPath).filter(path -> path.toString().endsWith("accesswidener")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String readAwNamespace(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        String[] header = headerLine.split("\\s+");
        return header[2];
    }

    public void load() throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(new InputStreamReader(FabricLoader.class.getClassLoader().getResource("mappings/mappings.tiny").openStream()), tree);
        accessWideners.forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String awNamespace = AccessWidenerLoader.readAwNamespace(reader);

                        byte[] data = Files.readAllBytes(path);
                        if (!awNamespace.equals(currNamespace))
                            data = this.remap(data, tree, awNamespace, currNamespace);

                        awReader.read(data, currNamespace);
                    } catch (IOException ignored) {
                    }
                }
        );
    }

    private byte[] remap(byte[] data, MappingTree tree, String from, String to) throws IOException {
        AccessWidenerWriter awWriter = new AccessWidenerWriter();
        AccessWidenerTinyRemapper awRemapper = new AccessWidenerTinyRemapper(awWriter, tree, from, to);
        AccessWidenerReader accessWidenerReader = new AccessWidenerReader(awRemapper);
        accessWidenerReader.read(data, from);
        byte[] result = awWriter.write();
        Files.write(awFolder.resolve("debug.accesswidener.disabled"), result);
        return result;
    }
}
