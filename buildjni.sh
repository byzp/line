git submodule init  &&
git submodule update  &&
rm -rf tmp libs

ndk-build NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=./src/cpp/Android.mk NDK_APPLICATION_MK=./src/cpp/Application.mk NDK_OUT=./build

#go install golang.org/x/mobile/cmd/gomobile@latest  &&
#export PATH=$PATH:~/go/bin  &&
cd frp/cmd/frp  &&
gomobile init  &&
gomobile bind -v -o libfrp.aar -target=android -androidapi 28 .  &&
unzip libfrp.aar jni/arm64-v8a/* -d ../../../libs  &&
cd ../../../libs &&
mv jni/arm64-v8a/libgojni.so arm64-v8a  &&
aarch64-linux-gnu-strip arm64-v8a/libgojni.so
rm -rf jni
