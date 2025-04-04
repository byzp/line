p2p:
	aarch64-linux-android-g++ src/cpp/client.cpp -fPIC -shared -static -o libs/client.so -static -O3  -ftls-model=global-dynamic
	aarch64-linux-android-g++ src/cpp/server.cpp -fPIC -shared -static -o libs/server.so -static -O3  -ftls-model=global-dynamic
	strip libs/client.so libs/server.so