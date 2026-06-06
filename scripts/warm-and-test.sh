#!/usr/bin/env bash
set -u
cd /mnt/d/JavaMSAEvaluator
mkdir -p .m2warm
echo "=== running all 6 challenge suites in maven:3.9-eclipse-temurin-17 (warms .m2warm) ==="
docker run --rm \
  -v /mnt/d/JavaMSAEvaluator/challenge-tests:/work \
  -v /mnt/d/JavaMSAEvaluator/.m2warm:/root/.m2/repository \
  -w /work maven:3.9-eclipse-temurin-17 \
  bash -c '
    for d in /work/challenge-*; do
      echo "########## $(basename $d) ##########"
      (cd "$d" && mvn -q -B -Dsurefire.useFile=false test 2>&1 | grep -E "JUNIT_RESULT::|BUILD SUCCESS|BUILD FAILURE|Tests run:|ERROR\]" | tail -25)
    done
  '
echo "=== warm repo size ==="
du -sh .m2warm
