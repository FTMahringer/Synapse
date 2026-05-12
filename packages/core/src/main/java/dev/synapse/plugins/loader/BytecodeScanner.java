package dev.synapse.plugins.loader;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * ASM bytecode scanner for plugin JARs.
 *
 * <p>Walks all classes in a JAR at install time and rejects forbidden references.
 * Checks for access to:
 * <ul>
 *   <li>{@code sun.*} / {@code com.sun.*} internal packages</li>
 *   <li>Spring framework internals</li>
 *   <li>JPA / Hibernate classes</li>
 *   <li>Redis / Lettuce classes</li>
 *   <li>PostgreSQL driver internals</li>
 *   <li>Core SYNAPSE classes (outside plugin API)</li>
 * </ul>
 */
public class BytecodeScanner {

    /** Packages that plugins are forbidden from referencing. */
    private static final Set<String> FORBIDDEN_PACKAGES = Set.of(
        "sun.",
        "com.sun.",
        "org.springframework.",
        "org.hibernate.",
        "jakarta.persistence.",
        "org.postgresql.",
        "io.lettuce.",
        "redis.clients.",
        "org.apache.catalina.",
        "org.flywaydb.",
        "dev.synapse.core.",
        "dev.synapse.agents.",
        "dev.synapse.conversation.",
        "dev.synapse.tasks.",
        "dev.synapse.users.",
        "dev.synapse.providers.",
        "dev.synapse.tools."
    );

    /** Packages that are allowed (plugin API + standard JDK). */
    private static final Set<String> ALLOWED_PACKAGES = Set.of(
        "dev.synapse.plugin.api.",
        "java.",
        "javax.",
        "jdk.",
        "org.w3c.",
        "org.xml.",
        "org.slf4j."
    );

