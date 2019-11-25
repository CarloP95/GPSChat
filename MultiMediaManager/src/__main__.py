#!/usr/bin/env python3

import connexion

import encoder

__service_name__ = "MultiMediaManager (3M)"

def main():
    # At the current state OpenAPI version 3.0 is not supporting the UI.
    options = {"swagger_ui": False}
    #app = connexion.App(__name__, specification_dir='./swagger/', options=options, server='tornado')
    app = connexion.App(__name__, specification_dir='./swagger/', options=options)
    app.app.json_encoder = encoder.JSONEncoder
    #app.add_api('swagger.yaml', base_path="/", arguments={'title': '3M Server'}, pythonic_params=True, options=options)
    app.add_api('swagger.yaml', arguments={'title': '3M Server'}, pythonic_params=True)

    @app.route("/")
    def home():
         return "<h1>Hello from " + __service_name__ + ". Please redirect to /api endpoint</h1>"


    app.run(host="0.0.0.0", port=10203)


if __name__ == '__main__':
    main()
