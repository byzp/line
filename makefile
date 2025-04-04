p2p:
	aarch64-linux-android-g++ src/cpp/p2pc.cpp -fPIC -shared -o lib/libp2pc.so -static -O3  -ftls-model=global-dynamic
	aarch64-linux-android-g++ src/cpp/p2ps.cpp -fPIC -shared -o lib/libp2ps.so -static -O3  -ftls-model=global-dynamic
	strip lib/libp2pc.so lib/libp2ps.so