    /**
     * Scans a plugin JAR for forbidden references.
     *
     * @param jarPath path to the plugin JAR
     * @return scan result with violations (empty if clean)
     * @throws IOException if the JAR cannot be read
     */
    public ScanResult scan(Path jarPath) throws IOException {
        List<Violation> violations = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                try (InputStream is = jarFile.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(is);
                    ClassVisitor visitor = new ForbiddenReferenceVisitor(
                        violations,
                        entry.getName()
                    );
                    reader.accept(visitor, ClassReader.SKIP_DEBUG);
                }
            }
        }

        return new ScanResult(violations.isEmpty(), violations);
    }

    /**
     * Result of a bytecode scan.
     */
    public record ScanResult(boolean clean, List<Violation> violations) {
        public List<String> getViolationMessages() {
            return violations.stream()
                .map(Violation::toString)
                .toList();
        }
    }

    /**
     * Individual violation record.
     */
    public record Violation(
        String classFile,
        String reference,
        ViolationType type
    ) {
        @Override
        public String toString() {
            return classFile + ": " + type + " " + reference;
        }
    }

    public enum ViolationType {
        CLASS_REFERENCE,
        METHOD_REFERENCE,
        FIELD_REFERENCE
    }

    private static class ForbiddenReferenceVisitor extends ClassVisitor {

        private final List<Violation> violations;
        private final String classFile;

        ForbiddenReferenceVisitor(List<Violation> violations, String classFile) {
            super(Opcodes.ASM9);
            this.violations = violations;
            this.classFile = classFile;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            if (superName != null) {
                checkReference(superName, ViolationType.CLASS_REFERENCE);
            }
            if (interfaces != null) {
                for (String iface : interfaces) {
                    checkReference(iface, ViolationType.CLASS_REFERENCE);
                }
            }
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                                       String signature, Object value) {
            checkDescriptor(descriptor, ViolationType.FIELD_REFERENCE);
            return new ForbiddenFieldVisitor(violations, classFile);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            checkDescriptor(descriptor, ViolationType.METHOD_REFERENCE);
            if (exceptions != null) {
                for (String ex : exceptions) {
                    checkReference(ex, ViolationType.CLASS_REFERENCE);
                }
            }
            return new ForbiddenMethodVisitor(violations, classFile);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            checkDescriptor(descriptor, ViolationType.CLASS_REFERENCE);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        private void checkReference(String internalName, ViolationType type) {
            String className = internalName.replace('/', '.');
            if (isForbidden(className)) {
                violations.add(new Violation(classFile, className, type));
            }
        }

        private void checkDescriptor(String descriptor, ViolationType type) {
            // Extract class references from descriptor (L...;)
            int idx = 0;
            while ((idx = descriptor.indexOf('L', idx)) != -1) {
                int end = descriptor.indexOf(';', idx);
                if (end == -1) break;
                String internalName = descriptor.substring(idx + 1, end);
                checkReference(internalName, type);
                idx = end + 1;
            }
        }

        private boolean isForbidden(String className) {
            // Check allowed first
            for (String allowed : ALLOWED_PACKAGES) {
                if (className.startsWith(allowed)) {
                    return false;
                }
            }
            // Check forbidden
            for (String forbidden : FORBIDDEN_PACKAGES) {
                if (className.startsWith(forbidden)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ForbiddenMethodVisitor extends MethodVisitor {
        private final List<Violation> violations;
        private final String classFile;

        ForbiddenMethodVisitor(List<Violation> violations, String classFile) {
            super(Opcodes.ASM9);
            this.violations = violations;
            this.classFile = classFile;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
            checkReference(owner);
            checkDescriptor(descriptor);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name,
                                   String descriptor) {
            checkReference(owner);
            checkDescriptor(descriptor);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            checkReference(type);
        }

        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof Type type) {
                String className = type.getClassName();
                if (className != null) {
                    checkReference(className.replace('.', '/'));
                }
            }
        }

        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            checkDescriptor(descriptor);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            checkDescriptor(descriptor);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor,
                                                           boolean visible) {
            checkDescriptor(descriptor);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath,
                                                      String descriptor, boolean visible) {
            checkDescriptor(descriptor);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath,
                                                               Label[] start, Label[] end,
                                                               int[] index, String descriptor,
                                                               boolean visible) {
            checkDescriptor(descriptor);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath,
                                                          String descriptor, boolean visible) {
            checkDescriptor(descriptor);
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        private void checkReference(String internalName) {
            String className = internalName.replace('/', '.');
            if (isForbidden(className)) {
                violations.add(new Violation(classFile, className,
                    ViolationType.METHOD_REFERENCE));
            }
        }

        private void checkDescriptor(String descriptor) {
            int idx = 0;
            while ((idx = descriptor.indexOf('L', idx)) != -1) {
                int end = descriptor.indexOf(';', idx);
                if (end == -1) break;
                String internalName = descriptor.substring(idx + 1, end);
                checkReference(internalName);
                idx = end + 1;
            }
        }

        private boolean isForbidden(String className) {
            for (String allowed : ALLOWED_PACKAGES) {
                if (className.startsWith(allowed)) {
                    return false;
                }
            }
            for (String forbidden : FORBIDDEN_PACKAGES) {
                if (className.startsWith(forbidden)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ForbiddenFieldVisitor extends FieldVisitor {
        private final List<Violation> violations;
        private final String classFile;

        ForbiddenFieldVisitor(List<Violation> violations, String classFile) {
            super(Opcodes.ASM9);
            this.violations = violations;
            this.classFile = classFile;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            int idx = 0;
            while ((idx = descriptor.indexOf('L', idx)) != -1) {
                int end = descriptor.indexOf(';', idx);
                if (end == -1) break;
                String internalName = descriptor.substring(idx + 1, end);
                String className = internalName.replace('/', '.');
                if (isForbidden(className)) {
                    violations.add(new Violation(classFile, className,
                        ViolationType.FIELD_REFERENCE));
                }
                idx = end + 1;
            }
            return new ForbiddenAnnotationVisitor(violations, classFile);
        }

        private boolean isForbidden(String className) {
            for (String allowed : ALLOWED_PACKAGES) {
                if (className.startsWith(allowed)) {
                    return false;
                }
            }
            for (String forbidden : FORBIDDEN_PACKAGES) {
                if (className.startsWith(forbidden)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ForbiddenAnnotationVisitor extends AnnotationVisitor {
        private final List<Violation> violations;
        private final String classFile;

        ForbiddenAnnotationVisitor(List<Violation> violations, String classFile) {
            super(Opcodes.ASM9);
            this.violations = violations;
            this.classFile = classFile;
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof Type type) {
                String className = type.getClassName();
                if (className != null && isForbidden(className)) {
                    violations.add(new Violation(classFile, className,
                        ViolationType.CLASS_REFERENCE));
                }
            }
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            int idx = descriptor.indexOf('L');
            if (idx != -1) {
                int end = descriptor.indexOf(';', idx);
                if (end != -1) {
                    String internalName = descriptor.substring(idx + 1, end);
                    String className = internalName.replace('/', '.');
                    if (isForbidden(className)) {
                        violations.add(new Violation(classFile, className,
                            ViolationType.CLASS_REFERENCE));
                    }
                }
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            int idx = descriptor.indexOf('L');
            if (idx != -1) {
                int end = descriptor.indexOf(';', idx);
                if (end != -1) {
                    String internalName = descriptor.substring(idx + 1, end);
                    String className = internalName.replace('/', '.');
                    if (isForbidden(className)) {
                        violations.add(new Violation(classFile, className,
                            ViolationType.CLASS_REFERENCE));
                    }
                }
            }
            return this;
        }

        private boolean isForbidden(String className) {
            for (String allowed : ALLOWED_PACKAGES) {
                if (className.startsWith(allowed)) {
                    return false;
                }
            }
            for (String forbidden : FORBIDDEN_PACKAGES) {
                if (className.startsWith(forbidden)) {
                    return true;
                }
            }
            return false;
        }
    }
}
