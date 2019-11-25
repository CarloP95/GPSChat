# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from swagger_server.models.inline_response200 import InlineResponse200  # noqa: E501
from swagger_server.models.inline_response400 import InlineResponse400  # noqa: E501
from swagger_server.models.inline_response404 import InlineResponse404  # noqa: E501
from swagger_server.models.inline_response500 import InlineResponse500  # noqa: E501
from swagger_server.test import BaseTestCase


class TestGenericImagesController(BaseTestCase):
    """GenericImagesController integration test stubs"""

    def test_delete_file(self):
        """Test case for delete_file

        
        """
        response = self.client.open(
            '/carlop.com/3MServer/1.0.0/3M/api/{bucketName}/{filename}'.format(bucket_name='bucket_name_example', filename=789),
            method='DELETE')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_file(self):
        """Test case for get_file

        
        """
        response = self.client.open(
            '/carlop.com/3MServer/1.0.0/3M/api/{bucketName}/{filename}'.format(bucket_name='bucket_name_example', filename=789),
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_upload_file(self):
        """Test case for upload_file

        
        """
        body = Object()
        response = self.client.open(
            '/carlop.com/3MServer/1.0.0/3M/api/{bucketName}/{filename}'.format(bucket_name='bucket_name_example', filename=789),
            method='POST',
            data=json.dumps(body),
            content_type='image/jpg')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()
