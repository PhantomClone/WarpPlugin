# WarpPlugin
*WarpPlugin* is a Java-based plugin designed to integrate various technologies such as Sadu, Dependency Injection, Flyway, Reflections, HikariCP, and PostgreSQL to enhance backend development.

## Features
- Sadu: Simple abstraction for database access. [sadu github](https://github.com/rainbowdashlabs/sadu)
- Dependency Injection: Efficiently manage dependencies.
- Flyway: Database version control and migration.
- Reflections: Simplified classpath scanning.
- HikariCP: Fast and lightweight JDBC connection pool.
- PostgreSQL: Backend database.

## Installation
Clone the repository:
Code kopieren
```bash
git clone https://github.com/PhantomClone/WarpPlugin.git
```
Navigate to the project folder and build with:
```bash
./gradlew shadowJar
```

## Development
To run the Plugin in your IDEA use:
```bash
./gradlew testPlugin
```
This Plugin needs a Database. For this reason a compose.yaml is provided and will be executed in testPlugin.
(It does not stop automatically!)

Keep in mind to use spotbugsMain, if you continue developing.

## Usage
Configure the PostgreSQL database and update connection settings in compose.yaml.
Use Flyway for managing database migrations.
Build and run the project using the provided gradlew scripts.

## Setup on Server
- Start your server with the plugin in your plugin folder.
- Stop your server.
- Navigate to PLUGINSFOLDER/WarpPlugin/.
- There you have to configure your datasource properties in datasource.properties.
- Start your server again.
- Setup done.

## Contributing
Pull requests are welcome. Please make sure to test your changes thoroughly.

License
This project is licensed under the Apache 2.0 License. See the LICENSE file for more details.