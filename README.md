Related project: SQL, JDBC and Hibernate.

There is a relational MySQL database with a schema (country-city, language by country). And there is a frequent request of the city, which slows down.
We came up with a solution - to move all the data that is requested frequently to Redis (in memory storage of the key-value type).

The results show that the speed of response to a request to the Redis database is about 1.5 times higher. As an experiment, you can also
change in the request the amount of requested data of the citiesCountForTest variable in the ConnectionSpeedTest class.
Here you can see that when requesting more data, the performance of Redis will be 2 or more times higher.

![2023-02-19_13-27-43](https://user-images.githubusercontent.com/104271423/219953413-d0ad6a39-f994-4837-8039-cc2107c3d431.png)


To start MySql database in Docker, run command in commandline:
docker run --name mysql -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root --restart unless-stopped -v mysql:/var/lib/mysql mysql:8 

To start Redis database in Docker, run command in commandline:
docker run -d --name redis -p 6379:6379 redis:latest

since the project was created for educational purposes, the resources folder contains a dump of the database for which this project was made
