# Custom Judge0 worker/server image that can actually run the challenge Maven
# builds. Stock Judge0 CE 1.13.0 ships only JDK 13, no Maven, and no network in
# the sandbox, so we bake in:
#   - JDK 17 (challenges target Java 17)
#   - Maven 3.9
#   - a pre-warmed local repository with every dependency the 6 challenges need
# All under /usr/local, which the isolate sandbox exposes read-only.
FROM judge0/judge0:1.13.0

COPY --from=eclipse-temurin:17-jdk /opt/java/openjdk /usr/local/jdk17
COPY --from=maven:3.9-eclipse-temurin-17 /usr/share/maven /usr/local/maven
COPY .m2warm /usr/local/m2repo
