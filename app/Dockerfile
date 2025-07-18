FROM openjdk:17-jdk-slim

# Cài đặt Android SDK và các công cụ cần thiết
ENV ANDROID_SDK_ROOT /sdk
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    mkdir -p /sdk/cmdline-tools && \
    unzip cmdline-tools.zip -d /sdk/cmdline-tools && \
    mv /sdk/cmdline-tools/cmdline-tools /sdk/cmdline-tools/latest && \
    rm cmdline-tools.zip && \
    yes | /sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/sdk --licenses && \
    /sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/sdk "platform-tools" "platforms;android-34" "build-tools;34.0.0"

ENV PATH="$PATH:/sdk/platform-tools:/sdk/build-tools/34.0.0:/sdk/cmdline-tools/latest/bin"

WORKDIR /workspace

COPY . .

RUN ./gradlew assembleDebug

