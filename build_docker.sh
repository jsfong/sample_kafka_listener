./gradlew clean
./gradlew build

docker build --build-arg JAR_FILE=build/libs/\*.jar -t localhost:5000/jsfong/sample_server:latest .
#docker tag sample_server:latest localhost:5000/jsfong/sample_server:latest
docker push localhost:5000/jsfong/sample_server:latest
docker tag localhost:5000/jsfong/sample_server:latest jsfong/sample_kafka_listener:latest