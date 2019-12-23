import connexion
import six

from models.inline_response200 import InlineResponse200  # noqa: E501
from models.inline_response400 import InlineResponse400  # noqa: E501
from models.inline_response404 import InlineResponse404  # noqa: E501
from models.inline_response500 import InlineResponse500  # noqa: E501
import util
import io
import os

from miniomanager import MinioManager
from flask import send_file

minioClient = MinioManager(os.getenv("3M_MINIO_HOST", "localhost"), os.getenv("3M_MINIO_PORT", "9000"))

def delete_file(bucket_name, file_id):  # noqa: E501
    """delete_file

    Delete an image from MultiMediaManager # noqa: E501

    :param bucket_name: 
    :type bucket_name: str
    :param file_id: 
    :type file_id: int

    :rtype: InlineResponse200
    """
    filename = f"{file_id}.jpg"
    response = minioClient.deleteFile(bucket_name, filename)
    if (response['status'] > 204):
        return InlineResponse500(response['status'], response['message']), 500
    else:
        return InlineResponse200(response['status'], response['message']), response['status']


def get_file(bucket_name, file_id):  # noqa: E501
    """get_file

    Get an image from MultiMediaManager # noqa: E501

    :param bucket_name: 
    :type bucket_name: str
    :param file_id: 
    :type file_id: int

    :rtype: str
    """
    filename = f"{file_id}.jpg"
    response = minioClient.getFile(bucket_name, filename)

    if (response['status'] > 204):
        return InlineResponse404(response['status'], response['message']), response['status']

    else:
        return send_file(
                io.BytesIO(response['data']),
                mimetype="image/jpg"
                )
            

def upload_file(body, bucket_name, file_id):  # noqa: E501
    """upload_file

    Upload an image from MultiMediaManager # noqa: E501

    :param body: 
    :type body: dict | bytes
    :param bucket_name: 
    :type bucket_name: str
    :param fileId: 
    :type fileId: int

    :rtype: InlineResponse200
    """
    if connexion.request.is_json:
        body = Object.from_dict(connexion.request.get_json())  # noqa: E501

    filename = f"{file_id}.jpg"

    response = minioClient.uploadFile(bucket_name, filename, body)
    if (response['status'] <= 204):
        return InlineResponse200(response['status'], response['message'], response['etag']), response['status']
    else:
        return InlineResponse500(response['status'], response['message']), response['status']
