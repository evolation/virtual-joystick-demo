import io
import socket
import struct
from PIL import Image

s = socket.socket()
host = '192.168.0.5' #ip of host
port = 1337
print("Socket Ok")
s.bind((host, port))
print("Bind Ok")
s.listen(5)
connection = s.accept()[0].makefile('rb')
print("Connection Accepted")
while True:
    try:
            print("Running")

            image_len= struct.unpack('<L',connection.read(struct.calcsize('<L')))
            size = image_len[0]
            image_stream = io.BytesIO()
            image_stream.write(connection.read(size))
            image_stream.seek(0)
            image = Image.open(image_stream)
            image.show()
            print('Image is verified')

    except Exception:
        s = socket.socket()
        host = '192.168.0.5'  # ip of host
        port = 1337
        print("Socket Ok")
        s.bind((host, port))
        print("Bind Ok")
        s.listen(5)
        connection = s.accept()[0].makefile('rb')
        print("Connection Accepted")