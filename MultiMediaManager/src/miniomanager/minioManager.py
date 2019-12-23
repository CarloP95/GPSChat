import os
import io
from minio import Minio
from minio.error import (ResponseError, BucketAlreadyOwnedByYou, BucketAlreadyExists, NoSuchBucket, NoSuchKey)

class MinioManager():
    def __init__(self, host, port):
        self.minioClient = Minio(f"{host}:{port}", 
                                    access_key=os.getenv("3M_MINIO_ACCESS_KEY", "admin") ,
                                    secret_key=os.getenv("3M_MINIO_SECRET_KEY", "minio2019") ,
                                    secure = False)


    def uploadFile(self, bucketName, filename, file):
        try:
            self.minioClient.make_bucket(bucketName)
        except BucketAlreadyOwnedByYou:
            pass
        except BucketAlreadyExists:
            pass
        except ResponseError as err:
            raise err

        fileRawIoBase = io.BytesIO(file)
        
        try:
            etag = self.minioClient.put_object(bucketName, filename, fileRawIoBase, len(file))

            return {"etag" : etag, "status": 200, "message": "Successfully uploaded"}
        except ResponseError as err:
            raise err

    def getFile(self, bucketName, filename):

        try:
            result = []
            data   = self.minioClient.get_object(bucketName, filename)

            image = b""
            for chunk in data.stream():
                image = image + chunk

            return { "status": 200, "data": image }

        except ResponseError as err:
            raise err
        except NoSuchBucket:
            return {"status": 404, "message": f"The Bucket {bucketName} does not exist on server."}
        except NoSuchKey:
            return {"status": 404, "message": f"The File {filename} does not exist on server."}

    def deleteFile(self, bucketName, filename):
        try:
            result = self.minioClient.remove_object(bucketName, filename)

            if (result is None):
                return {"status": 200, "message": f"File {filename} successfully deleted."}
        except ResponseError as err:
            raise err
