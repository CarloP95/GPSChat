import sys
import unittest

sys.path.append("../")
from MultiMediaManager.src.miniomanager import MinioManager

class MinioManagerTest(unittest.TestCase):

    def setUp(self):
        self.minioHost     = "192.168.1.90"
        self.minioPort     = 9000
        self.minioManager  = MinioManager(self.minioHost, self.minioPort)

        self.coffeeImgPath = "./res/coffe.jpg"
        self.filename      = "coffe.jpg"
        self.bucketName    = "example"


    def testUploadWithOneBucket(self):
        self.minioManager.uploadFile(self.coffeeImgPath, self.bucketName, self.filename)

    def testGetFile(self):
        self.minioManager.uploadFile(self.coffeeImgPath, self.bucketName, self.filename)

        self.minioManager.getFile(self.bucketName, self.filename)

    def testDeleteFile(self):
        self.minioManager.uploadFile(self.coffeeImgPath, self.bucketName, self.filename)

        self.minioManager.deleteFile(self.bucketName, self.filename)

    def tearDown(self):
        print(self.minioManager.minioClient.__dict__)
