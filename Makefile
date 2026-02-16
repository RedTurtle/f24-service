run: install
	mvn spring-boot:run

install:
	mvn install:install-file -Dfile=extras/F24Generator-2.1.1-SNAPSHOT.jar -DgroupId=org.f24 -Dpackaging=jar
	mvn install
#   -DartifactId=nome-artefatto-personalizza \
#   -Dversion=1.0.0 \

build: install
