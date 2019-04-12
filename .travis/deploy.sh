#!/usr/bin/env bash


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

PROJECT_VERSION=$(./gradlew -q printVersion | tail -n 1)

echo "Project version is ${PROJECT_VERSION}"

ENCODED_SIGN_FILE_PATH="${DIR}/${ENCRYPTED_GPG_FILE_NAME}"
SIGN_FILE_PATH="${DIR}/${GPG_FILE_NAME}"

