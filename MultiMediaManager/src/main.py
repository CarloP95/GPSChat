from coapthon.server.coap import CoAP
from minioManager.miniomanager import MinioManager

class CoAPServer(CoAP) :
    def __init__(self, host, port):
        CoAP.__init__(self, (host, port))

if __name__ == "__main__":
    (address, port) = ("0.0.0.0", 5683)
    print(f"Starting server at {address}:{port}")
    # Start Server
    server = CoAPServer(address, port)
    try:
        server.listen(10)
    except KeyboardInterrupt:
        print("Shutting down server")
        server.close()

    print("Sending image to minio")
    min = MinioManager("localhost", 9000)
    min.uploadFile("./res/coffe.jpg", "example", "coffe.jpg")

