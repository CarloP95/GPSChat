import sys
from setuptools import setup, find_packages

sys.path.insert(0, "src")


REQUIRES = ["connexion", "flask"]

setup(
	name = "MultiMediaManager (3M)",
	description = "MultiMedia Manager (3M) that will work with an Amazon S3 compatible storage to save Images and Avatars.",
	version = "0.1",
	author = "Carlo Francesco Pellegrino",
	author_email = "",
	license = "MIT",

    install_requires=REQUIRES,

	packages = find_packages("src"),
	package_dir = {"" : "src"},
    package_data={'': ['swagger/swagger.yaml']},
    include_package_data=True,
	namespace_packages = ["src", "test"]

)

