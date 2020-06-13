Drafting and stats tool for use against ametuer teams. Exisiting stats websites can go into detail about teams of professional players or individual ametuer players, but this project allows analysis of teams of ametuer players.

Project originally a partner project for the Programming Languages class at SDSMT by [Matthew Reff](https://github.com/matthewReff) and [Brian Brunner](https://github.com/Briaoeuidhtns)

# Setup
The database can either be run through docker or set up manually.

## Docker
The tests use github packages to store docker images, and even though they're public, you must be authenticated as per (these instructions)[https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-docker-for-use-with-github-packages#authenticating-to-github-packages]. More info (here)[https://github.community/t/download-from-github-package-registry-without-authentication/14407].

The docker image can also be used as a development server, to ensure a consistent environment.

`$ docker run --name tmp-locket-db -e MYSQL_DATABASE="mydb" -e MYSQL_USER="locket-user" -e MYSQL_PASSWORD="Password" -e MYSQL_RANDOM_ROOT_PASSWORD=1 docker.pkg.github.com/matthewreff/locket-match-queries/locket-ci-db:0.1.1`

All variables may be changed, but they must match those in `resources/config.edn`

## Manually

### Get mysql
`$ sudo apt install mysql-server`

### Create a usable config, use the same names you put in the config in your sql scripts
`$ cd resources`

`$ cp exampleConfig.edn config.edn`

`$ $EDITOR config.edn`

 Be sure to either change the database name in insertTables.sql or use the default "mydb" in the code below

`$ $EDITOR insertTables.sql`

### Add database, create a user and grant it the permissions needed

`$ sudo mysql`

`mysql> source insertTables.sql`

Look into password policy if your password isn't considered valid

You'll probably want to keep localhost as the ip unless you have a remote server set up

`mysql> CREATE USER '{:db_user}'@'{:db_ip}' IDENTIFIED BY '{:db_pass}';`

`mysql> GRANT ALL PRIVILEGES ON {:db_name}.* TO '{:db_user}'@'{:db_ip}';`

If you will be connecting from an ip other than localhost you'll need to allow connections from outside.
Make sure that you give as few permissions as possible to anything that can be accessed from the outside.

`mysql> CREATE USER '{:db_user}'@'%' IDENTIFIED BY '{db_pass}';`

`mysql> GRANT ALL PRIVILEGES ON {:db_name}.* TO '{:db_user}'@'%';`

### If you have a problem with server time zone values

`$ $EDITOR ~/../../etc/mysql/my.cnf`

Add the following lines to the bottom of your sql config file

>[mysqld]
>
>default-time-zone='+00:00'

Now restart your mysql server

`$ service mysql restart`
