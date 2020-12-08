call mvn clean:clean
call mvn package -Dtest assembly:assembly -DfailIfNoTests=false
call mvn install -Dmaven.test.skip=true 
@pause