export DOCKER_DEFAULT_PLATFORM=linux/amd64

./stop.sh

docker-compose up -d

echo "Wait 15 seconds to start up..."
sleep 15

echo "Add ACLs on topic orders for user alice"
docker exec -it broker kafka-topics --bootstrap-server broker:29092 --create --topic orders
docker exec -it broker kafka-acls --bootstrap-server broker:29092 --add --allow-principal "User:alice" --operation Read --operation Write --topic orders

echo "Add ACLs on consumer group order-app-group"
docker exec -it broker kafka-acls --bootstrap-server broker:29092 --add --allow-principal "User:alice" --operation Read --group order-app-group