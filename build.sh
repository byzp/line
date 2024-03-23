#mkdir build/lib
#go install golang.org/x/mobile/cmd/gomobile@latest

gradle jar &&
cd build/libs &&
rm -rf tmp/* &&
d8 *.jar --min-api 28 &&
unzip -n *.jar -d tmp &&
mv *.dex tmp &&
cd tmp &&
zip -r aaa.zip * &&
cp aaa.zip ../../../src/ &&
cd ../../..