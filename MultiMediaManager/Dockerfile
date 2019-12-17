FROM python:3.6-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY requirements.txt /usr/src/app/

RUN python3 -m pip install --no-cache-dir -r requirements.txt

COPY . /usr/src/app

EXPOSE 10203

#ENTRYPOINT ["python3"]

CMD [ "python3", "src"]
