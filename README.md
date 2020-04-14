Drafting and stats tool for use against ametuer teams. Exisiting stats websites can go into detail about teams of professional players or individual ametuer players, but this project allows analysis of teams of ametuer players.

Project originally a partner project for the Programming Languages class at SDSMT by (Matthew Reff)[https://github.com/matthewReff] and (Brian Brunner)[https://github.com/Briaoeuidhtns]

# Get mysql
$ sudo apt install mysql-server

# Create a usable config, use the same names you put in the config in your sql scripts
$ cd resources
$ cp exampleConfig.edn config.edn
$ {editor} config.edn

# Be sure to change the database name in the sql script, if you don't, the default is "mydb"
$ {editor} insertTables.sql

# Add database, create a user and grant it the permissions needed
$ sudo mysql
mysql> source insertTables.sql
# Look into password policy if your password isn't considered valid. You'll probably want to use localhost.
mysql> CREATE USER '{:db_user}'@'{ip}' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON {:db_name}.* TO '{:db_user}'@'{ip}';
