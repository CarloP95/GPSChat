# coding: utf-8

from __future__ import absolute_import
from datetime import date, datetime  # noqa: F401

from typing import List, Dict  # noqa: F401

from models.base_model_ import Model
import util


class InlineResponse404(Model):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """
    def __init__(self, code: float=None, message: str=None):  # noqa: E501
        """InlineResponse404 - a model defined in Swagger

        :param code: The code of this InlineResponse404.  # noqa: E501
        :type code: float
        :param message: The message of this InlineResponse404.  # noqa: E501
        :type message: str
        """
        self.swagger_types = {
            'code': float,
            'message': str
        }

        self.attribute_map = {
            'code': 'code',
            'message': 'message'
        }
        self._code = code
        self._message = message

    @classmethod
    def from_dict(cls, dikt) -> 'InlineResponse404':
        """Returns the dict as a model

        :param dikt: A dict.
        :type: dict
        :return: The inline_response_404 of this InlineResponse404.  # noqa: E501
        :rtype: InlineResponse404
        """
        return util.deserialize_model(dikt, cls)

    @property
    def code(self) -> float:
        """Gets the code of this InlineResponse404.


        :return: The code of this InlineResponse404.
        :rtype: float
        """
        return self._code

    @code.setter
    def code(self, code: float):
        """Sets the code of this InlineResponse404.


        :param code: The code of this InlineResponse404.
        :type code: float
        """

        self._code = code

    @property
    def message(self) -> str:
        """Gets the message of this InlineResponse404.


        :return: The message of this InlineResponse404.
        :rtype: str
        """
        return self._message

    @message.setter
    def message(self, message: str):
        """Sets the message of this InlineResponse404.


        :param message: The message of this InlineResponse404.
        :type message: str
        """

        self._message = message
