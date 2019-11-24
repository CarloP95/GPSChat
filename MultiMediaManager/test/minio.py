import sys
import unittest
sys.path.append("..")
from src.minioManager.minioManager import MinioManager

class MinioManagerTest(unittest.TestCase):

    def setUp(self):
        self.minioManager = MinioManager("localhost", 9000)

    def testUploadWithOneBucket(self):
        self.minioManager.uploadFile("./res/coffe.jpg", "example", "coffe.jpg")