### build project

mvn clean package

### binnary package

target/tc-emailverifycode-project-*.gz

### untar 

tar -xf tc-emailverifycode-project-*.gz

### Setting config

vi conf/config.properties

### start

cd tc-emailverifycode-project-*

./bin/start

### useage

If you have long session id you can use sha1 to hash first.

curl http://localhost:8989/evc/send/sessionId/email

curl http://localhost:8989/evc/verify/sessionId/code

curl http://localhost:8989/encrypt/password

browser: http://localhost:8989/html/index.html


