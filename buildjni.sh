git submodule init  &&
git submodule update  &&
rm -rf tmp lib
#go install golang.org/x/mobile/cmd/gomobile@latest  &&
#export PATH=~/go/bin  &&
cd frp/cmd/frp  &&
gomobile init  &&
gomobile bind -v -o libfrp.aar -target=android -androidapi 21 .  &&
cd ../../..  &&
mkdir tmp  &&
cd tmp  &&
unzip ../frp/cmd/frp/libfrp.aar  &&
mkdir ../lib &&
mv jni/arm64-v8a/* ../lib  &&
