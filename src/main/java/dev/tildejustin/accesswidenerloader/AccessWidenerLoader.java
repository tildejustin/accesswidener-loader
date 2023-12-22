package dev.tildejustin.accesswidenerloader;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.*;
import net.fabricmc.loader.impl.lib.accesswidener.*;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;

public class AccessWidenerLoader {
    static Path awFolder = FabricLoader.getInstance().getConfigDir().resolve("accesswideners");
    static List<Path> accessWideners;
    static FabricLauncher launcher = FabricLauncherBase.getLauncher();
    static String currNamespace = launcher.getTargetNamespace();
    static AccessWidenerReader awReader = new AccessWidenerReader(FabricLoaderImpl.INSTANCE.getAccessWidener());

    static {
        try {
            if (!Files.exists(awFolder)) Files.createDirectory(awFolder);
            try (Stream<Path> children = Files.list(awFolder)) {
                accessWideners = children.filter(path -> path.toString().endsWith(".accesswidener")).collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // I don't think this is very safe
    static String readAwNamespace(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        String[] header = headerLine.split("\\s+");
        return header[2];
    }

    // if you don't have a mappings set you'll have bigger problems than wondering why the accesswidener loader isn't working
    @SuppressWarnings("DataFlowIssue")
    public void load() throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(new InputStreamReader(FabricLoader.class.getClassLoader().getResource("mappings/mappings.tiny").openStream()), tree);
        accessWideners.forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String awNamespace = AccessWidenerLoader.readAwNamespace(reader);

                        byte[] data = Files.readAllBytes(path);
                        if (!awNamespace.equals(currNamespace))
                            data = this.remapAw(data, tree, awNamespace, currNamespace);

                        awReader.read(data, currNamespace);
                    } catch (IOException ignored) {
                    }
                }
        );
    }

    private byte[] remapAw(byte[] data, MappingTree tree, String from, String to) throws IOException {
        AccessWidenerWriter awWriter = new AccessWidenerWriter();
        AccessWidenerTinyRemapper awRemapper = new AccessWidenerTinyRemapper(awWriter, tree, from, to);
        AccessWidenerReader accessWidenerReader = new AccessWidenerReader(awRemapper);
        accessWidenerReader.read(data, from);
        return awWriter.write();
    }
}
