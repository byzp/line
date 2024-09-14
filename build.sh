#mkdir build/lib
#go install golang.org/x/mobile/cmd/gomobile@latest
#ANDROID_HOME=/home/zp/disk/hd1/home.new/zp/tools/cmdline-tools/bin/sdk
#ANDROID_NDK_HOME=$ANDROID_HOME/ndk/27.0.11902837

./buildjni.sh
./buildjar.sh
