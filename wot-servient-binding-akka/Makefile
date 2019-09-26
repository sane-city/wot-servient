package:
	cd ../ && mvn -DskipTests clean install
	mvn -DskipTests clean package

docker:
	docker build -t git.informatik.uni-hamburg.de:4567/bornholdt/sane-node-akka:wot-akka .
