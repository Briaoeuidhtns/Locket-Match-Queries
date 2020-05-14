Drafting and stats tool for use against ametuer teams. Exisiting stats websites can go into detail about teams of professional players or individual ametuer players, but this project allows analysis of teams of ametuer players.

Project originally a partner project for the Programming Languages class at SDSMT by [Matthew Reff](https://github.com/matthewReff) and [Brian Brunner](https://github.com/Briaoeuidhtns)

# Setup

### Get mysql
`$ sudo apt install mysql-server`

### Create a usable config, use the same names you put in the config in your sql scripts
`$ cd resources`

`$ cp exampleConfig.edn config.edn`

`$ {editor} config.edn`

 Be sure to either change the database name in insertTables.sql or use the default "mydb" in the code below

`$ {editor} insertTables.sql`

### Add database, create a user and grant it the permissions needed

`$ sudo mysql`

`mysql> source insertTables.sql`

Look into password policy if your password isn't considered valid

You'll probably want to keep localhost as the ip unless you have a remote server set up

`mysql> CREATE USER '{:db_user}'@'{ip}' IDENTIFIED BY '{password}';`

`mysql> GRANT ALL PRIVILEGES ON {:db_name}.* TO '{:db_user}'@'{ip}';`

### If you have a problem with server time zone values

`$ {editor} ~/../../etc/mysql/my.cnf`

Add the following lines to the bottom of your sql config file

>[mysqld]
>
>default-time-zone='+00:00'

Now restart your mysql server

`$ service mysql restart`
