from minio import Minio
from minio.error import (ResponseError, BucketAlreadyOwnedByYou, BucketAlreadyExists)

class MinioManager():
    def __init__(self, host, port):
        self.minioClient = Minio(f"{host}:{port}", 
                                    access_key="admin", 
                                    secret_key="minio2019",
                                    secure = False)


    def uploadFile(self, file, bucketName, filename):
        try:
            self.minioClient.make_bucket(bucketName)
        except BucketAlreadyOwnedByYou:
            pass
        except BucketAlreadyExists:
            pass
        except ResponseError as err:
            raise err
        
        try:
            self.minioClient.fput_object(bucketName, filename, file)
        except ResponseError as err:
            raise err