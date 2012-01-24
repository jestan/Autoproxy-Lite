set SCRIPT_DIR=%~dp0
java -Xmx1024M -Xss2M -XX:PermSize=96m -XX:MaxPermSize=500m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*