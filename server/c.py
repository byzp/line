import socket
 
s = socket.socket()
host = socket.gethostname()
port = 6568
s.bind((host, port))
 
s.listen(5)
while True:
    c,addr = s.accept()
    c.send(b"9999")
    c.close()