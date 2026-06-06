package com.assessment.service;

import com.assessment.model.Challenge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds a runnable, multi-file Maven project for a candidate submission:
 *   1. reads the pre-built template under {@code challenge.tests-dir}/{templateDir},
 *   2. replaces the CANDIDATE_SUBMISSION.java placeholder with the candidate's code
 *      (naming the file after the public type when present so javac is happy),
 *   3. adds the {@code compile}/{@code run} scripts the Judge0 multi-file language needs,
 *   4. zips and base64-encodes the whole project.
 */
@Service
public class CodeInjectionService {

    private static final Logger log = LoggerFactory.getLogger(CodeInjectionService.class);

    private static final String PLACEHOLDER_REL = "src/main/java/com/assessment/CANDIDATE_SUBMISSION.java";
    private static final String CANDIDATE_PKG_DIR = "src/main/java/com/assessment/";

    private static final Pattern PUBLIC_TYPE =
            Pattern.compile("public\\s+(?:final\\s+|abstract\\s+)?(?:class|interface|enum|record)\\s+(\\w+)");
    private static final Pattern PACKAGE_DECL =
            Pattern.compile("(?m)^\\s*package\\s+com\\.assessment\\s*;");

    // The template's Surefire config (useFile=false) prints the JUNIT_RESULT::
    // markers to stdout, which the backend parses out of the captured build log.
    private static final String COMPILE_SCRIPT =
            "#!/usr/bin/env bash\n# Dependencies are resolved and compiled in the run phase.\nexit 0\n";
    // Uses the JDK 17, Maven, and pre-warmed offline repository baked into the
    // custom Judge0 worker image (docker/judge0/Dockerfile), all under /usr/local
    // which the isolate sandbox exposes read-only.
    private static final String RUN_SCRIPT =
            "#!/usr/bin/env bash\n"
            + "export JAVA_HOME=/usr/local/jdk17\n"
            + "export PATH=$JAVA_HOME/bin:/usr/local/maven/bin:$PATH\n"
            + "mvn -o -q -Dstyle.color=never -Dsurefire.useFile=false "
            + "-Dmaven.repo.local=/usr/local/m2repo test\n";

    private final Path testsRoot;

    public CodeInjectionService(@Value("${challenge.tests-dir}") String testsDir) {
        this.testsRoot = Path.of(testsDir).toAbsolutePath().normalize();
    }

    public Path getTestsRoot() {
        return testsRoot;
    }

    /** Returns the base64-encoded zip of the candidate's project, ready for Judge0. */
    public String buildBase64ProjectZip(Challenge challenge, String candidateCode) {
        Path templateDir = testsRoot.resolve(challenge.getTemplateDir()).normalize();
        if (!Files.isDirectory(templateDir)) {
            throw new IllegalStateException("Challenge template not found: " + templateDir);
        }
        try {
            byte[] zip = zipProject(templateDir, candidateCode);
            log.debug("Built project zip for {} ({} bytes)", challenge.getSlug(), zip.length);
            return Base64.getEncoder().encodeToString(zip);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build project zip for " + challenge.getSlug(), e);
        }
    }

    private byte[] zipProject(Path templateDir, String candidateCode) throws IOException {
        String candidateFileName = candidateFileName(candidateCode);
        String normalizedCode = ensurePackage(candidateCode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            try (Stream<Path> walk = Files.walk(templateDir)) {
                List<Path> files = walk.filter(Files::isRegularFile).toList();
                for (Path file : files) {
                    String rel = templateDir.relativize(file).toString().replace('\\', '/');
                    if (rel.startsWith("target/") || rel.endsWith(".class")) {
                        continue;
                    }
                    if (rel.equals(PLACEHOLDER_REL)) {
                        continue; // replaced by the candidate file below
                    }
                    addEntry(zos, rel, Files.readAllBytes(file));
                }
            }
            addEntry(zos, CANDIDATE_PKG_DIR + candidateFileName, normalizedCode.getBytes(StandardCharsets.UTF_8));
            addEntry(zos, "compile", COMPILE_SCRIPT.getBytes(StandardCharsets.UTF_8));
            addEntry(zos, "run", RUN_SCRIPT.getBytes(StandardCharsets.UTF_8));
        }
        return baos.toByteArray();
    }

    private static void addEntry(ZipOutputStream zos, String name, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
    }

    /** Name the file after the public type so javac accepts it; fall back to the placeholder name. */
    static String candidateFileName(String code) {
        Matcher m = PUBLIC_TYPE.matcher(code);
        if (m.find()) {
            return m.group(1) + ".java";
        }
        return "CANDIDATE_SUBMISSION.java";
    }

    /** Guarantee the candidate code declares the expected package. */
    static String ensurePackage(String code) {
        if (PACKAGE_DECL.matcher(code).find()) {
            return code;
        }
        return "package com.assessment;\n\n" + code;
    }
}
