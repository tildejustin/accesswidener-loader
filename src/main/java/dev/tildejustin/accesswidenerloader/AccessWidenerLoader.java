package dev.tildejustin.accesswidenerloader;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.*;
import net.fabricmc.loader.impl.lib.accesswidener.*;
import net.fabricmc.loader.impl.lib.tinyremapper.TinyRemapper;
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper;
import org.objectweb.asm.commons.Remapper;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AccessWidenerLoader {
    static Path awFolder = FabricLoader.getInstance().getConfigDir().resolve("accesswidener");
    static List<Path> accessWideners;
    static FabricLauncher launcher = FabricLauncherBase.getLauncher();
    static TinyRemapper remapper = TinyRemapper.newRemapper()
            .withMappings(TinyRemapperMappingsHelper.create(launcher.getMappingConfiguration().getMappings(), "named", "intermediary"))
            .renameInvalidLocals(false)
            .build();
    static String currNamespace = launcher.getTargetNamespace();
    static AccessWidener aw = new AccessWidener();
    static AccessWidenerReader awReader = new AccessWidenerReader(aw);

    static {
        try {
            if (!Files.exists(awFolder)) Files.createDirectory(awFolder);
            accessWideners = Arrays.stream(awFolder.toFile().listFiles()).map(File::toPath).filter(path -> path.endsWith(".accesswidener")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String readAwNamespace(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        String[] header = headerLine.split("\\s+");
        return header[2];
    }

    public void load() {
        accessWideners.forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String awNamespace = AccessWidenerLoader.readAwNamespace(reader);

                        AtomicReference<byte[]> data = new AtomicReference<>(Files.readAllBytes(path));
                        if (!awNamespace.equals(currNamespace)) {
                            Optional.ofNullable(this.remap(data.get(), remapper.getRemapper(), awNamespace, currNamespace)).ifPresent(data::set);
                        }

                        awReader.read(data.get(), currNamespace);
                        new AccessWidenerReader(new AccessWidenerVisitor() {
                            @Override
                            public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
                                try {
                                    Class<?> clazz = Class.forName(name, false, FabricLoader.getInstance().getClass().getClassLoader());
                                    for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                                        ctor.setAccessible(true);

                                    }
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
                                Class<?> clazz = Class.forName()
                            }

                            @Override
                            public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
                                AccessWidenerVisitor.super.visitField(owner, name, descriptor, access, transitive);
                            }
                        })
                    } catch (IOException ignored) {
                    }
                    aw.getTargets().forEach(s -> {

                    });
                }
        );
    }

    private byte[] remap(byte[] data, Remapper remapper, String from, String to) throws IOException {
        System.out.printf("from: %s, to: %s\n", from, to);
        AccessWidenerWriter awWriter = new AccessWidenerWriter();
        AccessWidenerRemapper awRemapper = new AccessWidenerRemapper(awWriter, remapper, from, to);
        AccessWidenerReader accessWidenerReader = new AccessWidenerReader(awRemapper);
        accessWidenerReader.read(data, from);
        byte[] result = awWriter.write();
        Files.write(awFolder.resolve("debug.accesswidener"), result);
        return result;
    }
}
