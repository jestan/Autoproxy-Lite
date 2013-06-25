set SCRIPT_DIR=%~dp0
java -Duser.name=mysql -showversion -Xincgc -Xmx1024M -Xss2M  -XX:MaxPermSize=256m -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*