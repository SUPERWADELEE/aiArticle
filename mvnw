#!/bin/sh
# Maven Wrapper script
# Adapted from https://github.com/apache/maven-wrapper

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_DIST_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"

if [ ! -f "$MAVEN_HOME/bin/mvn" ]; then
    echo "Downloading Maven..."
    mkdir -p "$MAVEN_HOME"
    TMPFILE=$(mktemp)
    curl -sL "$MAVEN_DIST_URL" -o "$TMPFILE"
    unzip -qo "$TMPFILE" -d "$HOME/.m2/wrapper/dists/"
    rm "$TMPFILE"
    # Move contents if nested
    if [ -d "$HOME/.m2/wrapper/dists/apache-maven-3.9.9" ]; then
        MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-3.9.9"
    fi
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
